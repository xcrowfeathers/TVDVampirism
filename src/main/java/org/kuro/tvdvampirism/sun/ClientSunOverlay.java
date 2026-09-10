package org.kuro.tvdvampirism.sun;

import com.mojang.blaze3d.systems.RenderSystem;
import de.teamlapen.vampirism.client.gui.overlay.SunOverlay;
import de.teamlapen.vampirism.config.VampirismConfig;
import de.teamlapen.vampirism.core.ModEffects;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.effect.MobEffectInstance;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import org.kuro.tvdvampirism.Tvdvampirism;

@EventBusSubscriber(
        modid = Tvdvampirism.MODID,
        value = Dist.CLIENT
)
public final class ClientSunOverlay {

    private ClientSunOverlay() {
    }

    @SubscribeEvent
    public static void render(RenderGuiEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();

        if (minecraft.player == null
                || !minecraft.player.isAlive()
                || minecraft.options.hideGui
                || !VampirismConfig.CLIENT.enableSunOverlayRendering.get()
                || !SunDamageManager.isCustomSunSensitive(minecraft.player)) {
            return;
        }

        int ticksInSun = SunDamageManager.getTicksInSun(minecraft.player);
        float progress = Math.clamp(ticksInSun / 50.0F, 0.0F, 1.0F);

        if (progress <= 0.0F) {
            return;
        }

        MobEffectInstance sunscreen =
                minecraft.player.getEffect(ModEffects.SUNSCREEN);

        if (sunscreen != null && sunscreen.getAmplifier() >= 5) {
            return;
        }

        if (minecraft.player.getAbilities().instabuild
                || sunscreen != null && sunscreen.getAmplifier() >= 3) {
            progress = Math.min(0.5F, progress);
        }

        GuiGraphics graphics = event.getGuiGraphics();

        graphics.pose().pushPose();

        float scale = (float) Math.pow(progress, 0.2F);
        scale = 2.0F + scale * (1.0F - 2.0F);

        int width = graphics.guiWidth();
        int height = graphics.guiHeight();

        graphics.pose().translate(width / 2.0F, height / 2.0F, 0.0F);
        graphics.pose().scale(scale, scale, scale);
        graphics.pose().translate(-width / 2.0F, -height / 2.0F, 0.0F);

        renderTexture(graphics);

        graphics.pose().popPose();
    }

    private static void renderTexture(GuiGraphics graphics) {
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.enableBlend();

        graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);

        graphics.blit(
                SunOverlay.SUN_TEXTURE,
                0,
                0,
                -90,
                0.0F,
                0.0F,
                graphics.guiWidth(),
                graphics.guiHeight(),
                graphics.guiWidth(),
                graphics.guiHeight()
        );

        RenderSystem.disableBlend();
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();

        graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
    }
}