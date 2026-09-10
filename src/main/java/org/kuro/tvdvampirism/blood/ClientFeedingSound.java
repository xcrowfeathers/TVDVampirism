package org.kuro.tvdvampirism.blood;

import de.teamlapen.lib.VampLib;
import de.teamlapen.lib.util.ISoundReference;
import de.teamlapen.vampirism.core.ModSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.sounds.SoundSource;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import org.kuro.tvdvampirism.Tvdvampirism;

@EventBusSubscriber(
        modid = Tvdvampirism.MODID,
        value = Dist.CLIENT
)
public final class ClientFeedingSound {

    private static ISoundReference feedingSound;
    private static int activeTargetId = -1;

    private ClientFeedingSound() {
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();

        int targetId = minecraft.player == null
                ? -1
                : BloodManager.getCustomVampire(minecraft.player)
                .map(customPlayer -> customPlayer.getFeedingTargetId())
                .orElse(-1);

        if (targetId != activeTargetId) {
            stopSound();
            activeTargetId = targetId;
        }

        if (targetId < 0
                || minecraft.player == null
                || !minecraft.player.isAlive()) {
            stopSound();
            activeTargetId = -1;
            return;
        }

        if (feedingSound == null || !feedingSound.isPlaying()) {
            feedingSound = VampLib.proxy.createSoundReference(
                    ModSounds.VAMPIRE_FEEDING.get(),
                    SoundSource.PLAYERS,
                    minecraft.player.getX(),
                    minecraft.player.getY(),
                    minecraft.player.getZ(),
                    0.8F,
                    1.0F
            );

            feedingSound.startPlaying();
        }
    }

    private static void stopSound() {
        if (feedingSound != null) {
            feedingSound.stopPlaying();
            feedingSound = null;
        }
    }
}