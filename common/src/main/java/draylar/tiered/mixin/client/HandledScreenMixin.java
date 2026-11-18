package draylar.tiered.mixin.client;

import java.util.List;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import draylar.tiered.api.BorderTemplate;
import draylar.tiered.util.TieredTooltip;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

@Mixin(AbstractContainerScreen.class)
public abstract class HandledScreenMixin extends Screen {

    @Shadow
    @Final
    protected AbstractContainerMenu menu;

    @Shadow
    @Nullable
    protected Slot hoveredSlot;

    public HandledScreenMixin(Component title) {
        super(title);
    }

    @Inject(method = "extractTooltip", at = @At("HEAD"), cancellable = true)
    protected void drawMouseoverTooltipMixin(GuiGraphicsExtractor graphics, int mouseX, int mouseY, CallbackInfo info) {
        if (this.hoveredSlot != null && this.hoveredSlot.hasItem()) {
            ItemStack stack = this.hoveredSlot.getItem();
            BorderTemplate borderTemplate = TieredTooltip.getBorderTemplate(stack);
            if (borderTemplate != null && (this.menu.getCarried().isEmpty() || this.showTooltipWithItemInHand(stack))) {
                TieredTooltip.setTieredTooltipForNextFrame(graphics, this.font, this.getTooltipFromContainerItem(stack), stack.getTooltipImage(), mouseX, mouseY, borderTemplate);
                info.cancel();
            }
        }
    }

    @Shadow
    protected abstract List<Component> getTooltipFromContainerItem(ItemStack itemStack);

    @Shadow
    private boolean showTooltipWithItemInHand(ItemStack item) {
        return false;
    }

}
