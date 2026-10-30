package draylar.tiered.network;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import draylar.tiered.Tiered;
import draylar.tiered.access.AnvilScreenHandlerAccess;
import draylar.tiered.api.PotentialAttribute;
import draylar.tiered.config.ConfigInit;
import draylar.tiered.data.AttributeDataLoader;
import draylar.tiered.network.packet.AttributePacket;
import draylar.tiered.network.packet.HealthPacket;
import draylar.tiered.network.packet.ReforgeItemSyncPacket;
import draylar.tiered.network.packet.ReforgePacket;
import draylar.tiered.network.packet.ReforgeReadyPacket;
import draylar.tiered.network.packet.ReforgeScreenPacket;
import draylar.tiered.reforge.ReforgeScreenHandler;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.libz.network.LibzServerPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;

public class TieredServerPacket {

    public static void init() {
        PayloadTypeRegistry.playS2C().register(AttributePacket.PACKET_ID, AttributePacket.PACKET_CODEC);
        PayloadTypeRegistry.playS2C().register(HealthPacket.PACKET_ID, HealthPacket.PACKET_CODEC);
        PayloadTypeRegistry.playC2S().register(ReforgePacket.PACKET_ID, ReforgePacket.PACKET_CODEC);
        PayloadTypeRegistry.playS2C().register(ReforgeReadyPacket.PACKET_ID, ReforgeReadyPacket.PACKET_CODEC);
        PayloadTypeRegistry.playS2C().register(ReforgeItemSyncPacket.PACKET_ID, ReforgeItemSyncPacket.PACKET_CODEC);
        PayloadTypeRegistry.playC2S().register(ReforgeScreenPacket.PACKET_ID, ReforgeScreenPacket.PACKET_CODEC);

        ServerPlayNetworking.registerGlobalReceiver(ReforgeScreenPacket.PACKET_ID, (payload, context) -> {
            int mouseX = payload.mouseX();
            int mouseY = payload.mouseY();
            Boolean reforgingScreen = payload.reforgingScreen();

            BlockPos pos = reforgingScreen ? (context.player().containerMenu instanceof AnvilMenu ? ((AnvilScreenHandlerAccess) context.player().containerMenu).getPos() : null)
                    : (context.player().containerMenu instanceof ReforgeScreenHandler ? ((ReforgeScreenHandler) context.player().containerMenu).getPos() : null);
            if (pos != null) {
                context.server().execute(() -> {
                    if (reforgingScreen) {
                        context.player().openMenu(new SimpleMenuProvider((syncId, playerInventory, playerx) -> {
                            return new ReforgeScreenHandler(syncId, playerInventory, ContainerLevelAccess.create(playerx.level(), pos));
                        }, Component.translatable("container.reforge")));
                    } else {
                        context.player().openMenu(new SimpleMenuProvider((syncId, playerInventory, playerx) -> {
                            return new AnvilMenu(syncId, playerInventory, ContainerLevelAccess.create(playerx.level(), pos));
                        }, Component.translatable("container.repair")));
                    }
                    LibzServerPacket.writeS2CMousePositionPacket(context.player(), mouseX, mouseY);
                });
            }
        });
        ServerPlayNetworking.registerGlobalReceiver(ReforgePacket.PACKET_ID, (payload, context) -> {
            context.server().execute(() -> {
                if (context.player().containerMenu instanceof ReforgeScreenHandler reforgeScreenHandler) {
                    reforgeScreenHandler.reforge();
                }
            });
        });
    }

    public static void writeS2CHealthPacket(ServerPlayer serverPlayerEntity) {
        ServerPlayNetworking.send(serverPlayerEntity, new HealthPacket(serverPlayerEntity.getHealth()));
    }

    public static void writeS2CReforgeReadyPacket(ServerPlayer serverPlayerEntity, boolean disableButton) {
        ServerPlayNetworking.send(serverPlayerEntity, new ReforgeReadyPacket(disableButton, ConfigInit.CONFIG.uniqueReforge));
    }

    public static void writeS2CReforgeItemSyncPacket(ServerPlayer serverPlayerEntity) {
        List<ResourceLocation> ids = new ArrayList<ResourceLocation>();
        List<Integer> listSize = new ArrayList<Integer>();
        List<Integer> itemIds = new ArrayList<Integer>();

        Tiered.REFORGE_DATA_LOADER.getReforgeIdentifiers().forEach(id -> {
            ids.add(id);

            List<Integer> list = new ArrayList<Integer>();
            Tiered.REFORGE_DATA_LOADER.getReforgeBaseItems(BuiltInRegistries.ITEM.get(id)).forEach(item -> {
                list.add(BuiltInRegistries.ITEM.getId(item));
            });
            listSize.add(list.size());

            list.forEach(rawId -> {
                itemIds.add(rawId);
            });
        });

        ServerPlayNetworking.send(serverPlayerEntity, new ReforgeItemSyncPacket(ids, listSize, itemIds));
    }

    public static void writeS2CAttributePacket(ServerPlayer serverPlayerEntity) {
        List<String> attributeIds = new ArrayList<String>();
        List<String> attributeJsons = new ArrayList<String>();
        boolean reset = true;
        int packetLength = 0;

        for (Map.Entry<ResourceLocation, PotentialAttribute> entry : Tiered.ATTRIBUTE_DATA_LOADER.getItemAttributes().entrySet()) {
            String id = entry.getKey().toString();
            String json = AttributeDataLoader.GSON.toJson(entry.getValue());
            int entryLength = id.length() + json.length();
            if (!attributeIds.isEmpty() && packetLength + entryLength > AttributePacket.MAX_STRING_LENGTH) {
                ServerPlayNetworking.send(serverPlayerEntity, new AttributePacket(reset, attributeIds, attributeJsons));
                reset = false;
                attributeIds = new ArrayList<String>();
                attributeJsons = new ArrayList<String>();
                packetLength = 0;
            }
            attributeIds.add(id);
            attributeJsons.add(json);
            packetLength += entryLength;
        }

        ServerPlayNetworking.send(serverPlayerEntity, new AttributePacket(reset, attributeIds, attributeJsons));
    }

}
