package org.kuro.tvdvampirism.mixin.client;

import de.teamlapen.vampirism.api.entity.player.IFactionPlayer;
import de.teamlapen.vampirism.api.entity.player.actions.IAction;
import de.teamlapen.vampirism.client.gui.screens.SelectActionRadialScreen;
import org.kuro.tvdvampirism.client.action.SpeciesClientActionAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;


@Mixin(value = SelectActionRadialScreen.class, remap = false)
public abstract class SelectActionRadialScreenMixin {

    @Redirect(
            method = "drawSlice",
            at = @At(
                    value = "INVOKE",
                    target = "Lde/teamlapen/vampirism/api/entity/player/actions/IAction;canUse(Lde/teamlapen/vampirism/api/entity/player/IFactionPlayer;)Lde/teamlapen/vampirism/api/entity/player/actions/IAction$PERM;",
                    remap = false
            )
    )
    private IAction.PERM tvd$canUseInheritedAction(
            IAction<?> action,
            IFactionPlayer<?> player
    ) {
        return SpeciesClientActionAccess.canUse(action, player);
    }
}
