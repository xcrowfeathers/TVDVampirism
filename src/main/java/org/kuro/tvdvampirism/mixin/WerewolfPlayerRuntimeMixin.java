package org.kuro.tvdvampirism.mixin;

import de.teamlapen.vampirism.entity.player.skills.SkillHandler;
import de.teamlapen.vampirism.api.entity.player.skills.ISkill;
import de.teamlapen.vampirism.effects.VampireNightVisionEffectInstance;
import de.teamlapen.werewolves.entities.player.werewolf.WerewolfPlayer;
import de.teamlapen.werewolves.util.Helper;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.kuro.tvdvampirism.compat.SpeciesCompatibility;
import org.kuro.tvdvampirism.player.SpeciesRules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/** Lets the existing Werewolf lifecycle consume hybrid level and skill state. */
@Mixin(value = WerewolfPlayer.class, remap = false)
public abstract class WerewolfPlayerRuntimeMixin {
    @Redirect(method = "onUpdate", at = @At(value = "INVOKE",
            target = "Lde/teamlapen/werewolves/entities/player/werewolf/WerewolfPlayer;getLevel()I"))
    private int tvd$customLevel(WerewolfPlayer instance) {
        var custom = SpeciesCompatibility.customPlayer(instance.asEntity());
        return custom != null && custom.hasWerewolfSkillBridge()
                ? custom.getLevel() : instance.getLevel();
    }

    @Redirect(method = "onUpdate", at = @At(value = "INVOKE",
            target = "Lde/teamlapen/vampirism/entity/player/skills/SkillHandler;isSkillEnabled(Lde/teamlapen/vampirism/api/entity/player/skills/ISkill;)Z"))
    @SuppressWarnings({"rawtypes", "unchecked"})
    private boolean tvd$canonicalSkills(SkillHandler handler, ISkill skill) {
        var self = (WerewolfPlayer) (Object) this;
        var custom = SpeciesCompatibility.customPlayer(self.asEntity());
        // Gameplay reads shared skills; only the custom attachment may consume their dirty flag and sync them.
        return custom != null && custom.hasWerewolfSkillBridge()
                ? custom.getSkillHandler().isSkillEnabled(skill) : handler.isSkillEnabled(skill);
    }

    @Redirect(method = "onUpdate", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/player/Player;removeEffect(Lnet/minecraft/core/Holder;)Z"))
    private boolean tvd$preserveVampireNightVision(Player player, Holder<MobEffect> effect) {
        var custom = SpeciesCompatibility.customPlayer(player);
        if (custom != null && custom.hasWerewolfSkillBridge()
                && player.getEffect(effect) instanceof VampireNightVisionEffectInstance) {
            return false;
        }
        return player.removeEffect(effect);
    }

    @Redirect(method = "onUpdate", at = @At(value = "INVOKE",
            target = "Lde/teamlapen/werewolves/util/Helper;isFullMoon(Lnet/minecraft/world/level/Level;)Z"))
    private boolean tvd$keepHybridFreeWill(Level level) {
        WerewolfPlayer self = (WerewolfPlayer) (Object) this;
        return !SpeciesRules.ignoresWerewolfTransformationRestrictions(self.asEntity())
                && Helper.isFullMoon(level);
    }
}
