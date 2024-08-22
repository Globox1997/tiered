package draylar.tiered.gson;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import net.minecraft.entity.attribute.EntityAttributeModifier;

import java.lang.reflect.Type;

public class EntityAttributeModifierSerializer implements JsonSerializer<EntityAttributeModifier> {

    @Override
    public JsonElement serialize(EntityAttributeModifier src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject obj = new JsonObject();
        obj.addProperty("amount", src.value());
        obj.addProperty("operation", src.operation().toString());
        obj.addProperty("name", src.id().toString());
        return obj;
    }
}
