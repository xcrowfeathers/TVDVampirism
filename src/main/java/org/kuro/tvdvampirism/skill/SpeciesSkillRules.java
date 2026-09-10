package org.kuro.tvdvampirism.skill;

import de.teamlapen.vampirism.api.entity.player.skills.ISkill;
import de.teamlapen.vampirism.api.entity.player.actions.IAction;
import de.teamlapen.vampirism.entity.player.vampire.skills.VampireSkills;
import de.teamlapen.vampirism.entity.player.vampire.actions.VampireActions;
import de.teamlapen.werewolves.core.ModSkills;
import org.kuro.tvdvampirism.faction.player.CustomFactionPlayer;

/** Species exclusions only; stock registry entries and normal players are untouched. */
public final class SpeciesSkillRules {
    private SpeciesSkillRules() {}

    public static boolean allows(CustomFactionPlayer<?> player, ISkill<?> skill) {
        if (skill instanceof CustomSkills.Skill<?> custom) return CustomSkills.eligible(player, custom.definition);
        var profile = player.getSkillProfile();
        if (profile.hasWerewolfTree() && (skill == VampireSkills.SUNSCREEN.get()
                || skill == VampireSkills.LESS_SUNDAMAGE.get() || skill == ModSkills.NIGHT_VISION.get()
                || skill == ModSkills.BEAST_RAGE.get())) return false;
        if (profile == SpeciesSkillProfile.AUGUSTINE && (skill == VampireSkills.LESS_BLOOD_THIRST.get()
                || skill == VampireSkills.ADVANCED_BITER.get())) return false;
        // Original recovery uses its own configured timer, not the stock DBNO attribute.
        return skill != VampireSkills.DBNO_DURATION.get()
                || (profile != SpeciesSkillProfile.ORIGINAL_VAMPIRE && profile != SpeciesSkillProfile.ORIGINAL_HYBRID);
    }

    public static boolean allowsAction(CustomFactionPlayer<?> player, IAction<?> action) {
        if (action instanceof org.kuro.tvdvampirism.ability.SpeciesAbility<?> custom)
            return CustomSkills.eligible(player, custom.definition) && player.getSkillHandler().isSkillEnabled(custom.definition.skill.get());
        return action != VampireActions.SUNSCREEN.get() || allows(player, VampireSkills.SUNSCREEN.get());
    }

    public static void cleanLoadedState(CustomFactionPlayer<?> player) {
        // Vanilla loads permanent attribute modifiers after attachment NBT. Clean on join/login,
        // not just while reading the skill list, so legacy modifiers cannot survive migration.
        for (ISkill<?> skill : java.util.List.of(VampireSkills.SUNSCREEN.get(), VampireSkills.LESS_SUNDAMAGE.get(),
                VampireSkills.LESS_BLOOD_THIRST.get(), VampireSkills.ADVANCED_BITER.get(),
                VampireSkills.DBNO_DURATION.get(), ModSkills.NIGHT_VISION.get(), ModSkills.BEAST_RAGE.get())) {
            if (!allows(player, skill)) StockSkillBridge.disable(player, skill);
        }
    }
}
