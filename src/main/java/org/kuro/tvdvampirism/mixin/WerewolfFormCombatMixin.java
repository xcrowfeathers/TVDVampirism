package org.kuro.tvdvampirism.mixin;

import de.teamlapen.werewolves.entities.player.werewolf.WerewolfPlayer;
import org.kuro.tvdvampirism.compat.SpeciesCompatibility;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = WerewolfPlayer.class, remap = false)
public abstract class WerewolfFormCombatMixin {

    @Redirect(
            method = "checkToolDamage",
            at = @At(
                    value = "INVOKE",
                    target = "Lde/teamlapen/werewolves/entities/player/werewolf/WerewolfPlayer;getLevel()I"
            )
    )
    private int tvd$useCustomWerewolfLevel(WerewolfPlayer werewolf) {
        var custom = SpeciesCompatibility.customPlayer(werewolf.asEntity());

        if (custom != null && custom.hasWerewolfSkillBridge()) {
            return custom.getLevel();
        }

        return werewolf.getLevel();
    }
}