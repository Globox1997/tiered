package draylar.tiered;

import draylar.tiered.api.*;
import draylar.tiered.config.ConfigInit;
import draylar.tiered.data.AttributeDataLoader;
import draylar.tiered.data.ReforgeDataLoader;
import draylar.tiered.network.TieredServerPacket;
import draylar.tiered.platform.Services;
import draylar.tiered.reforge.ReforgeScreenHandler;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.item.ShieldItem;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@SuppressWarnings("unused")
public class Tiered {

    public static final String MOD_ID = "tiered";

    public static final boolean isLevelZLoaded = Services.PLATFORM.isModLoaded("levelz");

    /**
     * Attribute Data Loader instance which handles loading attribute .json files from "data/modid/item_attributes".
     */
    public static final AttributeDataLoader ATTRIBUTE_DATA_LOADER = new AttributeDataLoader();
    public static final Identifier ATTRIBUTE_DATA_LOADER_ID = id("item_attributes");

    /**
     * data/tiered/reforge_item
     */
    public static final ReforgeDataLoader REFORGE_DATA_LOADER = new ReforgeDataLoader();
    public static final Identifier REFORGE_DATA_LOADER_ID = id("reforge_loader");

    public static final MenuType<ReforgeScreenHandler> REFORGE_SCREEN_HANDLER_TYPE = Services.REGISTRY
            .createMenuType((syncId, inventory) -> new ReforgeScreenHandler(syncId, inventory, ContainerLevelAccess.NULL));

    public static final DataComponentType<TierComponent> TIER = DataComponentType.<TierComponent>builder().persistent(TierComponent.CODEC).networkSynchronized(TierComponent.PACKET_CODEC).build();

    public static final Logger LOGGER = LogManager.getLogger();

    public static void init() {
        ConfigInit.init();
        TieredItemTags.init();
        CustomEntityAttributes.init();
        Services.REGISTRY.register(Registries.DATA_COMPONENT_TYPE, id("tier"), () -> TIER);
        Services.REGISTRY.register(Registries.MENU, id("reforge"), () -> REFORGE_SCREEN_HANDLER_TYPE);
    }

    public static void onPlayerInit(ServerPlayer player) {
        ModifierUtils.updateItemStackComponent(player.getInventory());
    }

    public static void onPlayerJoin(ServerPlayer player) {
        TieredServerPacket.writeS2CReforgeItemSyncPacket(player);
        TieredServerPacket.writeS2CAttributePacket(player);
        TieredServerPacket.writeS2CHealthPacket(player);
    }

    public static void onDataPackReload(MinecraftServer server, boolean success) {
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
    }

    /**
     * Returns an {@link Identifier} namespaced with this mod's modid ("tiered").
     *
     * @param path path of identifier (eg. apple in "minecraft:apple")
     * @return Identifier created with a namespace of this mod's modid ("tiered") and provided path
     */
    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }

    public static boolean isPreferredEquipmentSlot(ItemStack stack, EquipmentSlot slot) {
        if (stack.getItem() instanceof ShieldItem || stack.getItem() instanceof ProjectileWeaponItem || stack.is(TieredItemTags.MAIN_OFFHAND_ITEM)) {
            return slot == EquipmentSlot.MAINHAND || slot == EquipmentSlot.OFFHAND;
        }
        Equippable equippable = stack.get(DataComponents.EQUIPPABLE);
        if (equippable != null) {
            return equippable.slot().equals(slot);
        }
        return slot == EquipmentSlot.MAINHAND;
    }

}
