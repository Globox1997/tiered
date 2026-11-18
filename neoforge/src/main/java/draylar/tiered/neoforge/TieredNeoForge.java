package draylar.tiered.neoforge;

import draylar.tiered.Tiered;
import draylar.tiered.api.CustomEntityAttributes;
import draylar.tiered.command.CommandInit;
import draylar.tiered.neoforge.platform.NeoForgeRegistryHelper;
import draylar.tiered.network.TieredServerPacket;
import draylar.tiered.network.packet.AttributePacket;
import draylar.tiered.network.packet.HealthPacket;
import draylar.tiered.network.packet.ReforgeItemSyncPacket;
import draylar.tiered.network.packet.ReforgePacket;
import draylar.tiered.network.packet.ReforgeReadyPacket;
import draylar.tiered.network.packet.ReforgeScreenPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityTypes;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeModificationEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@Mod(Tiered.MOD_ID)
public class TieredNeoForge {

    public TieredNeoForge(IEventBus modBus) {
        Tiered.init();
        NeoForgeRegistryHelper.registerAll(modBus);

        modBus.addListener(RegisterPayloadHandlersEvent.class, TieredNeoForge::registerPayloads);
        modBus.addListener(EntityAttributeModificationEvent.class, TieredNeoForge::modifyAttributes);

        NeoForge.EVENT_BUS.addListener(AddServerReloadListenersEvent.class, event -> {
            event.addListener(Tiered.ATTRIBUTE_DATA_LOADER_ID, Tiered.ATTRIBUTE_DATA_LOADER);
            event.addListener(Tiered.REFORGE_DATA_LOADER_ID, Tiered.REFORGE_DATA_LOADER);
        });
        NeoForge.EVENT_BUS.addListener(RegisterCommandsEvent.class, event -> CommandInit.register(event.getDispatcher()));
        NeoForge.EVENT_BUS.addListener(PlayerEvent.PlayerLoggedInEvent.class, event -> {
            if (event.getEntity() instanceof ServerPlayer player) {
                Tiered.onPlayerInit(player);
                Tiered.onPlayerJoin(player);
            }
        });
        NeoForge.EVENT_BUS.addListener(OnDatapackSyncEvent.class, event -> {
            if (event.getPlayer() == null) {
                Tiered.onDataPackReload(event.getPlayerList().getServer(), true);
            }
        });
    }

    private static void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToClient(AttributePacket.PACKET_ID, AttributePacket.PACKET_CODEC);
        registrar.playToClient(HealthPacket.PACKET_ID, HealthPacket.PACKET_CODEC);
        registrar.playToClient(ReforgeReadyPacket.PACKET_ID, ReforgeReadyPacket.PACKET_CODEC);
        registrar.playToClient(ReforgeItemSyncPacket.PACKET_ID, ReforgeItemSyncPacket.PACKET_CODEC);
        registrar.playToServer(ReforgePacket.PACKET_ID, ReforgePacket.PACKET_CODEC, (payload, context) -> TieredServerPacket.handleReforgePacket((ServerPlayer) context.player()));
        registrar.playToServer(ReforgeScreenPacket.PACKET_ID, ReforgeScreenPacket.PACKET_CODEC,
                (payload, context) -> TieredServerPacket.handleReforgeScreenPacket((ServerPlayer) context.player(), payload));
    }

    private static void modifyAttributes(EntityAttributeModificationEvent event) {
        event.add(EntityTypes.PLAYER, CustomEntityAttributes.CRIT_CHANCE);
        event.add(EntityTypes.PLAYER, CustomEntityAttributes.DIG_SPEED);
        event.add(EntityTypes.PLAYER, CustomEntityAttributes.DURABLE);
        event.add(EntityTypes.PLAYER, CustomEntityAttributes.RANGE_ATTACK_DAMAGE);
    }

}
