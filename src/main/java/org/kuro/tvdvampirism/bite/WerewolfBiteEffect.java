package org.kuro.tvdvampirism.bite;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

public final class WerewolfBiteEffect extends MobEffect {
    public WerewolfBiteEffect() { super(MobEffectCategory.HARMFUL, 0x586B35); }
    @Override public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) { return true; }
    @Override public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        if (!entity.level().isClientSide) {
            WerewolfBiteManager.tickInfection(entity);
        }
        return true;
    }
    @Override public void fillEffectCures(java.util.Set<net.neoforged.neoforge.common.EffectCure> cures,
            net.minecraft.world.effect.MobEffectInstance instance) { cures.clear(); }
}
