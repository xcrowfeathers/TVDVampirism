package org.kuro.tvdvampirism.mixin;

import de.teamlapen.vampirism.entity.player.vampire.VampirePlayer;
import net.minecraft.world.entity.player.Player;
import org.kuro.tvdvampirism.compat.SpeciesCompatibility;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import java.util.Optional;

@Mixin(value = VampirePlayer.class, remap = false)
public abstract class VampirePlayerLookupMixin {
    @Inject(method = "determineBiteType", at = @At("HEAD"), cancellable = true)
    private void tvd$feedingEligibility(net.minecraft.world.entity.LivingEntity target,
            CallbackInfoReturnable<de.teamlapen.vampirism.api.entity.player.vampire.IVampirePlayer.BITE_TYPE> cir) {
        if (!org.kuro.tvdvampirism.blood.AugustineFeedingPolicy.canFeedOn(
                ((VampirePlayer) (Object) this).asEntity(), target))
            cir.setReturnValue(de.teamlapen.vampirism.api.entity.player.vampire.IVampirePlayer.BITE_TYPE.NONE);
    }

    @Inject(method = "get", at = @At("HEAD"), cancellable = true)
    private static void tvd$player(Player player, CallbackInfoReturnable<VampirePlayer> cir) {
        var custom = SpeciesCompatibility.customPlayer(player);
        if (custom != null) cir.setReturnValue(custom.getVampireSkillBridge());
    }

    @Inject(method = "getOpt", at = @At("HEAD"), cancellable = true)
    private static void tvd$optionalPlayer(Player player, CallbackInfoReturnable<Optional<VampirePlayer>> cir) {
        var custom = SpeciesCompatibility.customPlayer(player);
        if (custom != null) cir.setReturnValue(Optional.of(custom.getVampireSkillBridge()));
    }
}
