package org.kuro.tvdvampirism.config;

import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.ArrayList;
import java.util.List;

public final class ServerConfig {

    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.DoubleValue HOLY_WATER_RESISTANCE, HYBRID_LEAP_DAMAGE, ORIGINAL_HYBRID_LEAP_DAMAGE, LEAP_RANGE, RIP_HEART_HEALTH;
    public static final ModConfigSpec.IntValue HYBRID_LEAP_COOLDOWN, ORIGINAL_HYBRID_LEAP_COOLDOWN, LEAP_WEAKNESS_SECONDS,
            RIP_HEART_COOLDOWN, COMPULSION_SECONDS, COMPULSION_COOLDOWN;
    public static final ModConfigSpec.BooleanValue COMPULSION_ENABLED;
    public static final ModConfigSpec.BooleanValue ELDER_DAGGER_ENABLED, CURSED_ELDER_DAGGER_ENABLED, DAGGER_BROADCAST_ENABLED;
    public static final ModConfigSpec.IntValue DAGGER_REMOVAL_DELAY_TICKS;
    public static final ModConfigSpec.ConfigValue<String> DAGGER_BROADCAST_MESSAGE;


    // =========================================================
    // GENERAL
    // =========================================================

    public static final ModConfigSpec.BooleanValue DISABLE_BAT_FORM, DISABLE_CLEANSING_ALTAR,
            DAYLIGHT_RING_ENABLED;
    public static final ModConfigSpec.BooleanValue OVERRIDE_VAMPIRISM_HOSTILE_IGNORE,
            OVERRIDE_WEREWOLVES_HOSTILE_IGNORE;
    public static final ModConfigSpec.IntValue VAMPIRISM_CURE_DURATION_SECONDS;

    public static final AttributeProfile AUGUSTINE_ATTRIBUTES;
    public static final AttributeProfile HYBRID_ATTRIBUTES;
    public static final AttributeProfile ORIGINAL_VAMPIRE_ATTRIBUTES;
    public static final AttributeProfile ORIGINAL_HYBRID_ATTRIBUTES;
    public static final ModConfigSpec.IntValue CUSTOM_VAMPIRE_FEEDING_AMOUNT;
    public static final ModConfigSpec.DoubleValue CUSTOM_VAMPIRE_FEEDING_DAMAGE;
    public static final ModConfigSpec.IntValue CUSTOM_VAMPIRE_FEEDING_INTERVAL_TICKS;


    // =========================================================
    // WEREWOLF BITE
    // =========================================================

    public static final ModConfigSpec.IntValue WOLF_BITE_DURATION_MINUTES;
    public static final ModConfigSpec.IntValue WOLF_BITE_MOB_DURATION_MINUTES;
    public static final ModConfigSpec.IntValue WOLF_BITE_CURE_MINUTES;
    public static final ModConfigSpec.DoubleValue WOLF_BITE_MOB_CHANCE;
    public static final ModConfigSpec.DoubleValue WOLF_BITE_PLAYER_CHANCE;
    public static final ModConfigSpec.IntValue HYBRID_FEED_HOLD_TICKS;
    public static final ModConfigSpec.IntValue HYBRID_FORM_BLOOD_COST, ORIGINAL_HYBRID_FORM_BLOOD_COST;
    public static final ModConfigSpec.IntValue HYBRID_FORM_BLOOD_INTERVAL, ORIGINAL_HYBRID_FORM_BLOOD_INTERVAL;

    public static final ModConfigSpec.BooleanValue WOLF_BITE_AFFECTS_NORMAL;
    public static final ModConfigSpec.BooleanValue WOLF_BITE_AFFECTS_AUGUSTINE;
    public static final ModConfigSpec.BooleanValue WOLF_BITE_AFFECTS_ORIGINAL;

    public static final ModConfigSpec.BooleanValue WOLF_BITE_KILLS_NORMAL;
    public static final ModConfigSpec.BooleanValue WOLF_BITE_KILLS_AUGUSTINE;

    public static final ModConfigSpec.DoubleValue WOLF_BITE_NAUSEA_START;
    public static final ModConfigSpec.DoubleValue WOLF_BITE_WEAKNESS_TWO_START;
    public static final ModConfigSpec.DoubleValue WOLF_BITE_SLOWNESS_START;
    public static final ModConfigSpec.DoubleValue WOLF_BITE_DAMAGE_START;
    public static final ModConfigSpec.DoubleValue WOLF_BITE_SEVERE_START;
    public static final ModConfigSpec.IntValue WOLF_BITE_STAGE_ONE_INTERVAL;
    public static final ModConfigSpec.IntValue WOLF_BITE_STAGE_TWO_INTERVAL;
    public static final ModConfigSpec.IntValue WOLF_BITE_STAGE_THREE_INTERVAL;
    public static final ModConfigSpec.IntValue WOLF_BITE_SYMPTOM_SECONDS;


    // =========================================================
    // SPECIES RULES
    // =========================================================

    public static final ModConfigSpec.BooleanValue HYBRID_SUN_IMMUNE;
    public static final ModConfigSpec.BooleanValue ORIGINAL_SUN_IMMUNE;
    public static final ModConfigSpec.BooleanValue ORIGINAL_HYBRID_SUN_IMMUNE;

    public static final ModConfigSpec.BooleanValue HYBRID_WOLF_BITE_IMMUNE;
    public static final ModConfigSpec.BooleanValue ORIGINAL_HYBRID_WOLF_BITE_IMMUNE;


    // =========================================================
    // FIRE VULNERABILITY
    // =========================================================

    public static final ModConfigSpec.BooleanValue AUGUSTINE_FIRE_VULNERABILITY;
    public static final ModConfigSpec.BooleanValue ORIGINAL_VAMPIRE_FIRE_VULNERABILITY;
    public static final ModConfigSpec.BooleanValue HYBRID_FIRE_VULNERABILITY;
    public static final ModConfigSpec.BooleanValue ORIGINAL_HYBRID_FIRE_VULNERABILITY;


    // =========================================================
    // TRANSFORMATION
    // =========================================================

    public static final ModConfigSpec.IntValue HYBRID_BLOOD_EFFECT_MINUTES;
    public static final ModConfigSpec.IntValue AUGUSTINE_TRANSFORMATION_SECONDS;

    public static final ModConfigSpec.IntValue ORIGINAL_CURSE_DURATION_MINUTES;
    public static final ModConfigSpec.IntValue ORIGINAL_HYBRID_CURSE_DURATION_MINUTES;

    public static final ModConfigSpec.IntValue ORIGINAL_AWAKENING_DBNO_SECONDS;
    public static final ModConfigSpec.IntValue ORIGINAL_HYBRID_AWAKENING_DBNO_SECONDS;
    public static final ModConfigSpec.IntValue ORIGINAL_DBNO_SECONDS;
    public static final ModConfigSpec.IntValue ORIGINAL_HYBRID_DBNO_SECONDS;


    // =========================================================
    // PROGRESSION
    // =========================================================

    public static final ModConfigSpec.IntValue AUGUSTINE_MAX_POTENCY;
    public static final ModConfigSpec.IntValue HYBRID_MAX_POTENCY;
    public static final ModConfigSpec.IntValue ORIGINAL_MAX_MASTERY;

    public static final SkillPointProfile AUGUSTINE_SKILL_POINTS;
    public static final SkillPointProfile HYBRID_SKILL_POINTS;
    public static final SkillPointProfile ORIGINAL_VAMPIRE_SKILL_POINTS;
    public static final SkillPointProfile ORIGINAL_HYBRID_SKILL_POINTS;

    // Compatibility aliases for code that only needs the per-level value.
    public static final ModConfigSpec.DoubleValue AUGUSTINE_SKILL_POINTS_PER_LEVEL;
    public static final ModConfigSpec.DoubleValue HYBRID_SKILL_POINTS_PER_LEVEL;
    public static final ModConfigSpec.DoubleValue ORIGINAL_VAMPIRE_SKILL_POINTS_PER_LEVEL;
    public static final ModConfigSpec.DoubleValue ORIGINAL_HYBRID_SKILL_POINTS_PER_LEVEL;

    public static final ModConfigSpec.BooleanValue CUSTOM_SPECIES_CAN_GAIN_LORD_LEVELS;


    // =========================================================
    // AUGUSTINE
    // =========================================================

    public static final ModConfigSpec.DoubleValue AUGUSTINE_HUMAN_BLOOD_MULTIPLIER;
    public static final ModConfigSpec.IntValue AUGUSTINE_VAMPIRE_BOTTLE_GAIN;
    public static final ModConfigSpec.IntValue AUGUSTINE_FEEDING_CAP;
    public static final ModConfigSpec.DoubleValue AUGUSTINE_FEEDING_FINISH_DAMAGE;
    public static final ModConfigSpec.DoubleValue AUGUSTINE_VAMPIRE_BLOOD_MULTIPLIER;

    public static final ModConfigSpec.DoubleValue AUGUSTINE_BLOOD_USAGE_MULTIPLIER;
    public static final ModConfigSpec.DoubleValue AUGUSTINE_FEED_DAMAGE_MULTIPLIER;
    public static final ModConfigSpec.DoubleValue AUGUSTINE_FEED_AMOUNT_MULTIPLIER;


    // =========================================================
    // LOOT
    // =========================================================

    public static final ModConfigSpec.DoubleValue AUGUSTINE_SERUM_LOOT_CHANCE;

    public static final ModConfigSpec.BooleanValue WHITE_OAK_STAKE_LOOT_ENABLED;
    public static final ModConfigSpec.BooleanValue ELDER_DAGGER_LOOT_ENABLED;
    public static final ModConfigSpec.BooleanValue CURSED_ELDER_DAGGER_LOOT_ENABLED;
    public static final ModConfigSpec.BooleanValue VAMPIRISM_CURE_LOOT_ENABLED;
    public static final ModConfigSpec.BooleanValue AUGUSTINE_SYRINGE_LOOT_ENABLED;
    public static final ModConfigSpec.BooleanValue ORIGINAL_HYBRID_BLOOD_LOOT_ENABLED;
    public static final ModConfigSpec.BooleanValue IMMORTALITY_SERUM_LOOT_ENABLED;
    public static final ModConfigSpec.BooleanValue DAYLIGHT_RING_LOOT_ENABLED;

    public static final ModConfigSpec.DoubleValue WHITE_OAK_STAKE_LOOT_CHANCE;
    public static final ModConfigSpec.DoubleValue ELDER_DAGGER_LOOT_CHANCE;
    public static final ModConfigSpec.DoubleValue CURSED_ELDER_DAGGER_LOOT_CHANCE;
    public static final ModConfigSpec.DoubleValue VAMPIRISM_CURE_LOOT_CHANCE;
    public static final ModConfigSpec.DoubleValue AUGUSTINE_SYRINGE_LOOT_CHANCE;
    public static final ModConfigSpec.DoubleValue ORIGINAL_HYBRID_BLOOD_LOOT_CHANCE;
    public static final ModConfigSpec.DoubleValue IMMORTALITY_SERUM_LOOT_CHANCE;
    public static final ModConfigSpec.DoubleValue DAYLIGHT_RING_LOOT_CHANCE;


    static {

        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        builder.push("custom_skills");
        HOLY_WATER_RESISTANCE = builder.comment("Fraction of holy-water damage and symptom duration resisted by the Augustine skill.")
                .defineInRange("holy_water_resistance", 0.5D, 0D, 1D);
        HYBRID_LEAP_DAMAGE = builder.defineInRange("hybrid_leap_damage", 8D, 0D, 1000D);
        ORIGINAL_HYBRID_LEAP_DAMAGE = builder.defineInRange("original_hybrid_leap_damage", 10D, 0D, 1000D);
        LEAP_RANGE = builder.defineInRange("leap_range", 15D, 1D, 15D);
        HYBRID_LEAP_COOLDOWN = builder.defineInRange("hybrid_leap_cooldown_seconds", 30, 1, 3600);
        ORIGINAL_HYBRID_LEAP_COOLDOWN = builder.defineInRange("original_hybrid_leap_cooldown_seconds", 15, 1, 3600);
        LEAP_WEAKNESS_SECONDS = builder.defineInRange("leap_weakness_seconds", 10, 1, 300);
        RIP_HEART_HEALTH = builder.defineInRange("rip_heart_max_health", 10D, 1D, 1000D);
        RIP_HEART_COOLDOWN = builder.defineInRange("rip_heart_cooldown_seconds", 30, 1, 3600);
        COMPULSION_ENABLED = builder.define("compulsion_enabled", true);
        COMPULSION_SECONDS = builder.defineInRange("compulsion_duration_seconds", 25, 1, 300);
        COMPULSION_COOLDOWN = builder.defineInRange("compulsion_cooldown_seconds", 60, 1, 3600);
        builder.pop();
        builder.push("elder_daggers");
        ELDER_DAGGER_ENABLED = builder.define("elder_dagger_enabled", true);
        CURSED_ELDER_DAGGER_ENABLED = builder.define("cursed_elder_dagger_enabled", true);
        DAGGER_REMOVAL_DELAY_TICKS = builder.comment("Ticks before an elder dagger can be removed.")
                .defineInRange("removal_delay_ticks", 30, 0, 200);
        DAGGER_BROADCAST_ENABLED = builder.define("broadcast_enabled", true);
        DAGGER_BROADCAST_MESSAGE = builder.define("broadcast_message", "The %species% %victim% was daggered by %attacker%");
        builder.pop();

        builder.push("attributes");
        AUGUSTINE_ATTRIBUTES = new AttributeProfile(builder, "augustine",
                18, 0.55, 0.18, 2, 3, 10, 10, 8, true, 1.0,
                List.of(24, 30, 34, 38, 40));
        HYBRID_ATTRIBUTES = new AttributeProfile(builder, "hybrid",
                20, 0.60, 0.20, 2, 3, 10, 12, 8, true, 0.7,
                List.of(24, 30, 34, 38, 40));
        ORIGINAL_VAMPIRE_ATTRIBUTES = new AttributeProfile(builder, "original_vampire",
                24, 0.70, 0.25, 3, 4, 12, 12, 10, false, 0.7,
                List.of(24, 30, 34, 38, 40));
        ORIGINAL_HYBRID_ATTRIBUTES = new AttributeProfile(builder, "original_hybrid",
                24, 0.75, 0.28, 4, 5, 12, 14, 10, false, 0.7,
                List.of(24, 30, 34, 38, 40));
        builder.pop();


        // =====================================================
        // GENERAL
        // =====================================================

        builder.push("general");

        DISABLE_BAT_FORM = builder
                .comment(
                        "If true, overrides vampirism's bat mode to be disabled.",
                        "Disable this to use the original Vampirism balance-config value.",
                        "Default: false"
                )
                .define("disable_bat_form", false);

        DISABLE_CLEANSING_ALTAR = builder
                .comment(
                        "Prevents interaction with Vampirism's Cleansing Altar so players will have to use the vampirism cure.",
                        "Default: true"
                )
                .define("disableCleansingAltar", true);

        VAMPIRISM_CURE_DURATION_SECONDS = builder
                .comment(
                        "Duration of the Vampirism Cure effect.",
                        "Default: 1800 seconds (30 minutes)"
                )
                .defineInRange("vampirismCureDurationSeconds", 1800, 1, 86400);

        DAYLIGHT_RING_ENABLED = builder
                .comment(
                        "Whether an equipped Daylight Ring protects eligible vampires from sunlight.",
                        "The item remains registered and equipable when this is disabled."
                )
                .define("daylightRingEnabled", true);

        builder.pop();

        builder.push("hostile_mob_targeting");

        OVERRIDE_VAMPIRISM_HOSTILE_IGNORE = builder
                .comment(
                        "When enabled, sets Vampirism's zombie, creeper and skeleton hostile-ignore settings false, so vampires are being targeted by default.",
                        "Disable this to use the original Vampirism balance-config values."
                )
                .define("overrideVampirismHostileIgnore", true);

        OVERRIDE_WEREWOLVES_HOSTILE_IGNORE = builder
                .comment(
                        "Same as the vampire hostile ignore.",
                        "Disable this to use the original Werewolves balance-config value."
                )
                .define("overrideWerewolvesHostileIgnore", true);

        builder.pop();

        builder.push("custom_vampire_blood");

        CUSTOM_VAMPIRE_FEEDING_AMOUNT = builder
                .comment("Maximum blood removed and gained per feeding pulse from creatures.")
                .defineInRange("feeding_amount", 3, 1, 100);

        CUSTOM_VAMPIRE_FEEDING_DAMAGE = builder
                .comment("Direct damage dealt to a victim on each successful feeding pulse.")
                .defineInRange("feeding_damage", 1.0D, 0.0D, 1000.0D);

        CUSTOM_VAMPIRE_FEEDING_INTERVAL_TICKS = builder
                .comment("Ticks between feeding pulses while the feeding key is held.")
                .defineInRange("feeding_interval_ticks", 20, 1, 200);

        builder.pop();


        // =====================================================
        // WEREWOLF BITE
        // =====================================================

        builder.push("werewolf_bite");
        WOLF_BITE_STAGE_ONE_INTERVAL = builder.defineInRange("stage_one_pulse_seconds", 120, 10, 600);
        WOLF_BITE_STAGE_TWO_INTERVAL = builder.defineInRange("stage_two_pulse_seconds", 60, 10, 600);
        WOLF_BITE_STAGE_THREE_INTERVAL = builder.defineInRange("stage_three_pulse_seconds", 20, 10, 600);
        WOLF_BITE_SYMPTOM_SECONDS = builder.comment("Pulse duration; doubled in stage three and capped at half the pulse interval.")
                .defineInRange("symptom_seconds", 4, 1, 30);
        WOLF_BITE_MOB_DURATION_MINUTES = builder.defineInRange("mob_duration_minutes", 2, 1, 5);
        WOLF_BITE_CURE_MINUTES = builder.defineInRange("cure_minutes", 5, 1, 240);
        WOLF_BITE_MOB_CHANCE = builder.defineInRange("mob_attack_chance", 0.05D, 0D, 1D);
        WOLF_BITE_PLAYER_CHANCE = builder.defineInRange("player_bite_chance", 0.25D, 0D, 1D);
        HYBRID_FEED_HOLD_TICKS = builder.defineInRange("hybrid_feed_hold_ticks", 7, 1, 40);
        HYBRID_FORM_BLOOD_COST = builder.defineInRange("hybrid_form_blood_cost", 1, 0, 100);
        ORIGINAL_HYBRID_FORM_BLOOD_COST = builder.defineInRange("original_hybrid_form_blood_cost", 1, 0, 100);
        HYBRID_FORM_BLOOD_INTERVAL = builder.defineInRange("hybrid_form_blood_interval_seconds", 30, 1, 3600);
        ORIGINAL_HYBRID_FORM_BLOOD_INTERVAL = builder.defineInRange("original_hybrid_form_blood_interval_seconds", 30, 1, 3600);

        WOLF_BITE_DURATION_MINUTES = builder
                .comment("Total duration of werewolf venom in minutes.")
                .defineInRange(
                        "duration_minutes",
                        25,
                        1,
                        240
                );

        WOLF_BITE_AFFECTS_NORMAL = builder
                .define(
                        "affects_normal_vampires",
                        true
                );

        WOLF_BITE_AFFECTS_AUGUSTINE = builder
                .define(
                        "affects_augustine_vampires",
                        true
                );

        WOLF_BITE_AFFECTS_ORIGINAL = builder
                .comment(
                        "Whether Original vampires suffer the werewolf venom symptoms. This will not kill them."
                )
                .define(
                        "affects_original_vampires",
                        true
                );

        WOLF_BITE_KILLS_NORMAL = builder
                .define(
                        "kills_normal_vampires",
                        true
                );

        WOLF_BITE_KILLS_AUGUSTINE = builder
                .define(
                        "kills_augustine_vampires",
                        true
                );


        /*
         * These values are percentages of the total infection duration.
         *
         * Example:
         * 0.50 = halfway through the infection.
         *
         * For example, changing the total duration from 30 to 20 minutes
         * automatically keeps the progression scaled. Magic!
         */

        WOLF_BITE_NAUSEA_START = builder
                .comment("Progress at which nausea can begin. 0.0 - 1.0.")
                .defineInRange(
                        "nausea_start",
                        0.17D,
                        0D,
                        1D
                );

        WOLF_BITE_WEAKNESS_TWO_START = builder
                .defineInRange(
                        "weakness_two_start",
                        0.40D,
                        0D,
                        1D
                );

        WOLF_BITE_SLOWNESS_START = builder
                .defineInRange(
                        "slowness_start",
                        0.40D,
                        0D,
                        1D
                );

        WOLF_BITE_DAMAGE_START = builder
                .defineInRange(
                        "damage_start",
                        0.67D,
                        0D,
                        1D
                );

        WOLF_BITE_SEVERE_START = builder
                .defineInRange(
                        "severe_start",
                        0.80D,
                        0D,
                        1D
                );

        builder.pop();


        // =====================================================
        // SPECIES RULES
        // =====================================================

        builder.push("species_rules");

        HYBRID_SUN_IMMUNE =
                builder.define(
                        "hybrid_sun_immune",
                        true
                );

        ORIGINAL_SUN_IMMUNE =
                builder.comment(
                        "Original vampires use normal vampire sunlight damage by default.",
                        "They may be incapacitated by sunlight, but will not permanently die."
                )
                .define(
                        "original_vampire_sun_immune",
                        false
                );

        ORIGINAL_HYBRID_SUN_IMMUNE =
                builder.define(
                        "original_hybrid_sun_immune",
                        true
                );

        HYBRID_WOLF_BITE_IMMUNE =
                builder.define(
                        "hybrid_wolf_bite_immune",
                        true
                );

        ORIGINAL_HYBRID_WOLF_BITE_IMMUNE =
                builder.define(
                        "original_hybrid_wolf_bite_immune",
                        true
                );

        builder.pop();


        // =====================================================
        // FIRE VULNERABILITY
        // =====================================================

        builder.push("fire_vulnerability");

        AUGUSTINE_FIRE_VULNERABILITY = builder
                .define("augustineFireVulnerability", true);

        ORIGINAL_VAMPIRE_FIRE_VULNERABILITY = builder
                .define("originalVampireFireVulnerability", true);

        HYBRID_FIRE_VULNERABILITY = builder
                .define("hybridFireVulnerability", false);

        ORIGINAL_HYBRID_FIRE_VULNERABILITY = builder
                .define("originalHybridFireVulnerability", false);

        builder.pop();


        // =====================================================
        // TRANSFORMATION
        // =====================================================

        builder.push("transformation");
        AUGUSTINE_TRANSFORMATION_SECONDS = builder.defineInRange(
                "augustine_transformation_seconds", 60, 1, 3600);

        HYBRID_BLOOD_EFFECT_MINUTES = builder
                .defineInRange(
                        "hybrid_blood_effect_minutes",
                        12,
                        1,
                        120
                );

        ORIGINAL_CURSE_DURATION_MINUTES = builder
                .defineInRange(
                        "original_vampire_curse_minutes",
                        12,
                        1,
                        120
                );

        ORIGINAL_HYBRID_CURSE_DURATION_MINUTES = builder
                .defineInRange(
                        "original_hybrid_curse_minutes",
                        12,
                        1,
                        120
                );

        ORIGINAL_DBNO_SECONDS = builder
                .comment("Incapacitation after lethal damage. Online time until the Original may wake.")
                .defineInRange("original_vampire_dbno_seconds", 480, 1, 86400);
        ORIGINAL_HYBRID_DBNO_SECONDS = builder
                .comment("Incapacitation after lethal damage. Online time until the Original Hybrid may wake.")
                .defineInRange("original_hybrid_dbno_seconds", 480, 1, 86400);

        ORIGINAL_AWAKENING_DBNO_SECONDS = builder
                .defineInRange(
                        "original_vampire_awakening_dbno_seconds",
                        30,
                        1,
                        600
                );

        ORIGINAL_HYBRID_AWAKENING_DBNO_SECONDS = builder
                .defineInRange(
                        "original_hybrid_awakening_dbno_seconds",
                        30,
                        1,
                        600
                );

        builder.pop();


        // =====================================================
        // PROGRESSION
        // =====================================================

        builder.push("progression");

        AUGUSTINE_MAX_POTENCY = builder
                .defineInRange(
                        "augustine_max_potency",
                        5,
                        1,
                        20
                );

        HYBRID_MAX_POTENCY = builder
                .defineInRange(
                        "hybrid_max_potency",
                        5,
                        1,
                        20
                );

        ORIGINAL_MAX_MASTERY = builder
                .defineInRange(
                        "original_max_mastery",
                        5,
                        1,
                        20
                );

        builder.push("skill_points");

        AUGUSTINE_SKILL_POINTS = new SkillPointProfile(
                builder,
                "augustine",
                2,
                2,
                "Augustine vampires use the Vampire tree plus augustine mastery skills."
        );

        HYBRID_SKILL_POINTS = new SkillPointProfile(
                builder,
                "hybrid",
                2,
                3,
                "Hybrids spend one shared pool across the full Vampire and Werewolf trees."
        );

        ORIGINAL_VAMPIRE_SKILL_POINTS = new SkillPointProfile(
                builder,
                "original_vampire",
                6,
                2,
                "Original Vampires use the Vampire tree plus Original skills."
        );

        ORIGINAL_HYBRID_SKILL_POINTS = new SkillPointProfile(
                builder,
                "original_hybrid",
                4,
                3,
                "Original Hybrids spend one shared pool across both full stock trees and mastery skills."
        );

        builder.pop();

        AUGUSTINE_SKILL_POINTS_PER_LEVEL = AUGUSTINE_SKILL_POINTS.skillPointsPerLevel;
        HYBRID_SKILL_POINTS_PER_LEVEL = HYBRID_SKILL_POINTS.skillPointsPerLevel;
        ORIGINAL_VAMPIRE_SKILL_POINTS_PER_LEVEL = ORIGINAL_VAMPIRE_SKILL_POINTS.skillPointsPerLevel;
        ORIGINAL_HYBRID_SKILL_POINTS_PER_LEVEL = ORIGINAL_HYBRID_SKILL_POINTS.skillPointsPerLevel;

        CUSTOM_SPECIES_CAN_GAIN_LORD_LEVELS = builder
                .comment(
                        "Allows Augustine vampires, hybrids and originals",
                        "to access Vampirism lord levels. Changing this might break the mod.",
                        "Default: false"
                )
                .define(
                        "custom_species_can_gain_lord_levels",
                        false
                );

        builder.pop();


        // =====================================================
        // AUGUSTINE
        // =====================================================

        builder.push("augustine");

        AUGUSTINE_VAMPIRE_BOTTLE_GAIN = builder.defineInRange("vampire_bottle_gain", 8, 0, 100);
        AUGUSTINE_FEEDING_CAP = builder.comment(
                        "Maximum health drained from a vampire player per Augustine feeding sequence.",
                        "The same value remains the blood-gain cap for other Augustine feeding targets.")
                .defineInRange("feeding_cap", 60, 1, 10000);
        AUGUSTINE_FEEDING_FINISH_DAMAGE = builder.defineInRange("feeding_finish_damage", 6D, 0D, 1000D);

        AUGUSTINE_HUMAN_BLOOD_MULTIPLIER = builder
                .comment("Multiplier of augustine feeding; 1/3 grants blood on every third successful pulse.")
                .defineInRange(
                        "human_blood_multiplier",
                        1.0D / 3.0D,
                        0D,
                        100D
                );

        AUGUSTINE_VAMPIRE_BLOOD_MULTIPLIER = builder
                .comment("Gain per base vampire-feeding pulse, independent of the victim drain multiplier.")
                .defineInRange(
                        "vampire_blood_multiplier",
                        3.0D,
                        0D,
                        100D
                );

        AUGUSTINE_BLOOD_USAGE_MULTIPLIER = builder
                .comment(
                        "Multiplier relative to normal vampire blood consumption.",
                        "Values below 1 mean lower consumption, values above 1 mean higher consumption. Example: 0.5 = 50%"
                )
                .defineInRange(
                        "blood_usage_multiplier",
                        1.250D,
                        0D,
                        100D
                );

        AUGUSTINE_FEED_DAMAGE_MULTIPLIER = builder
                .defineInRange(
                        "feeding_damage_multiplier",
                        2.0D,
                        0D,
                        100D
                );

        AUGUSTINE_FEED_AMOUNT_MULTIPLIER = builder
                .comment("Blood removed from vampire victims per base pulse; does not multiply the feeder's gain.")
                .defineInRange(
                        "feeding_amount_multiplier",
                        2.0D,
                        0D,
                        100D
                );

        builder.pop();


        // =====================================================
        // LOOT
        // =====================================================

        builder.push("loot");

        AUGUSTINE_SERUM_LOOT_CHANCE = builder
                .comment(
                        "Chance for Augustine Serum in eligible Hunter loot.",
                        "0.0075 = 0.75%."
                )
                .defineInRange(
                        "augustine_serum_chance",
                        0.0075D,
                        0D,
                        1D
                );

        WHITE_OAK_STAKE_LOOT_ENABLED = builder.define("whiteOakStakeLootEnabled", true);
        WHITE_OAK_STAKE_LOOT_CHANCE = builder
                .comment("Chance per Woodland Mansion loot generation. 0.007 = 0.7%.")
                .defineInRange("whiteOakStakeLootChance", 0.007D, 0D, 1D);

        ELDER_DAGGER_LOOT_ENABLED = builder.define("elderDaggerLootEnabled", true);
        ELDER_DAGGER_LOOT_CHANCE = builder
                .comment("Chance per Buried Treasure loot generation. 0.006 = 0.6%.")
                .defineInRange("elderDaggerLootChance", 0.006D, 0D, 1D);

        CURSED_ELDER_DAGGER_LOOT_ENABLED = builder.define("cursedElderDaggerLootEnabled", true);
        CURSED_ELDER_DAGGER_LOOT_CHANCE = builder
                .comment("Chance per Ancient City loot generation. 0.006 = 0.6%.")
                .defineInRange("cursedElderDaggerLootChance", 0.006D, 0D, 1D);

        VAMPIRISM_CURE_LOOT_ENABLED = builder.define("vampirismCureLootEnabled", true);
        VAMPIRISM_CURE_LOOT_CHANCE = builder
                .comment("Chance per Ominous Vault reward generation. 0.02 = 2%.")
                .defineInRange("vampirismCureLootChance", 0.02D, 0D, 1D);

        AUGUSTINE_SYRINGE_LOOT_ENABLED = builder.define("augustineSyringeLootEnabled", true);
        AUGUSTINE_SYRINGE_LOOT_CHANCE = builder
                .comment("Chance per Vampirism Hunter Outpost tent-chest loot generation. 0.05 = 5%.")
                .defineInRange("augustineSyringeLootChance", 0.05D, 0D, 1D);

        ORIGINAL_HYBRID_BLOOD_LOOT_ENABLED = builder.define("originalHybridBloodLootEnabled", false);
        ORIGINAL_HYBRID_BLOOD_LOOT_CHANCE = builder
                .comment("Chance per Woodland Mansion loot generation. Disabled by default. 0.02 = 2%.")
                .defineInRange("originalHybridBloodLootChance", 0.02D, 0D, 1D);

        IMMORTALITY_SERUM_LOOT_ENABLED = builder.define("immortalitySerumLootEnabled", true);
        IMMORTALITY_SERUM_LOOT_CHANCE = builder
                .comment("Chance per Buried Treasure loot generation. 0.004 = 0.4%.")
                .defineInRange("immortalitySerumLootChance", 0.004D, 0D, 1D);

        DAYLIGHT_RING_LOOT_ENABLED = builder.define("daylightRingLootEnabled", true);
        DAYLIGHT_RING_LOOT_CHANCE = builder
                .comment("Chance per Woodland Mansion loot generation. 0.02 = 2%.")
                .defineInRange("daylightRingLootChance", 0.02D, 0D, 1D);

        builder.pop();


        SPEC = builder.build();
    }


    /**
     * Config-backed skill-point progression for one custom species.
     *
     * <p>The cumulative pool follows the Step-1 rule:
     * {@code startingSkillPoints + (level - 1) * skillPointsPerLevel}.
     * Fractional cumulative results are floored so the exposed pool is always an integer.</p>
     */
    public static final class SkillPointProfile {
        public final ModConfigSpec.IntValue startingSkillPoints;
        public final ModConfigSpec.DoubleValue skillPointsPerLevel;

        private SkillPointProfile(
                ModConfigSpec.Builder builder,
                String name,
                int startingPoints,
                double pointsPerLevel,
                String description
        ) {
            builder.push(name);

            startingSkillPoints = builder
                    .comment(
                            description,
                            "Skill points available at level 1."
                    )
                    .defineInRange(
                            "startingSkillPoints",
                            startingPoints,
                            0,
                            1000
                    );

            skillPointsPerLevel = builder
                    .comment(
                            "Additional skill points per level after level 1."
                    )
                    .defineInRange(
                            "skillPointsPerLevel",
                            pointsPerLevel,
                            0.0D,
                            100.0D
                    );

            builder.pop();
        }

        public int pointsAt(int level) {
            if (level <= 0) {
                return 0;
            }

            double total = startingSkillPoints.get()
                    + (level - 1) * skillPointsPerLevel.get();
            return Math.max(0, (int) Math.floor(total));
        }

        public int pointsForLevel(int level) {
            return pointsAt(level);
        }
    }


    /** Shared definition, instantiated in four independent TOML sections. */
    public static final class AttributeProfile {
        public final ModConfigSpec.DoubleValue healthMaxMod;
        public final ModConfigSpec.DoubleValue speedMaxMod;
        public final ModConfigSpec.DoubleValue attackSpeedMaxMod;
        public final ModConfigSpec.DoubleValue levelCurveExponent;
        public final ModConfigSpec.IntValue attackDamageTier1Level;
        public final ModConfigSpec.DoubleValue attackDamageTier1Bonus;
        public final ModConfigSpec.IntValue attackDamageTier2Level;
        public final ModConfigSpec.DoubleValue attackDamageTier2Bonus;
        public final ModConfigSpec.DoubleValue naturalArmorBase;
        public final ModConfigSpec.DoubleValue naturalArmorIncrease;
        public final ModConfigSpec.DoubleValue naturalArmorToughnessIncrease;
        public final ModConfigSpec.IntValue naturalArmorRegenDurationSeconds;
        public final ModConfigSpec.DoubleValue naturalArmorRegenLossFraction;
        public final ModConfigSpec.BooleanValue armorPenaltyEnabled;
        public final ModConfigSpec.DoubleValue armorPenaltyThreshold;
        public final ModConfigSpec.DoubleValue armorPenaltyMultiplier;
        public final ModConfigSpec.DoubleValue bloodExhaustionFactor;
        public final ModConfigSpec.DoubleValue exhaustionMaxMod;
        public final List<ModConfigSpec.IntValue> bloodCapacityLevels;
        public final List<ModConfigSpec.IntValue> bloodCapacities;

        private AttributeProfile(ModConfigSpec.Builder builder, String name,
                                 double health, double speed, double attackSpeed,
                                 double damage1, double damage2, double armorBase,
                                 double armorIncrease, double toughness, boolean penalty,
                                 double exhaustionFactor, List<Integer> capacities) {
            builder.push(name);
            healthMaxMod = builder.comment("Max-level flat HP bonus (ADD_VALUE), rounded to whole hearts like Vampirism.")
                    .defineInRange("healthMaxMod", health, 0.0, 1000.0);
            speedMaxMod = builder.comment("Max-level movement bonus (ADD_MULTIPLIED_BASE); 0.55 means +55% base speed.")
                    .defineInRange("speedMaxMod", speed, 0.0, 10.0);
            attackSpeedMaxMod = builder.comment("Max-level attack-speed bonus (ADD_MULTIPLIED_BASE); decimal fraction.")
                    .defineInRange("attackSpeedMaxMod", attackSpeed, 0.0, 10.0);
            levelCurveExponent = builder.comment("Exponent for health, speed and exhaustion; 0.5 matches Vampirism. Armor scales linearly.")
                    .defineInRange("levelCurveExponent", 0.5, 0.01, 10.0);
            attackDamageTier1Level = builder.comment("First level granting the flat tier-1 attack bonus.")
                    .defineInRange("attackDamageTier1Level", 7, 0, 1000);
            attackDamageTier1Bonus = builder.comment("Tier-1 flat attack damage (ADD_VALUE), not a percentage.")
                    .defineInRange("attackDamageTier1Bonus", damage1, 0.0, 1000.0);
            attackDamageTier2Level = builder.comment("First tier-2 level; values below tier 1 resolve to the tier-1 level.")
                    .defineInRange("attackDamageTier2Level", 14, 0, 1000);
            attackDamageTier2Bonus = builder.comment("Tier-2 flat attack damage (ADD_VALUE), replaces tier 1.")
                    .defineInRange("attackDamageTier2Bonus", damage2, 0.0, 1000.0);
            naturalArmorBase = builder.comment("Flat natural armor target for active species before linear level increase; equipment is subtracted.")
                    .defineInRange("naturalArmorBase", armorBase, 0.0, 1000.0);
            naturalArmorIncrease = builder.comment("Max-level addition to natural armor target; missing equipment contribution uses ADD_VALUE.")
                    .defineInRange("naturalArmorIncrease", armorIncrease, 0.0, 1000.0);
            naturalArmorToughnessIncrease = builder.comment("Max-level natural toughness target, linear scaling; missing equipment contribution uses ADD_VALUE.")
                    .defineInRange("naturalArmorToughnessIncrease", toughness, 0.0, 1000.0);
            naturalArmorRegenDurationSeconds = builder.comment("Duration denominator when Vampirism's armor_regeneration effect is present; does not apply the effect.")
                    .defineInRange("naturalArmorRegenDurationSeconds", 240, 1, 2400);
            naturalArmorRegenLossFraction = builder.comment("Decimal armor-target reduction at full armor_regeneration duration; toughness is unaffected.")
                    .defineInRange("naturalArmorRegenLossFraction", 0.75, 0.0, 1.0);
            armorPenaltyEnabled = builder.comment("Reduce only species movement and attack-speed bonuses when equipment armor exceeds the threshold.")
                    .define("armorPenaltyEnabled", penalty);
            armorPenaltyThreshold = builder.comment("Flat recognized equipment armor threshold; penalty uses strictly greater than.")
                    .defineInRange("armorPenaltyThreshold", 7.0, 0.0, 1000.0);
            armorPenaltyMultiplier = builder.comment("Remaining decimal fraction of species speed bonuses while penalized; 0.5 keeps half.")
                    .defineInRange("armorPenaltyMultiplier", 0.5, 0.0, 1.0);
            bloodExhaustionFactor = builder.comment("Baseline exhaustion factor supplied by removable ADD_VALUE compensation; lower consumes less blood.")
                    .defineInRange("bloodExhaustionFactor", exhaustionFactor, 0.0, 10.0);
            exhaustionMaxMod = builder.comment("Max-level BLOOD_EXHAUSTION ADD_MULTIPLIED_BASE bonus; 1.0 doubles the baseline factor.")
                    .defineInRange("exhaustionMaxMod", 1.0, 0.0, 10.0);

            List<Integer> levels = List.of(1, 4, 7, 10, 14);
            var levelValues = new ArrayList<ModConfigSpec.IntValue>();
            var capacityValues = new ArrayList<ModConfigSpec.IntValue>();
            for (int i = 0; i < levels.size(); i++) {
                int tier = i + 1;
                levelValues.add(builder.comment("First level for blood-capacity tier " + tier
                                + "; lower-than-previous thresholds resolve to the previous threshold. Tier 1 is the fallback.")
                        .defineInRange("bloodCapacityTier" + tier + "Level", levels.get(i), 0, 1000));
                capacityValues.add(builder.comment("Flat maximum blood for tier " + tier + "; increasing capacity does not refill blood.")
                        .defineInRange("bloodCapacityTier" + tier, capacities.get(i), 1, 40));
            }
            bloodCapacityLevels = List.copyOf(levelValues);
            bloodCapacities = List.copyOf(capacityValues);
            builder.pop();
        }
    }

    private ServerConfig() {
    }
}
