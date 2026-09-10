package org.kuro.tvdvampirism.skill;

import de.teamlapen.vampirism.api.VampirismRegistries;
import de.teamlapen.vampirism.api.entity.factions.ISkillTree;
import de.teamlapen.vampirism.entity.factions.FactionPlayerHandler;
import de.teamlapen.vampirism.entity.player.vampire.skills.VampireSkills;
import de.teamlapen.werewolves.core.ModSkills;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import org.kuro.tvdvampirism.faction.player.CustomFactionPlayer;

import java.util.Collection;
import java.util.LinkedHashSet;

public final class SpeciesSkillAccess {

    private SpeciesSkillAccess() {
    }

    public static Collection<Holder<ISkillTree>> includeRequiredTrees(
            CustomFactionPlayer<?> player,
            Collection<Holder<ISkillTree>> detectedTrees
    ) {
        LinkedHashSet<Holder<ISkillTree>> result = new LinkedHashSet<>();

        detectedTrees.stream()
                .filter(tree -> tree.unwrapKey().map(key -> !key.location().getNamespace().equals("tvdvampirism")
                        || !key.location().getPath().startsWith("custom/")).orElse(true))
                .filter(tree -> !tree.is(VampireSkills.Trees.LORD))
                .filter(tree -> !tree.is(ModSkills.Trees.LORD))
                .forEach(result::add);

        if (player.getLevel() <= 0) {
            return result;
        }

        Registry<ISkillTree> registry = player.asEntity()
                .registryAccess()
                .registryOrThrow(VampirismRegistries.Keys.SKILL_TREE);

        SpeciesSkillProfile profile = player.getSkillProfile();
        if (player.getMasteryLevel() >= 1) registry.getHolder(CustomSkills.tree(profile)).ifPresent(result::add);

        if (profile.hasVampireTree()) {
            registry.getHolder(VampireSkills.Trees.LEVEL)
                    .ifPresent(result::add);
        }

        if (profile.hasWerewolfTree()) {
            registry.getHolder(ModSkills.Trees.LEVEL)
                    .ifPresent(result::add);
        }

        return result;
    }

    public static void refresh(CustomFactionPlayer<?> player) {
        if (!player.isRemote() && player.getLevel() > 0) {
            FactionPlayerHandler.get(player.asEntity())
                    .checkSkillTreeLocks();
        }
    }
}
