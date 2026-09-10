package org.kuro.tvdvampirism.mixin;

import de.teamlapen.werewolves.entities.player.werewolf.WerewolfPlayer;
import net.minecraft.world.entity.LivingEntity;
import org.kuro.tvdvampirism.bite.WerewolfBiteManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import de.teamlapen.werewolves.api.entities.werewolf.WerewolfForm;
import de.teamlapen.werewolves.entities.player.werewolf.WerewolfPlayerSpecialAttributes;
import org.kuro.tvdvampirism.compat.SpeciesCompatibility;
import org.kuro.tvdvampirism.player.SpeciesRules;
import de.teamlapen.vampirism.entity.player.skills.SkillHandler;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = WerewolfPlayer.class, remap = false)
public abstract class WerewolfBiteMixin {
    @org.spongepowered.asm.mixin.injection.ModifyVariable(method = "bite(Lnet/minecraft/world/entity/LivingEntity;)Z",
            at = @At("STORE"), ordinal = 0)
    private double tvd$humanHybridBiteDamage(double damage) {
        var wolf = (WerewolfPlayer) (Object) this;
        // Human Hybrids have no stock form modifier; use the stock Beast bite bonus once.
        return SpeciesRules.canUseHumanWolfBite(wolf.asEntity()) && wolf.getForm().isHumanLike()
                ? damage + de.teamlapen.werewolves.config.WerewolvesConfig.BALANCE.SKILLS.beast_form_bite_damage.get()
                : damage;
    }
    @Redirect(method = "bite(Lnet/minecraft/world/entity/LivingEntity;)Z", at = @At(value = "FIELD",
            target = "Lde/teamlapen/werewolves/entities/player/werewolf/WerewolfPlayer;skillHandler:Lde/teamlapen/vampirism/entity/player/skills/SkillHandler;"))
    private SkillHandler<?> tvd$hybridBiteSkills(WerewolfPlayer wolf) {
        var custom = SpeciesCompatibility.customPlayer(wolf.asEntity());
        return (SkillHandler<?>) (custom != null && custom.hasWerewolfSkillBridge()
                ? custom.getWerewolfSkillBridge().getSkillHandler() : wolf.getSkillHandler());
    }

    @Redirect(method = "bite(Lnet/minecraft/world/entity/LivingEntity;)Z", at = @At(value = "INVOKE",
            target = "Lde/teamlapen/werewolves/api/entities/werewolf/WerewolfForm;isHumanLike()Z"))
    private boolean tvd$hybridBiteEffectsInEveryForm(WerewolfForm form) {
        return !SpeciesRules.canUseHumanWolfBite(((WerewolfPlayer) (Object) this).asEntity()) && form.isHumanLike();
    }

    @Redirect(method = {"canBite", "bite(Lnet/minecraft/world/entity/LivingEntity;)Z"},
            at = @At(value = "INVOKE", target = "Lde/teamlapen/werewolves/api/entities/werewolf/WerewolfForm;isTransformed()Z"))
    private boolean tvd$hybridBiteInEveryForm(WerewolfForm form) {
        return form.isTransformed() || SpeciesRules.canUseHumanWolfBite(((WerewolfPlayer) (Object) this).asEntity());
    }

    @Redirect(method = "canBite", at = @At(value = "INVOKE",
            target = "Lde/teamlapen/werewolves/entities/player/werewolf/WerewolfPlayer;getLevel()I"))
    private int tvd$hybridBiteLevel(WerewolfPlayer wolf) {
        var custom = SpeciesCompatibility.customPlayer(wolf.asEntity());
        return custom != null && custom.hasWerewolfSkillBridge() ? custom.getLevel() : wolf.getLevel();
    }

    /** Keep the stock cooldown authoritative on the real attachment on both logical sides. */
    @Redirect(method = {"canBite", "bite(Lnet/minecraft/world/entity/LivingEntity;)Z", "lambda$bite$5"},
            at = @At(value = "FIELD",
                    target = "Lde/teamlapen/werewolves/entities/player/werewolf/WerewolfPlayer;specialAttributes:Lde/teamlapen/werewolves/entities/player/werewolf/WerewolfPlayerSpecialAttributes;"))
    private WerewolfPlayerSpecialAttributes tvd$sharedBiteCooldown(WerewolfPlayer wolf) {
        return wolf.getSpecialAttributes();
    }

    @Inject(method = "bite(Lnet/minecraft/world/entity/LivingEntity;)Z", at = @At("HEAD"), cancellable = true)
    private void tvd$validateHybridBite(LivingEntity target, CallbackInfoReturnable<Boolean> cir) {
        var player = ((WerewolfPlayer) (Object) this).asEntity();
        if (SpeciesRules.canUseHumanWolfBite(player)
                && (player.level().isClientSide || !player.isAlive() || !target.isAlive()
                || target == player || target.isRemoved() || !player.hasLineOfSight(target)
                || SpeciesCompatibility.rawVampire(player).isDBNO()
                || SpeciesCompatibility.customPlayer(player).getFeedingTargetId() >= 0)) cir.setReturnValue(false);
    }

    @Inject(method = "bite(Lnet/minecraft/world/entity/LivingEntity;)Z", at = @At("RETURN"))
    private void tvd$infectAfterSuccessfulBite(LivingEntity target, CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValue()) WerewolfBiteManager.tryApplyBite(((WerewolfPlayer) (Object) this).asEntity(), target);
    }
}
