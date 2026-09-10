package org.kuro.tvdvampirism.mastery;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.teamlapen.vampirism.api.entity.player.IFactionPlayer;
import de.teamlapen.vampirism.api.entity.player.task.*;
import net.minecraft.network.chat.Component;
import org.kuro.tvdvampirism.faction.player.CustomFactionPlayer;

public record MasteryReward(String species, int level) implements TaskReward, ITaskRewardInstance {
    public static final MapCodec<MasteryReward> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            Codec.STRING.fieldOf("species").forGetter(MasteryReward::species),
            Codec.intRange(1,5).fieldOf("level").forGetter(MasteryReward::level)).apply(i, MasteryReward::new));
    public void applyReward(IFactionPlayer<?> player) {
        if (!player.isRemote() && new MasteryUnlocker(species, level).isUnlocked(player)) {
            var custom = (CustomFactionPlayer<?>) player;
            custom.setMasteryLevel(level);
            org.kuro.tvdvampirism.skill.SpeciesSkillAccess.refresh(custom);
            custom.sync(true);
        }
    }
    public ITaskRewardInstance createInstance(IFactionPlayer<?> player) { return this; }
    public Component description() { return Component.literal("Mastery " + level + " (+1 skill point)"); }
    public MapCodec<MasteryReward> codec() { return CODEC; }
}
