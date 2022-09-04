package draylar.tiered.mixin.client;

import com.mojang.blaze3d.systems.RenderSystem;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import draylar.tiered.access.AnvilScreenHandlerAccess;
import draylar.tiered.config.ConfigInit;
import draylar.tiered.network.TieredClientPacket;
import draylar.tiered.reforge.ReforgeScreen;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.ingame.AnvilScreen;
import net.minecraft.client.gui.screen.ingame.ForgingScreen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.AnvilScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
@Mixin(value = AnvilScreen.class, priority = 1500)
public abstract class AnvilScreenMixin extends ForgingScreen<AnvilScreenHandler> {

    @Shadow
    @Mutable
    @Final
    private PlayerEntity player;

    public AnvilScreenMixin(AnvilScreenHandler handler, PlayerInventory playerInventory, Text title, Identifier texture) {
        super(handler, playerInventory, title, texture);
    }

    @Inject(method = "Lnet/minecraft/client/gui/screen/ingame/AnvilScreen;<init>(Lnet/minecraft/screen/AnvilScreenHandler;Lnet/minecraft/entity/player/PlayerInventory;Lnet/minecraft/text/Text;)V", at = @At("TAIL"))
    public void initMixin(AnvilScreenHandler handler, PlayerInventory inventory, Text title, CallbackInfo info) {
        TieredClientPacket.writeC2SSyncPosPacket(true);
    }

    @Override
    protected void drawBackground(MatrixStack matrices, float delta, int mouseX, int mouseY) {
        super.drawBackground(matrices, delta, mouseX, mouseY);

        RenderSystem.setShaderTexture(0, ReforgeScreen.TEXTURE);
        // anvil icon
        this.drawTexture(matrices, this.x + ConfigInit.CONFIG.xIconPosition, this.y - 23 + ConfigInit.CONFIG.yIconPosition, 0, 166, 24, 27);
        // reforge icon
        this.drawTexture(matrices, this.x + 25 + ConfigInit.CONFIG.xIconPosition, this.y - 21 + ConfigInit.CONFIG.yIconPosition, 48, 166, 24, 21);

        if (this.isPointWithinBounds(26 + ConfigInit.CONFIG.xIconPosition, -20 + ConfigInit.CONFIG.yIconPosition, 22, 19, (double) mouseX, (double) mouseY))
            this.renderTooltip(matrices, new TranslatableText("screen.tiered.reforging_screen"), mouseX, mouseY);

    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.client != null && this.focusedSlot == null
                && this.isPointWithinBounds(26 + ConfigInit.CONFIG.xIconPosition, -20 + ConfigInit.CONFIG.yIconPosition, 22, 19, (double) mouseX, (double) mouseY))
            TieredClientPacket.writeC2SScreenPacket(((AnvilScreenHandlerAccess) handler).getPos(), (int) this.client.mouse.getX(), (int) this.client.mouse.getY(), true);

        return super.mouseClicked(mouseX, mouseY, button);

    }
}
