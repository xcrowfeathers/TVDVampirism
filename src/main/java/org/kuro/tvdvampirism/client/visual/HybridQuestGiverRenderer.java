package org.kuro.tvdvampirism.client.visual;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import org.kuro.tvdvampirism.mastery.MasteryContent;

@EventBusSubscriber(modid="tvdvampirism", value=Dist.CLIENT)
public final class HybridQuestGiverRenderer {
    @SubscribeEvent public static void register(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(MasteryContent.HYBRID_QUEST_GIVER.get(),de.teamlapen.vampirism.client.renderer.entity.VampireTaskMasterRenderer::new);
    }
}
