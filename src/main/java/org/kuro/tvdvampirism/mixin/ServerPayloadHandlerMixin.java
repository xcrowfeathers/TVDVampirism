package org.kuro.tvdvampirism.mixin;

import de.teamlapen.vampirism.network.ServerboundSimpleInputEvent;
import de.teamlapen.vampirism.network.ServerboundStartFeedingPacket;
import de.teamlapen.vampirism.server.ServerPayloadHandler;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.kuro.tvdvampirism.blood.BloodManager;
import org.kuro.tvdvampirism.blood.FeedingManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPayloadHandler.class)
public abstract class ServerPayloadHandlerMixin {

    @Inject(method = "handleStartFeedingPacket", at = @At("HEAD"), cancellable = true)
    private void handleCustomStartFeeding(
            ServerboundStartFeedingPacket message,
            IPayloadContext context,
            CallbackInfo callback
    ) {
        if (BloodManager.getCustomVampire(context.player()).isEmpty()) {
            return;
        }
        context.enqueueWork(() -> message.target().ifLeft(entityId ->
                FeedingManager.startFeeding((ServerPlayer) context.player(), entityId)));
        callback.cancel();
    }

    @Inject(method = "handleSimpleInputEvent", at = @At("HEAD"), cancellable = true)
    private void handleCustomStopFeeding(
            ServerboundSimpleInputEvent message,
            IPayloadContext context,
            CallbackInfo callback
    ) {
        if (message.event() != ServerboundSimpleInputEvent.Event.FINISH_SUCK_BLOOD
                || BloodManager.getCustomVampire(context.player()).isEmpty()) {
            return;
        }
        context.enqueueWork(() -> FeedingManager.stopFeeding((ServerPlayer) context.player()));
        callback.cancel();
    }
}
