package org.kuro.tvdvampirism.mastery;

import de.teamlapen.vampirism.entity.vampire.VampireTaskMasterEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

/** Stock quest giver AI and appearance, with Mastery-only interaction routing. */
public final class HybridQuestGiver extends VampireTaskMasterEntity {
    public HybridQuestGiver(EntityType<? extends HybridQuestGiver> type, Level level) { super(type, level); }
}
