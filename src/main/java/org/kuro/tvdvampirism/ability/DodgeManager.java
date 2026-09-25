package org.kuro.tvdvampirism.ability;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.ProjectileImpactEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import org.joml.Vector3f;
import org.kuro.tvdvampirism.Tvdvampirism;
import org.kuro.tvdvampirism.compat.SpeciesCompatibility;
import org.kuro.tvdvampirism.config.ServerConfig;
import org.kuro.tvdvampirism.player.OriginalImmortalityManager;
import org.kuro.tvdvampirism.registry.DodgeSounds;
import org.kuro.tvdvampirism.skill.CustomSkills;

import java.util.ArrayList;
import java.util.List;

@EventBusSubscriber(modid = Tvdvampirism.MODID)
public final class DodgeManager {
    private static final DustParticleOptions RED_DUST =
            new DustParticleOptions(new Vector3f(0.85F, 0.035F, 0.08F), 1.0F);
    private static final double[] HEIGHTS = {0.0, 1.0, -1.0};
    private static final List<Offset> OFFSETS = offsets();

    private record Offset(int x, int z) {}

    private DodgeManager() {}

    @SubscribeEvent
    public static void onProjectileImpact(ProjectileImpactEvent event) {
        if (!(event.getRayTraceResult() instanceof EntityHitResult hit)
                || !(hit.getEntity() instanceof ServerPlayer player)
                || !active(player)) return;
        if (dodge(player)) {
            event.setCanceled(true);
            event.getProjectile().discard();
        }
    }

    @SubscribeEvent
    public static void onProjectileDamage(LivingIncomingDamageEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)
                || !(event.getSource().getDirectEntity() instanceof Projectile)
                || !active(player)) return;
        if (dodge(player)) {
            event.setCanceled(true);
            event.getSource().getDirectEntity().discard();
        }
    }

    private static boolean active(ServerPlayer player) {
        return player.hasEffect(CustomSkills.DODGE) && ServerConfig.DODGE_ENABLED.get()
                && player.isAlive() && !SpeciesCompatibility.rawVampire(player).isDBNO()
                && !OriginalImmortalityManager.isDown(player)
                && (CustomSkills.has(player, CustomSkills.Definition.ORIGINAL_DODGE)
                || CustomSkills.has(player, CustomSkills.Definition.ORIGINAL_HYBRID_DODGE));
    }

    private static boolean dodge(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        Vec3 origin = player.position();
        AABB bounds = player.getBoundingBox();
        int first = player.getRandom().nextInt(OFFSETS.size());
        for (int i = 0; i < OFFSETS.size(); i++) {
            Offset offset = OFFSETS.get((first + i) % OFFSETS.size());
            for (double height : HEIGHTS) {
                Vec3 destination = new Vec3(origin.x + offset.x, origin.y + height, origin.z + offset.z);
                if (!safePath(level, player, bounds, origin, destination)) continue;
                player.teleportTo(level, destination.x, destination.y, destination.z,
                        player.getYRot(), player.getXRot());
                if (player.position().distanceToSqr(destination) > 0.01) return false;
                player.setDeltaMovement(Vec3.ZERO);
                player.fallDistance = 0;
                effects(level, origin, destination);
                level.playSound(null, destination.x, destination.y, destination.z,
                        DodgeSounds.DODGE.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
                return true;
            }
        }
        return false;
    }

    private static List<Offset> offsets() {
        List<Offset> result = new ArrayList<>();
        for (int x = -3; x <= 3; x++) {
            for (int z = -3; z <= 3; z++) {
                int distanceSquared = x * x + z * z;
                if (distanceSquared >= 1 && distanceSquared <= 9) result.add(new Offset(x, z));
            }
        }
        return List.copyOf(result);
    }

    private static boolean safePath(ServerLevel level, ServerPlayer player, AABB bounds,
                                    Vec3 origin, Vec3 destination) {
        BlockPos feet = BlockPos.containing(destination);
        BlockPos floor = BlockPos.containing(destination.x, destination.y - 0.1, destination.z);
        AABB destinationBox = bounds.move(destination.subtract(origin));
        if (!level.hasChunkAt(feet) || !level.getWorldBorder().isWithinBounds(destinationBox)
                || !level.getBlockState(floor).isFaceSturdy(level, floor, Direction.UP)
                || !level.getFluidState(feet).isEmpty()
                || !level.getFluidState(feet.above()).isEmpty()
                || !level.noCollision(player, destinationBox)) return false;

        Vec3 movement = destination.subtract(origin);
        int steps = Math.max(1, (int) Math.ceil(movement.length() / 0.2));
        for (int i = 1; i <= steps; i++) {
            AABB alongPath = bounds.move(movement.scale((double) i / steps));
            if (!level.getWorldBorder().isWithinBounds(alongPath)
                    || !level.noCollision(player, alongPath)) return false;
        }
        return true;
    }

    private static void effects(ServerLevel level, Vec3 origin, Vec3 destination) {
        Vec3 start = origin.add(0, 1.0, 0);
        Vec3 end = destination.add(0, 1.0, 0);
        for (int i = 0; i <= 10; i++) {
            Vec3 point = start.lerp(end, i / 10.0);
            level.sendParticles(RED_DUST, point.x, point.y, point.z, 2, 0.12, 0.28, 0.12, 0.01);
        }
    }
}
