package org.kuro.tvdvampirism.bite;

import de.teamlapen.werewolves.api.entities.werewolf.IWerewolf;
import de.teamlapen.werewolves.entities.player.werewolf.WerewolfPlayer;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import org.kuro.tvdvampirism.Tvdvampirism;
import org.kuro.tvdvampirism.compat.SpeciesCompatibility;
import org.kuro.tvdvampirism.config.ServerConfig;
import org.kuro.tvdvampirism.faction.VampireFamily;
import org.kuro.tvdvampirism.player.*;
import org.kuro.tvdvampirism.registry.BiteContent;

@EventBusSubscriber(modid = Tvdvampirism.MODID)
public final class WerewolfBiteManager {
    private WerewolfBiteManager() {}
    private static final ThreadLocal<LivingEntity> CURING = new ThreadLocal<>();

    public static boolean validVictim(LivingEntity target) {
        return target instanceof Player player ? SpeciesRules.canReceiveWolfBite(player)
                : VampireFamily.isVampireDerived(target);
    }

    /** Server entry point for stock and Hybrid bites. Damage is independent of infection */
    public static boolean tryApplyBite(LivingEntity attacker, LivingEntity target) {
        if (target.level().isClientSide || attacker.level() != target.level() || !attacker.isAlive()
                || !target.isAlive() || target == attacker || !validVictim(target)
                || target.hasEffect(BiteContent.CURE) || target.hasEffect(BiteContent.WEREWOLF_BITE)) return false;
        boolean capable = attacker instanceof Player player
                ? SpeciesRules.canUseHumanWolfBite(player)
                    || (WerewolfPlayer.get(player).getLevel() > 0 && !WerewolfPlayer.get(player).getForm().isHumanLike())
                : attacker instanceof IWerewolf wolf && !wolf.getForm().isHumanLike();
        if (!capable) return false;
        if (attacker instanceof Player && attacker.getRandom().nextDouble() >= ServerConfig.WOLF_BITE_PLAYER_CHANCE.get()) return false;
        return target.addEffect(new MobEffectInstance(BiteContent.WEREWOLF_BITE, configuredDuration(target)), attacker);
    }

    private static int configuredDuration(LivingEntity target) {
        return (target instanceof Player ? ServerConfig.WOLF_BITE_DURATION_MINUTES.get()
                : ServerConfig.WOLF_BITE_MOB_DURATION_MINUTES.get()) * 1200;
    }

    /** Preserve finite requested durations; gameplay bites already supply the server default. */
    public static MobEffectInstance normalizeEffect(LivingEntity target, MobEffectInstance effect) {
        if (target.level().isClientSide || !effect.getEffect().equals(BiteContent.WEREWOLF_BITE)) return effect;
        int remaining = effect.isInfiniteDuration() ? configuredDuration(target) : Math.max(1, effect.getDuration());
        if (target instanceof Player player) {
            var data = SpeciesManager.getData(player);
            if (data.hasWolfBite()) {
                int total = data.getWolfBiteDurationTicks() > 0 ? data.getWolfBiteDurationTicks() : remaining;
                remaining = Math.max(1, total - data.getWolfBiteTicks());
            }
        }
        return new MobEffectInstance(BiteContent.WEREWOLF_BITE, remaining, 0,
                effect.isAmbient(), effect.isVisible(), effect.showIcon());
    }

    @SubscribeEvent public static void onAdded(MobEffectEvent.Added event) {
        if (!(event.getEntity() instanceof ServerPlayer player)
                || !event.getEffectInstance().getEffect().equals(BiteContent.WEREWOLF_BITE)) return;
        var data = SpeciesManager.getData(player);
        if (!data.hasWolfBite()) {
            data.startWolfBite();
            data.setWolfBiteDurationTicks(event.getEffectInstance().getDuration());
        }
    }

    /** Called only by the effect. Snapshot its clock; do not run a second timer or send packets. */
    public static void tickInfection(LivingEntity target) {
        // Mobs only need the vanilla countdown and the existing terminal expiry event.
        if (!(target instanceof Player)) return;
        var effect = target.getEffect(BiteContent.WEREWOLF_BITE);
        if (effect == null) return;
        int total = configuredDuration(target);
        if (target instanceof ServerPlayer player) {
            var data = SpeciesManager.getData(player);
            if (data.getWolfBiteDurationTicks() <= 0) data.setWolfBiteDurationTicks(total);
            total = data.getWolfBiteDurationTicks();
            data.setWolfBiteTicks(total - Math.max(0, effect.getDuration() - 1));
        }
        int elapsed = Math.max(0, total - effect.getDuration() + 1);
        double progress = elapsed / (double) total;
        int stage = progress >= ServerConfig.WOLF_BITE_SEVERE_START.get() ? 2
                : progress >= ServerConfig.WOLF_BITE_WEAKNESS_TWO_START.get() ? 1 : 0;
        int interval = (stage == 2 ? ServerConfig.WOLF_BITE_STAGE_THREE_INTERVAL.get()
                : stage == 1 ? ServerConfig.WOLF_BITE_STAGE_TWO_INTERVAL.get()
                : ServerConfig.WOLF_BITE_STAGE_ONE_INTERVAL.get()) * 20;
        double previousProgress = (elapsed - 1) / (double) total;
        boolean enteredStage = elapsed == 1
                || previousProgress < ServerConfig.WOLF_BITE_WEAKNESS_TWO_START.get() && stage == 1
                || previousProgress < ServerConfig.WOLF_BITE_SEVERE_START.get() && stage == 2;
        // Short command-driven tests must expose each stage even if shorter than a pulse interval.
        if (elapsed <= 0 || elapsed % interval != 0 && !(total < configuredDuration(target) && enteredStage)) return;
        // Defer only actual symptom pulses, so the active-effect iterator is never mutated.
        var server = target.getServer();
        server.tell(new TickTask(server.getTickCount(), () -> {
            if (!target.isAlive() || target.isRemoved() || target.getEffect(BiteContent.WEREWOLF_BITE) != effect) return;
            int duration = Math.min(interval / 2, ServerConfig.WOLF_BITE_SYMPTOM_SECONDS.get() * 20 * (stage == 2 ? 2 : 1));
            target.addEffect(new MobEffectInstance(MobEffects.CONFUSION, duration, 0, false, false));
            if (stage > 0) {
                // Vanilla Weakness I subtracts four damage. Never disable low-damage melee.
                var attack = target.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE);
                if (attack != null && attack.getValue() >= 5)
                    target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, duration, 0, false, false));
                target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, duration, stage - 1, false, false));
            }
        }));
    }

    private static void terminal(LivingEntity target) {
        if (!target.isAlive()) return;
        var source = DeathPolicy.terminalBiteSource(target);
        if (target instanceof ServerPlayer player && DeathPolicy.canPermanentlyKill(player, source)) {
            ((DbnoAccess) SpeciesCompatibility.rawVampire(player)).tvd$setDbnoTimer(-1);
            player.setForcedPose(null);
        }
        CustomDamageSounds.deathBeforeDirectDeath(target);
        target.setHealth(0);
        target.die(source);
    }

    public static boolean cureWithBlood(ServerPlayer player) {
        if (!validVictim(player) || !player.hasEffect(BiteContent.WEREWOLF_BITE)) return false;
        CURING.set(player);
        try {
            player.removeEffect(BiteContent.WEREWOLF_BITE);
            SpeciesManager.getData(player).cureWolfBite();
        } finally { CURING.remove(); }
        player.addEffect(new MobEffectInstance(BiteContent.CURE, ServerConfig.WOLF_BITE_CURE_MINUTES.get() * 1200));
        return true;
    }

    public static boolean protectRemoval(LivingEntity entity) {
        return !entity.level().isClientSide && CURING.get() != entity;
    }

    public static boolean applyByCommand(LivingEntity target, MobEffectInstance effect, net.minecraft.world.entity.Entity source) {
        if (effect.getEffect().equals(BiteContent.WEREWOLF_BITE)) {
            if (!validVictim(target) || target.hasEffect(BiteContent.CURE)) return false;
            clearByCommand(target, BiteContent.WEREWOLF_BITE);
        }
        return target.addEffect(effect, source);
    }

    /** Used only by vanilla's permission-checked /effect clear, not general removal. */
    public static boolean clearByCommand(LivingEntity target, net.minecraft.core.Holder<net.minecraft.world.effect.MobEffect> effect) {
        var previous = CURING.get();
        CURING.set(target);
        try {
            boolean removed = effect == null ? target.removeAllEffects() : target.removeEffect(effect);
            if ((effect == null || effect.equals(BiteContent.WEREWOLF_BITE))
                    && target instanceof Player player && !target.hasEffect(BiteContent.WEREWOLF_BITE))
                SpeciesManager.getData(player).cureWolfBite();
            return removed;
        } finally {
            if (previous == null) CURING.remove(); else CURING.set(previous);
        }
    }

    @SubscribeEvent public static void onRemove(MobEffectEvent.Remove event) {
        if (event.getEffect().equals(BiteContent.WEREWOLF_BITE) && protectRemoval(event.getEntity())) event.setCanceled(true);
    }

    @SubscribeEvent public static void onApply(MobEffectEvent.Applicable event) {
        if (event.getEffectInstance().getEffect().equals(BiteContent.WEREWOLF_BITE)
                && (!validVictim(event.getEntity()) || event.getEntity().hasEffect(BiteContent.CURE)
                    || event.getEntity().hasEffect(BiteContent.WEREWOLF_BITE)))
            event.setResult(MobEffectEvent.Applicable.Result.DO_NOT_APPLY);
    }

    @SubscribeEvent public static void onExpire(MobEffectEvent.Expired event) {
        var target = event.getEntity();
        if (target.level().isClientSide || event.getEffectInstance() == null
                || !event.getEffectInstance().getEffect().equals(BiteContent.WEREWOLF_BITE)) return;
        if (target instanceof Player player) {
            var data = SpeciesManager.getData(player);
            if (data.isWolfBiteTerminalTriggered()) return;
            data.finishWolfBite();
        }
        // Allow natural expiry to remove the HUD effect. No zero-second or infinite residue.
        var server = target.getServer();
        server.tell(new TickTask(server.getTickCount(), () -> {
            if (target instanceof Player player && !SpeciesManager.getData(player).isWolfBiteTerminalTriggered()) return;
            terminal(target);
        }));
    }

    @SubscribeEvent public static void onMobAttack(LivingDamageEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer) || event.getNewDamage() <= 0) return;
        var attacker = event.getSource().getDirectEntity();
        if (attacker instanceof Mob mob && mob instanceof IWerewolf
                && mob.getRandom().nextDouble() < ServerConfig.WOLF_BITE_MOB_CHANCE.get())
            tryApplyBite(mob, event.getEntity());
    }

    private static void restore(Player player) {
        if (!(player instanceof ServerPlayer)) return;
        var data = SpeciesManager.getData(player);
        var existing = player.getEffect(BiteContent.WEREWOLF_BITE);
        // Migrate the old infinite/zero-second wrappers once, not on every tick.
        if (data.isWolfBiteTerminalTriggered()) {
            CURING.set(player);
            try { player.removeEffect(BiteContent.WEREWOLF_BITE); }
            finally { CURING.remove(); }
            data.finishWolfBite();
            return;
        }
        if (data.hasWolfBite() && (existing == null || existing.isInfiniteDuration() || existing.getDuration() <= 0)) {
            var replacement = normalizeEffect(player, new MobEffectInstance(BiteContent.WEREWOLF_BITE, configuredDuration(player)));
            CURING.set(player);
            try { player.removeEffect(BiteContent.WEREWOLF_BITE); }
            finally { CURING.remove(); }
            player.addEffect(replacement);
        }
    }
    @SubscribeEvent public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) { restore(event.getEntity()); }
    @SubscribeEvent public static void onRespawn(PlayerEvent.PlayerRespawnEvent event) { restore(event.getEntity()); }
}
