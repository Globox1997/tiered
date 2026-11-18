package draylar.tiered.fabric;

import draylar.tiered.Tiered;
import draylar.tiered.command.CommandInit;
import draylar.tiered.network.TieredServerPacket;
import draylar.tiered.network.packet.AttributePacket;
import draylar.tiered.network.packet.HealthPacket;
import draylar.tiered.network.packet.ReforgeItemSyncPacket;
import draylar.tiered.network.packet.ReforgePacket;
import draylar.tiered.network.packet.ReforgeReadyPacket;
import draylar.tiered.network.packet.ReforgeScreenPacket;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.minecraft.server.packs.PackType;

public class TieredFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        Tiered.init();

        ResourceLoader.get(PackType.SERVER_DATA).registerReloadListener(Tiered.ATTRIBUTE_DATA_LOADER_ID, Tiered.ATTRIBUTE_DATA_LOADER);
        ResourceLoader.get(PackType.SERVER_DATA).registerReloadListener(Tiered.REFORGE_DATA_LOADER_ID, Tiered.REFORGE_DATA_LOADER);

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> CommandInit.register(dispatcher));

        PayloadTypeRegistry.clientboundPlay().register(AttributePacket.PACKET_ID, AttributePacket.PACKET_CODEC);
        PayloadTypeRegistry.clientboundPlay().register(HealthPacket.PACKET_ID, HealthPacket.PACKET_CODEC);
        PayloadTypeRegistry.clientboundPlay().register(ReforgeReadyPacket.PACKET_ID, ReforgeReadyPacket.PACKET_CODEC);
        PayloadTypeRegistry.clientboundPlay().register(ReforgeItemSyncPacket.PACKET_ID, ReforgeItemSyncPacket.PACKET_CODEC);
        PayloadTypeRegistry.serverboundPlay().register(ReforgePacket.PACKET_ID, ReforgePacket.PACKET_CODEC);
        PayloadTypeRegistry.serverboundPlay().register(ReforgeScreenPacket.PACKET_ID, ReforgeScreenPacket.PACKET_CODEC);

        ServerPlayNetworking.registerGlobalReceiver(ReforgeScreenPacket.PACKET_ID,
                (payload, context) -> context.server().execute(() -> TieredServerPacket.handleReforgeScreenPacket(context.player(), payload)));
        ServerPlayNetworking.registerGlobalReceiver(ReforgePacket.PACKET_ID, (payload, context) -> context.server().execute(() -> TieredServerPacket.handleReforgePacket(context.player())));

        ServerPlayConnectionEvents.INIT.register((handler, server) -> Tiered.onPlayerInit(handler.player));
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> Tiered.onPlayerJoin(handler.getPlayer()));
        ServerLifecycleEvents.END_DATA_PACK_RELOAD.register((server, resourceManager, success) -> Tiered.onDataPackReload(server, success));
    }

}
