package org.kuro.tvdvampirism.player;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.network.protocol.game.ClientboundEntityEventPacket;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.kuro.tvdvampirism.compat.LivingSoundAccess;

public final class CustomDamageSounds {
    private CustomDamageSounds() {}

    public static void hurt(LivingEntity entity, DamageSource source) {
        play(entity, ((LivingSoundAccess) entity).tvd$getHurtSound(source));
    }

    public static void deathBeforeDirectDeath(LivingEntity entity) {
        var access = (LivingSoundAccess) entity;
        var sound = access.tvd$getDeathSound();
        if (sound != null && !entity.isSilent())
            entity.playSound(sound, access.tvd$getSoundVolume(), entity.getVoicePitch());
    }

    public static void down(net.minecraft.server.level.ServerPlayer player) {
        if (player.isDeadOrDying()) {
            // Observers already heard the lethal hit; only the victim misses the death sound.
            player.connection.send(new ClientboundEntityEventPacket(player, (byte) 3));
        } else {
            // Direct DBNO, such as an elder dagger hit, has no earlier hurt or death sound.
            play(player, ((LivingSoundAccess) player).tvd$getDeathSound());
        }
    }

    private static void play(LivingEntity entity, SoundEvent sound) {
        if (sound == null || entity.isSilent() || !(entity.level() instanceof ServerLevel level)) return;
        var access = (LivingSoundAccess) entity;
        level.playSound(null, entity.getX(), entity.getY(), entity.getZ(), sound,
                entity.getSoundSource(), access.tvd$getSoundVolume(), entity.getVoicePitch());
    }
}
