package org.kuro.tvdvampirism.skill;

import org.kuro.tvdvampirism.faction.player.CustomFactionPlayer;

public final class SpeciesSkillPoints {

    private SpeciesSkillPoints() {
    }

    public static int total(CustomFactionPlayer<?> player) {
        int level = player.getLevel();

        if (level <= 0) {
            return 0;
        }

        return player.getSkillProfile()
                .skillPoints()
                .pointsAt(level) + player.getMasteryLevel();
    }
}
