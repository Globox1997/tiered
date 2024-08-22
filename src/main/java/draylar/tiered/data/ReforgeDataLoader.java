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

        resourceManager.findResources("reforge_items", id -> id.getPath().endsWith(".json")).forEach((id, resourceRef) -> {
            try {
                InputStream stream = resourceRef.getInputStream();
                JsonObject data = JsonParser.parseReader(new InputStreamReader(stream)).getAsJsonObject();

                for (int u = 0; u < data.getAsJsonArray("items").size(); u++) {
                    if (Registries.ITEM.get(Identifier.of(data.getAsJsonArray("items").get(u).getAsString())).toString().equals("air")) {
                        LOGGER.info("Resource {} was not loaded cause {} is not a valid item identifier", id.toString(), data.getAsJsonArray("items").get(u).getAsString());
                        continue;
                    }
                    List<Item> baseItems = new ArrayList<Item>();
                    for (int i = 0; i < data.getAsJsonArray("base").size(); i++) {
                        if (Registries.ITEM.get(Identifier.of(data.getAsJsonArray("base").get(i).getAsString())).toString().equals("air")) {
                            LOGGER.info("Resource {} was not loaded cause {} is not a valid item identifier", id.toString(), data.getAsJsonArray("base").get(i).getAsString());
                            continue;
                        }
                        baseItems.add(Registries.ITEM.get(Identifier.of(data.getAsJsonArray("base").get(i).getAsString())));
                    }

                    reforgeIdentifiers.add(Identifier.of(data.getAsJsonArray("items").get(u).getAsString()));
                    reforgeBaseMap.put(Identifier.of(data.getAsJsonArray("items").get(u).getAsString()), baseItems);
                }
            } catch (Exception e) {
                LOGGER.error("Error occurred while loading resource {}. {}", id.toString(), e.toString());
            }
        });
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
