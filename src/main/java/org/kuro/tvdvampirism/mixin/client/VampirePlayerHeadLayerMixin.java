package org.kuro.tvdvampirism.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import de.teamlapen.vampirism.client.renderer.entity.layers.VampirePlayerHeadLayer;
import de.teamlapen.vampirism.entity.player.VampirismPlayerAttributes;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.player.Player;
import org.kuro.tvdvampirism.client.visual.SpeciesClientVisualAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = {VampirePlayerHeadLayer.class}, remap = false)
public abstract class VampirePlayerHeadLayerMixin {
    @Redirect(method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/entity/player/Player;FFFFFF)V",
            at = @At(value = "FIELD",
                    target = "Lde/teamlapen/vampirism/entity/player/VampirismPlayerAttributes;vampireLevel:I"))
    private int tvdvampirism$visualLevel(VampirismPlayerAttributes attributes,
            PoseStack stack, MultiBufferSource buffers, int light, Player player,
            float limbSwing, float limbSwingAmount, float partialTick,
            float ageInTicks, float netHeadYaw, float headPitch) {
        return SpeciesClientVisualAccess.vampireVisualLevel(player, attributes.vampireLevel);
    }
}
