package org.kuro.tvdvampirism.mixin;

import de.teamlapen.werewolves.entities.player.werewolf.WerewolfPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.kuro.tvdvampirism.compat.SpeciesCompatibility;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

/** Exposes hybrid species to stock Werewolves consumers without changing faction identity. */
@Mixin(value = WerewolfPlayer.class, remap = false)
public abstract class WerewolfPlayerLookupMixin {
    @Inject(method = "get", at = @At("HEAD"), cancellable = true)
    private static void tvd$player(Player player, CallbackInfoReturnable<WerewolfPlayer> cir) {
        var custom = SpeciesCompatibility.customPlayer(player);
        if (custom != null && custom.hasWerewolfSkillBridge()) {
            cir.setReturnValue(custom.getWerewolfSkillBridge());
        }
    }

    @Inject(method = "getOpt", at = @At("HEAD"), cancellable = true)
    private static void tvd$optionalPlayer(Player player,
                                           CallbackInfoReturnable<Optional<WerewolfPlayer>> cir) {
        var custom = SpeciesCompatibility.customPlayer(player);
        if (custom != null && custom.hasWerewolfSkillBridge()) {
            cir.setReturnValue(Optional.of(custom.getWerewolfSkillBridge()));
        }
    }

    @Inject(method = "getOptEx", at = @At("HEAD"), cancellable = true)
    private static void tvd$optionalEntity(Entity entity,
                                           CallbackInfoReturnable<Optional<WerewolfPlayer>> cir) {
        if (entity instanceof Player player) {
            var custom = SpeciesCompatibility.customPlayer(player);
            if (custom != null && custom.hasWerewolfSkillBridge()) {
                cir.setReturnValue(Optional.of(custom.getWerewolfSkillBridge()));
            }
        }
    }
}
