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
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

@Environment(EnvType.CLIENT)
public class TieredClientPacket {

    @SuppressWarnings("resource")
    public static void init() {
        ClientPlayNetworking.registerGlobalReceiver(ReforgeReadyPacket.PACKET_ID, (payload, context) -> {
            boolean disableButton = payload.disableButton();
            boolean uniqueReforge = payload.uniqueReforge();
            context.client().execute(() -> {
                if (context.client().screen instanceof ReforgeScreen reforgeScreen) {
                    reforgeScreen.reforgeButton.setDisabled(disableButton);
                    reforgeScreen.setUniqueReforge(uniqueReforge);
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
            List<ResourceLocation> identifiers = payload.ids();
            List<Integer> listSize = payload.listSize();
            List<Integer> itemIds = payload.itemIds();

            context.client().execute(() -> {
                if (context.client().hasSingleplayerServer()) {
                    return;
                }
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
            });
        });
        ClientPlayNetworking.registerGlobalReceiver(AttributePacket.PACKET_ID, (payload, context) -> {
            context.client().execute(() -> {
                if (context.client().hasSingleplayerServer()) {
                    return;
                }
                if (payload.reset()) {
                    TieredClient.CACHED_ATTRIBUTES.putAll(Tiered.ATTRIBUTE_DATA_LOADER.getItemAttributes());
                    Tiered.ATTRIBUTE_DATA_LOADER.getItemAttributes().clear();
                }

                for (int i = 0; i < payload.attributeIds().size(); i++) {
                    ResourceLocation id = ResourceLocation.parse(payload.attributeIds().get(i));
                    PotentialAttribute pa = AttributeDataLoader.GSON.fromJson(payload.attributeJsons().get(i), PotentialAttribute.class);
                    Tiered.ATTRIBUTE_DATA_LOADER.getItemAttributes().put(id, pa);
                }
            });
        });
    }

    public static void writeC2SScreenPacket(int mouseX, int mouseY, boolean reforgingScreen) {
        ClientPlayNetworking.send(new ReforgeScreenPacket(mouseX, mouseY, reforgingScreen));
    }

    public static void writeC2SReforgePacket() {
        ClientPlayNetworking.send(new ReforgePacket());
    }

}
