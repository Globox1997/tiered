package draylar.tiered.gson;

import com.google.gson.*;
import java.lang.reflect.Type;
import net.minecraft.world.entity.EquipmentSlot;

public class EquipmentSlotSerializer implements JsonSerializer<EquipmentSlot> {
	@Override
	public JsonElement serialize(EquipmentSlot src, Type typeOfSrc, JsonSerializationContext context) {
		return new JsonPrimitive(src.name());
	}
}
