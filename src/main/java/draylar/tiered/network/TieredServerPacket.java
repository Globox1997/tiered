package draylar.tiered.network;

import java.util.ArrayList;
import java.util.List;

import draylar.tiered.Tiered;
import draylar.tiered.access.AnvilScreenHandlerAccess;
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
import net.minecraft.registry.Registries;
import net.minecraft.screen.AnvilScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

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

            BlockPos pos = reforgingScreen ? (context.player().currentScreenHandler instanceof AnvilScreenHandler ? ((AnvilScreenHandlerAccess) context.player().currentScreenHandler).getPos() : null)
                    : (context.player().currentScreenHandler instanceof ReforgeScreenHandler ? ((ReforgeScreenHandler) context.player().currentScreenHandler).getPos() : null);
            if (pos != null) {
                context.server().execute(() -> {
                    if (reforgingScreen) {
                        context.player().openHandledScreen(new SimpleNamedScreenHandlerFactory((syncId, playerInventory, playerx) -> {
                            return new ReforgeScreenHandler(syncId, playerInventory, ScreenHandlerContext.create(playerx.getWorld(), pos));
                        }, Text.translatable("container.reforge")));
                    } else {
                        context.player().openHandledScreen(new SimpleNamedScreenHandlerFactory((syncId, playerInventory, playerx) -> {
                            return new AnvilScreenHandler(syncId, playerInventory, ScreenHandlerContext.create(playerx.getWorld(), pos));
                        }, Text.translatable("container.repair")));
                    }
                    LibzServerPacket.writeS2CMousePositionPacket(context.player(), mouseX, mouseY);
                });
            }
        });
        ServerPlayNetworking.registerGlobalReceiver(ReforgePacket.PACKET_ID, (payload, context) -> {
            context.server().execute(() -> {
                if (context.player().currentScreenHandler instanceof ReforgeScreenHandler reforgeScreenHandler) {
                    reforgeScreenHandler.reforge();
                }
            });
        });
    }

    public static void writeS2CHealthPacket(ServerPlayerEntity serverPlayerEntity) {
        ServerPlayNetworking.send(serverPlayerEntity, new HealthPacket(serverPlayerEntity.getHealth()));
    }

    public static void writeS2CReforgeReadyPacket(ServerPlayerEntity serverPlayerEntity, boolean disableButton) {
        ServerPlayNetworking.send(serverPlayerEntity, new ReforgeReadyPacket(disableButton));
    }

    public static void writeS2CReforgeItemSyncPacket(ServerPlayerEntity serverPlayerEntity) {
        List<Identifier> ids = new ArrayList<Identifier>();
        List<Integer> listSize = new ArrayList<Integer>();
        List<Integer> itemIds = new ArrayList<Integer>();

        Tiered.REFORGE_DATA_LOADER.getReforgeIdentifiers().forEach(id -> {
            ids.add(id);

            List<Integer> list = new ArrayList<Integer>();
            Tiered.REFORGE_DATA_LOADER.getReforgeBaseItems(Registries.ITEM.get(id)).forEach(item -> {
                list.add(Registries.ITEM.getRawId(item));
            });
            listSize.add(list.size());

            list.forEach(rawId -> {
                itemIds.add(rawId);
            });
        });

        ServerPlayNetworking.send(serverPlayerEntity, new ReforgeItemSyncPacket(ids, listSize, itemIds));
    }

    public static void writeS2CAttributePacket(ServerPlayerEntity serverPlayerEntity) {
        List<String> attributeIds = new ArrayList<String>();
        List<String> attributeJsons = new ArrayList<String>();

        Tiered.ATTRIBUTE_DATA_LOADER.getItemAttributes().forEach((id, attribute) -> {
            attributeIds.add(id.toString());
            attributeJsons.add(AttributeDataLoader.GSON.toJson(attribute));
        });

        ServerPlayNetworking.send(serverPlayerEntity, new AttributePacket(attributeIds, attributeJsons));
    }

}
