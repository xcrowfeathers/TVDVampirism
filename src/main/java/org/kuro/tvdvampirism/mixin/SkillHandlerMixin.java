package org.kuro.tvdvampirism.mixin;

import de.teamlapen.vampirism.api.entity.factions.ISkillTree;
import de.teamlapen.vampirism.api.entity.factions.IPlayableFaction;
import de.teamlapen.vampirism.api.entity.player.IFactionPlayer;
import de.teamlapen.vampirism.api.entity.player.skills.ISkill;
import de.teamlapen.vampirism.api.entity.player.skills.ISkillHandler;
import de.teamlapen.vampirism.data.ISkillTreeData;
import de.teamlapen.vampirism.api.entity.player.skills.ISkillPointProvider;
import de.teamlapen.vampirism.entity.player.skills.SkillHandler;
import net.minecraft.core.Holder;
import org.kuro.tvdvampirism.faction.player.CustomFactionPlayer;
import org.kuro.tvdvampirism.compat.SpeciesCompatibility;
import org.kuro.tvdvampirism.skill.SpeciesSkillAccess;
import org.kuro.tvdvampirism.skill.SpeciesSkillPoints;
import org.kuro.tvdvampirism.skill.SpeciesSkillRules;
import org.kuro.tvdvampirism.skill.SpeciesSkillTreeData;
import org.kuro.tvdvampirism.skill.StockSkillBridge;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Collection;

/** Keep Vampirism's canonical skill storage, validation and sync; adapt only its integration boundaries. */
@Mixin(value = SkillHandler.class, remap = false)
public abstract class SkillHandlerMixin {
    @Shadow @Final private IFactionPlayer<?> player;
    @Shadow @Final @Mutable private ISkillTreeData treeData;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void tvd$speciesTreeView(CallbackInfo ci) {
        if (player instanceof CustomFactionPlayer<?> custom)
            treeData = new SpeciesSkillTreeData(custom);
    }

    @Inject(method = "canSkillBeEnabled", at = @At("HEAD"), cancellable = true)
    private void tvd$restrictPurchase(ISkill<?> skill, CallbackInfoReturnable<ISkillHandler.Result> cir) {
        if (player instanceof CustomFactionPlayer<?> custom && !SpeciesSkillRules.allows(custom, skill))
            cir.setReturnValue(ISkillHandler.Result.NOT_FOUND);
    }

    @Inject(method = "enableSkill", at = @At("HEAD"), cancellable = true)
    private void tvd$restrictEnable(ISkill<?> skill, boolean loading, CallbackInfo ci) {
        // Also omits excluded purchases from old saves, returning their point cost automatically.
        if (player instanceof CustomFactionPlayer<?> custom && !SpeciesSkillRules.allows(custom, skill)) ci.cancel();
    }

    @ModifyVariable(method = "updateUnlockedSkillTrees", at = @At("HEAD"), argsOnly = true)
    private Collection<Holder<ISkillTree>> tvd$includeSpeciesTrees(Collection<Holder<ISkillTree>> trees) {
        return player instanceof CustomFactionPlayer<?> custom
                ? SpeciesSkillAccess.includeRequiredTrees(custom, trees) : trees;
    }

    @Redirect(method = "enableSkill", at = @At(value = "INVOKE",
            target = "Lde/teamlapen/vampirism/api/entity/player/skills/ISkill;onEnable(Lde/teamlapen/vampirism/api/entity/player/IFactionPlayer;)V"))
    @SuppressWarnings({"rawtypes", "unchecked"})
    private void tvd$enableWithCompatiblePlayer(ISkill skill, IFactionPlayer owner) {
        if (owner instanceof CustomFactionPlayer<?> custom) {
            StockSkillBridge.enable(custom, skill);
        } else {
            skill.onEnable(owner);
        }
    }

    @Redirect(method = {"disableSkill", "disableAllSkills"}, at = @At(value = "INVOKE",
            target = "Lde/teamlapen/vampirism/api/entity/player/skills/ISkill;onDisable(Lde/teamlapen/vampirism/api/entity/player/IFactionPlayer;)V"), require = 2)
    @SuppressWarnings({"rawtypes", "unchecked"})
    private void tvd$disableWithCompatiblePlayer(ISkill skill, IFactionPlayer owner) {
        if (owner instanceof CustomFactionPlayer<?> custom) {
            StockSkillBridge.disable(custom, skill);
        } else {
            skill.onDisable(owner);
        }
    }

    @Redirect(method = "getLeftSkillPoints", at = @At(value = "INVOKE",
            target = "Lde/teamlapen/vampirism/api/entity/player/skills/ISkillPointProvider;getSkillPoints(Lde/teamlapen/vampirism/api/entity/player/IFactionPlayer;)I"))
    private int tvd$speciesPointBudget(ISkillPointProvider provider, IFactionPlayer<?> owner) {
        return owner instanceof CustomFactionPlayer<?> custom
                ? SpeciesSkillPoints.total(custom) : provider.getSkillPoints(owner);
    }

    @Redirect(method = "getLeftSkillPoints", at = @At(value = "INVOKE",
            target = "Lde/teamlapen/vampirism/api/entity/player/skills/ISkillPointProvider;ignoreSkillPointLimit(Lde/teamlapen/vampirism/api/entity/player/IFactionPlayer;)Z"))
    private boolean tvd$respectSpeciesPointBudget(ISkillPointProvider provider, IFactionPlayer<?> owner) {
        return !(owner instanceof CustomFactionPlayer<?>) && provider.ignoreSkillPointLimit(owner);
    }

    @Redirect(method = {"equipRefinementItem", "deserializeNBT", "deserializeUpdateNBT"},
            require = 3, at = @At(value = "INVOKE",
            target = "Lde/teamlapen/vampirism/api/entity/factions/IPlayableFaction;equals(Ljava/lang/Object;)Z"))
    private boolean tvd$acceptCompatibleRefinement(
            IPlayableFaction<?> faction,
            Object exclusiveFaction
    ) {
        return SpeciesCompatibility.acceptsRefinementFaction(faction, exclusiveFaction);
    }
}
