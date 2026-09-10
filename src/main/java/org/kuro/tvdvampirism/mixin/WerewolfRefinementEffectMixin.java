package org.kuro.tvdvampirism.mixin;

import de.teamlapen.vampirism.api.entity.player.skills.ISkillHandler;
import de.teamlapen.vampirism.entity.player.actions.ActionHandler;
import de.teamlapen.werewolves.entities.player.werewolf.WerewolfPlayer;
import org.kuro.tvdvampirism.compat.SpeciesCompatibility;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/** Routes the stock Werewolf kill refinement through a Hybrid's canonical state. */
@Mixin(value = WerewolfPlayer.class, remap = false)
public abstract class WerewolfRefinementEffectMixin {

    @Redirect(method = "onEntityKilled", at = @At(value = "INVOKE",
            target = "Lde/teamlapen/werewolves/entities/player/werewolf/WerewolfPlayer;getSkillHandler()Lde/teamlapen/vampirism/api/entity/player/skills/ISkillHandler;"))
    private ISkillHandler<?> tvd$killRefinementHandler(WerewolfPlayer werewolf) {
        var custom = SpeciesCompatibility.customPlayer(werewolf.asEntity());
        return custom == null || !custom.hasWerewolfSkillBridge()
                ? werewolf.getSkillHandler()
                : custom.getSkillHandler();
    }

    @Redirect(method = "onEntityKilled", at = @At(value = "FIELD",
            target = "Lde/teamlapen/werewolves/entities/player/werewolf/WerewolfPlayer;actionHandler:Lde/teamlapen/vampirism/entity/player/actions/ActionHandler;"))
    private ActionHandler<?> tvd$killRefinementActions(WerewolfPlayer werewolf) {
        var custom = SpeciesCompatibility.customPlayer(werewolf.asEntity());
        return custom == null || !custom.hasWerewolfSkillBridge()
                ? (ActionHandler<?>) werewolf.getActionHandler()
                : custom.getWerewolfSkillBridge().stockActionHandler();
    }
}
