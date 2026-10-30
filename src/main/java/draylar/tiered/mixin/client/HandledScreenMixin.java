package draylar.tiered.mixin.client;

import java.util.List;
import java.util.Optional;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;

import draylar.tiered.api.BorderTemplate;
import draylar.tiered.util.TieredTooltip;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipData;
import net.minecraft.text.Text;

@Environment(EnvType.CLIENT)
@Mixin(HandledScreen.class)
public abstract class HandledScreenMixin extends Screen {

    public HandledScreenMixin(Text title) {
        super(title);
    }

    @WrapOperation(method = "drawMouseoverTooltip", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawTooltip(Lnet/minecraft/client/font/TextRenderer;Ljava/util/List;Ljava/util/Optional;II)V"))
    protected void drawMouseoverTooltipMixin(DrawContext context, TextRenderer textRenderer, List<Text> text, Optional<TooltipData> data, int x, int y, Operation<Void> original, @Local ItemStack stack) {
        BorderTemplate borderTemplate = TieredTooltip.getBorderTemplate(stack);
        if (borderTemplate != null) {
            TieredTooltip.renderTieredTooltip(context, textRenderer, text, data, x, y, borderTemplate);
        } else {
            original.call(context, textRenderer, text, data, x, y);
        }
    }

}
