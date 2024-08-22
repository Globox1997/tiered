package draylar.tiered.command;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import draylar.tiered.Tiered;
import draylar.tiered.api.AttributeTemplate;
import draylar.tiered.api.ModifierUtils;
import draylar.tiered.api.TierComponent;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class CommandInit {

    private static final List<String> TIER_LIST = List.of("common", "uncommon", "rare", "epic", "legendary", "unique");

    public static void init() {
        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated, environment) -> {
            dispatcher.register((CommandManager.literal("tiered").requires((serverCommandSource) -> {
                return serverCommandSource.hasPermissionLevel(3);
            })).then(CommandManager.literal("tier").then(CommandManager.argument("targets", EntityArgumentType.players()).then(CommandManager.literal("common").executes((commandContext) -> {
                return executeCommand(commandContext.getSource(), EntityArgumentType.getPlayers(commandContext, "targets"), 0);
            })).then(CommandManager.literal("uncommon").executes((commandContext) -> {
                return executeCommand(commandContext.getSource(), EntityArgumentType.getPlayers(commandContext, "targets"), 1);
            })).then(CommandManager.literal("rare").executes((commandContext) -> {
                return executeCommand(commandContext.getSource(), EntityArgumentType.getPlayers(commandContext, "targets"), 2);
            })).then(CommandManager.literal("epic").executes((commandContext) -> {
                return executeCommand(commandContext.getSource(), EntityArgumentType.getPlayers(commandContext, "targets"), 3);
            })).then(CommandManager.literal("legendary").executes((commandContext) -> {
                return executeCommand(commandContext.getSource(), EntityArgumentType.getPlayers(commandContext, "targets"), 4);
            })).then(CommandManager.literal("unique").executes((commandContext) -> {
                return executeCommand(commandContext.getSource(), EntityArgumentType.getPlayers(commandContext, "targets"), 5);
            })))).then(CommandManager.literal("untier").then(CommandManager.argument("targets", EntityArgumentType.players()).executes((commandContext) -> {
                return executeCommand(commandContext.getSource(), EntityArgumentType.getPlayers(commandContext, "targets"), -1);
            }))));
        });
    }

    // 0: common; 1: uncommon; 2: rare; 3: epic; 4: legendary; 5: unique
    private static int executeCommand(ServerCommandSource source, Collection<ServerPlayerEntity> targets, int tier) {
        // loop over players
        for (ServerPlayerEntity serverPlayerEntity : targets) {
            ItemStack itemStack = serverPlayerEntity.getMainHandStack();

            if (itemStack.isEmpty()) {
                source.sendFeedback(() -> Text.translatable("commands.tiered.failed", serverPlayerEntity.getDisplayName()), true);
                continue;
            }

            if (tier == -1) {
                if (itemStack.get(Tiered.TIER) != null) {
                    ModifierUtils.removeItemStackAttribute(itemStack);

                    source.sendFeedback(() -> Text.translatable("commands.tiered.untier", itemStack.getItem().getName(itemStack).getString(), serverPlayerEntity.getDisplayName()), true);
                } else {
                    source.sendFeedback(() -> Text.translatable("commands.tiered.untier_failed", itemStack.getItem().getName(itemStack).getString(), serverPlayerEntity.getDisplayName()), true);
                }
            } else {
                ArrayList<Identifier> potentialAttributes = new ArrayList<Identifier>();
                Tiered.ATTRIBUTE_DATA_LOADER.getItemAttributes().forEach((id, attribute) -> {
                    if (attribute.isValid(Registries.ITEM.getId(itemStack.getItem()))) {
                        potentialAttributes.add(Identifier.of(attribute.getID()));
                    }
                });
                if (potentialAttributes.size() <= 0) {
                    source.sendFeedback(() -> Text.translatable("commands.tiered.tiering_failed", itemStack.getItem().getName(itemStack).getString(), serverPlayerEntity.getDisplayName()), true);
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
                        source.sendFeedback(() -> Text.translatable("commands.tiered.tiering_failed", itemStack.getItem().getName(itemStack).getString(), serverPlayerEntity.getDisplayName()), true);
                        continue;
                    } else {

                        ModifierUtils.removeItemStackAttribute(itemStack);

                        Identifier attribute = potentialTier.get(serverPlayerEntity.getWorld().getRandom().nextInt(potentialTier.size()));
                        if (attribute != null) {
                            // add durability nbt
                            float durableFactor = -1f;
                            int operation = 0;
                            List<AttributeTemplate> attributeList = Tiered.ATTRIBUTE_DATA_LOADER.getItemAttributes().get(Identifier.of(attribute.toString())).getAttributes();
                            for (AttributeTemplate attributeTemplate : attributeList) {
                                if (attributeTemplate.getAttributeTypeID().equals("tiered:generic.durable")) {
                                    durableFactor = (float) Math.round(attributeTemplate.getEntityAttributeModifier().value() * 100.0f) / 100.0f;
                                    operation = attributeTemplate.getEntityAttributeModifier().operation().getId();
                                    break;
                                }
                            }
                            itemStack.set(Tiered.TIER, new TierComponent(attribute.toString(), durableFactor, operation));

                            source.sendFeedback(() -> Text.translatable("commands.tiered.tier", itemStack.getItem().getName(itemStack).getString(), serverPlayerEntity.getDisplayName()), true);
                        }
                    }
                }
            }
        }
        return 1;
    }
}
