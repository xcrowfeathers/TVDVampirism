package org.kuro.tvdvampirism.mixin;

import de.teamlapen.vampirism.api.VReference;
import de.teamlapen.vampirism.api.entity.factions.IFaction;
import de.teamlapen.vampirism.entity.factions.FactionPlayerHandler;
import de.teamlapen.vampirism.entity.player.ModPlayerEventHandler;
import org.kuro.tvdvampirism.faction.player.CustomFactionPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;

@Mixin(value = ModPlayerEventHandler.class, remap = false)
public abstract class VampireItemPermissionMixin {
    @Redirect(method = "checkItemUsePerm", at = @At(value = "INVOKE",
            target = "Lde/teamlapen/vampirism/entity/factions/FactionPlayerHandler;isInFaction(Lde/teamlapen/vampirism/api/entity/factions/IFaction;)Z"))
    private boolean tvd$inheritedItems(FactionPlayerHandler handler, IFaction<?> faction) {
        return faction == VReference.VAMPIRE_FACTION
                && handler.getCurrentFactionPlayer().orElse(null) instanceof CustomFactionPlayer<?> custom
                && custom.getLevel() > 0 || handler.isInFaction(faction);
    }
}
