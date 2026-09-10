package org.kuro.tvdvampirism.mixin;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.kuro.tvdvampirism.compat.LivingSoundAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(LivingEntity.class)
public interface LivingSoundMixin extends LivingSoundAccess {
    @Override
    @Invoker("getHurtSound")
    SoundEvent tvd$getHurtSound(DamageSource source);

    @Override
    @Invoker("getDeathSound")
    SoundEvent tvd$getDeathSound();

    @Override
    @Invoker("getSoundVolume")
    float tvd$getSoundVolume();
}
