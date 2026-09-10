package org.kuro.tvdvampirism.mastery;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.teamlapen.vampirism.api.entity.player.IFactionPlayer;
import de.teamlapen.vampirism.api.entity.player.task.TaskUnlocker;
import net.minecraft.network.chat.Component;
import org.kuro.tvdvampirism.faction.player.CustomFactionPlayer;

public record MasteryUnlocker(String species, int level) implements TaskUnlocker {
    public static final MapCodec<MasteryUnlocker> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            Codec.STRING.fieldOf("species").forGetter(MasteryUnlocker::species),
            Codec.intRange(1,5).fieldOf("level").forGetter(MasteryUnlocker::level)).apply(i, MasteryUnlocker::new));
    public boolean isUnlocked(IFactionPlayer<?> player) {
        return player instanceof CustomFactionPlayer<?> custom && player.getFaction().getID().getPath().equals(species)
                && player.getLevel() == 14 && custom.getMasteryLevel() == level - 1;
    }
    public Component getDescription() { return Component.literal(level == 1 ? "Level 14" : "Level 14; complete Mastery " + (level - 1)); }
    public MapCodec<? extends TaskUnlocker> codec() { return CODEC; }
}
