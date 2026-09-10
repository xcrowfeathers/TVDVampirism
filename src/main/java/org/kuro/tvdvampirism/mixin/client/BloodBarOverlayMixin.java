package org.kuro.tvdvampirism.mixin.client;

import de.teamlapen.vampirism.client.gui.overlay.BloodBarOverlay;
import de.teamlapen.vampirism.modcompat.IMCHandler;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import org.jetbrains.annotations.NotNull;
import org.kuro.tvdvampirism.blood.BloodData;
import org.kuro.tvdvampirism.blood.BloodManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Reuses Vampirism's own blood-bar sprites and layout for custom blood. */
@Mixin(BloodBarOverlay.class)
public abstract class BloodBarOverlayMixin {

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void renderCustomBlood(
            @NotNull GuiGraphics graphics,
            @NotNull DeltaTracker partialTicks,
            CallbackInfo callback
    ) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) {
            return;
        }
        BloodData bloodData = BloodManager.getBloodData(minecraft.player).orElse(null);
        if (bloodData == null) {
            return;
        }

        callback.cancel();
        if (!minecraft.player.isAlive()
                || minecraft.options.hideGui
                || IMCHandler.requestedToDisableBloodbar
                || minecraft.gameMode == null
                || !minecraft.gameMode.hasExperience()) {
            return;
        }

        int left = minecraft.getWindow().getGuiScaledWidth() / 2 + 91;
        int top = minecraft.getWindow().getGuiScaledHeight() - minecraft.gui.rightHeight;
        minecraft.gui.rightHeight += 10;
        int blood = bloodData.getBloodLevel();
        int overflowBlood = blood - 20;

        for (int index = 0; index < 10; index++) {
            int bloodIndex = index * 2 + 1;
            int x = left - index * 8 - 9;
            graphics.blitSprite(BloodBarOverlay.BACKGROUND, x, top, 9, 9);
            if (bloodIndex < blood) {
                graphics.blitSprite(
                        bloodIndex < overflowBlood
                                ? BloodBarOverlay.FULL
                                : BloodBarOverlay.HALF,
                        x,
                        top,
                        9,
                        9
                );
                if (bloodIndex == overflowBlood) {
                    graphics.blitSprite(BloodBarOverlay.THREE_QUARTER, x, top, 9, 9);
                }
            } else if (bloodIndex == blood) {
                graphics.blitSprite(BloodBarOverlay.QUARTER, x, top, 9, 9);
            }
        }
    }
}
