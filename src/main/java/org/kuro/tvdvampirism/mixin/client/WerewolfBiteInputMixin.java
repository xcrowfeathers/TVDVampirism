package org.kuro.tvdvampirism.mixin.client;

import de.teamlapen.werewolves.client.core.ModKeys;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.client.event.InputEvent;
import org.kuro.tvdvampirism.client.action.HybridBiteInput;
import org.kuro.tvdvampirism.player.SpeciesRules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ModKeys.class, remap = false)
public abstract class WerewolfBiteInputMixin {
    @Shadow @Final private static KeyMapping BITE;

    @Inject(method = "handleInputEvent", at = @At("HEAD"))
    private void tvd$hybridInput(InputEvent event, CallbackInfo ci) {
        HybridBiteInput.input(BITE);
        var player = Minecraft.getInstance().player;
        if (player != null && SpeciesRules.canUseHumanWolfBite(player)) {
            // Consume only bite clicks; stock leap and normal Werewolf input remain untouched.
            while (BITE.consumeClick()) { }
        }
    }
}
