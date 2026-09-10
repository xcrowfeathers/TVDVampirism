package org.kuro.tvdvampirism.mixin;

import de.teamlapen.werewolves.util.Helper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.kuro.tvdvampirism.compat.SpeciesCompatibility;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Extends Werewolves' semantic classification to the two hybrid species. */
@Mixin(value = Helper.class, remap = false)
public abstract class WerewolvesHelperMixin {
    @Inject(method = "isWerewolf(Lnet/minecraft/world/entity/player/Player;)Z",
            at = @At("HEAD"), cancellable = true)
    private static void tvd$isHybridPlayer(Player player, CallbackInfoReturnable<Boolean> cir) {
        if (SpeciesCompatibility.isCustomWerewolfLike(player)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "isWerewolf(Lnet/minecraft/world/entity/Entity;)Z",
            at = @At("HEAD"), cancellable = true)
    private static void tvd$isHybridEntity(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        if (SpeciesCompatibility.isCustomWerewolfLike(entity)) {
            cir.setReturnValue(true);
        }
    }
}
