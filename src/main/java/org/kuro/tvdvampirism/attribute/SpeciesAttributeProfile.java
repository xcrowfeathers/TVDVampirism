package org.kuro.tvdvampirism.attribute;

import java.util.List;

/** Resolved server balance values; no live config objects or mutable collections. */
public record SpeciesAttributeProfile(
        double healthMaxMod,
        double speedMaxMod,
        double attackSpeedMaxMod,
        double levelCurveExponent,
        int attackDamageTier1Level,
        double attackDamageTier1Bonus,
        int attackDamageTier2Level,
        double attackDamageTier2Bonus,
        double naturalArmorBase,
        double naturalArmorIncrease,
        double naturalArmorToughnessIncrease,
        int naturalArmorRegenDurationSeconds,
        double naturalArmorRegenLossFraction,
        boolean armorPenaltyEnabled,
        double armorPenaltyThreshold,
        double armorPenaltyMultiplier,
        double bloodExhaustionFactor,
        double exhaustionMaxMod,
        List<BloodTier> bloodCapacityTiers
) {
    public SpeciesAttributeProfile {
        bloodCapacityTiers = List.copyOf(bloodCapacityTiers);
    }

    public record BloodTier(int level, int capacity) {
    }
}
