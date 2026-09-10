package org.kuro.tvdvampirism.mixin;

import de.teamlapen.vampirism.api.VReference;
import de.teamlapen.vampirism.api.entity.factions.IPlayableFaction;
import de.teamlapen.vampirism.entity.factions.FactionPlayerHandler;
import net.minecraft.world.entity.player.Player;
import org.kuro.tvdvampirism.compat.SpeciesCompatibility;
import org.kuro.tvdvampirism.faction.VampireFamily;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import java.util.Optional;

@Mixin(value = FactionPlayerHandler.class, remap = false)
public abstract class VampireFactionLevelMixin {
    @Shadow private IPlayableFaction<?> currentFaction;
    @Shadow private int currentLevel;

    @Inject(method = "getCurrentLevel(Lde/teamlapen/vampirism/api/entity/factions/IPlayableFaction;)I",
            at = @At("HEAD"), cancellable = true)
    private void tvd$readLevel(IPlayableFaction<?> faction, CallbackInfoReturnable<Integer> cir) {
        if (faction == VReference.VAMPIRE_FACTION && VampireFamily.isTechnicalSpecies(currentFaction))
            cir.setReturnValue(currentLevel);
    }

    @ModifyVariable(method = "setFactionLevel", at = @At("HEAD"), argsOnly = true)
    private IPlayableFaction<?> tvd$writeLevel(IPlayableFaction<?> faction) {
        return faction == VReference.VAMPIRE_FACTION && VampireFamily.isTechnicalSpecies(currentFaction)
                ? currentFaction : faction;
    }

    // Leaving the real vampire faction must reset its real attachment, not the
    // new custom species' facade. Current-faction callbacks remain unchanged.
    @Redirect(method = "notifyFaction", require = 2, at = @At(value = "INVOKE",
            target = "Lde/teamlapen/vampirism/api/entity/factions/IPlayableFaction;getPlayerCapability(Lnet/minecraft/world/entity/player/Player;)Ljava/util/Optional;"))
    private Optional<?> tvd$lifecycleCapability(IPlayableFaction<?> faction, Player player) {
        return faction == VReference.VAMPIRE_FACTION
                ? Optional.of(SpeciesCompatibility.rawVampire(player)) : faction.getPlayerCapability(player);
    }
}
