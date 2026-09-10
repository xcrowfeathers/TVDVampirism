package org.kuro.tvdvampirism.mixin;

import de.teamlapen.vampirism.api.entity.player.IFactionPlayer;
import de.teamlapen.vampirism.api.entity.player.task.Task;
import de.teamlapen.vampirism.entity.player.TaskManager;
import de.teamlapen.vampirism.inventory.TaskBoardMenu;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerPlayer;
import org.kuro.tvdvampirism.faction.player.CustomFactionPlayer;
import org.kuro.tvdvampirism.mastery.MasteryQuests;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
import java.util.*;

@Mixin(value=TaskManager.class, remap=false)
public abstract class MasteryTaskManagerMixin {
    @Shadow @Final private IFactionPlayer<?> factionPlayer;
    @Shadow @Final private ServerPlayer player;
    @Shadow @Final private Map<UUID,TaskManager.TaskWrapper> taskWrapperMap;
    @Unique private UUID tvd$giver;
    @Unique private int tvd$menuId;

    @Inject(method="matchesFaction(Lnet/minecraft/core/Holder;)Z",at=@At("HEAD"),cancellable=true)
    private void tvd$masteryCatalog(Holder<Task> task, CallbackInfoReturnable<Boolean> cir) {
        if (factionPlayer instanceof CustomFactionPlayer<?>) cir.setReturnValue(task.unwrapKey()
                .map(key -> key.location().getNamespace().equals("tvdvampirism") && key.location().getPath().startsWith("mastery/")).orElse(false));
    }

    @Inject(method="tick",at=@At("HEAD"),cancellable=true)
    private void tvd$eventDrivenQuests(CallbackInfo ci) { if(factionPlayer instanceof CustomFactionPlayer<?>) ci.cancel(); }

    @Inject(method="openTaskMasterScreen",at=@At("HEAD"),cancellable=true)
    private void tvd$rememberGiver(UUID id, CallbackInfo ci) {
        if (!(factionPlayer instanceof CustomFactionPlayer<?>)) return;
        var giver=player.serverLevel().getEntity(id);
        if (!MasteryQuests.correctGiver(player,giver) || !giver.isAlive() || player.distanceToSqr(giver)>64
                || !(player.containerMenu instanceof TaskBoardMenu)) { ci.cancel(); return; }
        tvd$giver=id;tvd$menuId=player.containerMenu.containerId;
    }

    @Inject(method={"acceptTask","completeTask"},at=@At("HEAD"),cancellable=true)
    private void tvd$validateTurnIn(UUID board, UUID task, CallbackInfo ci) {
        if (!(factionPlayer instanceof CustomFactionPlayer<?>)) return;
        var giver=tvd$giver==null?null:player.serverLevel().getEntity(tvd$giver);
        var wrapper=taskWrapperMap.get(board);
        var instance=wrapper==null?null:wrapper.getTaskInstance(task);
        if (!player.isAlive() || player.isSpectator() || !MasteryQuests.correctGiver(player,giver)
                || !giver.isAlive() || player.distanceToSqr(giver)>64 || !player.hasLineOfSight(giver)
                || !(player.containerMenu instanceof TaskBoardMenu) || player.containerMenu.containerId!=tvd$menuId
                || instance==null || !((TaskManager)(Object)this).isTaskUnlocked(instance.getTask())) ci.cancel();
    }

    @Inject(method="acceptTask",at=@At("HEAD"),cancellable=true)
    private void tvd$acceptOnce(UUID board, UUID task, CallbackInfo ci) {
        if (factionPlayer instanceof CustomFactionPlayer<?> && taskWrapperMap.containsKey(board)) {
            var instance=taskWrapperMap.get(board).getTaskInstance(task);
            if (instance!=null && instance.isAccepted()) ci.cancel();
        }
    }

    @Inject(method={"acceptTask","completeTask"},at=@At("TAIL"))
    private void tvd$refreshAfterChange(UUID board, UUID task, CallbackInfo ci) {
        if(factionPlayer instanceof CustomFactionPlayer<?> && tvd$giver!=null)
            ((TaskManager)(Object)this).openTaskMasterScreen(tvd$giver);
    }
}
