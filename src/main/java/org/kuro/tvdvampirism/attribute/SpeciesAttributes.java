package org.kuro.tvdvampirism.attribute;

import org.kuro.tvdvampirism.config.ServerConfig;
import org.kuro.tvdvampirism.faction.SpeciesFactions;
import org.kuro.tvdvampirism.faction.player.CustomFactionPlayer;

import java.util.ArrayList;

/** Maps the existing faction identity to an independently configurable profile */
public final class SpeciesAttributes {
    private SpeciesAttributes() {
    }

    public static SpeciesAttributeProfile getProfile(CustomFactionPlayer<?> player) {
        ServerConfig.AttributeProfile config = switch (SpeciesFactions.getSpecies(player.getFaction())) {
            case AUGUSTINE -> ServerConfig.AUGUSTINE_ATTRIBUTES;
            case HYBRID -> ServerConfig.HYBRID_ATTRIBUTES;
            case ORIGINAL -> ServerConfig.ORIGINAL_VAMPIRE_ATTRIBUTES;
            case ORIGINAL_HYBRID -> ServerConfig.ORIGINAL_HYBRID_ATTRIBUTES;
            default -> throw new IllegalArgumentException("Not a custom species: " + player.getFaction().getID());
        };

        // Cross-field constraints cannot be expressed by defineInRange. Resolve in tier order.
        int damageTier1 = config.attackDamageTier1Level.get();
        int damageTier2 = Math.max(damageTier1, config.attackDamageTier2Level.get());
        var bloodTiers = new ArrayList<SpeciesAttributeProfile.BloodTier>();
        int previousLevel = 0;
        for (int i = 0; i < config.bloodCapacityLevels.size(); i++) {
            int level = Math.max(previousLevel, config.bloodCapacityLevels.get(i).get());
            bloodTiers.add(new SpeciesAttributeProfile.BloodTier(level, config.bloodCapacities.get(i).get()));
            previousLevel = level;
        }

        return new SpeciesAttributeProfile(
                config.healthMaxMod.get(), config.speedMaxMod.get(), config.attackSpeedMaxMod.get(),
                config.levelCurveExponent.get(), damageTier1, config.attackDamageTier1Bonus.get(),
                damageTier2, config.attackDamageTier2Bonus.get(), config.naturalArmorBase.get(),
                config.naturalArmorIncrease.get(), config.naturalArmorToughnessIncrease.get(),
                config.naturalArmorRegenDurationSeconds.get(), config.naturalArmorRegenLossFraction.get(),
                config.armorPenaltyEnabled.get(), config.armorPenaltyThreshold.get(),
                config.armorPenaltyMultiplier.get(), config.bloodExhaustionFactor.get(),
                config.exhaustionMaxMod.get(), bloodTiers
        );
    }
}
