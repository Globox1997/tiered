package draylar.tiered;

import draylar.tiered.api.PotentialAttribute;
import draylar.tiered.api.ReforgeItem;
import draylar.tiered.data.AttributeDataLoader;
import draylar.tiered.data.ReforgeItemDataLoader;
import draylar.tiered.network.TieredClientPacket;
import draylar.tiered.reforge.ReforgeScreen;
import draylar.tiered.reforge.ReforgeScreenHandler;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TieredClient implements ClientModInitializer {

    // map for storing attributes before logging into a server
    public static final Map<Identifier, PotentialAttribute> CACHED_ATTRIBUTES = new HashMap<>();
    public static final List<ReforgeItem> CACHED_REFORGE_ITEM = new ArrayList<>();

    @Override
    public void onInitializeClient() {
        registerAttributeSyncHandler();
        registerReforgeItemSyncHandler();
        HandledScreens.<ReforgeScreenHandler, ReforgeScreen>register(Tiered.REFORGE_SCREEN_HANDLER_TYPE, ReforgeScreen::new);
        TieredClientPacket.init();
    }

    public static void registerAttributeSyncHandler() {
        ClientPlayNetworking.registerGlobalReceiver(Tiered.ATTRIBUTE_SYNC_PACKET, (client, play, packet, packetSender) -> {
            // save old attributes
            CACHED_ATTRIBUTES.putAll(Tiered.ATTRIBUTE_DATA_LOADER.getItemAttributes());
            Tiered.ATTRIBUTE_DATA_LOADER.getItemAttributes().clear();

            // for each id/attribute pair, load it
            int size = packet.readInt();
            for (int i = 0; i < size; i++) {
                Identifier id = new Identifier(packet.readString());
                PotentialAttribute pa = AttributeDataLoader.GSON.fromJson(packet.readString(), PotentialAttribute.class);
                Tiered.ATTRIBUTE_DATA_LOADER.getItemAttributes().put(id, pa);
            }
        });
    }

    public static void registerReforgeItemSyncHandler() {
        ClientPlayNetworking.registerGlobalReceiver(Tiered.REFORGE_ITEM_SYNC_PACKET, (client, play, packet, packetSender) -> {
            // save old attributes
            CACHED_REFORGE_ITEM.clear();
            CACHED_REFORGE_ITEM.addAll(Tiered.REFORGE_ITEM_DATA_LOADER.getReforgeItems());
            Tiered.REFORGE_ITEM_DATA_LOADER.getReforgeItems().clear();

            // for each id/attribute pair, load it
            int size = packet.readInt();
            for (int i = 0; i < size; i++) {
                ReforgeItem reforgeItem = ReforgeItemDataLoader.GSON.fromJson(packet.readString(), ReforgeItem.class);
                Tiered.REFORGE_ITEM_DATA_LOADER.getReforgeItems().add(reforgeItem);
            }
        });
    }
}
