package org.kuro.tvdvampirism.mixin;

import de.teamlapen.vampirism.api.entity.factions.IFaction;
import de.teamlapen.vampirism.entity.factions.FactionPredicate;
import net.minecraft.world.entity.LivingEntity;
import org.kuro.tvdvampirism.faction.FactionRelations;
import org.kuro.tvdvampirism.faction.VampireFamily;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FactionPredicate.class)
public abstract class FactionPredicateMixin {

    @Shadow
    @Final
    private IFaction<?> thisFaction;

    @Shadow
    @Final
    private boolean ignoreDisguise;

    @Shadow
    @Final
    private boolean player;

    @Shadow
    @Final
    @Nullable
    private IFaction<?> otherFaction;

    @Inject(
            method = "apply(Lnet/minecraft/world/entity/LivingEntity;)Z",
            at = @At("HEAD"),
            cancellable = true
    )
    private void applyLogicalRelations(
            LivingEntity target,
            CallbackInfoReturnable<Boolean> callback
    ) {
        IFaction<?> targetFaction = target == null
                ? null
                : FactionRelations.resolveFaction(target, ignoreDisguise);
        if (targetFaction != null
                && !FactionRelations.shouldProactivelyTarget(
                        thisFaction,
                        targetFaction
                )) {
            callback.setReturnValue(false);
        }
    }

    @Inject(
            method = "apply(Lnet/minecraft/world/entity/LivingEntity;)Z",
            at = @At("RETURN"),
            cancellable = true
    )
    private void includeVampireFamilyInVampireTargetFilters(
            LivingEntity target,
            CallbackInfoReturnable<Boolean> callback
    ) {
        if (callback.getReturnValueZ()
                || !player
                || target == null
                || !target.isAlive()) {
            return;
        }

        IFaction<?> targetFaction =
                FactionRelations.resolveFaction(target, ignoreDisguise);
        if (VampireFamily.matchesVampiricTargetFilter(
                otherFaction,
                targetFaction
        ) && FactionRelations.shouldProactivelyTarget(
                thisFaction,
                targetFaction
        )) {
            callback.setReturnValue(true);
        }
    }
}
