package org.kuro.tvdvampirism.mixin.client;

import de.teamlapen.vampirism.client.gui.overlay.FactionLevelOverlay;
import net.minecraft.client.Minecraft;
import org.kuro.tvdvampirism.compat.SpeciesCompatibility;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/** Displays custom Mastery in the stock faction-level HUD position. */
@Mixin(value = FactionLevelOverlay.class, remap = false)
public abstract class MasteryLevelOverlayMixin {
    @Redirect(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/lang/String;valueOf(I)Ljava/lang/String;"
            )
    )
    private String tvd$displayMastery(int baseLevel) {
        var player = Minecraft.getInstance().player;
        var custom = player == null ? null : SpeciesCompatibility.customPlayer(player);
        int mastery = custom == null ? 0 : custom.getMasteryLevel();
        return switch (mastery) {
            case 1 -> "I";
            case 2 -> "II";
            case 3 -> "III";
            case 4 -> "IV";
            case 5 -> "V";
            default -> String.valueOf(baseLevel);
        };
    }
}
