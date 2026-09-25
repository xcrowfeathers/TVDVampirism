package org.kuro.tvdvampirism.client.action;

import de.teamlapen.vampirism.api.VReference;
import de.teamlapen.vampirism.api.entity.factions.IPlayableFaction;
import de.teamlapen.vampirism.api.entity.player.IFactionPlayer;
import de.teamlapen.vampirism.api.entity.player.actions.IAction;
import de.teamlapen.vampirism.client.ClientConfigHelper;
import de.teamlapen.werewolves.api.WReference;
import net.minecraft.client.Minecraft;
import org.jetbrains.annotations.Nullable;
import org.kuro.tvdvampirism.faction.player.CustomFactionPlayer;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;

public final class SpeciesClientActionAccess {
    public static boolean hasReadyHybridBiteTarget() {
        var mc = Minecraft.getInstance();
        var player = mc.player;
        if (player == null || !player.isAlive()
                || !org.kuro.tvdvampirism.player.SpeciesRules.canUseHumanWolfBite(player)
                || de.teamlapen.werewolves.config.WerewolvesConfig.CLIENT.disableFangCrosshairRendering.get()) return false;
        var custom = org.kuro.tvdvampirism.compat.SpeciesCompatibility.customPlayer(player);
        var wolf = de.teamlapen.werewolves.entities.player.werewolf.WerewolfPlayer.get(player);
        return custom != null && custom.getFeedingTargetId() < 0 && wolf.canBite()
                && !org.kuro.tvdvampirism.compat.SpeciesCompatibility.rawVampire(player).isDBNO()
                && mc.hitResult instanceof net.minecraft.world.phys.EntityHitResult hit
                && hit.getEntity() instanceof net.minecraft.world.entity.LivingEntity target && target.isAlive()
                && wolf.canBiteEntity(target) && player.hasLineOfSight(target);
    }

    private SpeciesClientActionAccess() {
    }

    public static @Nullable CustomFactionPlayer<?> currentCustomPlayer(
            IPlayableFaction<?> faction
    ) {
        Minecraft minecraft = Minecraft.getInstance();

        if (minecraft.player == null) {
            return null;
        }

        IFactionPlayer<?> factionPlayer =
                faction.getPlayerCapability(minecraft.player).orElse(null);

        if (!(factionPlayer instanceof CustomFactionPlayer<?> customPlayer)) {
            return null;
        }

        return customPlayer.getFaction().getID().equals(faction.getID())
                ? customPlayer
                : null;
    }

    public static List<IAction<?>> inheritedDefaultActionOrder(
            CustomFactionPlayer<?> player,
            Collection<IAction<?>> existing
    ) {
        LinkedHashSet<IAction<?>> result = new LinkedHashSet<>(existing);

        if (player.hasVampireSkillBridge()) {
            result.addAll(
                    ClientConfigHelper.getDefaultActionOrder(
                            VReference.VAMPIRE_FACTION
                    )
            );
        }

        if (player.hasWerewolfSkillBridge()) {
            result.addAll(
                    ClientConfigHelper.getDefaultActionOrder(
                            WReference.WEREWOLF_FACTION
                    )
            );
        }

        player.getActionHandler().getUnlockedActions().stream()
                .filter(action -> action.getFaction().orElse(null) == player.getFaction()
                        || action instanceof org.kuro.tvdvampirism.ability.SpeciesAbility<?>)
                .forEach(result::add);
        result.removeIf(action -> !org.kuro.tvdvampirism.skill.SpeciesSkillRules.allowsAction(player, action));
        return List.copyOf(result);
    }

    public static List<IAction<?>> selectableActionCatalog(
            CustomFactionPlayer<?> player
    ) {
        return inheritedDefaultActionOrder(player, List.of())
                .stream()
                .filter(action ->
                        action.showInSelectAction(player.asEntity())
                )
                .toList();
    }

    public static IAction.PERM canUse(
            IAction<?> action,
            IFactionPlayer<?> player
    ) {
        if (!(player instanceof CustomFactionPlayer<?> customPlayer)) {
            return canUseRaw(action, player);
        }

        IPlayableFaction<?> actionFaction = action.getFaction().orElse(null);

        if (actionFaction == VReference.VAMPIRE_FACTION) {
            return customPlayer.hasVampireSkillBridge()
                    ? canUseRaw(action, customPlayer.getVampireSkillBridge())
                    : IAction.PERM.DISABLED;
        }

        if (actionFaction == WReference.WEREWOLF_FACTION) {
            return customPlayer.hasWerewolfSkillBridge()
                    ? canUseRaw(action, customPlayer.getWerewolfSkillBridge())
                    : IAction.PERM.DISABLED;
        }

        return canUseRaw(action, customPlayer);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static IAction.PERM canUseRaw(
            IAction action,
            IFactionPlayer player
    ) {
        return action.canUse(player);
    }
}
