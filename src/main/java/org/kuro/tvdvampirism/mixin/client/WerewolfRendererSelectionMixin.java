package org.kuro.tvdvampirism.mixin.client;

import de.teamlapen.werewolves.client.render.WerewolfPlayerRenderer;
import net.minecraft.world.entity.player.Player;
import org.kuro.tvdvampirism.client.visual.SpeciesClientVisualAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = {WerewolfPlayerRenderer.class}, remap = false)
public abstract class WerewolfRendererSelectionMixin {
    @Redirect(method = "getWerewolfRenderer", at = @At(value = "INVOKE",
            target = "Lde/teamlapen/werewolves/util/Helper;isWerewolf(Lnet/minecraft/world/entity/player/Player;)Z"))
    private static boolean tvdvampirism$visualSpecies(Player player) {
        return SpeciesClientVisualAccess.hasWerewolfVisuals(player);
    }
}
