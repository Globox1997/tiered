package draylar.tiered.network;

import java.util.ArrayList;
import java.util.List;

import draylar.tiered.Tiered;
import draylar.tiered.api.PotentialAttribute;
import draylar.tiered.data.AttributeDataLoader;
import draylar.tiered.network.packet.AttributePacket;
import draylar.tiered.network.packet.HealthPacket;
import draylar.tiered.network.packet.ReforgeItemSyncPacket;
import draylar.tiered.network.packet.ReforgePacket;
import draylar.tiered.network.packet.ReforgeReadyPacket;
import draylar.tiered.network.packet.ReforgeScreenPacket;
import draylar.tiered.platform.Services;
import draylar.tiered.reforge.ReforgeScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;

public class TieredClientPacket {

    public static void handleReforgeReadyPacket(ReforgeReadyPacket payload) {
        if (Minecraft.getInstance().gui.screen() instanceof ReforgeScreen reforgeScreen) {
            reforgeScreen.reforgeButton.setDisabled(payload.disableButton());
            reforgeScreen.setUniqueReforge(payload.uniqueReforge());
        }
    }

    public static void handleHealthPacket(HealthPacket payload) {
        if (Minecraft.getInstance().player != null) {
            Minecraft.getInstance().player.setHealth(payload.health());
        }
    }

    public static void handleReforgeItemSyncPacket(ReforgeItemSyncPacket payload) {
        if (Minecraft.getInstance().hasSingleplayerServer()) {
            return;
        }
        List<Identifier> identifiers = payload.ids();
        List<Integer> listSize = payload.listSize();
        List<Integer> itemIds = payload.itemIds();
        Tiered.REFORGE_DATA_LOADER.clearReforgeBaseItems();

        int count = 0;
        for (int i = 0; i < identifiers.size(); i++) {
            List<Item> items = new ArrayList<Item>();

            for (int u = count; u < (count + listSize.get(i)); u++) {
                items.add(BuiltInRegistries.ITEM.byId(itemIds.get(u)));
            }
            count += listSize.get(i);
            Tiered.REFORGE_DATA_LOADER.putReforgeBaseItems(identifiers.get(i), items);
        }
    }

    public static void handleAttributePacket(AttributePacket payload) {
        if (Minecraft.getInstance().hasSingleplayerServer()) {
            return;
        }
        if (payload.reset()) {
            Tiered.ATTRIBUTE_DATA_LOADER.getItemAttributes().clear();
        }

        for (int i = 0; i < payload.attributeIds().size(); i++) {
            Identifier id = Identifier.parse(payload.attributeIds().get(i));
            PotentialAttribute pa = AttributeDataLoader.GSON.fromJson(payload.attributeJsons().get(i), PotentialAttribute.class);
            Tiered.ATTRIBUTE_DATA_LOADER.getItemAttributes().put(id, pa);
        }
    }

    public static void writeC2SScreenPacket(int mouseX, int mouseY, boolean reforgingScreen) {
        Services.network().sendToServer(new ReforgeScreenPacket(mouseX, mouseY, reforgingScreen));
    }

    public static void writeC2SReforgePacket() {
        Services.network().sendToServer(new ReforgePacket());
    }

}
