package org.kuro.tvdvampirism.mixin;

import de.teamlapen.vampirism.api.VReference;
import de.teamlapen.vampirism.entity.vampire.VampireBaronEntity;
import net.minecraft.world.entity.LivingEntity;
import org.kuro.tvdvampirism.faction.FactionRelations;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Vampire barons use a private level predicate instead of Vampirism's shared
 * faction predicate for proactive player targeting.
 */
@Mixin(VampireBaronEntity.class)
public abstract class VampireBaronEntityMixin {

    @Inject(
            method = "isLowerLevel",
            at = @At("HEAD"),
            cancellable = true
    )
    private void applyLogicalRelationsToProactiveTargeting(
            LivingEntity target,
            CallbackInfoReturnable<Boolean> callback
    ) {
        if (!FactionRelations.shouldProactivelyTarget(
                VReference.VAMPIRE_FACTION,
                FactionRelations.resolveFaction(target, false)
        )) {
            callback.setReturnValue(false);
        }
    }
}
