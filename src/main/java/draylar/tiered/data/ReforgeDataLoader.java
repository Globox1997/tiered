package draylar.tiered.data;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.Item;

public class ReforgeDataLoader implements SimpleSynchronousResourceReloadListener {

    private static final Logger LOGGER = LogManager.getLogger("TieredZ");

    private List<ResourceLocation> reforgeIdentifiers = new ArrayList<>();
    private Map<ResourceLocation, List<Item>> reforgeBaseMap = new HashMap<>();

    @Override
    public ResourceLocation getFabricId() {
        return ResourceLocation.fromNamespaceAndPath("tiered", "reforge_loader");
    }

    @Override
    public void onResourceManagerReload(ResourceManager resourceManager) {
        List<ResourceLocation> reforgeIdentifiers = new ArrayList<>();
        Map<ResourceLocation, List<Item>> reforgeBaseMap = new HashMap<>();

        resourceManager.listResources("reforge_items", id -> id.getPath().endsWith(".json")).forEach((id, resourceRef) -> {
            try (InputStream stream = resourceRef.open(); InputStreamReader reader = new InputStreamReader(stream)) {
                JsonObject data = JsonParser.parseReader(reader).getAsJsonObject();

                for (int u = 0; u < data.getAsJsonArray("items").size(); u++) {
                    ResourceLocation itemId = ResourceLocation.tryParse(data.getAsJsonArray("items").get(u).getAsString());
                    if (itemId == null || !BuiltInRegistries.ITEM.containsKey(itemId)) {
                        LOGGER.info("Resource {} was not loaded cause {} is not a valid item identifier", id.toString(), data.getAsJsonArray("items").get(u).getAsString());
                        continue;
                    }
                    List<Item> baseItems = new ArrayList<Item>();
                    for (int i = 0; i < data.getAsJsonArray("base").size(); i++) {
                        ResourceLocation baseId = ResourceLocation.tryParse(data.getAsJsonArray("base").get(i).getAsString());
                        if (baseId == null || !BuiltInRegistries.ITEM.containsKey(baseId)) {
                            LOGGER.info("Resource {} was not loaded cause {} is not a valid item identifier", id.toString(), data.getAsJsonArray("base").get(i).getAsString());
                            continue;
                        }
                        baseItems.add(BuiltInRegistries.ITEM.get(baseId));
                    }

                    if (!reforgeIdentifiers.contains(itemId)) {
                        reforgeIdentifiers.add(itemId);
                    }
                    reforgeBaseMap.put(itemId, baseItems);
                }
            } catch (Exception e) {
                LOGGER.error("Error occurred while loading resource {}. {}", id.toString(), e.toString());
            }
        });

        this.reforgeIdentifiers = reforgeIdentifiers;
        this.reforgeBaseMap = reforgeBaseMap;
    }

    public List<Item> getReforgeBaseItems(Item item) {
        ArrayList<Item> list = new ArrayList<Item>();
        if (reforgeBaseMap.containsKey(BuiltInRegistries.ITEM.getKey(item))) {
            return reforgeBaseMap.get(BuiltInRegistries.ITEM.getKey(item));
        }
        return list;
    }

    public void putReforgeBaseItems(ResourceLocation id, List<Item> items) {
        reforgeBaseMap.put(id, items);
    }

    public void clearReforgeBaseItems() {
        reforgeBaseMap.clear();
    }

    public List<ResourceLocation> getReforgeIdentifiers() {
        return reforgeIdentifiers;
    }

}
