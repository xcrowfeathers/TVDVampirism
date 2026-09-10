package org.kuro.tvdvampirism.mixin;

import net.minecraft.world.entity.LivingEntity;
import org.kuro.tvdvampirism.skill.CustomSkills;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Prevents compelled entities from bypassing zero movement speed by jumping. */
@Mixin(LivingEntity.class)
public abstract class CompelledMovementMixin {
    @Inject(method = "jumpFromGround", at = @At("HEAD"), cancellable = true)
    private void tvd$preventCompelledJump(CallbackInfo ci) {
        if (((LivingEntity) (Object) this).hasEffect(CustomSkills.COMPELLED)) ci.cancel();
    }
}
