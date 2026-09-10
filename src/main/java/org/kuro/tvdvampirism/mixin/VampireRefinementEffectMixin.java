package org.kuro.tvdvampirism.mixin;

import de.teamlapen.vampirism.api.entity.player.actions.IActionHandler;
import de.teamlapen.vampirism.api.entity.player.skills.ISkillHandler;
import de.teamlapen.vampirism.entity.player.actions.ActionHandler;
import de.teamlapen.vampirism.entity.player.skills.SkillHandler;
import de.teamlapen.vampirism.entity.player.vampire.VampirePlayer;
import org.kuro.tvdvampirism.compat.SpeciesCompatibility;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/** Routes stock event-driven Vampire refinements to the custom player's canonical state. */
@Mixin(value = VampirePlayer.class, remap = false)
public abstract class VampireRefinementEffectMixin {

    @Redirect(method = "onEntityAttacked", at = @At(value = "FIELD",
            target = "Lde/teamlapen/vampirism/entity/player/vampire/VampirePlayer;skillHandler:Lde/teamlapen/vampirism/entity/player/skills/SkillHandler;"))
    private SkillHandler<?> tvd$damageRefinementHandler(VampirePlayer vampire) {
        var custom = SpeciesCompatibility.customPlayer(vampire.asEntity());
        return custom == null
                ? (SkillHandler<?>) vampire.getSkillHandler()
                : (SkillHandler<?>) custom.getSkillHandler();
    }

    @Redirect(method = "onEntityAttacked", at = @At(value = "INVOKE",
            target = "Lde/teamlapen/vampirism/entity/player/vampire/VampirePlayer;useBlood(IZ)Z"))
    private boolean tvd$damageRefinementBlood(
            VampirePlayer vampire,
            int amount,
            boolean allowPartial
    ) {
        var custom = SpeciesCompatibility.customPlayer(vampire.asEntity());
        return custom == null
                ? vampire.useBlood(amount, allowPartial)
                : custom.useBlood(amount, allowPartial);
    }

    @Redirect(method = "onEntityAttacked", at = @At(value = "FIELD",
            target = "Lde/teamlapen/vampirism/entity/player/vampire/VampirePlayer;actionHandler:Lde/teamlapen/vampirism/entity/player/actions/ActionHandler;"))
    private ActionHandler<?> tvd$damageRefinementActions(VampirePlayer vampire) {
        var custom = SpeciesCompatibility.customPlayer(vampire.asEntity());
        return custom == null
                ? (ActionHandler<?>) vampire.getActionHandler()
                : custom.getVampireSkillBridge().stockActionHandler();
    }

    @Redirect(method = "onEntityKilled", at = @At(value = "INVOKE",
            target = "Lde/teamlapen/vampirism/entity/player/vampire/VampirePlayer;getSkillHandler()Lde/teamlapen/vampirism/api/entity/player/skills/ISkillHandler;"))
    private ISkillHandler<?> tvd$killRefinementHandler(VampirePlayer vampire) {
        var custom = SpeciesCompatibility.customPlayer(vampire.asEntity());
        return custom == null ? vampire.getSkillHandler() : custom.getSkillHandler();
    }

    @Redirect(method = "onEntityKilled", at = @At(value = "INVOKE",
            target = "Lde/teamlapen/vampirism/entity/player/vampire/VampirePlayer;getActionHandler()Lde/teamlapen/vampirism/api/entity/player/actions/IActionHandler;"))
    private IActionHandler<?> tvd$killRefinementActions(VampirePlayer vampire) {
        var custom = SpeciesCompatibility.customPlayer(vampire.asEntity());
        return custom == null ? vampire.getActionHandler() : custom.getActionHandler();
    }
}
