package draylar.tiered.command;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import draylar.tiered.Tiered;
import draylar.tiered.api.AttributeTemplate;
import draylar.tiered.api.ModifierUtils;
import draylar.tiered.api.TierComponent;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public class CommandInit {

    private static final List<String> TIER_LIST = List.of("common", "uncommon", "rare", "epic", "legendary", "unique");

    public static void init() {
        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated, environment) -> {
            dispatcher.register((Commands.literal("tiered").requires(Commands.hasPermission(Commands.LEVEL_ADMINS))).then(Commands.literal("tier").then(Commands.argument("targets", EntityArgument.players()).then(Commands.literal("common").executes((commandContext) -> {
                return executeCommand(commandContext.getSource(), EntityArgument.getPlayers(commandContext, "targets"), 0);
            })).then(Commands.literal("uncommon").executes((commandContext) -> {
                return executeCommand(commandContext.getSource(), EntityArgument.getPlayers(commandContext, "targets"), 1);
            })).then(Commands.literal("rare").executes((commandContext) -> {
                return executeCommand(commandContext.getSource(), EntityArgument.getPlayers(commandContext, "targets"), 2);
            })).then(Commands.literal("epic").executes((commandContext) -> {
                return executeCommand(commandContext.getSource(), EntityArgument.getPlayers(commandContext, "targets"), 3);
            })).then(Commands.literal("legendary").executes((commandContext) -> {
                return executeCommand(commandContext.getSource(), EntityArgument.getPlayers(commandContext, "targets"), 4);
            })).then(Commands.literal("unique").executes((commandContext) -> {
                return executeCommand(commandContext.getSource(), EntityArgument.getPlayers(commandContext, "targets"), 5);
            })))).then(Commands.literal("untier").then(Commands.argument("targets", EntityArgument.players()).executes((commandContext) -> {
                return executeCommand(commandContext.getSource(), EntityArgument.getPlayers(commandContext, "targets"), -1);
            }))));
        });
    }

    // 0: common; 1: uncommon; 2: rare; 3: epic; 4: legendary; 5: unique
    private static int executeCommand(CommandSourceStack source, Collection<ServerPlayer> targets, int tier) {
        int successCount = 0;
        for (ServerPlayer serverPlayerEntity : targets) {
            ItemStack itemStack = serverPlayerEntity.getMainHandItem();

            if (itemStack.isEmpty()) {
                source.sendFailure(Component.translatable("commands.tiered.failed", serverPlayerEntity.getDisplayName()));
                continue;
            }

            if (tier == -1) {
                if (itemStack.get(Tiered.TIER) != null) {
                    ModifierUtils.removeItemStackAttribute(itemStack);
                    successCount++;

                    source.sendSuccess(() -> Component.translatable("commands.tiered.untier", itemStack.getItem().getName(itemStack), serverPlayerEntity.getDisplayName()), true);
                } else {
                    source.sendFailure(Component.translatable("commands.tiered.untier_failed", itemStack.getItem().getName(itemStack), serverPlayerEntity.getDisplayName()));
                }
            } else {
                ArrayList<Identifier> potentialAttributes = new ArrayList<Identifier>();
                Tiered.ATTRIBUTE_DATA_LOADER.getItemAttributes().forEach((id, attribute) -> {
                    if (attribute.isValid(BuiltInRegistries.ITEM.getKey(itemStack.getItem()))) {
                        potentialAttributes.add(Identifier.parse(attribute.getID()));
                    }
                });
                if (potentialAttributes.size() <= 0) {
                    source.sendFailure(Component.translatable("commands.tiered.tiering_failed", itemStack.getItem().getName(itemStack), serverPlayerEntity.getDisplayName()));
                    continue;
                } else {

                    List<Identifier> potentialTier = new ArrayList<Identifier>();
                    for (Identifier potentialAttribute : potentialAttributes) {
                        if (potentialAttribute.getPath().contains(TIER_LIST.get(tier))) {
                            if (TIER_LIST.get(tier).equals("common") && potentialAttribute.getPath().contains("uncommon")) {
                                continue;
                            }
                            potentialTier.add(potentialAttribute);
                        }
                    }

                    if (potentialTier.size() <= 0) {
                        source.sendFailure(Component.translatable("commands.tiered.tiering_failed", itemStack.getItem().getName(itemStack), serverPlayerEntity.getDisplayName()));
                        continue;
                    } else {

                        ModifierUtils.removeItemStackAttribute(itemStack);

                        Identifier attribute = potentialTier.get(serverPlayerEntity.level().getRandom().nextInt(potentialTier.size()));
                        if (attribute != null) {
                            // add durability nbt
                            float durableFactor = -1f;
                            int operation = 0;
                            List<AttributeTemplate> attributeList = Tiered.ATTRIBUTE_DATA_LOADER.getItemAttributes().get(Identifier.parse(attribute.toString())).getAttributes();
                            for (AttributeTemplate attributeTemplate : attributeList) {
                                if (attributeTemplate.getAttributeTypeID().equals("tiered:generic.durable")) {
                                    durableFactor = (float) Math.round(attributeTemplate.getEntityAttributeModifier().amount() * 100.0f) / 100.0f;
                                    operation = attributeTemplate.getEntityAttributeModifier().operation().id();
                                    break;
                                }
                            }
                            itemStack.set(Tiered.TIER, new TierComponent(attribute.toString(), durableFactor, operation));
                            successCount++;

                            source.sendSuccess(() -> Component.translatable("commands.tiered.tier", itemStack.getItem().getName(itemStack), serverPlayerEntity.getDisplayName()), true);
                        }
                    }
                }
            }
        }
        return successCount;
    }
}
