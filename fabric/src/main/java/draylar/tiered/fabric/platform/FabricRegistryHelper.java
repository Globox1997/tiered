package draylar.tiered.fabric.platform;

import java.util.function.BiFunction;
import java.util.function.Supplier;

import draylar.tiered.platform.services.IRegistryHelper;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;

public class FabricRegistryHelper implements IRegistryHelper {

    @Override
    @SuppressWarnings("unchecked")
    public <T> Holder<T> register(ResourceKey<? extends Registry<T>> registryKey, Identifier id, Supplier<T> supplier) {
        Registry<T> registry = (Registry<T>) BuiltInRegistries.REGISTRY.getValue(registryKey.identifier());
        return Registry.registerForHolder(registry, id, supplier.get());
    }

    @Override
    public <T extends AbstractContainerMenu> MenuType<T> createMenuType(BiFunction<Integer, Inventory, T> factory) {
        return new MenuType<>(factory::apply, FeatureFlags.VANILLA_SET);
    }

}
