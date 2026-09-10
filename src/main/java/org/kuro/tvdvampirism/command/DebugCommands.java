package org.kuro.tvdvampirism.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import de.teamlapen.vampirism.core.ModAttributes;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import org.kuro.tvdvampirism.Tvdvampirism;
import org.kuro.tvdvampirism.attribute.SpeciesAttributeManager;
import org.kuro.tvdvampirism.attribute.SpeciesAttributes;
import org.kuro.tvdvampirism.blood.BloodData;
import org.kuro.tvdvampirism.blood.BloodManager;
import org.kuro.tvdvampirism.player.PlayerData;
import org.kuro.tvdvampirism.player.Species;
import org.kuro.tvdvampirism.player.SpeciesManager;
import org.kuro.tvdvampirism.player.SpeciesTransitionManager;

@EventBusSubscriber(modid = Tvdvampirism.MODID)
public final class DebugCommands {

    private DebugCommands() {
    }

    @SubscribeEvent
    public static void onRegisterCommands(
            RegisterCommandsEvent event
    ) {
        register(event.getDispatcher());
    }

    private static void register(
            CommandDispatcher<CommandSourceStack> dispatcher
    ) {

        dispatcher.register(
                Commands.literal("tvd")

                        /*
                         * Operator/cheats only.
                         */
                        .requires(source ->
                                source.hasPermission(2)
                        )
                        .then(Commands.literal("white_oak_test")
                                .executes(context -> {
                                    var player = context.getSource().getPlayerOrException();
                                    boolean killed = org.kuro.tvdvampirism.item.WhiteOakStake.finish(player, player,
                                            new net.minecraft.world.item.ItemStack(org.kuro.tvdvampirism.registry.WhiteOakContent.WHITE_OAK_STAKE.get()));
                                    if (!killed) context.getSource().sendFailure(Component.literal("White Oak requires a living, downed Original Vampire or Original Hybrid."));
                                    return killed ? 1 : 0;
                                })
                                .then(Commands.argument("player", EntityArgument.player())
                                        .executes(context -> {
                                            var player = EntityArgument.getPlayer(context, "player");
                                            boolean killed = org.kuro.tvdvampirism.item.WhiteOakStake.finish(player, player,
                                                    new net.minecraft.world.item.ItemStack(org.kuro.tvdvampirism.registry.WhiteOakContent.WHITE_OAK_STAKE.get()));
                                            if (!killed) context.getSource().sendFailure(Component.literal("White Oak requires a living, downed Original Vampire or Original Hybrid."));
                                            return killed ? 1 : 0;
                                        })))

                        .then(
                                Commands.literal("species")

                                        .then(
                                                Commands.literal("info")
                                                        .executes(context ->
                                                                showInfo(
                                                                        context.getSource()
                                                                )
                                                        )
                                        )

                                        .then(
                                                Commands.argument(
                                                                "species",
                                                                StringArgumentType.word()
                                                        )
                                                        .suggests(
                                                                (context, builder) -> {

                                                                    builder.suggest("normal");
                                                                    builder.suggest("augustine");
                                                                    builder.suggest("hybrid");
                                                                    builder.suggest("original");
                                                                    builder.suggest("original_hybrid");
                                                                    builder.suggest("none");

                                                                    return builder.buildFuture();
                                                                }
                                                        )
                                                        .executes(context ->
                                                                setSpecies(
                                                                        context.getSource(),
                                                                        StringArgumentType.getString(
                                                                                context,
                                                                                "species"
                                                                        )
                                                                )
                                                        )
                                        )
                        )

                        .then(
                                Commands.literal("bite")

                                        .then(
                                                Commands.literal("start")
                                                        .executes(context ->
                                                                startBite(
                                                                        context.getSource()
                                                                )
                                                        )
                                        )

                                        .then(
                                                Commands.literal("cure")
                                                        .executes(context ->
                                                                cureBite(
                                                                        context.getSource()
                                                                )
                                                        )
                                        )
                        )

                        .then(
                                Commands.literal("blood")
                                        .then(Commands.literal("info")
                                                .executes(context -> showBlood(context.getSource())))
                                        .then(Commands.literal("set")
                                                .then(Commands.argument("amount", IntegerArgumentType.integer(0))
                                                        .executes(context -> setBlood(
                                                                context.getSource(),
                                                                IntegerArgumentType.getInteger(context, "amount")
                                                        ))))
                                        .then(Commands.literal("drain")
                                                .then(Commands.argument("amount", IntegerArgumentType.integer(0))
                                                        .executes(context -> drainBlood(
                                                                context.getSource(),
                                                                IntegerArgumentType.getInteger(context, "amount")
                                                        ))))
                                        .then(Commands.literal("refill")
                                                .executes(context -> refillBlood(context.getSource())))
                        )
                        .then(Commands.literal("mastery")
                                .then(Commands.argument("level", IntegerArgumentType.integer(0, 5))
                                        .executes(context -> setMastery(
                                                context.getSource(),
                                                IntegerArgumentType.getInteger(context, "level")
                                        ))))
                        .then(Commands.literal("attributes")
                                .then(Commands.literal("info")
                                        .executes(context -> showAttributes(context.getSource(), false)))
                                .then(Commands.literal("refresh")
                                        .executes(context -> showAttributes(context.getSource(), true))))
        );
    }


    private static int setSpecies(
            CommandSourceStack source,
            String input
    ) {

        ServerPlayer player;

        try {
            player = source.getPlayerOrException();
        } catch (Exception exception) {
            return 0;
        }


        Species species = switch (
                input.toLowerCase()
                ) {

            case "none" ->
                    Species.NONE;

            case "normal" ->
                    Species.NORMAL;

            case "augustine" ->
                    Species.AUGUSTINE;

            case "hybrid" ->
                    Species.HYBRID;

            case "original" ->
                    Species.ORIGINAL;

            case "original_hybrid" ->
                    Species.ORIGINAL_HYBRID;

            default ->
                    null;
        };


        if (species == null) {

            source.sendFailure(
                    Component.literal(
                            "Unknown species: " + input
                    )
            );

            return 0;
        }


        boolean success =
                SpeciesTransitionManager
                        .forceSpecies(
                                player,
                                species
                        );


        if (!success) {

            source.sendFailure(
                    Component.literal(
                            "Species transition failed."
                    )
            );

            return 0;
        }


        source.sendSuccess(
                () -> Component.literal(
                        "Species: "
                                + SpeciesManager
                                .getSpecies(player)
                ),
                false
        );

        return 1;
    }


    private static int showInfo(
            CommandSourceStack source
    ) {

        ServerPlayer player;

        try {
            player = source.getPlayerOrException();
        } catch (Exception exception) {
            return 0;
        }


        PlayerData data =
                SpeciesManager.getData(player);


        source.sendSuccess(
                () -> Component.literal(
                        "§6--- TVD Player Data ---"
                ),
                false
        );

        source.sendSuccess(
                () -> Component.literal(
                        "Species: §e"
                                + SpeciesManager
                                .getSpecies(player)
                ),
                false
        );

        source.sendSuccess(
                () -> Component.literal(
                        "Species Level: §e"
                                + SpeciesManager
                                .getSpeciesLevel(player)
                ),
                false
        );

        source.sendSuccess(
                () -> Component.literal(
                        "Lord Level: §e"
                                + SpeciesManager
                                .getLordLevel(player)
                ),
                false
        );

        source.sendSuccess(
                () -> Component.literal(
                        "Potency XP: §e"
                                + data.getPotencyXp()
                ),
                false
        );

        source.sendSuccess(
                () -> Component.literal(
                        "Mastery XP: §e"
                                + data.getMasteryXp()
                ),
                false
        );

        source.sendSuccess(
                () -> Component.literal(
                        "Wolf Bite: §e"
                                + data.hasWolfBite()
                ),
                false
        );

        return 1;
    }


    private static int startBite(
            CommandSourceStack source
    ) {

        try {

            ServerPlayer player =
                    source.getPlayerOrException();

            SpeciesManager
                    .getData(player)
                    .startWolfBite();

            return 1;

        } catch (Exception exception) {
            return 0;
        }
    }


    private static int cureBite(
            CommandSourceStack source
    ) {

        try {

            ServerPlayer player =
                    source.getPlayerOrException();

            SpeciesManager
                    .getData(player)
                    .cureWolfBite();

            return 1;

        } catch (Exception exception) {
            return 0;
        }
    }

    private static int showAttributes(CommandSourceStack source, boolean refresh) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        var custom = BloodManager.getCustomVampire(player).orElse(null);
        if (custom == null) {
            source.sendFailure(Component.literal("Species: " + SpeciesManager.getSpecies(player)
                    + "; no custom species attributes. Normal factions keep their own attribute system."));
            return 0;
        }
        if (refresh) {
            SpeciesAttributeManager.refresh(custom);
        }
        var profile = SpeciesAttributes.getProfile(custom);
        String info = "Species: " + SpeciesManager.getSpecies(player)
                + ", level=" + custom.getLevel() + "/" + custom.getMaxLevel()
                + "\nHealth species modifier (flat HP): " + SpeciesAttributeManager.modifierAmount(
                        player, Attributes.MAX_HEALTH, SpeciesAttributeManager.MAX_HEALTH)
                + "\nMovement species modifier (base fraction): " + SpeciesAttributeManager.modifierAmount(
                        player, Attributes.MOVEMENT_SPEED, SpeciesAttributeManager.MOVEMENT_SPEED)
                + "\nAttack-speed species modifier (base fraction): " + SpeciesAttributeManager.modifierAmount(
                        player, Attributes.ATTACK_SPEED, SpeciesAttributeManager.ATTACK_SPEED)
                + "\nAttack-damage species modifier (flat): " + SpeciesAttributeManager.modifierAmount(
                        player, Attributes.ATTACK_DAMAGE, SpeciesAttributeManager.ATTACK_DAMAGE)
                + "\nNatural armor target / modifier: " + SpeciesAttributeManager.naturalArmorTarget(
                        player, profile, custom.getLevel(), custom.getMaxLevel())
                + " / " + SpeciesAttributeManager.modifierAmount(
                        player, Attributes.ARMOR, SpeciesAttributeManager.NATURAL_ARMOR)
                + "\nNatural toughness target / modifier: " + SpeciesAttributeManager.naturalToughnessTarget(
                        profile, custom.getLevel(), custom.getMaxLevel())
                + " / " + SpeciesAttributeManager.modifierAmount(
                        player, Attributes.ARMOR_TOUGHNESS, SpeciesAttributeManager.NATURAL_TOUGHNESS)
                + "\nBlood exhaustion effective factor: " + player.getAttributeValue(ModAttributes.BLOOD_EXHAUSTION)
                + " (profile baseline=" + profile.bloodExhaustionFactor() + ", level modifier="
                + SpeciesAttributeManager.modifierAmount(player, ModAttributes.BLOOD_EXHAUSTION,
                        SpeciesAttributeManager.BLOOD_EXHAUSTION) + ")"
                + "\nBlood capacity: " + custom.getBloodData().getMaxBlood()
                + "\nHeavy armor penalty active: " + SpeciesAttributeManager.hasHeavyArmorPenalty(player, profile);
        source.sendSuccess(() -> Component.literal(info), false);
        return 1;
    }

    private static int showBlood(CommandSourceStack source) {
        try {
            ServerPlayer player = source.getPlayerOrException();
            BloodData blood = BloodManager.getBloodData(player).orElse(null);
            if (blood == null) {
                source.sendFailure(Component.literal("Current species does not use custom blood data."));
                return 0;
            }
            source.sendSuccess(
                    () -> Component.literal("Blood: " + blood.getBloodLevel()
                            + "/" + blood.getMaxBlood()
                            + ", saturation=" + blood.getSaturationLevel()
                            + ", exhaustion=" + blood.getExhaustionLevel()),
                    false
            );
            return 1;
        } catch (Exception exception) {
            return 0;
        }
    }

    private static int setBlood(CommandSourceStack source, int amount) {
        return mutateBlood(source, player -> BloodManager.setBlood(player, amount), "Blood set to " + amount + ".");
    }

    private static int setMastery(CommandSourceStack source, int level) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        var custom = BloodManager.getCustomVampire(player).orElse(null);
        if (custom == null) {
            source.sendFailure(Component.literal("Current species does not use mastery levels."));
            return 0;
        }

        custom.setMasteryLevel(level);
        org.kuro.tvdvampirism.skill.SpeciesSkillAccess.refresh(custom);
        custom.sync(true);
        source.sendSuccess(() -> Component.literal(level == 0
                ? "Mastery removed."
                : "Mastery level set to M" + level + "."), false);
        return 1;
    }

    private static int drainBlood(CommandSourceStack source, int amount) {
        return mutateBlood(source, player -> BloodManager.drain(player, amount), "Drained " + amount + " blood.");
    }

    private static int refillBlood(CommandSourceStack source) {
        return mutateBlood(source, BloodManager::refill, "Blood refilled.");
    }

    private static int mutateBlood(
            CommandSourceStack source,
            java.util.function.Predicate<ServerPlayer> mutation,
            String successMessage
    ) {
        try {
            ServerPlayer player = source.getPlayerOrException();
            if (!mutation.test(player)) {
                source.sendFailure(Component.literal("Current species does not use custom blood data."));
                return 0;
            }
            source.sendSuccess(() -> Component.literal(successMessage), false);
            return 1;
        } catch (Exception exception) {
            return 0;
        }
    }
}
