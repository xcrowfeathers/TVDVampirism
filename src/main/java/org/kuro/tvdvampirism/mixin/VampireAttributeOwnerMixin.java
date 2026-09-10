package org.kuro.tvdvampirism.mixin;

import de.teamlapen.vampirism.entity.player.VampirismPlayerAttributes;
import net.minecraft.world.entity.player.Player;
import org.kuro.tvdvampirism.compat.SpeciesCompatibility;
import org.kuro.tvdvampirism.compat.VampireLevelAccess;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = VampirismPlayerAttributes.class, remap = false)
public abstract class VampireAttributeOwnerMixin implements VampireLevelAccess {
    @Unique private Player tvd$player;
    @Shadow public int vampireLevel;

    @Inject(method = "get", at = @At("RETURN"))
    private static void tvd$bindPlayer(Player player, CallbackInfoReturnable<VampirismPlayerAttributes> cir) {
        ((VampireLevelAccess) cir.getReturnValue()).tvd$setPlayer(player);
    }

    public void tvd$setPlayer(Player player) { tvd$player = player; }

    public int tvd$vampireLevel() {
        var custom = SpeciesCompatibility.customPlayer(tvd$player);
        return custom == null ? vampireLevel : custom.getLevel();
    }
}
