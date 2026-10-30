package draylar.tiered.mixin;

import java.util.List;
import java.util.function.Consumer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import draylar.tiered.api.ModifierUtils;
import draylar.tiered.config.ConfigInit;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectListIterator;

@Mixin(LootTable.class)
public class LootTableMixin {

    // lambda injection
    @Inject(method = "method_331", at = @At(value = "INVOKE", target = "Ljava/util/function/Consumer;accept(Ljava/lang/Object;)V", ordinal = 0))
    private static void processStacksMixin(ServerLevel world, Consumer<ItemStack> lootConsumer, ItemStack itemStack, CallbackInfo info) {
        if (!world.isClientSide() && ConfigInit.CONFIG.lootContainerModifier) {
            ModifierUtils.setItemStackAttribute(null, itemStack, false);
        }
    }

    @Inject(method = "method_331", at = @At(value = "INVOKE", target = "Ljava/util/function/Consumer;accept(Ljava/lang/Object;)V", ordinal = 1), locals = LocalCapture.CAPTURE_FAILSOFT)
    private static void processStacksMixin(ServerLevel world, Consumer<ItemStack> lootConsumer, ItemStack itemStack, CallbackInfo info, int i, ItemStack itemStack2) {
        if (!world.isClientSide() && ConfigInit.CONFIG.lootContainerModifier) {
            ModifierUtils.setItemStackAttribute(null, itemStack2, false);
        }
    }

    @Inject(method = "fill", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/Container;setItem(ILnet/minecraft/world/item/ItemStack;)V", ordinal = 1), locals = LocalCapture.CAPTURE_FAILSOFT)
    private void supplyInventoryMixin(Container inventory, LootParams parameters, long seed, CallbackInfo info, LootContext lootContext, ObjectArrayList<ItemStack> objectArrayList,
            RandomSource random, List<Integer> list, ObjectListIterator<ItemStack> var9, ItemStack itemStack) {
        if (!lootContext.getLevel().isClientSide() && ConfigInit.CONFIG.lootContainerModifier) {
            ModifierUtils.setItemStackAttribute(null, itemStack, false);
        }
    }
}
