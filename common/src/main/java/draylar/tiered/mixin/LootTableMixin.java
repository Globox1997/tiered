package draylar.tiered.mixin;

import java.util.function.Consumer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.sugar.Local;

import draylar.tiered.api.ModifierUtils;
import draylar.tiered.config.ConfigInit;

@Mixin(LootTable.class)
public class LootTableMixin {

    // lambda injection
    @Inject(method = "lambda$createStackSplitter$0", at = @At(value = "INVOKE", target = "Ljava/util/function/Consumer;accept(Ljava/lang/Object;)V", ordinal = 0))
    private static void processStacksMixin(ServerLevel world, Consumer<ItemStack> lootConsumer, ItemStack itemStack, CallbackInfo info) {
        if (!world.isClientSide() && ConfigInit.CONFIG.lootContainerModifier) {
            ModifierUtils.setItemStackAttribute(null, itemStack, false);
        }
    }

    @Inject(method = "lambda$createStackSplitter$0", at = @At(value = "INVOKE", target = "Ljava/util/function/Consumer;accept(Ljava/lang/Object;)V", ordinal = 1))
    private static void processStacksMixin(ServerLevel world, Consumer<ItemStack> lootConsumer, ItemStack itemStack, CallbackInfo info, @Local(ordinal = 1) ItemStack itemStack2) {
        if (!world.isClientSide() && ConfigInit.CONFIG.lootContainerModifier) {
            ModifierUtils.setItemStackAttribute(null, itemStack2, false);
        }
    }

    @Inject(method = "fill", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/Container;setItem(ILnet/minecraft/world/item/ItemStack;)V", ordinal = 1))
    private void supplyInventoryMixin(Container inventory, LootParams parameters, long seed, CallbackInfo info, @Local LootContext lootContext, @Local ItemStack itemStack) {
        if (!lootContext.getLevel().isClientSide() && ConfigInit.CONFIG.lootContainerModifier) {
            ModifierUtils.setItemStackAttribute(null, itemStack, false);
        }
    }
}
