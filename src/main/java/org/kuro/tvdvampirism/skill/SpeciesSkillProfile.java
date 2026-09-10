package org.kuro.tvdvampirism.skill;

import org.kuro.tvdvampirism.config.ServerConfig;

public enum SpeciesSkillProfile {

    AUGUSTINE(true, false),
    HYBRID(true, true),
    ORIGINAL_VAMPIRE(true, false),
    ORIGINAL_HYBRID(true, true);

    private final boolean vampireTree;
    private final boolean werewolfTree;

    SpeciesSkillProfile(boolean vampireTree, boolean werewolfTree) {
        this.vampireTree = vampireTree;
        this.werewolfTree = werewolfTree;
    }

    public boolean hasVampireTree() {
        return vampireTree;
    }

    public boolean hasWerewolfTree() {
        return werewolfTree;
    }

    public ServerConfig.SkillPointProfile skillPoints() {
        return switch (this) {
            case AUGUSTINE -> ServerConfig.AUGUSTINE_SKILL_POINTS;
            case HYBRID -> ServerConfig.HYBRID_SKILL_POINTS;
            case ORIGINAL_VAMPIRE -> ServerConfig.ORIGINAL_VAMPIRE_SKILL_POINTS;
            case ORIGINAL_HYBRID -> ServerConfig.ORIGINAL_HYBRID_SKILL_POINTS;
        };
    }
}