package org.kuro.tvdvampirism.mixin;

import net.minecraft.core.Holder;
import net.minecraft.server.commands.EffectCommands;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.LivingEntity;
import org.kuro.tvdvampirism.bite.WerewolfBiteManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/** Administrative clear is intentionally distinct from milk and gameplay removal. */
@Mixin(EffectCommands.class)
public abstract class BiteEffectCommandMixin {
    @Redirect(method = "giveEffect", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/LivingEntity;addEffect(Lnet/minecraft/world/effect/MobEffectInstance;Lnet/minecraft/world/entity/Entity;)Z"))
    private static boolean tvd$give(LivingEntity target, net.minecraft.world.effect.MobEffectInstance effect,
            net.minecraft.world.entity.Entity source) {
        return WerewolfBiteManager.applyByCommand(target, effect, source);
    }
    @Redirect(method = "clearEffects", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/LivingEntity;removeAllEffects()Z"))
    private static boolean tvd$clearAll(LivingEntity target) {
        return WerewolfBiteManager.clearByCommand(target, null);
    }

    @Redirect(method = "clearEffect", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/LivingEntity;removeEffect(Lnet/minecraft/core/Holder;)Z"))
    private static boolean tvd$clearOne(LivingEntity target, Holder<MobEffect> effect) {
        return WerewolfBiteManager.clearByCommand(target, effect);
    }
}
