package draylar.tiered.data;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import draylar.tiered.api.AttributeTemplate;
import draylar.tiered.api.PotentialAttribute;
import draylar.tiered.gson.*;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

public class AttributeDataLoader extends SimpleJsonResourceReloadListener<JsonElement> implements SimpleSynchronousResourceReloadListener {
    public static final Gson GSON = new GsonBuilder().disableHtmlEscaping().registerTypeAdapter(AttributeModifier.class, new EntityAttributeModifierDeserializer())
            .registerTypeAdapter(AttributeModifier.class, new EntityAttributeModifierSerializer()).registerTypeAdapter(EquipmentSlot.class, new EquipmentSlotSerializer())
            .registerTypeAdapter(EquipmentSlot.class, new EquipmentSlotDeserializer()).registerTypeAdapter(Style.class, new StyleDeserializer()).registerTypeAdapter(Style.class, new StyleSerializer()).create();

    private static final String PARSING_ERROR_MESSAGE = "Parsing error loading tier {}";
    private static final String LOADED_TIERS_MESSAGE = "Loaded {} tiers";
    private static final Logger LOGGER = LogManager.getLogger();

    private Map<Identifier, PotentialAttribute> itemAttributes = new HashMap<>();
    private Map<Identifier, PotentialAttribute> previousItemAttributes = new HashMap<>();

    public AttributeDataLoader() {
        super(ExtraCodecs.JSON, FileToIdConverter.json("item_attributes"));
    }

    @Override
    protected void apply(Map<Identifier, JsonElement> loader, ResourceManager manager, ProfilerFiller profiler) {
        Map<Identifier, PotentialAttribute> readItemAttributes = Maps.newHashMap();

        for (Map.Entry<Identifier, JsonElement> entry : loader.entrySet()) {
            Identifier identifier = entry.getKey();
            try {
                PotentialAttribute itemAttribute = GSON.fromJson(entry.getValue(), PotentialAttribute.class);
                validate(itemAttribute);
                readItemAttributes.put(Identifier.parse(itemAttribute.getID()), itemAttribute);
            } catch (RuntimeException exception) {
                LOGGER.error(PARSING_ERROR_MESSAGE, identifier, exception);
            }
        }

        previousItemAttributes = itemAttributes;
        itemAttributes = readItemAttributes;
        LOGGER.info(LOADED_TIERS_MESSAGE, readItemAttributes.size());
    }

    private static void validate(PotentialAttribute itemAttribute) {
        if (itemAttribute == null) {
            throw new JsonParseException("Tier is empty");
        }
        if (itemAttribute.getID() == null || Identifier.tryParse(itemAttribute.getID()) == null) {
            throw new JsonParseException("Tier requires a valid id");
        }
        if (itemAttribute.getVerifiers() == null || itemAttribute.getVerifiers().contains(null)) {
            throw new JsonParseException("Tier " + itemAttribute.getID() + " requires verifiers");
        }
        if (itemAttribute.getAttributes() == null) {
            throw new JsonParseException("Tier " + itemAttribute.getID() + " requires attributes");
        }
        for (AttributeTemplate attributeTemplate : itemAttribute.getAttributes()) {
            if (attributeTemplate == null || attributeTemplate.getAttributeTypeID() == null || Identifier.tryParse(attributeTemplate.getAttributeTypeID()) == null
                    || attributeTemplate.getEntityAttributeModifier() == null) {
                throw new JsonParseException("Tier " + itemAttribute.getID() + " has an invalid attribute entry");
            }
        }
    }

    public Map<Identifier, PotentialAttribute> getItemAttributes() {
        return itemAttributes;
    }

    public Map<Identifier, PotentialAttribute> getPreviousItemAttributes() {
        return previousItemAttributes;
    }

    @Override
    public Identifier getFabricId() {
        return Identifier.fromNamespaceAndPath("tiered", "item_attributes");
    }

    @Override
    public void onResourceManagerReload(ResourceManager resourceManager) {
    }

}
