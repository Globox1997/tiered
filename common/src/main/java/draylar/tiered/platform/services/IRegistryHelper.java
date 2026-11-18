package draylar.tiered.platform.services;

import java.util.function.BiFunction;
import java.util.function.Supplier;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;

public interface IRegistryHelper {

    <T> Holder<T> register(ResourceKey<? extends Registry<T>> registryKey, Identifier id, Supplier<T> supplier);

    <T extends AbstractContainerMenu> MenuType<T> createMenuType(BiFunction<Integer, Inventory, T> factory);

}
