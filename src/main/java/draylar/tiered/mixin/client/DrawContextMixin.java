package draylar.tiered.mixin.client;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import draylar.tiered.api.BorderTemplate;
import draylar.tiered.util.TieredTooltip;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.item.ItemStack;

@Environment(EnvType.CLIENT)
@Mixin(GuiGraphicsExtractor.class)
public class DrawContextMixin {

    @Shadow
    @Final
    private Minecraft minecraft;

    @Inject(method = "setTooltipForNextFrame(Lnet/minecraft/client/gui/Font;Lnet/minecraft/world/item/ItemStack;II)V", at = @At("HEAD"), cancellable = true)
    private void drawItemTooltipMixin(Font textRenderer, ItemStack stack, int x, int y, CallbackInfo info) {
        BorderTemplate borderTemplate = TieredTooltip.getBorderTemplate(stack);
        if (borderTemplate != null) {
            TieredTooltip.setTieredTooltipForNextFrame((GuiGraphicsExtractor) (Object) this, textRenderer, Screen.getTooltipFromItem(this.minecraft, stack), stack.getTooltipImage(), x, y,
                    borderTemplate);
            info.cancel();
        }
    }

}
