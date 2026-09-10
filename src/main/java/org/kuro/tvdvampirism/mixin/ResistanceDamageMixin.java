package org.kuro.tvdvampirism.mixin;

import de.teamlapen.vampirism.api.EnumStrength;
import de.teamlapen.vampirism.api.entity.vampire.IVampire;
import de.teamlapen.vampirism.util.DamageHandler;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.effect.MobEffectInstance;
import org.kuro.tvdvampirism.ability.ResistanceSkills;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value=DamageHandler.class,remap=false)
public abstract class ResistanceDamageMixin {
    @Inject(method="affectVampireGarlic",at=@At("HEAD"),cancellable=true)
    private static void tvd$resistGarlic(IVampire vampire,EnumStrength strength,float multiplier,boolean ambient,CallbackInfo ci) {
        if (ResistanceSkills.garlic(vampire.asEntity())) ci.cancel();
    }
    @Redirect(method="affectEntityHolyWaterSplash(Lnet/minecraft/world/entity/LivingEntity;Lde/teamlapen/vampirism/api/EnumStrength;DZLnet/minecraft/world/entity/LivingEntity;)V",
            at=@At(value="INVOKE",target="Lnet/minecraft/world/entity/LivingEntity;addEffect(Lnet/minecraft/world/effect/MobEffectInstance;)Z"))
    private static boolean tvd$reduceHolySymptoms(LivingEntity entity,MobEffectInstance effect) {
        int duration=Math.round(effect.getDuration()*ResistanceSkills.holyWaterMultiplier(entity));
        return duration>0 && entity.addEffect(new MobEffectInstance(effect.getEffect(),duration,effect.getAmplifier(),effect.isAmbient(),effect.isVisible(),effect.showIcon()));
    }
}
