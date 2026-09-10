package org.kuro.tvdvampirism.mixin;

import de.teamlapen.vampirism.config.VampirismConfig;
import de.teamlapen.vampirism.entity.player.vampire.VampirePlayer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import org.kuro.tvdvampirism.compat.SpeciesCompatibility;
import org.kuro.tvdvampirism.player.OriginalImmortalityManager;
import org.kuro.tvdvampirism.player.SpeciesRules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Adapts Vampirism's stock DBNO lifecycle for custom vampire species while
 * preserving the special immortality lifecycle of Original species.
 */
@Mixin(value = VampirePlayer.class, remap = false)
public abstract class OriginalDbnoMixin
        implements org.kuro.tvdvampirism.player.DbnoAccess {

    @Shadow
    private boolean wasDBNO;

    @Invoker("setDBNOTimer")
    public abstract void tvd$setDbnoTimer(int ticks);

    @Redirect(
            method = "onDeadlyHit",
            at = @At(
                    value = "INVOKE",
                    target = "Lde/teamlapen/vampirism/entity/player/vampire/VampirePlayer;getLevel()I"
            )
    )
    private int tvd$customDbnoLevel(VampirePlayer vampire) {
        var custom = SpeciesCompatibility.customPlayer(vampire.asEntity());

        return custom != null
                ? custom.getLevel()
                : vampire.getLevel();
    }

    @Redirect(
            method = "onDeadlyHit",
            at = @At(
                    value = "INVOKE",
                    target = "Lde/teamlapen/vampirism/entity/player/vampire/VampirePlayer;getDbnoDuration()I"
            )
    )
    private int tvd$customDbnoDuration(VampirePlayer vampire) {
        var custom = SpeciesCompatibility.customPlayer(vampire.asEntity());

        if (custom != null
                && !SpeciesRules.hasOriginalImmortality(vampire.asEntity())) {
            return Math.max(
                    1,
                    VampirismConfig.BALANCE.vpDbnoDuration.get() * 20
            );
        }

        return vampire.getDbnoDuration();
    }

    @Inject(
            method = "onDeadlyHit",
            at = @At("HEAD"),
            cancellable = true
    )
    private void tvd$originalDeadlyHit(
            DamageSource source,
            CallbackInfoReturnable<Boolean> cir
    ) {
        var player = ((VampirePlayer) (Object) this).asEntity();

        if (player instanceof ServerPlayer serverPlayer
                && org.kuro.tvdvampirism.player.VampirismCureManager.onLethalDamage(serverPlayer, source)) {
            cir.setReturnValue(true);
            return;
        }

        if (org.kuro.tvdvampirism.player.DeathPolicy.bypassesOrdinaryDbno(player, source)
                && org.kuro.tvdvampirism.player.DeathPolicy.canPermanentlyKill(player, source)) {
            cir.setReturnValue(false);
            return;
        }

        if (player instanceof ServerPlayer serverPlayer
                && !org.kuro.tvdvampirism.player.DeathPolicy.isTerminalBite(source)
                && org.kuro.tvdvampirism.player.TransformationManager.onLethalDamage(serverPlayer, source)) {
            cir.setReturnValue(true);
            return;
        }

        if (player instanceof ServerPlayer serverPlayer
                && !org.kuro.tvdvampirism.player.DeathPolicy
                .canPermanentlyKill(player, source)) {

            cir.setReturnValue(
                    OriginalImmortalityManager.enter(
                            serverPlayer,
                            source
                    )
            );
        }
    }

    @Inject(
            method = "onUpdate",
            at = @At("HEAD"),
            cancellable = true
    )
    private void tvd$preserveOriginal(CallbackInfo ci) {
        var player = ((VampirePlayer) (Object) this).asEntity();

        if (!SpeciesRules.hasOriginalImmortality(player)) {
            return;
        }

        wasDBNO = false;

        if (player instanceof ServerPlayer
                && OriginalImmortalityManager.isDown(player)) {
            ci.cancel();
        }
    }

    @Inject(
            method = {
                    "onPlayerLoggedOut",
                    "giveUpDBNO"
            },
            at = @At("HEAD"),
            cancellable = true
    )
    private void tvd$protectOriginalLifecycle(CallbackInfo ci) {
        var player = ((VampirePlayer) (Object) this).asEntity();

        if (SpeciesRules.hasOriginalImmortality(player)) {
            ci.cancel();
        }
    }

    @Inject(
            method = "tryResurrect",
            at = @At("HEAD"),
            cancellable = true
    )
    private void tvd$wakeOriginal(CallbackInfo ci) {
        var player = ((VampirePlayer) (Object) this).asEntity();

        if (SpeciesRules.hasOriginalImmortality(player)) {
            if (player instanceof ServerPlayer serverPlayer) {
                OriginalImmortalityManager.tryWake(serverPlayer);
            }

            ci.cancel();
        }
    }
}
