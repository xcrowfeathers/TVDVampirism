package org.kuro.tvdvampirism.player;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;

/** The only authorization point for a permanent player kill. */
public final class DeathPolicy {
    private DeathPolicy() {}
    private record WhiteOakKill(Player target, DamageSource source) {}
    private static final ThreadLocal<WhiteOakKill> WHITE_OAK_KILL = new ThreadLocal<>();
    private static final ThreadLocal<WhiteOakKill> DAGGER_BACKLASH = new ThreadLocal<>();
    private static final ThreadLocal<WhiteOakKill> HEART_RIP = new ThreadLocal<>();
    private static final ThreadLocal<WhiteOakKill> VAMPIRISM_CURE = new ThreadLocal<>();

    public static boolean bypassesOrdinaryDbno(Player player, DamageSource source) {
        var backlash = DAGGER_BACKLASH.get();
        return isTerminalBite(source) || isAuthorizedWhiteOak(player, source) || isHeartRip(player, source)
                || isAuthorizedVampirismCure(player, source)
                || isLethalBurn(player, source)
                || backlash != null && backlash.target() == player && backlash.source() == source;
    }

    public static boolean hurtWithHeartRip(net.minecraft.server.level.ServerPlayer attacker,
            net.minecraft.world.entity.LivingEntity target, float amount) {
        var source = target.damageSources().playerAttack(attacker);
        if (!(target instanceof Player player) || SpeciesRules.hasOriginalImmortality(player)) {
            return target.hurt(source, amount);
        }

        Species species = SpeciesManager.getSpecies(player);
        if (species != Species.NORMAL && species != Species.AUGUSTINE && species != Species.HYBRID) {
            return target.hurt(source, amount);
        }

        var previous = HEART_RIP.get();
        HEART_RIP.set(new WhiteOakKill(player, source));
        try {
            return target.hurt(source, amount);
        } finally {
            if (previous == null) HEART_RIP.remove(); else HEART_RIP.set(previous);
        }
    }

    private static boolean isHeartRip(Player target, DamageSource source) {
        var rip = HEART_RIP.get();
        return rip != null && rip.target() == target && rip.source() == source;
    }

    private static boolean isLethalBurn(Player player, DamageSource source) {
        Species species = SpeciesManager.getSpecies(player);
        if (species != Species.NORMAL && species != Species.AUGUSTINE && species != Species.HYBRID) return false;
        return source.is(net.minecraft.world.damagesource.DamageTypes.IN_FIRE)
                || source.is(net.minecraft.world.damagesource.DamageTypes.ON_FIRE)
                || source.is(net.minecraft.world.damagesource.DamageTypes.LAVA)
                || source.is(de.teamlapen.vampirism.core.ModDamageTypes.SUN_DAMAGE)
                || source.is(de.teamlapen.vampirism.core.ModDamageTypes.VAMPIRE_IN_FIRE)
                || source.is(de.teamlapen.vampirism.core.ModDamageTypes.VAMPIRE_ON_FIRE);
    }

    public static void killDaggerAttacker(net.minecraft.world.entity.LivingEntity attacker) {
        if (attacker instanceof Player player && SpeciesRules.hasOriginalImmortality(player)) return;
        var source = attacker.damageSources().genericKill();
        var previous = DAGGER_BACKLASH.get();
        if (attacker instanceof Player player) DAGGER_BACKLASH.set(new WhiteOakKill(player, source));
        try {
            attacker.setHealth(0);
            attacker.die(source);
        } finally {
            if (previous == null) DAGGER_BACKLASH.remove(); else DAGGER_BACKLASH.set(previous);
        }
    }

    public static boolean isAuthorizedWhiteOak(Player target, DamageSource source) {
        var kill = WHITE_OAK_KILL.get();
        return kill != null && kill.target() == target && kill.source() == source;
    }

    public static boolean isAuthorizedVampirismCure(Player target, DamageSource source) {
        var kill = VAMPIRISM_CURE.get();
        return kill != null && kill.target() == target && kill.source() == source;
    }

    /** Explicit kill mechanics retain their existing behavior while a cure is active. */
    public static boolean isExplicitTerminal(Player target, DamageSource source) {
        var backlash = DAGGER_BACKLASH.get();
        return isTerminalBite(source) || isAuthorizedWhiteOak(target, source)
                || isHeartRip(target, source) || isAuthorizedVampirismCure(target, source)
                || backlash != null && backlash.target() == target && backlash.source() == source;
    }

    public static boolean killForVampirismCure(net.minecraft.server.level.ServerPlayer target) {
        var source = terminalVampirismCureSource(target);
        var previous = VAMPIRISM_CURE.get();
        VAMPIRISM_CURE.set(new WhiteOakKill(target, source));
        try {
            CustomDamageSounds.deathBeforeDirectDeath(target);
            target.setHealth(0);
            target.die(source);
            return target.isDeadOrDying();
        } finally {
            if (previous == null) VAMPIRISM_CURE.remove();
            else VAMPIRISM_CURE.set(previous);
        }
    }

    /** Snapshot eligibility once; death listeners may clear DBNO before later listeners run. */
    public static boolean tryWhiteOakKill(net.minecraft.server.level.ServerPlayer attacker,
            net.minecraft.server.level.ServerPlayer target, net.minecraft.world.item.ItemStack stake) {
        if (!stake.is(org.kuro.tvdvampirism.registry.WhiteOakContent.WHITE_OAK_STAKE.get())
                || !target.isAlive() || !OriginalImmortalityManager.isDown(target)
                || SpeciesManager.getData(target).isDaggered()) return false;
        var source = target.damageSources().playerAttack(attacker);
        var previous = WHITE_OAK_KILL.get();
        WHITE_OAK_KILL.set(new WhiteOakKill(target, source));
        try {
            target.setHealth(0);
            target.die(source);
            return target.isDeadOrDying();
        } finally {
            if (previous == null) WHITE_OAK_KILL.remove();
            else WHITE_OAK_KILL.set(previous);
        }
    }
    private static final net.minecraft.resources.ResourceKey<net.minecraft.world.damagesource.DamageType> TERMINAL_BITE =
            net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.DAMAGE_TYPE,
                    net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("tvdvampirism", "terminal_werewolf_bite"));
    private static final net.minecraft.resources.ResourceKey<net.minecraft.world.damagesource.DamageType> TERMINAL_VAMPIRISM_CURE =
            net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.DAMAGE_TYPE,
                    net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("tvdvampirism", "terminal_vampirism_cure"));

    public static boolean isTerminalBite(DamageSource source) { return source.is(TERMINAL_BITE); }

    public static DamageSource terminalBiteSource(net.minecraft.world.entity.LivingEntity target) {
        return new DamageSource(target.registryAccess().registryOrThrow(net.minecraft.core.registries.Registries.DAMAGE_TYPE)
                .getHolderOrThrow(TERMINAL_BITE));
    }

    public static DamageSource terminalVampirismCureSource(net.minecraft.world.entity.LivingEntity target) {
        return new DamageSource(target.registryAccess().registryOrThrow(net.minecraft.core.registries.Registries.DAMAGE_TYPE)
                .getHolderOrThrow(TERMINAL_VAMPIRISM_CURE));
    }

    public static boolean canPermanentlyKill(Player player, DamageSource source) {
        return !SpeciesRules.hasOriginalImmortality(player) || isAuthorizedWhiteOak(player, source)
                || isAuthorizedVampirismCure(player, source);
    }
}
