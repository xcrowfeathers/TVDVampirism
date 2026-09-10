package org.kuro.tvdvampirism.player;

import net.minecraft.world.entity.player.Player;
import org.kuro.tvdvampirism.config.ServerConfig;

public final class SpeciesRules {

    private SpeciesRules() {
    }


    // ---------------------------------------------------------
    // Basic classification
    // ---------------------------------------------------------

    public static boolean isVampiric(Player player) {

        return SpeciesManager.getSpecies(player)
                != Species.NONE;
    }

    public static boolean isCustom(Player player) {

        Species species =
                SpeciesManager.getSpecies(player);

        return species != Species.NONE
                && species != Species.NORMAL;
    }

    public static boolean isAugustine(Player player) {

        return SpeciesManager.getSpecies(player)
                == Species.AUGUSTINE;
    }

    public static boolean isHybrid(Player player) {

        Species species =
                SpeciesManager.getSpecies(player);

        return species == Species.HYBRID
                || species == Species.ORIGINAL_HYBRID;
    }

    public static boolean isOriginal(Player player) {

        Species species =
                SpeciesManager.getSpecies(player);

        return species == Species.ORIGINAL
                || species == Species.ORIGINAL_HYBRID;
    }

    public static boolean hasWerewolfSide(Player player) {
        return isHybrid(player);
    }


    // ---------------------------------------------------------
    // Progression
    // ---------------------------------------------------------

    public static boolean usesPotency(Player player) {

        Species species =
                SpeciesManager.getSpecies(player);

        return species == Species.AUGUSTINE
                || species == Species.HYBRID;
    }

    public static boolean usesMastery(Player player) {

        Species species =
                SpeciesManager.getSpecies(player);

        return species == Species.ORIGINAL
                || species == Species.ORIGINAL_HYBRID;
    }

    public static boolean canGainLordLevels(Player player) {
        return SpeciesManager.getSpecies(player) == Species.NORMAL;
    }


    // ---------------------------------------------------------
    // Sun
    // ---------------------------------------------------------

    public static boolean isSunImmune(Player player) {

        return switch (SpeciesManager.getSpecies(player)) {

            case HYBRID ->
                    ServerConfig
                            .HYBRID_SUN_IMMUNE
                            .get();

            case ORIGINAL ->
                    ServerConfig
                            .ORIGINAL_SUN_IMMUNE
                            .get();

            case ORIGINAL_HYBRID ->
                    ServerConfig
                            .ORIGINAL_HYBRID_SUN_IMMUNE
                            .get();

            default -> false;
        };
    }


    // ---------------------------------------------------------
    // Werewolf bite
    // ---------------------------------------------------------

    public static boolean isWolfBiteImmune(Player player) {

        return switch (SpeciesManager.getSpecies(player)) {

            case HYBRID ->
                    ServerConfig
                            .HYBRID_WOLF_BITE_IMMUNE
                            .get();

            case ORIGINAL_HYBRID ->
                    ServerConfig
                            .ORIGINAL_HYBRID_WOLF_BITE_IMMUNE
                            .get();

            default -> false;
        };
    }

    public static boolean canReceiveWolfBite(Player player) {

        Species species =
                SpeciesManager.getSpecies(player);

        if (species == Species.NONE) {
            return false;
        }

        if (isWolfBiteImmune(player)) {
            return false;
        }

        return switch (species) {

            case NORMAL ->
                    ServerConfig
                            .WOLF_BITE_AFFECTS_NORMAL
                            .get();

            case AUGUSTINE ->
                    ServerConfig
                            .WOLF_BITE_AFFECTS_AUGUSTINE
                            .get();

            case ORIGINAL ->
                    ServerConfig
                            .WOLF_BITE_AFFECTS_ORIGINAL
                            .get();

            case HYBRID, ORIGINAL_HYBRID -> false;

            default -> false;
        };
    }

    public static boolean wolfBiteCanKill(Player player) {

        return switch (SpeciesManager.getSpecies(player)) {

            case NORMAL ->
                    ServerConfig
                            .WOLF_BITE_KILLS_NORMAL
                            .get();

            case AUGUSTINE ->
                    ServerConfig
                            .WOLF_BITE_KILLS_AUGUSTINE
                            .get();

            /*
             * Originals sind loremäßig nicht durch
             * Werewolf Venom permanent tötbar.
             */
            case ORIGINAL,
                 HYBRID,
                 ORIGINAL_HYBRID,
                 NONE -> false;
        };
    }


    // ---------------------------------------------------------
    // Hybrid abilities
    // ---------------------------------------------------------

    public static boolean canUseHumanWolfBite(Player player) {

        Species species =
                SpeciesManager.getSpecies(player);

        return species == Species.HYBRID
                || species == Species.ORIGINAL_HYBRID;
    }

    public static boolean ignoresWerewolfTransformationRestrictions(
            Player player
    ) {

        Species species =
                SpeciesManager.getSpecies(player);

        return species == Species.HYBRID
                || species == Species.ORIGINAL_HYBRID;
    }


    // ---------------------------------------------------------
    // Immortality
    // ---------------------------------------------------------

    public static boolean hasOriginalImmortality(Player player) {

        return isOriginal(player);
    }

    public static boolean requiresWhiteOakToDie(Player player) {

        return isOriginal(player);
    }


    // ---------------------------------------------------------
    // Immortality
    // ---------------------------------------------------------

    public static boolean hasFireVulnerability(Player player) {
        var custom = org.kuro.tvdvampirism.compat.SpeciesCompatibility.customPlayer(player);
        if (custom == null) return false;

        return switch (custom.getSkillProfile()) {
            case AUGUSTINE -> ServerConfig.AUGUSTINE_FIRE_VULNERABILITY.get();
            case ORIGINAL_VAMPIRE -> ServerConfig.ORIGINAL_VAMPIRE_FIRE_VULNERABILITY.get();
            case HYBRID -> ServerConfig.HYBRID_FIRE_VULNERABILITY.get();
            case ORIGINAL_HYBRID -> ServerConfig.ORIGINAL_HYBRID_FIRE_VULNERABILITY.get();
        };
    }
}
