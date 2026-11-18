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
import draylar.tiered.network.packet.ReforgeReadyPacket;
import draylar.tiered.network.packet.ReforgeScreenPacket;
import draylar.tiered.reforge.ReforgeScreenHandler;
import draylar.tiered.platform.Services;
import net.libz.network.LibzServerPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;

public class TieredServerPacket {

    public static void handleReforgeScreenPacket(ServerPlayer player, ReforgeScreenPacket payload) {
        int mouseX = payload.mouseX();
        int mouseY = payload.mouseY();
        boolean reforgingScreen = payload.reforgingScreen();

        BlockPos pos = reforgingScreen ? (player.containerMenu instanceof AnvilMenu ? ((AnvilScreenHandlerAccess) player.containerMenu).getPos() : null)
                : (player.containerMenu instanceof ReforgeScreenHandler ? ((ReforgeScreenHandler) player.containerMenu).getPos() : null);
        if (pos != null) {
            if (reforgingScreen) {
                player.openMenu(new SimpleMenuProvider((syncId, playerInventory, playerx) -> {
                    return new ReforgeScreenHandler(syncId, playerInventory, ContainerLevelAccess.create(playerx.level(), pos));
                }, Component.translatable("container.reforge")));
            } else {
                player.openMenu(new SimpleMenuProvider((syncId, playerInventory, playerx) -> {
                    return new AnvilMenu(syncId, playerInventory, ContainerLevelAccess.create(playerx.level(), pos));
                }, Component.translatable("container.repair")));
            }
            LibzServerPacket.writeS2CMousePositionPacket(player, mouseX, mouseY);
        }
    }

    public static void handleReforgePacket(ServerPlayer player) {
        if (player.containerMenu instanceof ReforgeScreenHandler reforgeScreenHandler) {
            reforgeScreenHandler.reforge();
        }
    }

    public static void writeS2CHealthPacket(ServerPlayer serverPlayerEntity) {
        Services.network().sendToPlayer(serverPlayerEntity, new HealthPacket(serverPlayerEntity.getHealth()));
    }

    public static void writeS2CReforgeReadyPacket(ServerPlayer serverPlayerEntity, boolean disableButton) {
        Services.network().sendToPlayer(serverPlayerEntity, new ReforgeReadyPacket(disableButton, ConfigInit.CONFIG.uniqueReforge));
    }

    public static void writeS2CReforgeItemSyncPacket(ServerPlayer serverPlayerEntity) {
        List<Identifier> ids = new ArrayList<Identifier>();
        List<Integer> listSize = new ArrayList<Integer>();
        List<Integer> itemIds = new ArrayList<Integer>();

        Tiered.REFORGE_DATA_LOADER.getReforgeIdentifiers().forEach(id -> {
            ids.add(id);

            List<Integer> list = new ArrayList<Integer>();
            Tiered.REFORGE_DATA_LOADER.getReforgeBaseItems(BuiltInRegistries.ITEM.getValue(id)).forEach(item -> {
                list.add(BuiltInRegistries.ITEM.getId(item));
            });
            listSize.add(list.size());

            list.forEach(rawId -> {
                itemIds.add(rawId);
            });
        });

        Services.network().sendToPlayer(serverPlayerEntity, new ReforgeItemSyncPacket(ids, listSize, itemIds));
    }

    public static void writeS2CAttributePacket(ServerPlayer serverPlayerEntity) {
        List<String> attributeIds = new ArrayList<String>();
        List<String> attributeJsons = new ArrayList<String>();
        boolean reset = true;
        int packetLength = 0;

        for (Map.Entry<Identifier, PotentialAttribute> entry : Tiered.ATTRIBUTE_DATA_LOADER.getItemAttributes().entrySet()) {
            String id = entry.getKey().toString();
            String json = AttributeDataLoader.GSON.toJson(entry.getValue());
            int entryLength = id.length() + json.length();
            if (!attributeIds.isEmpty() && packetLength + entryLength > AttributePacket.MAX_STRING_LENGTH) {
                Services.network().sendToPlayer(serverPlayerEntity, new AttributePacket(reset, attributeIds, attributeJsons));
                reset = false;
                attributeIds = new ArrayList<String>();
                attributeJsons = new ArrayList<String>();
                packetLength = 0;
            }
            attributeIds.add(id);
            attributeJsons.add(json);
            packetLength += entryLength;
        }

        Services.network().sendToPlayer(serverPlayerEntity, new AttributePacket(reset, attributeIds, attributeJsons));
    }

}
