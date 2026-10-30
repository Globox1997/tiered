package draylar.tiered.gson;

import com.google.gson.*;
import draylar.tiered.mixin.access.StyleAccessor;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;
import java.util.Optional;
import net.minecraft.ResourceLocationException;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;

public class StyleDeserializer implements JsonDeserializer<Style> {

    @Nullable
    private static String parseInsertion(JsonObject root) {
        return GsonHelper.getAsString(root, "insertion", null);
    }

    @Nullable
    private static TextColor parseColor(JsonObject root) {
        if (root.has("color")) {
            String string = GsonHelper.getAsString(root, "color");
            return TextColor.parseColor(string).getOrThrow();
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
    private static ResourceLocation getFont(JsonObject root) {
        if (root.has("font")) {
            String string = GsonHelper.getAsString(root, "font");
            try {
                return ResourceLocation.parse(string);
            } catch (ResourceLocationException invalidIdentifierException) {
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
            ResourceLocation font = getFont(jsonObject);
            return StyleAccessor.invokeOf(Optional.ofNullable(textColor), Optional.ofNullable(bold), Optional.ofNullable(italic), Optional.ofNullable(underlined), Optional.ofNullable(strikethrough),
                    Optional.ofNullable(obfuscated), Optional.empty(), Optional.empty(), Optional.ofNullable(insertion), Optional.ofNullable(font));
        }
        return null;
    }
}
