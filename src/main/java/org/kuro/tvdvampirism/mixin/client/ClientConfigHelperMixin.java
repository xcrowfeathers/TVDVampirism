package org.kuro.tvdvampirism.mixin.client;

import de.teamlapen.vampirism.api.entity.factions.IPlayableFaction;
import de.teamlapen.vampirism.api.entity.player.actions.IAction;
import de.teamlapen.vampirism.client.ClientConfigHelper;
import org.kuro.tvdvampirism.client.action.SpeciesClientActionAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(value = ClientConfigHelper.class, remap = false)
public abstract class ClientConfigHelperMixin {

    @Inject(
            method = "getDefaultActionOrder(Lde/teamlapen/vampirism/api/entity/factions/IPlayableFaction;)Ljava/util/List;",
            at = @At("RETURN"),
            cancellable = true
    )
    private static void tvd$includeInheritedDefaultActions(
            IPlayableFaction<?> faction,
            CallbackInfoReturnable<List<IAction<?>>> cir
    ) {
        var customPlayer =
                SpeciesClientActionAccess.currentCustomPlayer(faction);

        if (customPlayer == null) {
            return;
        }

        cir.setReturnValue(
                SpeciesClientActionAccess.inheritedDefaultActionOrder(
                        customPlayer,
                        cir.getReturnValue()
                )
        );
    }

    /** Older saves can hold an empty action order, so repair it here. */
    @Inject(
            method = "getActionOrder(Lde/teamlapen/vampirism/api/entity/factions/IPlayableFaction;)Ljava/util/List;",
            at = @At("RETURN"),
            cancellable = true
    )
    private static void tvd$repairCachedEmptyActionOrder(
            IPlayableFaction<?> faction,
            CallbackInfoReturnable<List<IAction<?>>> cir
    ) {
        var customPlayer =
                SpeciesClientActionAccess.currentCustomPlayer(faction);

        if (customPlayer == null || !cir.getReturnValue().isEmpty()) {
            return;
        }

        List<IAction<?>> repaired =
                SpeciesClientActionAccess.inheritedDefaultActionOrder(
                        customPlayer,
                        List.of()
                );

        if (repaired.isEmpty()) {
            return;
        }

        ClientConfigHelper.saveActionOrder(
                faction.getID(),
                repaired
        );
        cir.setReturnValue(repaired);
    }
}
