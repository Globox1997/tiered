package draylar.tiered.gson;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Locale;
import net.minecraft.ChatFormatting;

public class FormattingDeserializer implements JsonDeserializer<ChatFormatting> {

    @Override
    public ChatFormatting deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        return Arrays.stream(ChatFormatting.values()).filter(formatting -> formatting.name().equals(json.getAsString().toUpperCase(Locale.ROOT))).findFirst().orElse(null);
    }
}
