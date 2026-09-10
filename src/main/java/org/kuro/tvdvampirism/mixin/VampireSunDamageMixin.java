package org.kuro.tvdvampirism.mixin;

import de.teamlapen.vampirism.entity.player.vampire.VampirePlayer;
import net.minecraft.world.level.LevelAccessor;
import org.kuro.tvdvampirism.compat.DaylightRingAccess;
import org.kuro.tvdvampirism.sun.SunDamageManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Gates Vampirism's existing sunlight predicate for equipped Daylight Rings. */
@Mixin(value = VampirePlayer.class, remap = false)
public abstract class VampireSunDamageMixin {
    @Inject(
            method = "isGettingSundamage(Lnet/minecraft/world/level/LevelAccessor;Z)Z",
            at = @At("HEAD"),
            cancellable = true
    )
    private void tvd$daylightRing(
            LevelAccessor level,
            boolean refresh,
            CallbackInfoReturnable<Boolean> cir
    ) {
        var player = ((VampirePlayer) (Object) this).asEntity();
        if (DaylightRingAccess.protectsFromSun(player)) {
            SunDamageManager.suppressSunlight(player);
            cir.setReturnValue(false);
        }
    }
}
