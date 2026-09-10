package org.kuro.tvdvampirism.ability;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.*;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.kuro.tvdvampirism.config.ServerConfig;
import org.kuro.tvdvampirism.compat.SpeciesCompatibility;
import org.kuro.tvdvampirism.skill.CustomSkills;
import org.kuro.tvdvampirism.skill.SpeciesSkillProfile;
import java.util.*;

/** Only the temporary effect processes a leap. State is server-only and never resumes after logout. */
@net.neoforged.fml.common.EventBusSubscriber(modid="tvdvampirism")
public final class LeapEffect extends MobEffect {
    private record Flight(UUID target, CustomSkills.Definition skill, Vec3 start, net.minecraft.resources.ResourceKey<net.minecraft.world.level.Level> dimension, int ticks) {}
    private static final Map<ServerPlayer,Flight> FLIGHTS=new WeakHashMap<>();
    public LeapEffect() { super(MobEffectCategory.BENEFICIAL,0x903A3A); }
    public static boolean start(ServerPlayer player, LivingEntity target, CustomSkills.Definition skill) {
        if (player.isPassenger() || player.isFallFlying() || !player.onGround()) return false;
        if (!player.addEffect(new MobEffectInstance(CustomSkills.LEAP,40,0,false,false,false))) return false;
        Vec3 delta=target.position().subtract(player.position());
        // Scale the launch speed with distance
        Vec3 horizontal=new Vec3(delta.x,0,delta.z);
        double horizontalDistance=horizontal.length();
        double speed=Math.clamp(1.05+horizontalDistance*0.09,1.05,2.35);
        Vec3 launch=horizontalDistance>0.001 ? horizontal.scale(speed/horizontalDistance) : Vec3.ZERO;
        player.setDeltaMovement(launch.x,0.55+Math.clamp(delta.y/12D,-0.15,0.35),launch.z);
        player.setOnGround(false);
        player.hurtMarked=true;
        FLIGHTS.put(player,new Flight(target.getUUID(),skill,player.position(),player.level().dimension(),0));
        return true;
    }
    @Override public boolean shouldApplyEffectTickThisTick(int duration,int amplifier) { return true; }
    @Override public boolean applyEffectTick(LivingEntity entity,int amplifier) {
        if (!(entity instanceof ServerPlayer player)) return false;
        var flight=FLIGHTS.get(player);
        if (flight==null) return false;
        var found=player.serverLevel().getEntity(flight.target());
        if (player.level().dimension()!=flight.dimension() || !(found instanceof LivingEntity target) || !target.isAlive() || !CustomSkills.has(player,flight.skill())
                || !player.isAlive() || player.isSpectator() || player.isPassenger() || SpeciesCompatibility.rawVampire(player).isDBNO()
                || player.position().distanceToSqr(flight.start())>400 || flight.ticks()>=38) return finish(player);
        int ticks=flight.ticks()+1;
        boolean reaches=player.getBoundingBox().inflate(1.5).intersects(target.getBoundingBox());
        if (ticks>2 && (reaches || player.onGround() || player.horizontalCollision)) {
            if (reaches && player.hasLineOfSight(target) && AbilityTargets.canHarm(player,target)) {
                float damage=(flight.skill().species==SpeciesSkillProfile.HYBRID ? ServerConfig.HYBRID_LEAP_DAMAGE : ServerConfig.ORIGINAL_HYBRID_LEAP_DAMAGE).get().floatValue();
                if (target.hurt(player.damageSources().playerAttack(player),damage))
                    target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS,ServerConfig.LEAP_WEAKNESS_SECONDS.get()*20,0));
            }
            return finish(player);
        }
        FLIGHTS.put(player,new Flight(flight.target(),flight.skill(),flight.start(),flight.dimension(),ticks));
        return true;
    }
    private static boolean finish(ServerPlayer player) { FLIGHTS.remove(player); return false; }
    @net.neoforged.bus.api.SubscribeEvent
    public static void removed(net.neoforged.neoforge.event.entity.living.MobEffectEvent.Remove event) {
        if (event.getEffect().equals(CustomSkills.LEAP) && event.getEntity() instanceof ServerPlayer player) finish(player);
    }
    @net.neoforged.bus.api.SubscribeEvent
    public static void logout(net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) finish(player);
    }
}
