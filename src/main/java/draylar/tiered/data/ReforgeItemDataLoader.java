package draylar.tiered.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import draylar.tiered.api.ItemVerifier;
import draylar.tiered.api.ReforgeItem;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.resource.JsonDataLoader;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryEntryList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.stream.Collectors;

public class ReforgeItemDataLoader extends JsonDataLoader implements SimpleSynchronousResourceReloadListener {
    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    private static final Logger LOGGER = LogManager.getLogger();

    private static final String PARSING_ERROR_MESSAGE = "Parsing error loading reforge item data {}";
    private static final String LOADED_MESSAGE = "Loaded {} reforge item data";

    private final List<ReforgeItem> reforgeItems = new ArrayList<>();

    public ReforgeItemDataLoader() {
        super(GSON, "reforge_item");
    }

    @Override
    protected void apply(Map<Identifier, JsonElement> loader, ResourceManager manager, Profiler profiler) {
        Map<ItemVerifier, ReforgeItem> readReforgeItems = new HashMap<>();

        for (Map.Entry<Identifier, JsonElement> entry : loader.entrySet()) {
            Identifier identifier = entry.getKey();

            try {
                ReforgeItem reforgeItem = GSON.fromJson(entry.getValue(), ReforgeItem.class);
                if (reforgeItem.getProduct() == null) {
                    continue;
                }
                readReforgeItems.put(reforgeItem.getProduct(), reforgeItem);
            } catch (IllegalArgumentException | JsonParseException exception) {
                LOGGER.error(PARSING_ERROR_MESSAGE, identifier, exception);
            }
        }
        reforgeItems.clear();
        reforgeItems.addAll(readReforgeItems.values().stream()
                .sorted(Comparator.comparing(ReforgeItem::isCover)
                        .thenComparing(a -> a.getProduct().getId() == null ? "" : a.getProduct().getId()).reversed()
                ).toList());
        LOGGER.info(LOADED_MESSAGE, reforgeItems.size());
    }

    public List<ReforgeItem> getReforgeItems() {
        return reforgeItems;
    }

    public List<ItemStack> getReforgeItems(Item product) {
        List<ReforgeItem> filtered = reforgeItems.stream().filter(it -> it.getProduct().isValid(Registry.ITEM.getId(product))).toList();
        if (filtered.stream().anyMatch(ReforgeItem::isCover)) {
            filtered = filtered.stream().filter(ReforgeItem::isCover).findFirst().stream().toList();
        }
        if (filtered.isEmpty()) {
            return Collections.emptyList();
        }
        Set<ItemVerifier> itemVerifiers = new HashSet<>();
        filtered.forEach(value -> itemVerifiers.addAll(value.getBase()));
        Set<ItemStack> bases = new HashSet<>();
        for (ItemVerifier verifier : itemVerifiers) {
            if (verifier.getId() != null) {
                Optional<Item> item = Registry.ITEM.getOrEmpty(Identifier.tryParse(verifier.getId()));
                item.ifPresent(value -> bases.add(value.getDefaultStack()));
            } else if(verifier.getTagKey() != null) {
                Optional<RegistryEntryList.Named<Item>> entryList = Registry.ITEM.getEntryList(verifier.getTagKey());
                entryList.ifPresent(value -> bases.addAll(value.stream().map(it -> it.value().getDefaultStack()).toList()));
            }
        }
        return bases.stream().sorted(Comparator.comparing(c -> c.getName().getString())).collect(Collectors.toList());
    }

    @Override
    public Identifier getFabricId() {
        return new Identifier("tiered", "reforge_item");
    }

    @Override
    public void reload(ResourceManager resourceManager) {
    }

}
