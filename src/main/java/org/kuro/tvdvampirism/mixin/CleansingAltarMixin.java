package org.kuro.tvdvampirism.mixin;

import de.teamlapen.vampirism.blocks.AltarCleansingBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.kuro.tvdvampirism.config.ServerConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Gates only the Cleansing Altar's interaction entry point. */
@Mixin(value = AltarCleansingBlock.class, remap = false)
public abstract class CleansingAltarMixin {
    @Inject(method = "useWithoutItem", at = @At("HEAD"), cancellable = true)
    private void tvd$disableCleansingAltar(
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            BlockHitResult hit,
            CallbackInfoReturnable<InteractionResult> cir
    ) {
        if (ServerConfig.DISABLE_CLEANSING_ALTAR.get()) {
            cir.setReturnValue(InteractionResult.FAIL);
        }
    }
}
