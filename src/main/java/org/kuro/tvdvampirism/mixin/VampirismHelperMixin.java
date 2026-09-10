package org.kuro.tvdvampirism.mixin;

import de.teamlapen.vampirism.api.entity.factions.IFactionPlayerHandler;
import de.teamlapen.vampirism.api.items.IFactionLevelItem;
import de.teamlapen.vampirism.util.Helper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.kuro.tvdvampirism.compat.SpeciesCompatibility;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Extends only Vampirism's semantic "is vampire" and inherited faction-item
 * gates. It does not change the player's registered faction.
 */
@Mixin(value = Helper.class, remap = false)
public abstract class VampirismHelperMixin {

    @Inject(
            method = "isVampire(Lnet/minecraft/world/entity/player/Player;)Z",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void tvd$isCustomVampirePlayer(
            Player player,
            CallbackInfoReturnable<Boolean> cir
    ) {
        if (SpeciesCompatibility.isCustomVampireLike(player)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(
            method = "isVampire(Lnet/minecraft/world/entity/Entity;)Z",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void tvd$isCustomVampireEntity(
            Entity entity,
            CallbackInfoReturnable<Boolean> cir
    ) {
        if (SpeciesCompatibility.isCustomVampireLike(entity)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(
            method = "canUseFactionItem",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void tvd$allowInheritedVampireFactionItem(
            ItemStack stack,
            IFactionLevelItem<?> item,
            IFactionPlayerHandler handler,
            CallbackInfoReturnable<Boolean> cir
    ) {
        Boolean result = SpeciesCompatibility.canUseInheritedFactionItem(
                stack,
                item,
                handler
        );

        if (result != null) {
            cir.setReturnValue(result);
        }
    }
}
