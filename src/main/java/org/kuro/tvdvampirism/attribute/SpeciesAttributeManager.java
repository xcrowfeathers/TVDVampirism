package org.kuro.tvdvampirism.attribute;

import de.teamlapen.vampirism.core.ModAttributes;
import de.teamlapen.vampirism.core.ModEffects;
import de.teamlapen.vampirism.config.VampirismConfig;
import de.teamlapen.vampirism.entity.factions.FactionPlayerHandler;
import de.teamlapen.vampirism.util.ArmorModifier;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import org.kuro.tvdvampirism.Tvdvampirism;
import org.kuro.tvdvampirism.config.ServerConfig;
import org.kuro.tvdvampirism.faction.VampireFamily;
import org.kuro.tvdvampirism.faction.player.CustomFactionPlayer;
import org.kuro.tvdvampirism.skill.SpeciesSkillProfile;

/** Owns only the species level layer. Other mods and future progression layers keep their modifiers */
public final class SpeciesAttributeManager {
    public static final ResourceLocation MAX_HEALTH = id("species_max_health");
    public static final ResourceLocation MOVEMENT_SPEED = id("species_movement_speed");
    public static final ResourceLocation ATTACK_SPEED = id("species_attack_speed");
    public static final ResourceLocation ATTACK_DAMAGE = id("species_attack_damage");
    public static final ResourceLocation BLOOD_EXHAUSTION_BASE = id("species_blood_exhaustion_base");
    public static final ResourceLocation BLOOD_EXHAUSTION = id("species_blood_exhaustion");
    public static final ResourceLocation NATURAL_ARMOR = id("species_natural_armor");
    public static final ResourceLocation NATURAL_TOUGHNESS = id("species_natural_toughness");

    private SpeciesAttributeManager() {
    }

    public static void refresh(CustomFactionPlayer<?> custom) {
        refresh(custom, custom.getLevel());
    }

    /** Use the supplied level during faction deserialization, before its attachment is fully installed. */
    public static void refresh(CustomFactionPlayer<?> custom, int level) {
        if (custom.isRemote()) {
            return;
        }
        Player player = custom.asEntity();
        if (level <= 0) {
            var handler = FactionPlayerHandler.get(player);
            boolean replacingCustomProfile = handler.getCurrentLevel() > 0
                    && VampireFamily.isTechnicalSpecies(handler.getCurrentFaction());
            // The new custom faction applies immediately after this callback. Clamp its final maximum,
            // not the temporary human maximum between two profiles.
            clear(player, !replacingCustomProfile);
            return;
        }

        SpeciesAttributeProfile profile = SpeciesAttributes.getProfile(custom);
        int maxLevel = custom.getMaxLevel();
        level = Math.min(level, maxLevel);
        double scale = levelScale(level, maxLevel, profile.levelCurveExponent());
        replace(player, Attributes.MAX_HEALTH, MAX_HEALTH,
                Math.round(profile.healthMaxMod() * scale / 2.0) * 2.0, Operation.ADD_VALUE);
        replace(player, Attributes.ATTACK_DAMAGE, ATTACK_DAMAGE,
                attackDamage(profile, level), Operation.ADD_VALUE);

        AttributeInstance exhaustion = player.getAttribute(ModAttributes.BLOOD_EXHAUSTION);
        if (exhaustion != null) {
            // Vampirism leaves base 0 when leaving its faction, while fresh players start at 1.
            // ADD_VALUE participates in the base used by ADD_MULTIPLIED_BASE in Minecraft 1.21.1.
            replace(player, ModAttributes.BLOOD_EXHAUSTION, BLOOD_EXHAUSTION_BASE,
                    profile.bloodExhaustionFactor() - exhaustion.getBaseValue(), Operation.ADD_VALUE);
            replace(player, ModAttributes.BLOOD_EXHAUSTION, BLOOD_EXHAUSTION,
                    profile.exhaustionMaxMod() * scale, Operation.ADD_MULTIPLIED_BASE);
        }

        applySpeedBonuses(player, profile, scale);
        setDbnoBase(custom);
        updateNaturalArmor(player, profile, level, maxLevel);
        custom.getBloodData().setMaxBlood(bloodCapacity(profile, level));
        custom.syncBlood(false);
        clampHealth(player);
    }

    public static void clear(Player player) {
        clear(player, true);
    }

    private static void clear(Player player, boolean clamp) {
        if (player.level().isClientSide()) {
            return;
        }
        boolean hadHealthModifier = modifierAmount(player, Attributes.MAX_HEALTH, MAX_HEALTH) != 0.0;
        remove(player, Attributes.MAX_HEALTH, MAX_HEALTH);
        remove(player, Attributes.MOVEMENT_SPEED, MOVEMENT_SPEED);
        remove(player, Attributes.ATTACK_SPEED, ATTACK_SPEED);
        remove(player, Attributes.ATTACK_DAMAGE, ATTACK_DAMAGE);
        remove(player, ModAttributes.BLOOD_EXHAUSTION, BLOOD_EXHAUSTION_BASE);
        remove(player, ModAttributes.BLOOD_EXHAUSTION, BLOOD_EXHAUSTION);
        remove(player, Attributes.ARMOR, NATURAL_ARMOR);
        remove(player, Attributes.ARMOR_TOUGHNESS, NATURAL_TOUGHNESS);
        AttributeInstance dbno = player.getAttribute(ModAttributes.DBNO_DURATION);
        if (dbno != null) {
            dbno.setBaseValue(0.0);
        }
        AttributeInstance neonatal = player.getAttribute(ModAttributes.NEONATAL_DURATION);
        if (neonatal != null) {
            neonatal.setBaseValue(0.0);
        }
        if (clamp && hadHealthModifier) {
            clampHealth(player);
        }
    }

    /** Called after equipment changes have actually been applied by LivingEntity. */
    public static void updateEquipment(CustomFactionPlayer<?> custom) {
        if (custom.isRemote() || custom.getLevel() <= 0) {
            return;
        }
        SpeciesAttributeProfile profile = SpeciesAttributes.getProfile(custom);
        applySpeedBonuses(custom.asEntity(), profile,
                levelScale(custom.getLevel(), custom.getMaxLevel(), profile.levelCurveExponent()));
        updateNaturalArmor(custom.asEntity(), profile, custom.getLevel(), custom.getMaxLevel());
    }

    /** The periodic path only updates armor and toughness, never the complete profile. */
    public static void updateNaturalArmor(CustomFactionPlayer<?> custom) {
        if (!custom.isRemote() && custom.getLevel() > 0) {
            updateNaturalArmor(custom.asEntity(), SpeciesAttributes.getProfile(custom),
                    custom.getLevel(), custom.getMaxLevel());
        }
    }

    private static void updateNaturalArmor(Player player, SpeciesAttributeProfile profile, int level, int maxLevel) {
        replace(player, Attributes.ARMOR, NATURAL_ARMOR,
                Math.max(0.0, naturalArmorTarget(player, profile, level, maxLevel)
                        - equipmentValue(player, Attributes.ARMOR)), Operation.ADD_VALUE);
        replace(player, Attributes.ARMOR_TOUGHNESS, NATURAL_TOUGHNESS,
                Math.max(0.0, naturalToughnessTarget(profile, level, maxLevel)
                        - equipmentValue(player, Attributes.ARMOR_TOUGHNESS)), Operation.ADD_VALUE);
    }

    public static double naturalArmorTarget(Player player, SpeciesAttributeProfile profile, int level, int maxLevel) {
        if (level <= 0) {
            return 0.0;
        }
        double target = profile.naturalArmorBase()
                + profile.naturalArmorIncrease() * levelScale(level, maxLevel, 1.0);
        var regeneration = player.getEffect(ModEffects.ARMOR_REGENERATION);
        if (regeneration != null) {
            double remaining = Math.clamp(regeneration.getDuration()
                    / (profile.naturalArmorRegenDurationSeconds() * 20.0), 0.0, 1.0);
            target *= 1.0 - profile.naturalArmorRegenLossFraction() * remaining;
        }
        return target;
    }

    public static double naturalToughnessTarget(SpeciesAttributeProfile profile, int level, int maxLevel) {
        return profile.naturalArmorToughnessIncrease() * levelScale(level, maxLevel, 1.0);
    }

    public static double levelScale(int level, int maxLevel, double exponent) {
        if (level <= 0 || maxLevel <= 0) {
            return 0.0;
        }
        return Math.pow(Math.min(level, maxLevel) / (double) maxLevel, exponent);
    }

    public static double attackDamage(SpeciesAttributeProfile profile, int level) {
        if (level <= 0 || level < profile.attackDamageTier1Level()) {
            return 0.0;
        }
        return level >= profile.attackDamageTier2Level()
                ? profile.attackDamageTier2Bonus() : profile.attackDamageTier1Bonus();
    }

    public static int bloodCapacity(SpeciesAttributeProfile profile, int level) {
        int capacity = profile.bloodCapacityTiers().getFirst().capacity();
        for (var tier : profile.bloodCapacityTiers()) {
            if (level < tier.level()) {
                break;
            }
            capacity = tier.capacity();
        }
        return capacity;
    }

    public static boolean hasHeavyArmorPenalty(Player player, SpeciesAttributeProfile profile) {
        return profile.armorPenaltyEnabled()
                && equipmentValue(player, Attributes.ARMOR) > profile.armorPenaltyThreshold();
    }

    /** Same ADD_VALUE/armor-ID filter as Vampirism, using the public modifier collection. */
    public static double equipmentValue(Player player, Holder<Attribute> attribute) {
        AttributeInstance instance = player.getAttribute(attribute);
        if (instance == null) {
            return 0.0;
        }
        return instance.getModifiers().stream()
                .filter(modifier -> modifier.operation() == Operation.ADD_VALUE)
                .filter(modifier -> ArmorModifier.ARMOR_IDS.contains(modifier.id()))
                .mapToDouble(AttributeModifier::amount)
                .sum();
    }

    private static void applySpeedBonuses(Player player, SpeciesAttributeProfile profile, double scale) {
        double penalty = hasHeavyArmorPenalty(player, profile) ? profile.armorPenaltyMultiplier() : 1.0;
        replace(player, Attributes.MOVEMENT_SPEED, MOVEMENT_SPEED,
                profile.speedMaxMod() * scale * penalty, Operation.ADD_MULTIPLIED_BASE);
        replace(player, Attributes.ATTACK_SPEED, ATTACK_SPEED,
                profile.attackSpeedMaxMod() * scale * penalty, Operation.ADD_MULTIPLIED_BASE);
    }

    /** Supplies the stock attribute base so its Swift Resurrection modifier remains authoritative. */
    private static void setDbnoBase(CustomFactionPlayer<?> custom) {
        Player player = custom.asEntity();
        AttributeInstance instance = player.getAttribute(ModAttributes.DBNO_DURATION);

        int seconds = switch (custom.getSkillProfile()) {
            case ORIGINAL_VAMPIRE -> ServerConfig.ORIGINAL_DBNO_SECONDS.get();
            case ORIGINAL_HYBRID -> ServerConfig.ORIGINAL_HYBRID_DBNO_SECONDS.get();
            default -> VampirismConfig.BALANCE.vpDbnoDuration.get();
        };
        if (instance != null) instance.setBaseValue(seconds * 20.0);

        AttributeInstance neonatal = player.getAttribute(ModAttributes.NEONATAL_DURATION);
        if (neonatal != null) {
            boolean usesStockDbno = switch (custom.getSkillProfile()) {
                case AUGUSTINE, HYBRID -> true;
                case ORIGINAL_VAMPIRE, ORIGINAL_HYBRID -> false;
            };
            neonatal.setBaseValue(usesStockDbno
                    ? VampirismConfig.BALANCE.vpNeonatalDuration.get() * 20.0
                    : 0.0);
        }
    }

    public static double modifierAmount(Player player, Holder<Attribute> attribute, ResourceLocation id) {
        AttributeInstance instance = player.getAttribute(attribute);
        AttributeModifier modifier = instance == null ? null : instance.getModifier(id);
        return modifier == null ? 0.0 : modifier.amount();
    }

    private static void replace(Player player, Holder<Attribute> attribute, ResourceLocation id,
                                double amount, Operation operation) {
        AttributeInstance instance = player.getAttribute(attribute);
        if (instance == null) {
            return;
        }
        AttributeModifier previous = instance.getModifier(id);
        if (previous != null && previous.amount() == amount && previous.operation() == operation) {
            return;
        }
        instance.removeModifier(id);
        if (amount != 0.0) {
            instance.addTransientModifier(new AttributeModifier(id, amount, operation));
        }
    }

    private static void remove(Player player, Holder<Attribute> attribute, ResourceLocation id) {
        AttributeInstance instance = player.getAttribute(attribute);
        if (instance != null) {
            instance.removeModifier(id);
        }
    }

    private static void clampHealth(Player player) {
        if (player.getHealth() > player.getMaxHealth()) {
            player.setHealth(player.getMaxHealth());
        }
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(Tvdvampirism.MODID, path);
    }
}
