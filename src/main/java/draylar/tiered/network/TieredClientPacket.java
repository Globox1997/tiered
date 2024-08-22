package draylar.tiered.network;

import java.util.ArrayList;
import java.util.List;

import draylar.tiered.Tiered;
import draylar.tiered.TieredClient;
import draylar.tiered.api.PotentialAttribute;
import draylar.tiered.data.AttributeDataLoader;
import draylar.tiered.network.packet.AttributePacket;
import draylar.tiered.network.packet.HealthPacket;
import draylar.tiered.network.packet.ReforgeItemSyncPacket;
import draylar.tiered.network.packet.ReforgePacket;
import draylar.tiered.network.packet.ReforgeReadyPacket;
import draylar.tiered.network.packet.ReforgeScreenPacket;
import draylar.tiered.reforge.ReforgeScreen;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class TieredClientPacket {

    @SuppressWarnings("resource")
    public static void init() {
        ClientPlayNetworking.registerGlobalReceiver(ReforgeReadyPacket.PACKET_ID, (payload, context) -> {
            boolean disableButton = payload.disableButton();
            context.client().execute(() -> {
                if (context.client().currentScreen instanceof ReforgeScreen reforgeScreen) {
                    reforgeScreen.reforgeButton.setDisabled(disableButton);
                }
            });
        });
        ClientPlayNetworking.registerGlobalReceiver(HealthPacket.PACKET_ID, (payload, context) -> {
            float health = payload.health();
            context.client().execute(() -> {
                context.player().setHealth(health);
            });
        });
        ClientPlayNetworking.registerGlobalReceiver(ReforgeItemSyncPacket.PACKET_ID, (payload, context) -> {
            List<Identifier> identifiers = payload.ids();
            List<Integer> listSize = payload.listSize();
            List<Integer> itemIds = payload.itemIds();

            context.client().execute(() -> {
                Tiered.REFORGE_DATA_LOADER.clearReforgeBaseItems();

                int count = 0;
                for (int i = 0; i < identifiers.size(); i++) {
                    List<Item> items = new ArrayList<Item>();

                    for (int u = count; u < (count + listSize.get(i)); u++) {
                        items.add(Registries.ITEM.get(itemIds.get(u)));
                    }
                    count += listSize.get(i);
                    Tiered.REFORGE_DATA_LOADER.putReforgeBaseItems(identifiers.get(i), items);
                }
            });
        });
        ClientPlayNetworking.registerGlobalReceiver(AttributePacket.PACKET_ID, (payload, context) -> {
            // save old attributes
            TieredClient.CACHED_ATTRIBUTES.putAll(Tiered.ATTRIBUTE_DATA_LOADER.getItemAttributes());
            Tiered.ATTRIBUTE_DATA_LOADER.getItemAttributes().clear();

            // for each id/attribute pair, load it
            for (int i = 0; i < payload.attributeIds().size(); i++) {
                Identifier id = Identifier.of(payload.attributeIds().get(i));
                PotentialAttribute pa = AttributeDataLoader.GSON.fromJson(payload.attributeJsons().get(i), PotentialAttribute.class);
                Tiered.ATTRIBUTE_DATA_LOADER.getItemAttributes().put(id, pa);
            }
        });
    }

    public static void writeC2SScreenPacket(int mouseX, int mouseY, boolean reforgingScreen) {
        ClientPlayNetworking.send(new ReforgeScreenPacket(mouseX, mouseY, reforgingScreen));
    }

    public static void writeC2SReforgePacket() {
        ClientPlayNetworking.send(new ReforgePacket());
    }

}
