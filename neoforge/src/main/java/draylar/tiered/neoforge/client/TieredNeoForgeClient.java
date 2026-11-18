package draylar.tiered.neoforge.client;

import draylar.tiered.Tiered;
import draylar.tiered.TieredClient;
import draylar.tiered.config.TieredConfig;
import draylar.tiered.network.TieredClientPacket;
import draylar.tiered.network.packet.AttributePacket;
import draylar.tiered.network.packet.HealthPacket;
import draylar.tiered.network.packet.ReforgeItemSyncPacket;
import draylar.tiered.network.packet.ReforgeReadyPacket;
import draylar.tiered.reforge.ReforgeScreen;
import me.shedaniel.autoconfig.AutoConfigClient;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.AddClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.client.network.event.RegisterClientPayloadHandlersEvent;

@Mod(value = Tiered.MOD_ID, dist = Dist.CLIENT)
public class TieredNeoForgeClient {

    public TieredNeoForgeClient(IEventBus modBus, ModContainer container) {
        TieredClient.init();

        modBus.addListener(RegisterMenuScreensEvent.class, event -> event.register(Tiered.REFORGE_SCREEN_HANDLER_TYPE, ReforgeScreen::new));
        modBus.addListener(AddClientReloadListenersEvent.class, event -> event.addListener(TieredClient.TOOLTIP_BORDER_LOADER_ID, TieredClient.TOOLTIP_BORDER_LOADER));
        modBus.addListener(RegisterClientPayloadHandlersEvent.class, event -> {
            event.register(ReforgeReadyPacket.PACKET_ID, (payload, context) -> TieredClientPacket.handleReforgeReadyPacket(payload));
            event.register(HealthPacket.PACKET_ID, (payload, context) -> TieredClientPacket.handleHealthPacket(payload));
            event.register(ReforgeItemSyncPacket.PACKET_ID, (payload, context) -> TieredClientPacket.handleReforgeItemSyncPacket(payload));
            event.register(AttributePacket.PACKET_ID, (payload, context) -> TieredClientPacket.handleAttributePacket(payload));
        });

        container.registerExtensionPoint(IConfigScreenFactory.class, (modContainer, parent) -> AutoConfigClient.getConfigScreen(TieredConfig.class, parent).get());
    }

}
