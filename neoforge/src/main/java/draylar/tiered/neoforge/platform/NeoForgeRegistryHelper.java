package draylar.tiered.neoforge.platform;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Supplier;

import draylar.tiered.platform.services.IRegistryHelper;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredRegister;

public class NeoForgeRegistryHelper implements IRegistryHelper {

    private static final Map<ResourceKey<?>, DeferredRegister<?>> REGISTERS = new LinkedHashMap<>();

    @Override
    @SuppressWarnings("unchecked")
    public <T> Holder<T> register(ResourceKey<? extends Registry<T>> registryKey, Identifier id, Supplier<T> supplier) {
        DeferredRegister<T> register = (DeferredRegister<T>) REGISTERS.computeIfAbsent(registryKey, key -> DeferredRegister.create(registryKey, id.getNamespace()));
        return register.register(id.getPath(), supplier);
    }

    @Override
    public <T extends AbstractContainerMenu> MenuType<T> createMenuType(BiFunction<Integer, Inventory, T> factory) {
        return IMenuTypeExtension.create((windowId, inventory, data) -> factory.apply(windowId, inventory));
    }

    public static void registerAll(IEventBus modBus) {
        REGISTERS.values().forEach(register -> register.register(modBus));
    }

}
