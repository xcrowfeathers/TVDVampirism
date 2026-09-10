package org.kuro.tvdvampirism.mixin;

import de.teamlapen.werewolves.api.entities.player.IWerewolfPlayer;
import de.teamlapen.werewolves.entities.player.werewolf.WerewolfPlayerSpecialAttributes;
import de.teamlapen.werewolves.entities.player.werewolf.actions.WerewolfFormAction;
import de.teamlapen.werewolves.util.Helper;
import net.minecraft.world.level.Level;
import org.kuro.tvdvampirism.compat.HybridFormBlood;
import org.kuro.tvdvampirism.player.SpeciesRules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = WerewolfFormAction.class, remap = false)
public abstract class HybridFormResourceMixin {
    @Inject(method = "usesTransformationTime", at = @At("HEAD"), cancellable = true)
    private void tvd$noHybridTimer(IWerewolfPlayer wolf, CallbackInfoReturnable<Boolean> cir) {
        if (SpeciesRules.canUseHumanWolfBite(wolf.asEntity())) cir.setReturnValue(false);
    }

    @Redirect(method = "canBeUsedBy", at = @At(value = "FIELD",
            target = "Lde/teamlapen/werewolves/entities/player/werewolf/WerewolfPlayerSpecialAttributes;transformationTime:D"))
    private double tvd$ignoreHybridResource(WerewolfPlayerSpecialAttributes attributes, IWerewolfPlayer wolf) {
        return SpeciesRules.canUseHumanWolfBite(wolf.asEntity()) ? 0 : attributes.transformationTime;
    }

    @Redirect(method = "canBeUsedBy", at = @At(value = "INVOKE",
            target = "Lde/teamlapen/werewolves/util/Helper;isFullMoon(Lnet/minecraft/world/level/Level;)Z"))
    private boolean tvd$allowHybridReversion(Level level, IWerewolfPlayer wolf) {
        return !SpeciesRules.canUseHumanWolfBite(wolf.asEntity()) && Helper.isFullMoon(level);
    }

    @Inject(method = "onUpdate", at = @At("RETURN"), cancellable = true)
    private void tvd$payWithBlood(IWerewolfPlayer wolf, CallbackInfoReturnable<Boolean> cir) {
        if (HybridFormBlood.shouldEndForm(wolf)) cir.setReturnValue(true);
    }
}
