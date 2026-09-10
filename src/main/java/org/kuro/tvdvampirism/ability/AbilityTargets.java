package org.kuro.tvdvampirism.ability;

import de.teamlapen.vampirism.api.VampirismAPI;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.Vec3;
import org.kuro.tvdvampirism.faction.VampireFamily;
import org.kuro.tvdvampirism.player.*;

public final class AbilityTargets {
    private AbilityTargets() {}
    public static LivingEntity lookedAt(ServerPlayer player, double range) {
        Vec3 start=player.getEyePosition();
        Vec3 end=start.add(Vec3.directionFromRotation(player.getXRot(),player.getYRot()).scale(range));
        var block=player.level().clip(new ClipContext(start,end,ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,player));
        var hit=ProjectileUtil.getEntityHitResult(player,start,end,player.getBoundingBox().expandTowards(end.subtract(start)).inflate(1),
                entity -> entity instanceof LivingEntity living && living.isAlive() && !entity.isSpectator() && entity.isPickable(),
                Math.min(range*range,start.distanceToSqr(block.getLocation())));
        return hit!=null && hit.getEntity() instanceof LivingEntity target && player.hasLineOfSight(target) ? target : null;
    }
    public static boolean canHarm(ServerPlayer player, LivingEntity target) {
        return target!=player && target.isAlive() && !target.isInvulnerable()
                && (!(target instanceof Player other) || (!other.isCreative() && !other.isSpectator()
                && player.server.isPvpAllowed() && player.canHarmPlayer(other)));
    }
    public static boolean canCompel(LivingEntity target) {
        if (target instanceof Player player) {
            var species=SpeciesManager.getSpecies(player);
            return species==Species.NORMAL || species==Species.HYBRID
                    || (species==Species.NONE && VampirismAPI.factionRegistry().getFaction(player)==null);
        }
        return target instanceof Villager || VampireFamily.isVampireDerived(target);
    }

    public static void immobilize(LivingEntity target) {
        Vec3 movement = target.getDeltaMovement();
        target.setDeltaMovement(0, Math.min(0, movement.y), 0);
        target.hasImpulse = true;
        if (target instanceof net.minecraft.world.entity.Mob mob) mob.getNavigation().stop();
    }
}
