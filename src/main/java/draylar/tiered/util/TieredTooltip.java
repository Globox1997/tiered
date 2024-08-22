package draylar.tiered.util;

import java.util.List;

import org.joml.Vector2ic;

import draylar.tiered.api.BorderTemplate;
import draylar.tiered.config.ConfigInit;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.gui.tooltip.TooltipPositioner;

@Environment(EnvType.CLIENT)
public class TieredTooltip {

    public static void renderTieredTooltipFromComponents(DrawContext context, TextRenderer textRenderer, List<TooltipComponent> components, int x, int y, TooltipPositioner positioner,
            BorderTemplate borderTemplate) {
        if (components.isEmpty()) {
            return;
        }
        int i = 0;
        int j = components.size() == 1 ? -2 : 0;
        for (TooltipComponent tooltipComponent : components) {
            if (tooltipComponent == null) {
                continue;
            }
            int k = tooltipComponent.getWidth(textRenderer);
            if (k > i) {
                i = k;
            }
            j += tooltipComponent.getHeight();
        }
        if (i < 64) {
            i = 64;
        }
        if (j < 16) {
            j = 16;
        }

        int l = i;
        int m = j;

        Vector2ic vector2ic = positioner.getPosition(context.getScaledWindowWidth(), context.getScaledWindowHeight(), x, y, l, m);
        int n = vector2ic.x();
        int o = vector2ic.y();
        context.getMatrices().push();

        int backgroundColor = borderTemplate.getBackgroundGradient();
        int colorStart = borderTemplate.getStartGradient();
        int colorEnd = borderTemplate.getEndGradient();

        renderTooltipBackground(context, n, o, l, m, 400, backgroundColor, colorStart, colorEnd);
        context.getMatrices().translate(0.0f, 0.0f, 400.0f);
        int q = o;
        int r;
        TooltipComponent tooltipComponent2;
        for (r = 0; r < components.size(); ++r) {
            int nameCentering = 0;
            tooltipComponent2 = components.get(r);
            if (r == 0 && ConfigInit.CONFIG.centerName) {
                nameCentering = i / 2 - tooltipComponent2.getWidth(textRenderer) / 2;
            }
            tooltipComponent2.drawText(textRenderer, n + nameCentering, q, context.getMatrices().peek().getPositionMatrix(), context.getVertexConsumers());
            q += tooltipComponent2.getHeight() + (r == 0 ? 2 : 0);
        }
        q = o;
        for (r = 0; r < components.size(); ++r) {
            tooltipComponent2 = components.get(r);
            tooltipComponent2.drawItems(textRenderer, n, q, context);
            q += tooltipComponent2.getHeight() + (r == 0 ? 2 : 0);
        }
        context.getMatrices().pop();

        int border = borderTemplate.getIndex();
        int secondHalf = border > 7 ? 1 : 0;
        if (border > 7) {
            border -= 8;
        }

        context.getMatrices().push();
        context.getMatrices().translate(0.0f, 0.0f, 400.0f);
        // left top corner
        context.drawTexture(borderTemplate.getIdentifier(), n - 6, o - 6, 0 + secondHalf * 64, 0 + border * 16, 8, 8, 128, 128);
        // right top corner
        context.drawTexture(borderTemplate.getIdentifier(), n + l - 2, o - 6, 56 + secondHalf * 64, 0 + border * 16, 8, 8, 128, 128);

        // left down corner
        context.drawTexture(borderTemplate.getIdentifier(), n - 6, o + m - 2, 0 + secondHalf * 64, 8 + border * 16, 8, 8, 128, 128);
        // right down corner
        context.drawTexture(borderTemplate.getIdentifier(), n + l - 2, o + m - 2, 56 + secondHalf * 64, 8 + border * 16, 8, 8, 128, 128);

        // middle header
        context.drawTexture(borderTemplate.getIdentifier(), (n - 6 + n + l + 6) / 2 - 24, o - 9, 8 + secondHalf * 64, 0 + border * 16, 48, 8, 128, 128);
        // bottom footer
        context.drawTexture(borderTemplate.getIdentifier(), (n - 6 + n + l + 6) / 2 - 24, o + m + 1, 8 + secondHalf * 64, 8 + border * 16, 48, 8, 128, 128);

        context.getMatrices().pop();
    }

    private static void renderTooltipBackground(DrawContext context, int x, int y, int width, int height, int z, int backgroundColor, int colorStart, int colorEnd) {
        int i = x - 3;
        int j = y - 3;
        int k = width + 6;
        int l = height + 6;
        renderHorizontalLine(context, i, j - 1, k, z, backgroundColor);
        renderHorizontalLine(context, i, j + l, k, z, backgroundColor);
        renderRectangle(context, i, j, k, l, z, backgroundColor);
        renderVerticalLine(context, i - 1, j, l, z, backgroundColor);
        renderVerticalLine(context, i + k, j, l, z, backgroundColor);
        renderBorder(context, i, j + 1, k, l, z, colorStart, colorEnd);
    }

    private static void renderBorder(DrawContext context, int x, int y, int width, int height, int z, int startColor, int endColor) {
        renderVerticalLine(context, x, y, height - 2, z, startColor, endColor);
        renderVerticalLine(context, x + width - 1, y, height - 2, z, startColor, endColor);
        renderHorizontalLine(context, x, y - 1, width, z, startColor);
        renderHorizontalLine(context, x, y - 1 + height - 1, width, z, endColor);
    }

    private static void renderVerticalLine(DrawContext context, int x, int y, int height, int z, int color) {
        context.fill(x, y, x + 1, y + height, z, color);
    }

    private static void renderVerticalLine(DrawContext context, int x, int y, int height, int z, int startColor, int endColor) {
        context.fillGradient(x, y, x + 1, y + height, z, startColor, endColor);
    }

    private static void renderHorizontalLine(DrawContext context, int x, int y, int width, int z, int color) {
        context.fill(x, y, x + width, y + 1, z, color);
    }

    private static void renderRectangle(DrawContext context, int x, int y, int width, int height, int z, int color) {
        context.fill(x, y, x + width, y + height, z, color);
    }

}
