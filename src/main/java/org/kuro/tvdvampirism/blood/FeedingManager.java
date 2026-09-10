package org.kuro.tvdvampirism.blood;

import de.teamlapen.lib.lib.util.UtilLib;
import de.teamlapen.vampirism.api.EnumStrength;
import de.teamlapen.vampirism.api.entity.IBiteableEntity;
import de.teamlapen.vampirism.api.entity.factions.IFaction;
import de.teamlapen.vampirism.api.entity.hunter.IHunterMob;
import de.teamlapen.vampirism.api.entity.player.vampire.IDrinkBloodContext;
import de.teamlapen.vampirism.api.entity.vampire.IVampire;
import de.teamlapen.vampirism.core.ModParticles;
import de.teamlapen.vampirism.entity.ExtendedCreature;
import de.teamlapen.vampirism.entity.player.vampire.VampirePlayer;
import de.teamlapen.vampirism.entity.vampire.DrinkBloodContext;
import de.teamlapen.vampirism.items.HunterArmorItem;
import de.teamlapen.vampirism.particle.FlyingBloodEntityParticleOptions;
import de.teamlapen.vampirism.util.DamageHandler;
import de.teamlapen.vampirism.util.Permissions;
import de.teamlapen.vampirism.world.ModDamageSources;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.village.ReputationEventType;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.level.LevelAccessor;
import org.jetbrains.annotations.NotNull;
import org.kuro.tvdvampirism.config.ServerConfig;
import org.kuro.tvdvampirism.faction.player.CustomFactionPlayer;
import org.kuro.tvdvampirism.player.CustomDamageSounds;

import java.util.Optional;

public final class FeedingManager {

    private static final float PLAYER_BLOOD_FRACTION = 0.20F;
    private static final ResourceLocation FEEDING_RESISTANCE = ResourceLocation.fromNamespaceAndPath("tvdvampirism", "feeding_resistance");

    private FeedingManager() {
    }

    public static void startFeeding(ServerPlayer player, int entityId) {
        BloodManager.getCustomVampire(player).ifPresent(customPlayer -> {
            if (isForcedFeeding(customPlayer)) return;
            LivingEntity target = resolveValidTarget(customPlayer, entityId);

            if (target == null) {
                customPlayer.stopFeeding(true);
                return;
            }

            if (ExtendedCreature.getSafe(target)
                    .map(ExtendedCreature::hasPoisonousBlood)
                    .orElse(false)) {
                player.addEffect(new MobEffectInstance(MobEffects.POISON, 60));
                customPlayer.stopFeeding(true);
                return;
            }

            customPlayer.startFeeding(target.getId());
            holdStill(player, target);
        });
    }

    public static void stopFeeding(ServerPlayer player) {
        BloodManager.getCustomVampire(player)
                .ifPresent(customPlayer -> {
                    if (!isForcedFeeding(customPlayer)) customPlayer.stopFeeding(true);
                });
    }

    public static boolean isForcedFeeding(CustomFactionPlayer<?> feeder) {
        Entity target = feeder.asEntity().level().getEntity(feeder.getFeedingTargetId());
        return target instanceof LivingEntity living
                && feeder.matchesFeedingTarget(target)
                && AugustineFeedingPolicy.isVampire(living)
                && !AugustineFeedingPolicy.canControlVampireFeeding(feeder.asEntity())
                && isStillValid(feeder, living);
    }

    public static void tick(CustomFactionPlayer<?> customPlayer) {
        int targetId = customPlayer.getFeedingTargetId();

        if (targetId < 0 || customPlayer.isRemote()) {
            return;
        }

        Entity entity = customPlayer.asEntity().level().getEntity(targetId);
        if (!(entity instanceof LivingEntity target)
                || !customPlayer.matchesFeedingTarget(entity)
                || !isStillValid(customPlayer, target)) {
            customPlayer.stopFeeding(true);
            return;
        }

        holdStill(customPlayer.asEntity(), target);

        if (!customPlayer.advanceFeedingTick(
                ServerConfig.CUSTOM_VAMPIRE_FEEDING_INTERVAL_TICKS.get())) {
            return;
        }

        spawnBloodParticles(customPlayer.asEntity(), target);

        int amount = removeVictimBlood(customPlayer, target);
        if (amount <= 0) {
            if (!isForcedFeeding(customPlayer)) customPlayer.stopFeeding(true);
            return;
        }

        customPlayer.drinkBlood(
                amount,
                getBloodSaturation(target),
                new DrinkBloodContext(target)
        );
        customPlayer.syncBlood(false);

        boolean drainsVampirePlayerLife = target instanceof Player
                && AugustineFeedingPolicy.isVampire(target)
                && AugustineFeedingPolicy.isAugustine(customPlayer.asEntity());
        if (drainsVampirePlayerLife && customPlayer.reachedFeedingLifeDrainCap()) {
            customPlayer.stopFeeding(true);
        } else if (!drainsVampirePlayerLife
                && AugustineFeedingPolicy.isAugustine(customPlayer.asEntity())
                && customPlayer.reachedFeedingCap()) {
            customPlayer.stopFeeding(true);
            if (target.isAlive()) {
                // The final bite is additional to this pulse, not swallowed by its hurt cooldown.
                target.invulnerableTime = 0;
                hurtFeedingTarget(target, ServerConfig.AUGUSTINE_FEEDING_FINISH_DAMAGE.get().floatValue());
            }
        } else if (shouldStopForAdvancedBiter(customPlayer, target)) {
            customPlayer.stopFeeding(true);
        } else if (!target.isAlive()) {
            customPlayer.stopFeeding(true);
        }
    }

    public static Optional<HudTargetInfo> getHudTargetInfo(
            CustomFactionPlayer<?> feeder,
            LivingEntity target
    ) {
        if (!target.isAlive() || !AugustineFeedingPolicy.canFeedOn(feeder.asEntity(), target)) {
            return Optional.empty();
        }

        if (AugustineFeedingPolicy.isVampire(target)) {
            float relative = target instanceof Player player
                    ? player.getHealth() / player.getMaxHealth()
                    : ExtendedCreature.getSafe(target).filter(c -> c.getMaxBlood() > 0)
                    .map(ExtendedCreature::getBloodLevelRelative)
                    .orElse(target.getHealth() / target.getMaxHealth());
            return Optional.of(new HudTargetInfo(Mth.clamp(relative, 0, 1), false));
        }

        FeedingVampire vampire = new FeedingVampire(feeder);
        float relative;

        if (target instanceof Player player) {
            Optional<BloodData> customBlood = BloodManager.getBloodData(player);

            if (customBlood.isPresent()) {
                BloodData blood = customBlood.get();

                if (player.isCreative()
                        || player.isSpectator()
                        || blood.getBloodLevel() <= 0) {
                    return Optional.empty();
                }

                relative = blood.getBloodLevel() / (float) blood.getMaxBlood();
            } else {
                VampirePlayer vampirePlayer = VampirePlayer.get(player);

                if (!vampirePlayer.canBeBitten(vampire)) {
                    return Optional.empty();
                }

                relative = vampirePlayer.getBloodLevelRelative();
            }
        } else if (target instanceof IBiteableEntity biteable) {
            if (!biteable.canBeBitten(vampire)) {
                return Optional.empty();
            }

            relative = biteable.getBloodLevelRelative();
        } else {
            Optional<ExtendedCreature> creature = ExtendedCreature.getSafe(target);

            if (creature.isEmpty() || !creature.get().canBeBitten(vampire)) {
                return Optional.empty();
            }

            relative = creature.get().getBloodLevelRelative();
        }

        boolean poisonous = target instanceof IHunterMob
                || ExtendedCreature.getSafe(target)
                .map(ExtendedCreature::hasPoisonousBlood)
                .orElse(false);

        return Optional.of(new HudTargetInfo(
                Mth.clamp(relative, 0.0F, 1.0F),
                poisonous
        ));
    }

    private static LivingEntity resolveValidTarget(
            CustomFactionPlayer<?> customPlayer,
            int entityId
    ) {
        Player player = customPlayer.asEntity();

        if (!(player instanceof ServerPlayer serverPlayer)
                || customPlayer.getLevel() <= 0
                || player.isSpectator()
                || Permissions.FEED.isDisallowed(serverPlayer)) {
            return null;
        }

        Entity entity = player.level().getEntity(entityId);

        if (!(entity instanceof LivingEntity target)
                || target == player
                || !isStillValid(customPlayer, target)
                || !canFeedFrom(customPlayer, target)) {
            return null;
        }

        return target;
    }

    private static boolean isStillValid(
            CustomFactionPlayer<?> customPlayer,
            LivingEntity target
    ) {
        Player player = customPlayer.asEntity();
        double reach = player.getAttributeValue(Attributes.ENTITY_INTERACTION_RANGE) + 1.0D;

        return player.isAlive()
                && customPlayer.getLevel() > 0
                && !org.kuro.tvdvampirism.compat.SpeciesCompatibility.rawVampire(player).isDBNO()
                && !player.isSpectator()
                && target.isAlive()
                && !target.isRemoved()
                && player.level() == target.level()
                && AugustineFeedingPolicy.canFeedOn(player, target)
                && (!(target instanceof Player victim)
                || (!victim.isCreative() && !victim.isSpectator()
                && !org.kuro.tvdvampirism.compat.SpeciesCompatibility.rawVampire(victim).isDBNO()))
                && target.distanceTo(player) <= reach;
    }

    private static boolean canFeedFrom(
            CustomFactionPlayer<?> customPlayer,
            LivingEntity target
    ) {
        Player player = customPlayer.asEntity();
        FeedingVampire vampire = new FeedingVampire(customPlayer);

        if (!AugustineFeedingPolicy.canFeedOn(player, target)) return false;

        if (target instanceof Player targetPlayer) {
            if (targetPlayer.getAbilities().instabuild
                    || !Permissions.isPvpEnabled(player)
                    || !(player instanceof ServerPlayer serverPlayer)
                    || Permissions.FEED_PLAYER.isDisallowed(serverPlayer)
                    || UtilLib.canReallySee(target, player, false)
                    || target.getItemBySlot(EquipmentSlot.CHEST).getItem()
                    instanceof HunterArmorItem) {
                return false;
            }

            if (AugustineFeedingPolicy.isAugustine(player)
                    && AugustineFeedingPolicy.isVampire(targetPlayer)) return true;

            return BloodManager.getBloodData(targetPlayer)
                    .map(data -> data.getBloodLevel() > 0)
                    .orElseGet(() -> VampirePlayer.get(targetPlayer).canBeBitten(vampire));
        }

        if (AugustineFeedingPolicy.isVampire(target)) return true;

        if (target instanceof IBiteableEntity biteable) {
            return biteable.canBeBitten(vampire);
        }

        return ExtendedCreature.getSafe(target)
                .map(creature -> creature.canBeBitten(vampire))
                .orElse(false);
    }

    private static int removeVictimBlood(
            CustomFactionPlayer<?> customPlayer,
            LivingEntity target
    ) {
        if (AugustineFeedingPolicy.isVampire(target)
                && AugustineFeedingPolicy.isAugustine(customPlayer.asEntity())) {
            return removeVampireBlood(customPlayer, target);
        }
        if (target instanceof Player player) {
            return removePlayerBlood(player);
        }

        FeedingVampire vampire = new FeedingVampire(customPlayer);

        if (target instanceof IBiteableEntity biteable) {
            return biteable.onBite(vampire);
        }

        return ExtendedCreature.getSafe(target)
                .map(creature -> removeCreatureBlood(customPlayer, creature))
                .orElse(0);
    }

    private static int removeVampireBlood(CustomFactionPlayer<?> feeder, LivingEntity target) {
        if (target instanceof Player victim) {
            float damage = (float) (ServerConfig.CUSTOM_VAMPIRE_FEEDING_DAMAGE.get()
                    * ServerConfig.AUGUSTINE_FEED_DAMAGE_MULTIPLIER.get());
            damage = Math.min(damage, feeder.remainingFeedingLifeDrain());
            if (damage <= 0) return 0;
            target.setLastHurtByMob(feeder.asEntity());
            float removed = hurtFeedingTarget(target, damage);
            feeder.recordFeedingLifeDrain(removed);
            return removed > 0 ? Math.max(1, Mth.ceil(removed)) : 0;
        }
        double multiplier = ServerConfig.AUGUSTINE_FEED_AMOUNT_MULTIPLIER.get();
        if (multiplier <= 0) return 0;
        var creature = ExtendedCreature.getSafe(target).filter(c -> c.getMaxBlood() > 0);
        if (creature.isPresent()) {
            var blood = creature.get();
            if (blood.getBlood() <= 0) return 0;
            int base = Math.max(1, blood.getMaxBlood() / 6);
            int drain = Math.min(blood.getBlood(), (int) Math.ceil(base * multiplier));
            blood.setBlood(blood.getBlood() - drain);
            blood.sync();
            target.setLastHurtByMob(feeder.asEntity());
            if (blood.getBlood() == 0)
                hurtFeedingTarget(target, 1000.0F);
            return Math.min(base, (int) Math.ceil(drain / multiplier));
        }
        // Stock vampire mobs have no ExtendedCreature blood pool. Use actual
        // health removed as the available blood, never invent a second reserve.
        int base = ServerConfig.CUSTOM_VAMPIRE_FEEDING_AMOUNT.get();
        float before = target.getHealth();
        target.setLastHurtByMob(feeder.asEntity());
        hurtFeedingTarget(target,
                (float) Math.min(before, base * multiplier));
        float removed = Math.max(0, before - target.getHealth());
        return Math.min(base, (int) Math.ceil(removed / multiplier));
    }

    private static int removePlayerBlood(Player target) {
        Optional<CustomFactionPlayer<?>> customVictim =
                BloodManager.getCustomVampire(target);

        if (customVictim.isPresent()) {
            CustomFactionPlayer<?> victim = customVictim.get();
            BloodData blood = victim.getBloodData();

            int removed = (int) Math.ceil(
                    blood.getBloodLevel() * PLAYER_BLOOD_FRACTION
            );

            blood.removeBlood(removed, true);
            victim.syncBlood(false);
            return removed;
        }

        return VampirePlayer.get(target)
                .removeBlood(PLAYER_BLOOD_FRACTION);
    }

    private static int removeCreatureBlood(
            CustomFactionPlayer<?> customPlayer,
            ExtendedCreature creature
    ) {
        int current = creature.getBlood();

        if (current <= 0) {
            return 0;
        }

        int removed = Math.max(1, creature.getMaxBlood() / 6);
        removed = Math.min(removed, current);

        if (customPlayer.getVampireSkillBridge().isAdvancedBiter()
                && current > 1
                && current - removed <= 0) {
            removed = current - 1;
        }

        if (removed <= 0) {
            return 0;
        }

        creature.setBlood(current - removed);
        creature.sync();

        LivingEntity target = creature.getEntity();
        target.setLastHurtByMob(customPlayer.asEntity());

        if (creature.getBlood() == 0) {
            hurtFeedingTarget(target, 1000.0F);
        }

        int gained = removed;

        if (target instanceof AgeableMob ageable && ageable.getAge() < 0) {
            gained = Math.round(gained / 3.0F);
        }

        if (target instanceof Villager villager
                && !villager.isSleeping()
                && villager.level() instanceof ServerLevel serverLevel) {
            serverLevel.onReputationEvent(
                    ReputationEventType.VILLAGER_HURT,
                    customPlayer.asEntity(),
                    villager
            );
        }

        return gained;
    }

    private static boolean shouldStopForAdvancedBiter(
            CustomFactionPlayer<?> feeder,
            LivingEntity target
    ) {
        if (AugustineFeedingPolicy.isVampire(target)
                || !feeder.getVampireSkillBridge().isAdvancedBiter()) {
            return false;
        }

        return ExtendedCreature.getSafe(target)
                .map(creature -> creature.getBlood() == 1)
                .orElse(false);
    }

    private static float getBloodSaturation(LivingEntity target) {
        if (target instanceof IBiteableEntity biteable) {
            return biteable.getBloodSaturation();
        }

        return ExtendedCreature.getSafe(target)
                .map(ExtendedCreature::getBloodSaturation)
                .orElse(1.0F);
    }

    private static void holdStill(Player player, LivingEntity target) {
        var motion = target.getDeltaMovement();
        target.setDeltaMovement(0, Math.min(0, motion.y), 0);
        if (target instanceof net.minecraft.world.entity.Mob mob) mob.getNavigation().stop();
        refreshEffect(target, MobEffects.MOVEMENT_SLOWDOWN, 7);
        refreshEffect(target, MobEffects.WEAKNESS, 4);
        refreshEffect(player, MobEffects.MOVEMENT_SLOWDOWN, 5);
    }

    private static void refreshEffect(LivingEntity entity,
                                      net.minecraft.core.Holder<net.minecraft.world.effect.MobEffect> effect, int amplifier) {
        var current = entity.getEffect(effect);
        if (current == null || current.getDuration() <= 10 || current.getAmplifier() < amplifier)
            entity.addEffect(new MobEffectInstance(effect, 25, amplifier, false, false));
    }

    private static float hurtFeedingTarget(LivingEntity target, float damage) {
        if (damage <= 0) return 0;
        float healthBefore = target.getHealth();
        var resistance = target.getAttribute(Attributes.KNOCKBACK_RESISTANCE);
        boolean added = resistance != null && !resistance.hasModifier(FEEDING_RESISTANCE);
        if (added) resistance.addTransientModifier(new AttributeModifier(
                FEEDING_RESISTANCE, 1, AttributeModifier.Operation.ADD_VALUE));
        try {
            boolean vanillaWillDispatchSound = target.invulnerableTime <= 10;
            if (DamageHandler.hurtModded(target, ModDamageSources::noBlood, damage)) {
                if (!vanillaWillDispatchSound) {
                    DamageHandler.getDamageSource(target.level(), ModDamageSources::noBlood)
                            .ifPresent(source -> CustomDamageSounds.hurt(target, source));
                }
            }
        } finally {
            if (added) resistance.removeModifier(FEEDING_RESISTANCE);
        }
        return Math.max(0.0F, healthBefore - target.getHealth());
    }

    private static void spawnBloodParticles(Player player, LivingEntity target) {
        ModParticles.spawnParticlesServer(
                player.level(),
                new FlyingBloodEntityParticleOptions(player.getId(), true),
                target.getX(),
                target.getY() + target.getEyeHeight() / 2.0D,
                target.getZ(),
                10,
                0.1F,
                0.1F,
                0.1F,
                0.0D
        );
    }

    public record HudTargetInfo(
            float bloodLevelRelative,
            boolean poisonous
    ) {
    }

    private record FeedingVampire(
            CustomFactionPlayer<?> owner
    ) implements IVampire {

        @Override
        public @NotNull IFaction<?> getFaction() {
            return owner.getFaction();
        }

        @Override
        public LivingEntity getRepresentingEntity() {
            return owner.asEntity();
        }

        @Override
        public boolean isAdvancedBiter() {
            return owner.getVampireSkillBridge().isAdvancedBiter();
        }

        @Override
        public boolean doesResistGarlic(EnumStrength strength) {
            return false;
        }

        @Override
        public @NotNull EnumStrength isGettingGarlicDamage(
                LevelAccessor level,
                boolean force
        ) {
            return EnumStrength.NONE;
        }

        @Override
        public boolean isGettingSundamage(
                LevelAccessor level,
                boolean force
        ) {
            return false;
        }

        @Override
        public boolean isIgnoringSundamage() {
            return false;
        }

        @Override
        public boolean useBlood(int amount, boolean allowPartial) {
            boolean result = owner.getBloodData()
                    .removeBlood(amount, allowPartial);

            owner.syncBlood(false);
            return result;
        }

        @Override
        public boolean wantsBlood() {
            return owner.getBloodData().needsBlood();
        }

        @Override
        public void drinkBlood(
                int amount,
                float saturationModifier,
                boolean useRemaining,
                IDrinkBloodContext context
        ) {
            owner.drinkBlood(
                    amount,
                    saturationModifier,
                    context
            );
        }
    }
}
