package draylar.tiered;

import draylar.tiered.api.*;
import draylar.tiered.command.CommandInit;
import draylar.tiered.config.ConfigInit;
import draylar.tiered.data.AttributeDataLoader;
import draylar.tiered.data.ReforgeDataLoader;
import draylar.tiered.network.TieredServerPacket;
import draylar.tiered.reforge.ReforgeScreenHandler;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.component.ComponentType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.Equipment;
import net.minecraft.item.ItemStack;
import net.minecraft.item.RangedWeaponItem;
import net.minecraft.item.ShieldItem;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.resource.ResourceType;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.function.UnaryOperator;

@SuppressWarnings("unused")
public class Tiered implements ModInitializer {

    public static final boolean isLevelZLoaded = FabricLoader.getInstance().isModLoaded("levelz");

    /**
     * Attribute Data Loader instance which handles loading attribute .json files from "data/modid/item_attributes".
     */
    public static final AttributeDataLoader ATTRIBUTE_DATA_LOADER = new AttributeDataLoader();

    /**
     * data/tiered/reforge_item
     */
    public static final ReforgeDataLoader REFORGE_DATA_LOADER = new ReforgeDataLoader();

    public static ScreenHandlerType<ReforgeScreenHandler> REFORGE_SCREEN_HANDLER_TYPE;

    public static final ComponentType<TierComponent> TIER = registerComponent("tiered:tier", builder -> builder.codec(TierComponent.CODEC).packetCodec(TierComponent.PACKET_CODEC));

    public static final Logger LOGGER = LogManager.getLogger();

    @Override
    public void onInitialize() {
        ConfigInit.init();
        TieredItemTags.init();
        CustomEntityAttributes.init();
        CommandInit.init();
        ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(Tiered.ATTRIBUTE_DATA_LOADER);
        ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(Tiered.REFORGE_DATA_LOADER);

        REFORGE_SCREEN_HANDLER_TYPE = Registry.register(Registries.SCREEN_HANDLER, "tiered:reforge",
                new ScreenHandlerType<>((syncId, inventory) -> new ReforgeScreenHandler(syncId, inventory, ScreenHandlerContext.EMPTY), FeatureFlags.VANILLA_FEATURES));

        TieredServerPacket.init();

        ServerPlayConnectionEvents.JOIN.register((network, packetSender, minecraftServer) -> {
            TieredServerPacket.writeS2CReforgeItemSyncPacket(network.getPlayer());
            TieredServerPacket.writeS2CAttributePacket(network.getPlayer());
        });

        ServerLifecycleEvents.END_DATA_PACK_RELOAD.register((server, serverResourceManager, success) -> {
            if (success) {
                for (int i = 0; i < server.getPlayerManager().getPlayerList().size(); i++) {
                    ModifierUtils.updateItemStackComponent(server.getPlayerManager().getPlayerList().get(i).getInventory());
                }
                LOGGER.info("Finished reload on {}", Thread.currentThread());
            } else {
                LOGGER.error("Failed to reload on {}", Thread.currentThread());
            }
        });
        ServerPlayConnectionEvents.INIT.register((handler, server) -> {
            ModifierUtils.updateItemStackComponent(handler.player.getInventory());
        });

    }

    private static <T> ComponentType<T> registerComponent(String id, UnaryOperator<ComponentType.Builder<T>> builderOperator) {
        return Registry.register(Registries.DATA_COMPONENT_TYPE, id, builderOperator.apply(ComponentType.builder()).build());
    }

    /**
     * Returns an {@link Identifier} namespaced with this mod's modid ("tiered").
     *
     * @param path path of identifier (eg. apple in "minecraft:apple")
     * @return Identifier created with a namespace of this mod's modid ("tiered") and provided path
     */
    public static Identifier id(String path) {
        return Identifier.of("tiered", path);
    }

    public static boolean isPreferredEquipmentSlot(ItemStack stack, EquipmentSlot slot) {
        if (stack.getItem() instanceof Equipment equipment) {
            return equipment.getSlotType().equals(slot);
        }
        if (stack.getItem() instanceof ShieldItem || stack.getItem() instanceof RangedWeaponItem || stack.isIn(TieredItemTags.MAIN_OFFHAND_ITEM)) {
            return slot == EquipmentSlot.MAINHAND || slot == EquipmentSlot.OFFHAND;
        }
        return slot == EquipmentSlot.MAINHAND;
    }

}
