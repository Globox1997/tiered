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
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

public class ReforgeDataLoader implements SimpleSynchronousResourceReloadListener {

    private static final Logger LOGGER = LogManager.getLogger("TieredZ");

    private List<Identifier> reforgeIdentifiers = new ArrayList<>();
    private Map<Identifier, List<Item>> reforgeBaseMap = new HashMap<>();

    @Override
    public Identifier getFabricId() {
        return Identifier.of("tiered", "reforge_loader");
    }

    @Override
    public void reload(ResourceManager resourceManager) {
        List<Identifier> reforgeIdentifiers = new ArrayList<>();
        Map<Identifier, List<Item>> reforgeBaseMap = new HashMap<>();

        resourceManager.findResources("reforge_items", id -> id.getPath().endsWith(".json")).forEach((id, resourceRef) -> {
            try (InputStream stream = resourceRef.getInputStream(); InputStreamReader reader = new InputStreamReader(stream)) {
                JsonObject data = JsonParser.parseReader(reader).getAsJsonObject();

                for (int u = 0; u < data.getAsJsonArray("items").size(); u++) {
                    Identifier itemId = Identifier.tryParse(data.getAsJsonArray("items").get(u).getAsString());
                    if (itemId == null || !Registries.ITEM.containsId(itemId)) {
                        LOGGER.info("Resource {} was not loaded cause {} is not a valid item identifier", id.toString(), data.getAsJsonArray("items").get(u).getAsString());
                        continue;
                    }
                    List<Item> baseItems = new ArrayList<Item>();
                    for (int i = 0; i < data.getAsJsonArray("base").size(); i++) {
                        Identifier baseId = Identifier.tryParse(data.getAsJsonArray("base").get(i).getAsString());
                        if (baseId == null || !Registries.ITEM.containsId(baseId)) {
                            LOGGER.info("Resource {} was not loaded cause {} is not a valid item identifier", id.toString(), data.getAsJsonArray("base").get(i).getAsString());
                            continue;
                        }
                        baseItems.add(Registries.ITEM.get(baseId));
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
        if (reforgeBaseMap.containsKey(Registries.ITEM.getId(item))) {
            return reforgeBaseMap.get(Registries.ITEM.getId(item));
        }
        return list;
    }

    public void putReforgeBaseItems(Identifier id, List<Item> items) {
        reforgeBaseMap.put(id, items);
    }

    public void clearReforgeBaseItems() {
        reforgeBaseMap.clear();
    }

    public List<Identifier> getReforgeIdentifiers() {
        return reforgeIdentifiers;
    }

}
