package org.kuro.tvdvampirism.compat;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;

/** Access to the entity-specific sounds selected by vanilla LivingEntity. */
public interface LivingSoundAccess {
    SoundEvent tvd$getHurtSound(DamageSource source);

    SoundEvent tvd$getDeathSound();

    float tvd$getSoundVolume();
}
