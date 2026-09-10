package org.kuro.tvdvampirism.mastery;

import com.mojang.serialization.MapCodec;
import de.teamlapen.vampirism.api.VampirismRegistries;
import de.teamlapen.vampirism.api.entity.player.task.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.*;

public final class MasteryContent {
    private MasteryContent() {}
    private static final DeferredRegister<MapCodec<? extends TaskUnlocker>> UNLOCKERS = DeferredRegister.create(VampirismRegistries.Keys.TASK_UNLOCKER,"tvdvampirism");
    private static final DeferredRegister<MapCodec<? extends TaskReward>> REWARDS = DeferredRegister.create(VampirismRegistries.Keys.TASK_REWARD,"tvdvampirism");
    private static final DeferredRegister<MapCodec<? extends ITaskRewardInstance>> INSTANCES = DeferredRegister.create(VampirismRegistries.Keys.TASK_REWARD_INSTANCE,"tvdvampirism");
    private static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(Registries.ENTITY_TYPE,"tvdvampirism");
    public static final DeferredHolder<EntityType<?>,EntityType<HybridQuestGiver>> HYBRID_QUEST_GIVER = ENTITIES.register("hybrid_quest_giver",
            () -> EntityType.Builder.of(HybridQuestGiver::new, MobCategory.CREATURE).sized(0.6F,1.95F).clientTrackingRange(10).build("tvdvampirism:hybrid_quest_giver"));
    static {
        UNLOCKERS.register("mastery", () -> MasteryUnlocker.CODEC);
        REWARDS.register("mastery", () -> MasteryReward.CODEC);
        INSTANCES.register("mastery", () -> MasteryReward.CODEC);
    }
    public static void register(IEventBus bus) {
        UNLOCKERS.register(bus);REWARDS.register(bus);INSTANCES.register(bus);ENTITIES.register(bus);
        bus.addListener((net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent event) ->
                event.put(HYBRID_QUEST_GIVER.get(), HybridQuestGiver.getAttributeBuilder().build()));
    }
}
