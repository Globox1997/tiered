package draylar.tiered.gson;

import com.google.gson.*;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;
import net.minecraft.network.chat.Style;

public class StyleSerializer implements JsonSerializer<Style> {

    @Override
    @Nullable
    public JsonElement serialize(Style style, Type type, JsonSerializationContext jsonSerializationContext) {
        if (style.isEmpty()) {
            return null;
        }
        JsonObject jsonObject = new JsonObject();
        if (style.isBold()) {
            jsonObject.addProperty("bold", style.isBold());
        }
        if (style.isItalic()) {
            jsonObject.addProperty("italic", style.isItalic());
        }
        if (style.isUnderlined()) {
            jsonObject.addProperty("underlined", style.isUnderlined());
        }
        if (style.isStrikethrough()) {
            jsonObject.addProperty("strikethrough", style.isStrikethrough());
        }
        if (style.isObfuscated()) {
            jsonObject.addProperty("obfuscated", style.isObfuscated());
        }
        if (style.getColor() != null) {
            jsonObject.addProperty("color", style.getColor().serialize());
        }
        if (style.getInsertion() != null) {
            jsonObject.add("insertion", jsonSerializationContext.serialize(style.getInsertion()));
        }
        if (style.getFont() != null) {
            jsonObject.addProperty("font", style.getFont().toString());
        }
        return jsonObject;
    }
}
