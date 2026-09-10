package org.kuro.tvdvampirism.mixin.client;

import de.teamlapen.vampirism.api.entity.player.IFactionPlayer;
import de.teamlapen.vampirism.api.entity.player.actions.IAction;
import de.teamlapen.vampirism.api.util.ItemOrdering;
import de.teamlapen.vampirism.client.ClientConfigHelper;
import de.teamlapen.vampirism.client.gui.screens.EditSelectActionScreen;
import org.kuro.tvdvampirism.client.action.SpeciesClientActionAccess;
import org.kuro.tvdvampirism.faction.player.CustomFactionPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

@Mixin(value = EditSelectActionScreen.class, remap = false)
public abstract class EditSelectActionScreenMixin {

    @Inject(
            method = "getOrdering",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void tvd$createCustomSpeciesOrdering(
            IFactionPlayer<?> player,
            CallbackInfoReturnable<ItemOrdering<IAction<?>>> cir
    ) {
        if (!(player instanceof CustomFactionPlayer<?> customPlayer)) {
            return;
        }

        List<IAction<?>> ordering =
                ClientConfigHelper.getActionOrder(customPlayer.getFaction())
                        .stream()
                        .filter(action ->
                                action.showInSelectAction(
                                        customPlayer.asEntity()
                                )
                        )
                        .toList();

        List<IAction<?>> allActions =
                SpeciesClientActionAccess.selectableActionCatalog(
                        customPlayer
                );

        cir.setReturnValue(
                new ItemOrdering<>(
                        ordering,
                        new ArrayList<>(),
                        () -> allActions
                )
        );
    }
}
