package org.kuro.tvdvampirism.blood;

import com.mojang.blaze3d.systems.RenderSystem;
import de.teamlapen.vampirism.api.util.VResourceLocation;
import de.teamlapen.vampirism.modcompat.IMCHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import org.kuro.tvdvampirism.Tvdvampirism;
import org.kuro.tvdvampirism.faction.player.CustomFactionPlayer;

@EventBusSubscriber(
        modid = Tvdvampirism.MODID,
        value = Dist.CLIENT
)
public final class ClientBloodHudEvents {

    private static final ResourceLocation FANG_SPRITE =
            VResourceLocation.mod("fang/fang");

    private static final ResourceLocation PROGRESS_BACKGROUND_SPRITE =
            VResourceLocation.mod("fang/progress_background");

    private static final ResourceLocation PROGRESS_FOREGROUND_SPRITE =
            VResourceLocation.mod("fang/progress_foreground");

    private static final int BLOOD_COLOR = 0xFF0000;
    private static final int POISON_COLOR = 0x099022;

    private ClientBloodHudEvents() {
    }

    @SubscribeEvent
    public static void lockFeedingView(net.neoforged.neoforge.client.event.RenderFrameEvent.Pre event) {
        var minecraft = Minecraft.getInstance();
        var player = minecraft.player;
        if (player == null || minecraft.isPaused()) return;
        var feeder = org.kuro.tvdvampirism.compat.SpeciesCompatibility.customPlayer(player);
        if (feeder == null || !FeedingManager.isForcedFeeding(feeder)) return;
        var target = player.level().getEntity(feeder.getFeedingTargetId());
        if (!(target instanceof LivingEntity living)) return;
        float partial = event.getPartialTick().getGameTimeDeltaPartialTick(false);
        var direction = living.getEyePosition(partial).subtract(player.getEyePosition(partial));
        float yaw = (float) (Mth.atan2(direction.z, direction.x) * Mth.RAD_TO_DEG) - 90;
        float pitch = (float) (-Mth.atan2(direction.y, direction.horizontalDistance()) * Mth.RAD_TO_DEG);
        player.setYRot(yaw);
        player.setXRot(pitch);
        player.yRotO = yaw;
        player.xRotO = pitch;
        player.setYHeadRot(yaw);
    }

    @SubscribeEvent
    public static void hideFoodAndAirForCustomVampires(
            RenderGuiLayerEvent.Pre event
    ) {
        Minecraft minecraft = Minecraft.getInstance();

        if (minecraft.player == null
                || !minecraft.player.isAlive()
                || BloodManager.getBloodData(minecraft.player).isEmpty()) {
            return;
        }

        ResourceLocation layer = event.getName();

        boolean hideFood =
                VanillaGuiLayers.FOOD_LEVEL.equals(layer)
                        && !IMCHandler.requestedToDisableBloodbar
                        && minecraft.gameMode != null
                        && minecraft.gameMode.hasExperience();

        if (VanillaGuiLayers.AIR_LEVEL.equals(layer) || hideFood) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void renderCustomVampireCrosshair(
            RenderGuiLayerEvent.Pre event
    ) {
        if (!VanillaGuiLayers.CROSSHAIR.equals(event.getName())) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();

        if (minecraft.player == null
                || !minecraft.player.isAlive()
                || minecraft.player.isSpectator()) {
            return;
        }

        CustomFactionPlayer<?> customPlayer =
                BloodManager.getCustomVampire(minecraft.player)
                        .orElse(null);

        if (customPlayer == null || customPlayer.getLevel() <= 0) {
            return;
        }

        HitResult hit = minecraft.hitResult;

        // Let the stock Werewolves crosshair own the ready tap-bite indicator.
        if (org.kuro.tvdvampirism.client.action.SpeciesClientActionAccess.hasReadyHybridBiteTarget()) {
            renderFeedProgress(event.getGuiGraphics(), customPlayer, minecraft);
            return;
        }

        if (hit instanceof EntityHitResult entityHit
                && entityHit.getEntity() instanceof LivingEntity target
                && !target.isInvisible()) {

            FeedingManager.getHudTargetInfo(customPlayer, target)
                    .ifPresent(info -> {
                        renderFangs(
                                event.getGuiGraphics(),
                                Mth.clamp(
                                        info.bloodLevelRelative(),
                                        0.2F,
                                        1.0F
                                ),
                                info.poisonous()
                                        ? POISON_COLOR
                                        : BLOOD_COLOR
                        );

                        event.setCanceled(true);
                    });
        }

        renderFeedProgress(
                event.getGuiGraphics(),
                customPlayer,
                minecraft
        );
    }

    private static void renderFangs(
            GuiGraphics graphics,
            float bloodLevel,
            int color
    ) {
        int left = graphics.guiWidth() / 2 - 8;
        int top = graphics.guiHeight() / 2 - 4;

        float red = ((color >> 16) & 0xFF) / 255.0F;
        float green = ((color >> 8) & 0xFF) / 255.0F;
        float blue = (color & 0xFF) / 255.0F;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        graphics.setColor(
                1.0F,
                1.0F,
                1.0F,
                1.0F
        );

        graphics.blitSprite(
                FANG_SPRITE,
                left,
                top,
                16,
                10
        );

        int filledHeight = Mth.clamp(
                (int) (10.0F * bloodLevel),
                0,
                10
        );

        if (filledHeight > 0) {
            graphics.setColor(
                    red,
                    green,
                    blue,
                    1.0F
            );

            graphics.blitSprite(
                    FANG_SPRITE,
                    16,
                    10,
                    0,
                    10 - filledHeight,
                    left,
                    top + 10 - filledHeight,
                    16,
                    filledHeight
            );
        }

        graphics.setColor(
                1.0F,
                1.0F,
                1.0F,
                1.0F
        );

        RenderSystem.disableBlend();
    }

    private static void renderFeedProgress(
            GuiGraphics graphics,
            CustomFactionPlayer<?> customPlayer,
            Minecraft minecraft
    ) {
        if (minecraft.gameMode == null
                || !minecraft.options.getCameraType().isFirstPerson()
                || minecraft.gameMode.getPlayerMode() == GameType.SPECTATOR) {
            return;
        }

        float progress = customPlayer.getFeedProgress();

        if (progress <= 0.0F || progress > 1.0F) {
            return;
        }

        int x = graphics.guiWidth() / 2 - 8;
        int y = graphics.guiHeight() / 2 + 9;
        int length = Mth.clamp(
                (int) (progress * 14.0F) + 2,
                2,
                16
        );

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        graphics.setColor(
                1.0F,
                1.0F,
                1.0F,
                1.0F
        );

        graphics.blitSprite(
                PROGRESS_BACKGROUND_SPRITE,
                x,
                y,
                16,
                2
        );

        graphics.blitSprite(
                PROGRESS_FOREGROUND_SPRITE,
                16,
                2,
                0,
                0,
                x,
                y,
                length,
                2
        );

        graphics.setColor(
                1.0F,
                1.0F,
                1.0F,
                1.0F
        );

        RenderSystem.disableBlend();
    }
}
