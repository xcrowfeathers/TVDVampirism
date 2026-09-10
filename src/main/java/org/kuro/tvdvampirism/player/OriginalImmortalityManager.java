package org.kuro.tvdvampirism.player;

import de.teamlapen.lib.HelperLib;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import org.kuro.tvdvampirism.Tvdvampirism;
import org.kuro.tvdvampirism.compat.SpeciesCompatibility;
import org.kuro.tvdvampirism.config.ServerConfig;

@EventBusSubscriber(modid = Tvdvampirism.MODID)
public final class OriginalImmortalityManager {
    private OriginalImmortalityManager() {}

    public static int duration(net.minecraft.world.entity.player.Player player) {
        var custom = SpeciesCompatibility.customPlayer(player);
        if (custom != null) {
            return custom.getVampireSkillBridge().getDbnoDuration();
        }
        return (SpeciesManager.getSpecies(player) == Species.ORIGINAL_HYBRID
                ? ServerConfig.ORIGINAL_HYBRID_DBNO_SECONDS.get()
                : ServerConfig.ORIGINAL_DBNO_SECONDS.get()) * 20;
    }

    public static boolean isDown(net.minecraft.world.entity.player.Player player) {
        return SpeciesRules.hasOriginalImmortality(player)
                && SpeciesManager.getData(player).getOriginalDbnoTicks() >= 0;
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!DeathPolicy.bypassesOrdinaryDbno(player, event.getSource()) && TransformationManager.onLethalDamage(player, event.getSource())) {
            event.setCanceled(true);
            return;
        }
        if (!DeathPolicy.canPermanentlyKill(player, event.getSource())) {
            event.setCanceled(true);
            enter(player, event.getSource());
        } else if (isDown(player)) {
            // An authorized permanent kill must also stop recovery.
            SpeciesManager.getData(player).setOriginalDbnoTicks(-1);
            setStockTimer(player, -1);
            player.setForcedPose(null);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onDamage(LivingIncomingDamageEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && isDown(player)
                && !DeathPolicy.canPermanentlyKill(player, event.getSource())) {
            event.setCanceled(true);
            rescueFromVoid(player);
        }
    }

    public static boolean enter(ServerPlayer player, DamageSource source) {
        if (DeathPolicy.canPermanentlyKill(player, source)) return false;
        var data = SpeciesManager.getData(player);
        if (data.getOriginalDbnoTicks() < 0) {
            CustomDamageSounds.down(player);
            data.setOriginalDbnoTicks(duration(player));
            var custom = SpeciesCompatibility.customPlayer(player);
            if (custom != null) {
                custom.stopFeeding(true);
                custom.getActionHandler().deactivateAllActions();
            }
        }
        player.setHealth(0.5F);
        player.stopRiding();
        player.setForcedPose(Pose.SLEEPING);
        rescueFromVoid(player);
        publish(player);
        return true;
    }

    @SubscribeEvent
    public static void onTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        var data = SpeciesManager.getData(player);
        if (!SpeciesRules.hasOriginalImmortality(player)) {
            if (data.getOriginalDbnoTicks() >= 0) {
                data.setOriginalDbnoTicks(-1);
                publish(player);
                player.setForcedPose(null);
            }
            return;
        }
        if (!isDown(player)) return;
        int ticks = data.getOriginalDbnoTicks();
        if (ticks > 0 && !data.isDaggered()) data.setOriginalDbnoTicks(ticks - 1);
        player.setHealth(0.5F);
        player.setAirSupply(player.getMaxAirSupply());
        player.setForcedPose(Pose.SLEEPING);
        player.setDeltaMovement(0, Math.min(0, player.getDeltaMovement().y), 0);
        rescueFromVoid(player);
        setStockTimer(player, data.getOriginalDbnoTicks());
        if (ticks == 1 || player.tickCount % 20 == 0) publish(player);
    }

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && isDown(player)) {
            player.setHealth(0.5F);
            player.setForcedPose(Pose.SLEEPING);
            rescueFromVoid(player);
            publish(player);
        }
    }

    //public static void tryWake(ServerPlayer player) {
    //    if (!isDown(player) || SpeciesManager.getData(player).getOriginalDbnoTicks() != 0) return;
    //    rescueFromVoid(player);
    //    SpeciesManager.getData(player).setOriginalDbnoTicks(-1);
    //    player.setForcedPose(null);
    //    player.setHealth(Math.max(0.5F, player.getMaxHealth()));
    //    player.clearFire();
    //    player.fallDistance = 0;
    //    publish(player);
    //    player.refreshDimensions();
    //}

    public static void tryWake(ServerPlayer player) {
        if (!isDown(player)
                || SpeciesManager.getData(player).isDaggered()
                || SpeciesManager.getData(player).getOriginalDbnoTicks() != 0) {
            return;
        }

        rescueFromVoid(player);

        SpeciesManager.getData(player).setOriginalDbnoTicks(-1);

        player.setForcedPose(null);

        // goofy ah resurrection with 20 HP.
        player.setHealth(Math.min(20.0F, player.getMaxHealth()));

        // no blood reserves ):
        var custom = SpeciesCompatibility.customPlayer(player);
        if (custom != null) {
            int blood = custom.getBloodData().getBloodLevel();

            if (blood > 0) {
                custom.useBlood(blood, true);
                custom.syncBlood(false);
            }
        }

        player.clearFire();
        player.fallDistance = 0;

        publish(player);
        player.refreshDimensions();
    }

    private static void setStockTimer(ServerPlayer player, int ticks) {
        ((DbnoAccess) SpeciesCompatibility.rawVampire(player)).tvd$setDbnoTimer(ticks);
    }

    private static void publish(ServerPlayer player) {
        int ticks = SpeciesManager.getData(player).getOriginalDbnoTicks();
        setStockTimer(player, ticks);
        CompoundTag update = new CompoundTag();
        update.putInt("dbno", ticks);
        // Use the existing attachment's packet route and DBNO screen/pose state.
        HelperLib.sync(SpeciesCompatibility.rawVampire(player), update, player, true);
    }

    private static void rescueFromVoid(ServerPlayer player) {
        if (player.getY() >= player.level().getMinBuildHeight()) return;
        ServerLevel destination = player.server.overworld();
        BlockPos spawn = destination.getSharedSpawnPos();
        BlockPos safe = null;
        for (int dx = -8; dx <= 8 && safe == null; dx++) {
            for (int dz = -8; dz <= 8 && safe == null; dz++) {
                BlockPos candidate = destination.getHeightmapPos(
                        Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, spawn.offset(dx, 0, dz));
                if (candidate.getY() > destination.getMinBuildHeight()
                        && !destination.getBlockState(candidate.below()).getCollisionShape(destination, candidate.below()).isEmpty()
                        && destination.getFluidState(candidate.below()).isEmpty()
                        && destination.getBlockState(candidate).isAir()
                        && destination.getBlockState(candidate.above()).isAir()) safe = candidate;
            }
        }
        if (safe == null) {
            // Empty/void spawn worlds also need a stable recovery point.
            safe = new BlockPos(spawn.getX(), destination.getMinBuildHeight() + 64, spawn.getZ());
            while (!destination.getBlockState(safe).isAir() || !destination.getBlockState(safe.above()).isAir())
                safe = safe.above();
            if (destination.getBlockState(safe.below()).isAir())
                destination.setBlockAndUpdate(safe.below(), Blocks.STONE.defaultBlockState());
        }
        player.teleportTo(destination, safe.getX() + 0.5, safe.getY(), safe.getZ() + 0.5,
                player.getYRot(), player.getXRot());
        player.fallDistance = 0;
        player.setDeltaMovement(0, 0, 0);
    }
}
