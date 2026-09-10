package org.kuro.tvdvampirism.mixin;

import de.teamlapen.vampirism.api.VReference;
import de.teamlapen.werewolves.api.WReference;
import de.teamlapen.vampirism.entity.factions.PlayableFaction;
import net.minecraft.world.entity.player.Player;
import org.kuro.tvdvampirism.compat.SpeciesCompatibility;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import java.util.Optional;

@Mixin(value = PlayableFaction.class, remap = false)
public abstract class VampireCapabilityMixin {
    @Inject(method = "getPlayerCapability", at = @At("HEAD"), cancellable = true)
    private void tvd$capability(Player player, CallbackInfoReturnable<Optional<?>> cir) {
        if ((Object) this == VReference.VAMPIRE_FACTION) {
            var custom = SpeciesCompatibility.customPlayer(player);
            if (custom != null) cir.setReturnValue(Optional.of(custom.getVampireSkillBridge()));
        } else if ((Object) this == WReference.WEREWOLF_FACTION) {
            var custom = SpeciesCompatibility.customPlayer(player);
            if (custom != null && custom.hasWerewolfSkillBridge()) {
                cir.setReturnValue(Optional.of(custom.getWerewolfSkillBridge()));
            }
        }
    }
}
