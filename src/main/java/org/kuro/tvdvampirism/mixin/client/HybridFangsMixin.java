package org.kuro.tvdvampirism.mixin.client;

import de.teamlapen.werewolves.client.core.ModHUDOverlay;
import de.teamlapen.werewolves.entities.player.werewolf.WerewolfPlayer;
import de.teamlapen.werewolves.util.Helper;
import net.minecraft.world.entity.player.Player;
import org.kuro.tvdvampirism.player.SpeciesRules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;

@Mixin(value = ModHUDOverlay.class, remap = false)
public abstract class HybridFangsMixin {
    @Redirect(method = "renderCrosshair", at = @At(value = "INVOKE",
            target = "Lde/teamlapen/werewolves/util/Helper;isWerewolf(Lnet/minecraft/world/entity/player/Player;)Z"))
    private boolean tvd$hybridFangs(Player player) {
        return player != null && (Helper.isWerewolf(player) || SpeciesRules.canUseHumanWolfBite(player));
    }

    @Redirect(method = "renderCrosshair", at = @At(value = "INVOKE",
            target = "Lde/teamlapen/werewolves/entities/player/werewolf/WerewolfPlayer;canBite()Z"))
    private boolean tvd$readyTarget(WerewolfPlayer wolf) {
        if (!SpeciesRules.canUseHumanWolfBite(wolf.asEntity())) return wolf.canBite();
        return org.kuro.tvdvampirism.client.action.SpeciesClientActionAccess.hasReadyHybridBiteTarget();
    }
}
