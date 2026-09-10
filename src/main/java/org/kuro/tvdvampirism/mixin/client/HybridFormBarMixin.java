package org.kuro.tvdvampirism.mixin.client;

import de.teamlapen.werewolves.client.gui.overlay.WerewolfFormDurationOverlay;
import net.minecraft.client.Minecraft;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;
import org.kuro.tvdvampirism.player.SpeciesRules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = WerewolfFormDurationOverlay.class, remap = false)
public abstract class HybridFormBarMixin {
    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void tvd$hideUnusedResource(GuiGraphics graphics, DeltaTracker delta, CallbackInfo ci) {
        var player = Minecraft.getInstance().player;
        if (player != null && SpeciesRules.canUseHumanWolfBite(player)) ci.cancel();
    }
}
