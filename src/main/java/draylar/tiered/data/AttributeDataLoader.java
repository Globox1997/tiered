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
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.resource.JsonDataLoader;
import net.minecraft.resource.ResourceManager;
import net.minecraft.text.Style;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

public class AttributeDataLoader extends JsonDataLoader implements SimpleSynchronousResourceReloadListener {
    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().registerTypeAdapter(EntityAttributeModifier.class, new EntityAttributeModifierDeserializer())
            .registerTypeAdapter(EntityAttributeModifier.class, new EntityAttributeModifierSerializer()).registerTypeAdapter(EquipmentSlot.class, new EquipmentSlotSerializer())
            .registerTypeAdapter(EquipmentSlot.class, new EquipmentSlotDeserializer()).registerTypeAdapter(Style.class, new StyleDeserializer()).registerTypeAdapter(Style.class, new StyleSerializer()).create();

    private static final String PARSING_ERROR_MESSAGE = "Parsing error loading tier {}";
    private static final String LOADED_TIERS_MESSAGE = "Loaded {} tiers";
    private static final Logger LOGGER = LogManager.getLogger();

    private Map<Identifier, PotentialAttribute> itemAttributes = new HashMap<>();

    public AttributeDataLoader() {
        super(GSON, "item_attributes");
    }

    @Override
    protected void apply(Map<Identifier, JsonElement> loader, ResourceManager manager, Profiler profiler) {
        Map<Identifier, PotentialAttribute> readItemAttributes = Maps.newHashMap();

        for (Map.Entry<Identifier, JsonElement> entry : loader.entrySet()) {
            Identifier identifier = entry.getKey();
            try {
                PotentialAttribute itemAttribute = GSON.fromJson(entry.getValue(), PotentialAttribute.class);
                validate(itemAttribute);
                readItemAttributes.put(Identifier.of(itemAttribute.getID()), itemAttribute);
            } catch (RuntimeException exception) {
                LOGGER.error(PARSING_ERROR_MESSAGE, identifier, exception);
            }
        }

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

    @Override
    public Identifier getFabricId() {
        return Identifier.of("tiered", "item_attributes");
    }

    @Override
    public void reload(ResourceManager resourceManager) {
    }

}
