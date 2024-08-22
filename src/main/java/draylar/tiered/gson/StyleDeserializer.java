package draylar.tiered.gson;

import com.google.gson.*;
import draylar.tiered.mixin.access.StyleAccessor;
import net.minecraft.text.Style;
import net.minecraft.text.TextColor;
import net.minecraft.util.Identifier;
import net.minecraft.util.InvalidIdentifierException;
import net.minecraft.util.JsonHelper;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;
import java.util.Optional;

public class StyleDeserializer implements JsonDeserializer<Style> {

    @Nullable
    private static String parseInsertion(JsonObject root) {
        return JsonHelper.getString(root, "insertion", null);
    }

    @Nullable
    private static TextColor parseColor(JsonObject root) {
        if (root.has("color")) {
            String string = JsonHelper.getString(root, "color");
            return TextColor.parse(string).getOrThrow();
        }
        return null;
    }

    @Nullable
    private static Boolean parseNullableBoolean(JsonObject root, String key) {
        if (root.has(key)) {
            return root.get(key).getAsBoolean();
        }
        return null;
    }

    @Nullable
    private static Identifier getFont(JsonObject root) {
        if (root.has("font")) {
            String string = JsonHelper.getString(root, "font");
            try {
                return Identifier.of(string);
            } catch (InvalidIdentifierException invalidIdentifierException) {
                throw new JsonSyntaxException("Invalid font name: " + string);
            }
        }
        return null;
    }

    @Nullable
    @Override
    public Style deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        if (jsonElement.isJsonObject()) {
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            if (jsonObject == null) {
                return null;
            }
            Boolean bold = parseNullableBoolean(jsonObject, "bold");
            Boolean italic = parseNullableBoolean(jsonObject, "italic");
            Boolean underlined = parseNullableBoolean(jsonObject, "underlined");
            Boolean strikethrough = parseNullableBoolean(jsonObject, "strikethrough");
            Boolean obfuscated = parseNullableBoolean(jsonObject, "obfuscated");
            TextColor textColor = parseColor(jsonObject);
            String insertion = parseInsertion(jsonObject);
            Identifier font = getFont(jsonObject);
            return StyleAccessor.invokeOf(Optional.ofNullable(textColor), Optional.ofNullable(bold), Optional.ofNullable(italic), Optional.ofNullable(underlined), Optional.ofNullable(strikethrough),
                    Optional.ofNullable(obfuscated), Optional.empty(), Optional.empty(), Optional.ofNullable(insertion), Optional.ofNullable(font));
        }
        return null;
    }
}
