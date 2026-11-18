package draylar.tiered.fabric.client;

import draylar.tiered.Tiered;
import draylar.tiered.TieredClient;
import draylar.tiered.network.TieredClientPacket;
import draylar.tiered.network.packet.AttributePacket;
import draylar.tiered.network.packet.HealthPacket;
import draylar.tiered.network.packet.ReforgeItemSyncPacket;
import draylar.tiered.network.packet.ReforgeReadyPacket;
import draylar.tiered.reforge.ReforgeScreen;
import draylar.tiered.reforge.ReforgeScreenHandler;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.server.packs.PackType;

@Environment(EnvType.CLIENT)
public class TieredFabricClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        MenuScreens.<ReforgeScreenHandler, ReforgeScreen>register(Tiered.REFORGE_SCREEN_HANDLER_TYPE, ReforgeScreen::new);
        ResourceLoader.get(PackType.CLIENT_RESOURCES).registerReloadListener(TieredClient.TOOLTIP_BORDER_LOADER_ID, TieredClient.TOOLTIP_BORDER_LOADER);
        TieredClient.init();

        ClientPlayNetworking.registerGlobalReceiver(ReforgeReadyPacket.PACKET_ID, (payload, context) -> context.client().execute(() -> TieredClientPacket.handleReforgeReadyPacket(payload)));
        ClientPlayNetworking.registerGlobalReceiver(HealthPacket.PACKET_ID, (payload, context) -> context.client().execute(() -> TieredClientPacket.handleHealthPacket(payload)));
        ClientPlayNetworking.registerGlobalReceiver(ReforgeItemSyncPacket.PACKET_ID, (payload, context) -> context.client().execute(() -> TieredClientPacket.handleReforgeItemSyncPacket(payload)));
        ClientPlayNetworking.registerGlobalReceiver(AttributePacket.PACKET_ID, (payload, context) -> context.client().execute(() -> TieredClientPacket.handleAttributePacket(payload)));
    }

}
