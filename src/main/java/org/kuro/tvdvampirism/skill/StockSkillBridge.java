package org.kuro.tvdvampirism.skill;

import de.teamlapen.vampirism.api.VReference;
import de.teamlapen.vampirism.api.entity.factions.IPlayableFaction;
import de.teamlapen.vampirism.api.entity.player.IFactionPlayer;
import de.teamlapen.vampirism.api.entity.player.skills.ISkill;
import de.teamlapen.werewolves.api.WReference;
import org.kuro.tvdvampirism.faction.player.CustomFactionPlayer;

public final class StockSkillBridge {

    private StockSkillBridge() {
    }

    public static void enable(
            CustomFactionPlayer<?> owner,
            ISkill<?> skill
    ) {
        invoke(owner, skill, true);
    }

    public static void disable(
            CustomFactionPlayer<?> owner,
            ISkill<?> skill
    ) {
        invoke(owner, skill, false);
    }

    private static void invoke(
            CustomFactionPlayer<?> owner,
            ISkill<?> skill,
            boolean enable
    ) {
        IPlayableFaction<?> faction = skill.getFaction().orElse(null);
        IFactionPlayer<?> target;

        if (faction == VReference.VAMPIRE_FACTION) {
            if (!owner.getSkillProfile().hasVampireTree()) {
                throw new IllegalStateException(
                        "Vampire skill used by species without vampire tree: " + skill
                );
            }

            target = owner.getVampireSkillBridge();

        } else if (faction == WReference.WEREWOLF_FACTION) {
            if (!owner.getSkillProfile().hasWerewolfTree()) {
                throw new IllegalStateException(
                        "Werewolf skill used by species without werewolf tree: " + skill
                );
            }

            target = owner.getWerewolfSkillBridge();

        } else {
            target = owner;
        }

        if (enable) {
            enableRaw(skill, target);
        } else {
            disableRaw(skill, target);
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void enableRaw(
            ISkill skill,
            IFactionPlayer player
    ) {
        skill.onEnable(player);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void disableRaw(
            ISkill skill,
            IFactionPlayer player
    ) {
        skill.onDisable(player);
    }
}