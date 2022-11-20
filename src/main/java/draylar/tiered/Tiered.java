package draylar.tiered;

import draylar.tiered.api.AttributeTemplate;
import draylar.tiered.api.CustomEntityAttributes;
import draylar.tiered.api.ModifierUtils;
import draylar.tiered.api.TieredItemTags;
import draylar.tiered.config.ConfigInit;
import draylar.tiered.api.PotentialAttribute;
import draylar.tiered.data.AttributeDataLoader;
import draylar.tiered.network.TieredServerPacket;
import draylar.tiered.reforge.ReforgeScreenHandler;
import io.netty.buffer.Unpooled;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.item.v1.ModifyItemAttributeModifiersCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ShieldItem;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.resource.ResourceType;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;

@SuppressWarnings("unused")
public class Tiered implements ModInitializer {

    public static final boolean isLevelZLoaded = FabricLoader.getInstance().isModLoaded("levelz");

    /**
     * Attribute Data Loader instance which handles loading attribute .json files from "data/modid/item_attributes".
     * <p>
     * This field is registered to the server's data manager in {@link ServerResourceManagerMixin}
     */
    public static final AttributeDataLoader ATTRIBUTE_DATA_LOADER = new AttributeDataLoader();

    public static ScreenHandlerType<ReforgeScreenHandler> REFORGE_SCREEN_HANDLER_TYPE;

    // Same UUIDs as in ArmorItem
    // public static final UUID[] MODIFIERS = new UUID[] { UUID.fromString("845DB27C-C624-495F-8C9F-6020A9A58B6B"), UUID.fromString("D8499B04-0E66-4726-AB29-64469D734E0D"),
    // UUID.fromString("9F3D476D-C118-4544-8365-64846904B48E"), UUID.fromString("2AD3F246-FEE1-4E67-B886-69FD380BB150"), UUID.fromString("4a88bc27-9563-4eeb-96d5-fe50917cc24f"),
    // UUID.fromString("fee48d8c-1b51-4c46-9f4b-c58162623a7a") };

    public static final UUID[] MODIFIERS = new UUID[] { UUID.fromString("baf8e074-f7f9-4549-ba1f-e21f82684b8c"), UUID.fromString("9b3416de-98d1-407f-bc6b-e673c2ab5252"),
            UUID.fromString("1e3ceca6-aa30-4165-9715-20bb63c11348"), UUID.fromString("c99bfa17-4886-4cbb-86c2-ebf9369616d5"), UUID.fromString("19e4dc8d-3892-4ffe-a558-f96c68491144"),
            UUID.fromString("b1641cff-84ed-4b63-85f8-2634005adc9b"), UUID.fromString("92f546e9-0d00-4159-8c8f-0499e49f5811"), UUID.fromString("e25c7fa8-13b0-4ea0-8db7-e26b78f36c90"),
            UUID.fromString("2f9dcfce-bd03-4181-86b7-91c88f71e67c") };

    public static final Logger LOGGER = LogManager.getLogger();

    public static final Identifier ATTRIBUTE_SYNC_PACKET = new Identifier("attribute_sync");
    public static final String NBT_SUBTAG_KEY = "Tiered";
    public static final String NBT_SUBTAG_DATA_KEY = "Tier";

    @Override
    public void onInitialize() {
        ConfigInit.init();
        TieredItemTags.init();
        CustomEntityAttributes.init();
        registerAttributeSyncer();
        ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(Tiered.ATTRIBUTE_DATA_LOADER);

        REFORGE_SCREEN_HANDLER_TYPE = Registry.register(Registry.SCREEN_HANDLER, "tiered",
                new ScreenHandlerType<>((syncId, inventory) -> new ReforgeScreenHandler(syncId, inventory, ScreenHandlerContext.EMPTY)));

        TieredServerPacket.init();
        // if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
        // // setupModifierLabel();
        // }
        ServerLifecycleEvents.END_DATA_PACK_RELOAD.register((server, serverResourceManager, success) -> {
            if (success) {
                for (int i = 0; i < server.getPlayerManager().getPlayerList().size(); i++)
                    updateItemStackNbt(server.getPlayerManager().getPlayerList().get(i).getInventory());
                LOGGER.info("Finished reload on {}", Thread.currentThread());
            } else
                LOGGER.error("Failed to reload on {}", Thread.currentThread());
        });
        ServerPlayConnectionEvents.INIT.register((handler, server) -> {
            updateItemStackNbt(handler.player.getInventory());
        });
        ModifyItemAttributeModifiersCallback.EVENT.register((itemStack, slot, modifiers) -> {
            if (itemStack.getSubNbt(Tiered.NBT_SUBTAG_KEY) != null) {
                Identifier tier = new Identifier(itemStack.getOrCreateSubNbt(Tiered.NBT_SUBTAG_KEY).getString(Tiered.NBT_SUBTAG_DATA_KEY));

                if (!itemStack.hasNbt() || !itemStack.getNbt().contains("AttributeModifiers", 9)) {
                    PotentialAttribute potentialAttribute = Tiered.ATTRIBUTE_DATA_LOADER.getItemAttributes().get(tier);
                    if (potentialAttribute != null) {
                        PotentialAttribute.Template template = potentialAttribute.getTemplate(Registry.ITEM.getId(itemStack.getItem()));
                        template.getAttributes().forEach(attributeTemplate -> {
                            // get required equipment slots
                            if (attributeTemplate.getRequiredEquipmentSlots() != null) {
                                List<EquipmentSlot> requiredEquipmentSlots = new ArrayList<>(Arrays.asList(attributeTemplate.getRequiredEquipmentSlots()));

                                if (requiredEquipmentSlots.contains(slot))
                                    attributeTemplate.realize(modifiers, slot);
                            }

                            // get optional equipment slots
                            if (attributeTemplate.getOptionalEquipmentSlots() != null) {
                                List<EquipmentSlot> optionalEquipmentSlots = new ArrayList<>(Arrays.asList(attributeTemplate.getOptionalEquipmentSlots()));

                                // optional equipment slots are valid ONLY IF the equipment slot is valid for the thing
                                if (optionalEquipmentSlots.contains(slot) && Tiered.isPreferredEquipmentSlot(itemStack, slot))
                                    attributeTemplate.realize(modifiers, slot);
                            }
                        });
                    }
                }
            }
        });
    }

    /**
     * Returns an {@link Identifier} namespaced with this mod's modid ("tiered").
     *
     * @param path path of identifier (eg. apple in "minecraft:apple")
     * @return Identifier created with a namespace of this mod's modid ("tiered") and provided path
     */
    public static Identifier id(String path) {
        return new Identifier("tiered", path);
    }

    /**
     * Creates an {@link ItemTooltipCallback} listener that adds the modifier name at the top of an Item tooltip.
     * <p>
     * A tool name is only displayed if the item has a modifier.
     */
    private void setupModifierLabel() {
        ItemTooltipCallback.EVENT.register((stack, tooltipContext, lines) -> {
            // has tier
            if (stack.getSubNbt(NBT_SUBTAG_KEY) != null) {
                // get tier
                Identifier tier = new Identifier(stack.getOrCreateSubNbt(NBT_SUBTAG_KEY).getString(Tiered.NBT_SUBTAG_DATA_KEY));

                // attempt to display attribute if it is valid
                PotentialAttribute potentialAttribute = Tiered.ATTRIBUTE_DATA_LOADER.getItemAttributes().get(tier);

                if (potentialAttribute != null)
                    lines.add(1, Text.translatable(potentialAttribute.getID() + ".label").setStyle(potentialAttribute.getStyle()));
            }
        });
    }

    public static boolean isPreferredEquipmentSlot(ItemStack stack, EquipmentSlot slot) {
        if (stack.getItem() instanceof ArmorItem) {
            ArmorItem item = (ArmorItem) stack.getItem();
            return item.getSlotType().equals(slot);
        }
        if (stack.getItem() instanceof ShieldItem)
            return slot == EquipmentSlot.MAINHAND || slot == EquipmentSlot.OFFHAND;

        return slot == EquipmentSlot.MAINHAND;
    }

    public static void registerAttributeSyncer() {
        ServerPlayConnectionEvents.JOIN.register((network, packetSender, minecraftServer) -> {
            PacketByteBuf packet = new PacketByteBuf(Unpooled.buffer());

            // serialize each attribute file as a string to the packet
            packet.writeInt(ATTRIBUTE_DATA_LOADER.getItemAttributes().size());

            // write each value
            ATTRIBUTE_DATA_LOADER.getItemAttributes().forEach((id, attribute) -> {
                packet.writeString(id.toString());
                packet.writeString(AttributeDataLoader.GSON.toJson(attribute));
            });

            // send packet with attributes to client
            packetSender.sendPacket(ATTRIBUTE_SYNC_PACKET, packet);
        });
    }

    public static void updateItemStackNbt(PlayerInventory playerInventory) {
        for (int u = 0; u < playerInventory.size(); u++) {
            ItemStack itemStack = playerInventory.getStack(u);
            if (!itemStack.isEmpty() && itemStack.getSubNbt(Tiered.NBT_SUBTAG_KEY) != null) {

                // Check if attribute exists
                List<String> attributeIds = new ArrayList<>();
                Tiered.ATTRIBUTE_DATA_LOADER.getItemAttributes().forEach((id, attribute) -> {
                    if (attribute.isValid(Registry.ITEM.getId(itemStack.getItem())))
                        attributeIds.add(attribute.getID());

                });
                Identifier attributeID = null;
                for (int i = 0; i < attributeIds.size(); i++) {
                    if (itemStack.getSubNbt(Tiered.NBT_SUBTAG_KEY).asString().contains(attributeIds.get(i))) {
                        attributeID = new Identifier(attributeIds.get(i));
                        break;
                    } else if (i == attributeIds.size() - 1) {
                        ModifierUtils.removeItemStackAttribute(itemStack);
                        attributeID = ModifierUtils.getRandomAttributeIDFor(null, itemStack.getItem(), false);
                    }
                }

                // found an ID
                if (attributeID != null) {
                    PotentialAttribute.Template template = Tiered.ATTRIBUTE_DATA_LOADER.getItemAttributes().get(new Identifier(attributeID.toString())).getTemplate(Registry.ITEM.getId(itemStack.getItem()));

                    HashMap<String, Object> nbtMap = template.getNbtValues();
                    // update durability nbt

                    List<AttributeTemplate> attributeList = template.getAttributes();
                    for (int i = 0; i < attributeList.size(); i++)
                        if (attributeList.get(i).getAttributeTypeID().equals("tiered:generic.durable")) {
                            if (nbtMap == null)
                                nbtMap = new HashMap<String, Object>();
                            nbtMap.put("durable", (double) Math.round(attributeList.get(i).getEntityAttributeModifier().getValue() * 100.0) / 100.0);
                            break;
                        }

                    // add nbtMap
                    if (nbtMap != null) {
                        NbtCompound nbtCompound = itemStack.getNbt();
                        for (HashMap.Entry<String, Object> entry : nbtMap.entrySet()) {
                            String key = entry.getKey();
                            Object value = entry.getValue();

                            // json list will get read as ArrayList class
                            // json map will get read as linkedtreemap
                            // json integer is read by gson -> always double
                            if (value instanceof String)
                                nbtCompound.putString(key, (String) value);
                            else if (value instanceof Boolean)
                                nbtCompound.putBoolean(key, (boolean) value);
                            else if (value instanceof Double) {
                                if ((double) value % 1.0 < 0.0001D)
                                    nbtCompound.putInt(key, (int) Math.round((double) value));
                                else
                                    nbtCompound.putDouble(key, Math.round((double) value * 100.0) / 100.0);
                            }
                        }
                        itemStack.setNbt(nbtCompound);
                    }
                    if (itemStack.getSubNbt(Tiered.NBT_SUBTAG_KEY) == null)
                        itemStack.getOrCreateSubNbt(Tiered.NBT_SUBTAG_KEY).putString(Tiered.NBT_SUBTAG_DATA_KEY, attributeID.toString());

                    playerInventory.setStack(u, itemStack);
                }
            }
        }
    }
}
