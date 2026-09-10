package org.kuro.tvdvampirism.player;

import de.teamlapen.lib.HelperLib;
import de.teamlapen.vampirism.entity.factions.FactionPlayerHandler;
import de.teamlapen.werewolves.api.entities.werewolf.WerewolfForm;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import org.kuro.tvdvampirism.Tvdvampirism;
import org.kuro.tvdvampirism.attribute.SpeciesAttributeManager;
import org.kuro.tvdvampirism.compat.SpeciesCompatibility;
import org.kuro.tvdvampirism.config.ServerConfig;
import org.kuro.tvdvampirism.registry.VampirismCureContent;

/** Server-authoritative lifecycle for the dedicated cure effect. */
@EventBusSubscriber(modid = Tvdvampirism.MODID)
public final class VampirismCureManager {
    private VampirismCureManager() {}

    public static boolean canDrink(Player player) {
        return player.isAlive()
                && SpeciesRules.isVampiric(player)
                && !SpeciesCompatibility.rawVampire(player).isDBNO()
                && !SpeciesManager.getData(player).isVampirismCureTerminalPending();
    }

    public static boolean start(ServerPlayer player) {
        if (!canDrink(player)) return false;
        int duration = ServerConfig.VAMPIRISM_CURE_DURATION_SECONDS.get() * 20;
        if (player.hasEffect(VampirismCureContent.VAMPIRISM_CURE_EFFECT)) {
            player.removeEffect(VampirismCureContent.VAMPIRISM_CURE_EFFECT);
        }
        if (!player.addEffect(new MobEffectInstance(
                VampirismCureContent.VAMPIRISM_CURE_EFFECT, duration))) return false;

        return true;
    }

    @SubscribeEvent
    public static void onAdded(MobEffectEvent.Added event) {
        if (!(event.getEntity() instanceof ServerPlayer player)
                || !event.getEffectInstance().getEffect().equals(
                        VampirismCureContent.VAMPIRISM_CURE_EFFECT)
                || !SpeciesRules.isVampiric(player)) return;

        // The effect itself owns the lifecycle, regardless of whether it came
        // from the item or an operator's /effect command.
        PlayerData data = SpeciesManager.getData(player);
        data.setVampirismCureActive(true);
        data.setVampirismCureTerminalPending(false);
    }

    /** Called before Vampirism decides whether a lethal hit enters DBNO. */
    public static boolean onLethalDamage(ServerPlayer player, DamageSource source) {
        PlayerData data = SpeciesManager.getData(player);
        if (!data.isVampirismCureActive()) return false;

        if (DeathPolicy.isAuthorizedVampirismCure(player, source)) return false;
        cancel(player);

        // White Oak, terminal venom, heart-rip and dagger backlash keep their
        // dedicated permanent-death behavior. Ordinary lethal damage fails the
        // cure and enters the species' existing DBNO lifecycle.
        if (DeathPolicy.isExplicitTerminal(player, source)) return false;
        return enterDbno(player, source);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)
                || !SpeciesManager.getData(player).isVampirismCureActive()
                || DeathPolicy.isAuthorizedVampirismCure(player, event.getSource())) return;

        boolean protectedByCure = onLethalDamage(player, event.getSource());
        if (protectedByCure) event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onExpired(MobEffectEvent.Expired event) {
        if (!(event.getEntity() instanceof ServerPlayer player)
                || event.getEffectInstance() == null
                || !event.getEffectInstance().getEffect().equals(
                        VampirismCureContent.VAMPIRISM_CURE_EFFECT)) return;

        PlayerData data = SpeciesManager.getData(player);
        if (data.isVampirismCureTerminalPending()
                || !data.isVampirismCureActive() && !SpeciesRules.isVampiric(player)) return;
        data.setVampirismCureActive(false);
        data.setVampirismCureTerminalPending(true);

        // LivingEntity is iterating its active-effect map during this event.
        scheduleTerminalDeath(player);
    }

    @SubscribeEvent
    public static void onRemoved(MobEffectEvent.Remove event) {
        if (!(event.getEntity() instanceof ServerPlayer player)
                || !event.getEffect().equals(VampirismCureContent.VAMPIRISM_CURE_EFFECT)) return;
        PlayerData data = SpeciesManager.getData(player);
        if (!data.isVampirismCureTerminalPending()) {
            data.setVampirismCureActive(false);
        }
    }

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        PlayerData data = SpeciesManager.getData(player);
        if (!data.isVampirismCureTerminalPending()
                && player.hasEffect(VampirismCureContent.VAMPIRISM_CURE_EFFECT)
                && SpeciesRules.isVampiric(player)) {
            data.setVampirismCureActive(true);
            return;
        }
        if (data.isVampirismCureTerminalPending()
                || data.isVampirismCureActive()
                    && !player.hasEffect(VampirismCureContent.VAMPIRISM_CURE_EFFECT)) {
            data.setVampirismCureActive(false);
            data.setVampirismCureTerminalPending(true);
            scheduleTerminalDeath(player);
        }
    }

    @SubscribeEvent
    public static void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)
                || !SpeciesManager.getData(player).isVampirismCureTerminalPending()) return;
        finishCure(player);
    }

    private static void cancel(ServerPlayer player) {
        PlayerData data = SpeciesManager.getData(player);
        data.setVampirismCureActive(false);
        data.setVampirismCureTerminalPending(false);
        player.removeEffect(VampirismCureContent.VAMPIRISM_CURE_EFFECT);
    }

    private static void scheduleTerminalDeath(ServerPlayer player) {
        player.server.tell(new TickTask(player.server.getTickCount(), () -> {
            if (!player.isRemoved() && player.isAlive()
                    && SpeciesManager.getData(player).isVampirismCureTerminalPending()) {
                DeathPolicy.killForVampirismCure(player);
            }
        }));
    }

    private static boolean enterDbno(ServerPlayer player, DamageSource source) {
        if (SpeciesRules.hasOriginalImmortality(player)) {
            return OriginalImmortalityManager.enter(player, source);
        }

        CustomDamageSounds.down(player);
        var custom = SpeciesCompatibility.customPlayer(player);
        if (custom != null) {
            custom.stopFeeding(true);
            custom.getActionHandler().deactivateAllActions();
        }
        var vampire = SpeciesCompatibility.rawVampire(player);
        int ticks = custom != null
                ? custom.getVampireSkillBridge().getDbnoDuration()
                : vampire.getDbnoDuration();
        ((DbnoAccess) vampire).tvd$setDbnoTimer(Math.max(1, ticks));
        player.setHealth(0.5F);
        player.stopRiding();
        player.setForcedPose(Pose.SLEEPING);
        player.refreshDimensions();
        CompoundTag update = new CompoundTag();
        update.putInt("dbno", Math.max(1, ticks));
        HelperLib.sync(vampire, update, player, true);
        return true;
    }

    private static void finishCure(ServerPlayer player) {
        PlayerData data = SpeciesManager.getData(player);
        if (FactionPlayerHandler.get(player).getCurrentFaction() != null
                && !SpeciesTransitionManager.forceSpecies(player, Species.NONE)) {
            Tvdvampirism.LOGGER.error("Could not clear supernatural faction after Vampirism Cure for {}",
                    player.getGameProfile().getName());
            return;
        }

        // Hybrids keep compatibility data in the stock Werewolves attachment;
        // clear that dormant state after the real faction transition as well.
        var werewolf = SpeciesCompatibility.rawWerewolf(player);
        werewolf.setForm(null, WerewolfForm.NONE);
        werewolf.getLevelHandler().reset();
        werewolf.sync(true);

        ((DbnoAccess) SpeciesCompatibility.rawVampire(player)).tvd$setDbnoTimer(-1);
        data.setOriginalDbnoTicks(-1);
        data.setPotencyXp(0);
        data.setMasteryXp(0);
        data.cureWolfBite();
        data.setVampirismCureActive(false);
        data.setVampirismCureTerminalPending(false);
        player.setForcedPose(null);
        player.clearFire();
        SpeciesAttributeManager.clear(player);
    }
}
