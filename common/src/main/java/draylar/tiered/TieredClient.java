package draylar.tiered;

import draylar.tiered.api.BorderTemplate;
import draylar.tiered.data.TooltipBorderLoader;
import draylar.tiered.reforge.ReforgeScreen;
import draylar.tiered.reforge.widget.AnvilTab;
import draylar.tiered.reforge.widget.ReforgeTab;
import net.libz.registry.TabRegistry;
import net.minecraft.client.gui.screens.inventory.AnvilScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import java.util.ArrayList;
import java.util.List;

public class TieredClient {

    public static final List<BorderTemplate> BORDER_TEMPLATES = new ArrayList<BorderTemplate>();

    public static final TooltipBorderLoader TOOLTIP_BORDER_LOADER = new TooltipBorderLoader();
    public static final Identifier TOOLTIP_BORDER_LOADER_ID = Tiered.id("tooltip_loader");

    private static final Identifier ANVIL_TAB_ICON = Identifier.parse("tiered:textures/gui/anvil_tab_icon.png");
    private static final Identifier REFORGE_TAB_ICON = Identifier.parse("tiered:textures/gui/reforge_tab_icon.png");

    public static void init() {
        TabRegistry.registerOtherTab(new AnvilTab(Component.translatable("container.repair"), ANVIL_TAB_ICON, 0, AnvilScreen.class), AnvilScreen.class);
        TabRegistry.registerOtherTab(new ReforgeTab(Component.translatable("screen.tiered.reforging_screen"), REFORGE_TAB_ICON, 1, ReforgeScreen.class), AnvilScreen.class);
    }

}
