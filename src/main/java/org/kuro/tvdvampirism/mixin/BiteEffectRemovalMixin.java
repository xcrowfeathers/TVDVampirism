package org.kuro.tvdvampirism.mixin;

import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import org.kuro.tvdvampirism.bite.WerewolfBiteManager;
import org.kuro.tvdvampirism.registry.BiteContent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** This low-level removal method does not fire NeoForge's removal event. */
@Mixin(LivingEntity.class)
public abstract class BiteEffectRemovalMixin {
    @ModifyVariable(method = "addEffect(Lnet/minecraft/world/effect/MobEffectInstance;Lnet/minecraft/world/entity/Entity;)Z",
            at = @At("HEAD"), argsOnly = true)
    private MobEffectInstance tvd$normalizeBite(MobEffectInstance effect) {
        return WerewolfBiteManager.normalizeEffect((LivingEntity) (Object) this, effect);
    }
    @Inject(method = "removeEffectNoUpdate", at = @At("HEAD"), cancellable = true)
    private void tvd$protectBite(Holder<MobEffect> effect, CallbackInfoReturnable<MobEffectInstance> cir) {
        if (effect.equals(BiteContent.WEREWOLF_BITE) && WerewolfBiteManager.protectRemoval((LivingEntity) (Object) this))
            cir.setReturnValue(null);
    }
}
