package org.kuro.tvdvampirism.client.action;

import de.teamlapen.vampirism.VampirismMod;
import de.teamlapen.vampirism.network.ServerboundSimpleInputEvent;
import de.teamlapen.vampirism.network.ServerboundStartFeedingPacket;
import de.teamlapen.werewolves.network.ServerboundBiteEventPackage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.KeyMapping;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.EntityHitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import org.kuro.tvdvampirism.blood.FeedingManager;
import org.kuro.tvdvampirism.compat.SpeciesCompatibility;
import org.kuro.tvdvampirism.config.ServerConfig;
import org.kuro.tvdvampirism.player.SpeciesRules;

/** Only input state; all target and combat decisions are revalidated by existing server handlers */
@EventBusSubscriber(modid = "tvdvampirism", value = Dist.CLIENT)
public final class HybridBiteInput {
    private static KeyMapping key;
    private static boolean pressed, feeding;
    private static int heldTicks;

    private HybridBiteInput() {}

    public static void input(KeyMapping biteKey) {
        key = biteKey;
        update(false);
    }

    @SubscribeEvent
    public static void tick(ClientTickEvent.Post event) { update(true); }

    private static void update(boolean tick) {
        var mc = Minecraft.getInstance();
        var player = mc.player;
        boolean usable = player != null && player.isAlive() && !player.isSpectator()
                && mc.screen == null && mc.isWindowActive() && SpeciesRules.canUseHumanWolfBite(player)
                && !SpeciesCompatibility.rawVampire(player).isDBNO();
        boolean down = usable && key != null && key.isDown();
        if (!down) {
            if (feeding && player != null) VampirismMod.proxy.sendToServer(new ServerboundSimpleInputEvent(
                    ServerboundSimpleInputEvent.Event.FINISH_SUCK_BLOOD));
            else if (usable && pressed && heldTicks < ServerConfig.HYBRID_FEED_HOLD_TICKS.get()
                    && mc.hitResult instanceof EntityHitResult hit && hit.getEntity() instanceof LivingEntity)
                player.connection.send(new ServerboundBiteEventPackage(hit.getEntity().getId()));
            pressed = feeding = false;
            heldTicks = 0;
            return;
        }
        pressed = true;
        if (tick) heldTicks = Math.min(heldTicks + 1, ServerConfig.HYBRID_FEED_HOLD_TICKS.get());
        if (!feeding && heldTicks >= ServerConfig.HYBRID_FEED_HOLD_TICKS.get()
                && mc.hitResult instanceof EntityHitResult hit && hit.getEntity() instanceof LivingEntity target
                && FeedingManager.getHudTargetInfo(SpeciesCompatibility.customPlayer(player), target).isPresent()) {
            VampirismMod.proxy.sendToServer(new ServerboundStartFeedingPacket(target.getId()));
            feeding = true;
        }
    }
}
