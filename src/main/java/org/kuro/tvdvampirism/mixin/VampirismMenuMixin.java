package org.kuro.tvdvampirism.mixin;

import de.teamlapen.vampirism.api.entity.factions.IPlayableFaction;
import de.teamlapen.vampirism.inventory.VampirismMenu;
import org.kuro.tvdvampirism.compat.SpeciesCompatibility;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/** Keeps the stock three-slot refinement menu while accepting compatible families. */
@Mixin(value = VampirismMenu.class, remap = false)
public abstract class VampirismMenuMixin {

    @Redirect(method = {"lambda$static$1", "lambda$static$2", "lambda$static$3"},
            require = 3, at = @At(value = "INVOKE",
            target = "Lde/teamlapen/vampirism/api/entity/factions/IPlayableFaction;equals(Ljava/lang/Object;)Z"))
    private static boolean tvd$acceptCompatibleRefinement(
            IPlayableFaction<?> faction,
            Object exclusiveFaction
    ) {
        return SpeciesCompatibility.acceptsRefinementFaction(faction, exclusiveFaction);
    }
}
