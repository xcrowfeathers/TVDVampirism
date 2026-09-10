package org.kuro.tvdvampirism.client.visual;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.world.item.ItemDisplayContext;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import org.kuro.tvdvampirism.player.SpeciesManager;

@EventBusSubscriber(modid = "tvdvampirism", value = Dist.CLIENT)
public final class EmbeddedDaggerLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
    private EmbeddedDaggerLayer(PlayerRenderer renderer) { super(renderer); }

    @SubscribeEvent public static void layers(EntityRenderersEvent.AddLayers event) {
        for (var skin : event.getSkins()) {
            PlayerRenderer renderer = event.getSkin(skin);
            if (renderer != null) renderer.addLayer(new EmbeddedDaggerLayer(renderer));
        }
    }

    @Override public void render(PoseStack pose, MultiBufferSource buffers, int light, AbstractClientPlayer player,
            float limbSwing, float limbAmount, float partial, float age, float yaw, float pitch) {
        var dagger = SpeciesManager.getData(player).getEmbeddedDagger();
        if (dagger.isEmpty() || !player.isAlive() || player.getPose() != net.minecraft.world.entity.Pose.SLEEPING) return;
        pose.pushPose();
        getParentModel().body.translateAndRotate(pose);
        pose.translate(0, 0.2, -0.22);
        pose.mulPose(Axis.XP.rotationDegrees(-90));
        pose.mulPose(Axis.ZP.rotationDegrees(135));
        pose.scale(0.65F, 0.65F, 0.65F);
        Minecraft.getInstance().gameRenderer.itemInHandRenderer.renderItem(player, dagger, ItemDisplayContext.FIXED,
                false, pose, buffers, light);
        pose.popPose();
    }
}
