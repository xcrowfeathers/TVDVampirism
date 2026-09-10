package org.kuro.tvdvampirism.player;

import de.teamlapen.lib.HelperLib;
import de.teamlapen.vampirism.api.VReference;
import de.teamlapen.vampirism.entity.factions.FactionPlayerHandler;
import de.teamlapen.werewolves.api.WReference;
import de.teamlapen.werewolves.api.entities.werewolf.WerewolfForm;
import de.teamlapen.werewolves.entities.player.werewolf.WerewolfPlayer;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;
import org.kuro.tvdvampirism.Tvdvampirism;
import org.kuro.tvdvampirism.compat.SpeciesCompatibility;
import org.kuro.tvdvampirism.config.ServerConfig;
import org.kuro.tvdvampirism.registry.TransformationContent;

/** Effects own all countdowns; faction and DBNO ownership stay in existing systems. */
@EventBusSubscriber(modid = Tvdvampirism.MODID)
public final class TransformationManager {
    public enum Reagent { AUGUSTINE_SYRINGE, ORIGINAL_HYBRID_BLOOD, INVINCIBILITY_CURSE }
    public enum Type {
        AUGUSTINE(Species.AUGUSTINE), HYBRID(Species.HYBRID),
        ORIGINAL(Species.ORIGINAL), ORIGINAL_HYBRID(Species.ORIGINAL_HYBRID);
        private final Species target;
        Type(Species target) { this.target = target; }
        public Holder<MobEffect> effect() { return TransformationContent.effect(this); }
        public int durationTicks() {
            return switch (this) {
                case AUGUSTINE -> ServerConfig.AUGUSTINE_TRANSFORMATION_SECONDS.get() * 20;
                case HYBRID -> ServerConfig.HYBRID_BLOOD_EFFECT_MINUTES.get() * 1200;
                case ORIGINAL -> ServerConfig.ORIGINAL_CURSE_DURATION_MINUTES.get() * 1200;
                case ORIGINAL_HYBRID -> ServerConfig.ORIGINAL_HYBRID_CURSE_DURATION_MINUTES.get() * 1200;
            };
        }
    }

    private TransformationManager() {}

    private static boolean eligible(Player player, Type type) {
        var faction = FactionPlayerHandler.get(player).getCurrentFaction();
        return switch (type) {
            case AUGUSTINE -> faction == VReference.VAMPIRE_FACTION;
            case HYBRID, ORIGINAL_HYBRID -> faction == WReference.WEREWOLF_FACTION;
            case ORIGINAL -> faction == null || faction == VReference.VAMPIRE_FACTION;
        };
    }

    public static Type target(Player player, Reagent reagent) {
        Type type = switch (reagent) {
            case AUGUSTINE_SYRINGE -> Type.AUGUSTINE;
            case ORIGINAL_HYBRID_BLOOD -> Type.HYBRID;
            case INVINCIBILITY_CURSE -> eligible(player, Type.ORIGINAL_HYBRID)
                    ? Type.ORIGINAL_HYBRID : Type.ORIGINAL;
        };
        return eligible(player, type) ? type : null;
    }

    public static boolean canStart(Player player, Reagent reagent) {
        if (!player.isAlive() || target(player, reagent) == null
                || SpeciesCompatibility.rawVampire(player).isDBNO()) return false;
        for (Type type : Type.values()) if (player.hasEffect(type.effect())) return false;
        return true;
    }

    public static boolean start(ServerPlayer player, Reagent reagent) {
        if (!canStart(player, reagent)) return false;
        Type type = target(player, reagent);
        boolean added = player.addEffect(new MobEffectInstance(type.effect(), type.durationTicks()));
        if (added && type == Type.AUGUSTINE)
            player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, type.durationTicks()));
        return added;
    }

    /** Called by the existing death boundary, before stock death/DBNO processing. */
    public static boolean onLethalDamage(ServerPlayer player, DamageSource source) {
        for (Type type : Type.values()) {
            if (type != Type.AUGUSTINE && player.hasEffect(type.effect()) && eligible(player, type))
                return complete(player, type, source);
        }
        return false;
    }

    @SubscribeEvent
    public static void onExpired(MobEffectEvent.Expired event) {
        if (!(event.getEntity() instanceof ServerPlayer player)
                || event.getEffectInstance() == null
                || !event.getEffectInstance().getEffect().equals(Type.AUGUSTINE.effect())) return;
        // Never mutate the effect map while LivingEntity is iterating it.
        player.server.tell(new TickTask(player.server.getTickCount(), () -> {
            if (!player.isRemoved() && player.isAlive() && !player.hasEffect(Type.AUGUSTINE.effect()))
                complete(player, Type.AUGUSTINE, player.damageSources().generic());
        }));
    }

    private static boolean complete(ServerPlayer player, Type type, DamageSource source) {
        if (!eligible(player, type)) return false;
        boolean wasWerewolf = FactionPlayerHandler.get(player).getCurrentFaction() == WReference.WEREWOLF_FACTION;
        // Existing API preserves the current level, clamps to the target cap and
        // gives factionless humans its existing minimum/start level of one.
        // Its exit callbacks reset old skills, actions, attributes and lord data.
        if (!SpeciesTransitionManager.forceSpecies(player, type.target)) return false;
        if (wasWerewolf) {
            var werewolf = WerewolfPlayer.get(player);
            werewolf.setForm(null, WerewolfForm.NONE);
            werewolf.getLevelHandler().reset();
            werewolf.sync(true);
        }
        for (Type effect : Type.values()) player.removeEffect(effect.effect());

        if (SpeciesRules.hasOriginalImmortality(player))
            return OriginalImmortalityManager.enter(player, source);

        // Normal stock DBNO, without onDeadlyHit's killer-tag/neonatal admission
        // gates: transformation completion itself is not a new damage event.
        var custom = SpeciesCompatibility.customPlayer(player);
        custom.stopFeeding(true);
        custom.getActionHandler().deactivateAllActions();
        var vampire = SpeciesCompatibility.rawVampire(player);
        int ticks = custom.getVampireSkillBridge().getDbnoDuration();
        ((DbnoAccess) vampire).tvd$setDbnoTimer(ticks);
        player.setHealth(0.5F);
        player.stopRiding();
        player.setForcedPose(Pose.SLEEPING);
        player.refreshDimensions();
        CompoundTag update = new CompoundTag();
        update.putInt("dbno", ticks);
        HelperLib.sync(vampire, update, player, true);
        return true;
    }
}
