package org.kuro.tvdvampirism.mixin;

import de.teamlapen.vampirism.entity.player.vampire.VampirePlayer;
import de.teamlapen.vampirism.api.EnumStrength;
import org.kuro.tvdvampirism.ability.ResistanceSkills;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value=VampirePlayer.class,remap=false)
public abstract class GarlicResistanceMixin {
    @Inject(method="doesResistGarlic",at=@At("HEAD"),cancellable=true)
    private void tvd$garlicResistance(EnumStrength strength,CallbackInfoReturnable<Boolean> cir) {
        if (ResistanceSkills.garlic(((VampirePlayer)(Object)this).asEntity())) cir.setReturnValue(true);
    }
}
