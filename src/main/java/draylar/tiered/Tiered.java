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
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Equipable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.item.ShieldItem;
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

    public static MenuType<ReforgeScreenHandler> REFORGE_SCREEN_HANDLER_TYPE;

    public static final DataComponentType<TierComponent> TIER = registerComponent("tiered:tier", builder -> builder.persistent(TierComponent.CODEC).networkSynchronized(TierComponent.PACKET_CODEC));

    public static final Logger LOGGER = LogManager.getLogger();

    @Override
    public void onInitialize() {
        ConfigInit.init();
        TieredItemTags.init();
        CustomEntityAttributes.init();
        CommandInit.init();
        ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(Tiered.ATTRIBUTE_DATA_LOADER);
        ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(Tiered.REFORGE_DATA_LOADER);

        REFORGE_SCREEN_HANDLER_TYPE = Registry.register(BuiltInRegistries.MENU, "tiered:reforge",
                new MenuType<>((syncId, inventory) -> new ReforgeScreenHandler(syncId, inventory, ContainerLevelAccess.NULL), FeatureFlags.VANILLA_SET));

        TieredServerPacket.init();

        ServerPlayConnectionEvents.JOIN.register((network, packetSender, minecraftServer) -> {
            TieredServerPacket.writeS2CReforgeItemSyncPacket(network.getPlayer());
            TieredServerPacket.writeS2CAttributePacket(network.getPlayer());
            TieredServerPacket.writeS2CHealthPacket(network.getPlayer());
        });

        ServerLifecycleEvents.END_DATA_PACK_RELOAD.register((server, serverResourceManager, success) -> {
            if (success) {
                for (int i = 0; i < server.getPlayerList().getPlayers().size(); i++) {
                    ModifierUtils.refreshPlayerTiers(server.getPlayerList().getPlayers().get(i));
                    TieredServerPacket.writeS2CReforgeItemSyncPacket(server.getPlayerList().getPlayers().get(i));
                    TieredServerPacket.writeS2CAttributePacket(server.getPlayerList().getPlayers().get(i));
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

    private static <T> DataComponentType<T> registerComponent(String id, UnaryOperator<DataComponentType.Builder<T>> builderOperator) {
        return Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, id, builderOperator.apply(DataComponentType.builder()).build());
    }

    /**
     * Returns an {@link ResourceLocation} namespaced with this mod's modid ("tiered").
     *
     * @param path path of identifier (eg. apple in "minecraft:apple")
     * @return Identifier created with a namespace of this mod's modid ("tiered") and provided path
     */
    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath("tiered", path);
    }

    public static boolean isPreferredEquipmentSlot(ItemStack stack, EquipmentSlot slot) {
        if (stack.getItem() instanceof ShieldItem || stack.getItem() instanceof ProjectileWeaponItem || stack.is(TieredItemTags.MAIN_OFFHAND_ITEM)) {
            return slot == EquipmentSlot.MAINHAND || slot == EquipmentSlot.OFFHAND;
        }
        if (stack.getItem() instanceof Equipable equipment) {
            return equipment.getEquipmentSlot().equals(slot);
        }
        return slot == EquipmentSlot.MAINHAND;
    }

}
