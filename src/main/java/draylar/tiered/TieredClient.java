package draylar.tiered;

import draylar.tiered.api.BorderTemplate;
import draylar.tiered.api.PotentialAttribute;
import draylar.tiered.data.TooltipBorderLoader;
import draylar.tiered.network.TieredClientPacket;
import draylar.tiered.reforge.ReforgeScreen;
import draylar.tiered.reforge.ReforgeScreenHandler;
import draylar.tiered.reforge.widget.AnvilTab;
import draylar.tiered.reforge.widget.ReforgeTab;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.libz.registry.TabRegistry;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.inventory.AnvilScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Environment(EnvType.CLIENT)
public class TieredClient implements ClientModInitializer {

    // map for storing attributes before logging into a server
    public static final Map<Identifier, PotentialAttribute> CACHED_ATTRIBUTES = new HashMap<>();

    public static final List<BorderTemplate> BORDER_TEMPLATES = new ArrayList<BorderTemplate>();

    private static final Identifier ANVIL_TAB_ICON = Identifier.parse("tiered:textures/gui/anvil_tab_icon.png");
    private static final Identifier REFORGE_TAB_ICON = Identifier.parse("tiered:textures/gui/reforge_tab_icon.png");

    @Override
    public void onInitializeClient() {
        MenuScreens.<ReforgeScreenHandler, ReforgeScreen>register(Tiered.REFORGE_SCREEN_HANDLER_TYPE, ReforgeScreen::new);
        TieredClientPacket.init();
        ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(new TooltipBorderLoader());
        TabRegistry.registerOtherTab(new AnvilTab(Component.translatable("container.repair"), ANVIL_TAB_ICON, 0, AnvilScreen.class), AnvilScreen.class);
        TabRegistry.registerOtherTab(new ReforgeTab(Component.translatable("screen.tiered.reforging_screen"), REFORGE_TAB_ICON, 1, ReforgeScreen.class), AnvilScreen.class);
    }

}
