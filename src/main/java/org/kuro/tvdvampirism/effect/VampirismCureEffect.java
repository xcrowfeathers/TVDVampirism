package org.kuro.tvdvampirism.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/** Marker effect whose vanilla duration is the cure's only countdown. */
public final class VampirismCureEffect extends MobEffect {
    public VampirismCureEffect() {
        super(MobEffectCategory.BENEFICIAL, 0xB53A45);
    }
}
