package draylar.tiered.mixin.client;

import java.util.List;
import java.util.stream.Collectors;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import draylar.tiered.Tiered;
import draylar.tiered.TieredClient;
import draylar.tiered.config.ConfigInit;
import draylar.tiered.util.TieredTooltip;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.HoveredTooltipPositioner;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

@Environment(EnvType.CLIENT)
@Mixin(DrawContext.class)
public class DrawContextMixin {

    @Shadow
    @Mutable
    @Final
    private MinecraftClient client;

    @Inject(method = "drawItemTooltip", at = @At("HEAD"), cancellable = true)
    private void drawItemTooltipMixin(TextRenderer textRenderer, ItemStack stack, int x, int y, CallbackInfo info) {
        if (ConfigInit.CONFIG.tieredTooltip && stack.get(Tiered.TIER) != null) {
            String nbtString = stack.get(Tiered.TIER).tier();
            for (int i = 0; i < TieredClient.BORDER_TEMPLATES.size(); i++) {
                if (!TieredClient.BORDER_TEMPLATES.get(i).containsStack(stack) && TieredClient.BORDER_TEMPLATES.get(i).containsDecider(nbtString)) {
                    TieredClient.BORDER_TEMPLATES.get(i).addStack(stack);
                } else if (TieredClient.BORDER_TEMPLATES.get(i).containsStack(stack)) {
                    List<Text> text = Screen.getTooltipFromItem(client, stack);

                    List<TooltipComponent> list = text.stream().map(Text::asOrderedText).map(TooltipComponent::of).collect(Collectors.toList());
                    stack.getTooltipData().ifPresent(data -> list.add(1, TooltipComponent.of(data)));

                    TieredTooltip.renderTieredTooltipFromComponents((DrawContext) (Object) this, textRenderer, list, x, y, HoveredTooltipPositioner.INSTANCE, TieredClient.BORDER_TEMPLATES.get(i));

                    info.cancel();
                    break;
                }
            }
        }
    }

}
