package org.kuro.tvdvampirism.mixin.client;

import de.teamlapen.vampirism.client.gui.screens.DBNOScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import org.kuro.tvdvampirism.player.SpeciesRules;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = DBNOScreen.class, remap = false)
public abstract class OriginalDbnoScreenMixin {
    @Shadow private Button dieButton;
    @Shadow private de.teamlapen.vampirism.client.gui.components.CooldownButton resurrectButton;

    @Inject(method = "tick", at = @At("TAIL"))
    private void tvd$disableGiveUp(CallbackInfo ci) {
        var player = Minecraft.getInstance().player;
        if (player != null && SpeciesRules.hasOriginalImmortality(player)) dieButton.active = false;
        if (player != null && org.kuro.tvdvampirism.player.DaggerManager.isDaggered(player)) {
            // Keep the disabled texture static instead of rendering the live DBNO refill overlay.
            resurrectButton.updateState(1F);
        }
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void tvd$daggerMessage(net.minecraft.client.gui.GuiGraphics graphics, int mouseX, int mouseY, float partial, CallbackInfo ci) {
        var mc = Minecraft.getInstance();
        if (mc.player != null && org.kuro.tvdvampirism.player.DaggerManager.isDaggered(mc.player))
            graphics.drawCenteredString(mc.font, net.minecraft.network.chat.Component.translatable("gui.tvdvampirism.daggered"),
                    graphics.guiWidth() / 2, 105, 0xFF5555);
    }
}
