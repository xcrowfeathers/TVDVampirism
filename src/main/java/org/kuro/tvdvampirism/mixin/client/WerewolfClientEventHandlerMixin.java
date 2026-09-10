package org.kuro.tvdvampirism.mixin.client;

import de.teamlapen.werewolves.client.core.ClientEventHandler;
import de.teamlapen.werewolves.entities.player.werewolf.WerewolfPlayer;
import net.minecraft.world.entity.player.Player;
import org.kuro.tvdvampirism.client.visual.SpeciesClientVisualAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = {ClientEventHandler.class}, remap = false)
public abstract class WerewolfClientEventHandlerMixin {
    @Redirect(method = {"onPlayerRender", "onRenderNamePlate"}, require = 2, at = @At(value = "INVOKE",
            target = "Lde/teamlapen/werewolves/util/Helper;isWerewolf(Lnet/minecraft/world/entity/player/Player;)Z"))
    private boolean tvdvampirism$visualSpecies(Player player) {
        return SpeciesClientVisualAccess.hasWerewolfVisuals(player);
    }

    @Redirect(method = "onRenderNamePlate", at = @At(value = "INVOKE",
            target = "Lde/teamlapen/werewolves/entities/player/werewolf/WerewolfPlayer;get(Lnet/minecraft/world/entity/player/Player;)Lde/teamlapen/werewolves/entities/player/werewolf/WerewolfPlayer;"))
    private WerewolfPlayer tvdvampirism$visualPlayer(Player player) {
        return SpeciesClientVisualAccess.werewolf(player);
    }
}
