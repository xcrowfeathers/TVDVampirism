package org.kuro.tvdvampirism.player;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.network.protocol.game.ClientboundEntityEventPacket;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.kuro.tvdvampirism.compat.LivingSoundAccess;

/** Restores vanilla audio for addon damage paths which bypass vanilla sound dispatch. */
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

    /** Complete vanilla's sound dispatch when DBNO replaces an actual or synthetic death. */
    public static void down(net.minecraft.server.level.ServerPlayer player) {
        if (player.isDeadOrDying()) {
            // The lethal hurt already sounded for observers; cancellation only
            // prevents the victim's normal death entity-event packet.
            player.connection.send(new ClientboundEntityEventPacket(player, (byte) 3));
        } else {
            // Direct incapacitation (for example, an elder dagger) has no
            // preceding hurt/death sound dispatch at all.
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
