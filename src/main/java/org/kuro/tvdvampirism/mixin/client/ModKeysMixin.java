package org.kuro.tvdvampirism.mixin.client;

import de.teamlapen.vampirism.VampirismMod;
import de.teamlapen.vampirism.client.core.ModKeys;
import de.teamlapen.vampirism.network.ServerboundStartFeedingPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import org.kuro.tvdvampirism.blood.BloodManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ModKeys.class)
public abstract class ModKeysMixin {

    @Shadow
    private boolean suckKeyDown;

    @Inject(method = "suck", at = @At("HEAD"), cancellable = true)
    private void startCustomFeeding(CallbackInfo callback) {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (player == null || BloodManager.getCustomVampire(player).isEmpty()) {
            return;
        }

        callback.cancel();
        if (org.kuro.tvdvampirism.player.SpeciesRules.canUseHumanWolfBite(player)) return;
        if (suckKeyDown) {
            return;
        }
        suckKeyDown = true;
        HitResult hit = minecraft.hitResult;
        if (hit == null || player.isSpectator()) {
            return;
        }
        if (hit instanceof EntityHitResult entityHit) {
            VampirismMod.proxy.sendToServer(
                    new ServerboundStartFeedingPacket(entityHit.getEntity().getId())
            );
        } else if (hit instanceof BlockHitResult blockHit) {
            BlockPos position = blockHit.getBlockPos();
            VampirismMod.proxy.sendToServer(new ServerboundStartFeedingPacket(position));
        }
    }
}
