package org.kuro.tvdvampirism.skill;

import de.teamlapen.vampirism.api.entity.factions.ISkillNode;
import de.teamlapen.vampirism.api.entity.factions.ISkillTree;
import de.teamlapen.vampirism.data.ClientSkillTreeData;
import de.teamlapen.vampirism.data.ServerSkillTreeData;
import de.teamlapen.vampirism.entity.player.skills.SkillNode;
import de.teamlapen.vampirism.entity.player.skills.SkillTreeConfiguration;
import de.teamlapen.vampirism.entity.player.skills.SkillTreeConfiguration.SkillTreeNodeConfiguration;
import net.minecraft.core.Holder;
import org.kuro.tvdvampirism.faction.player.CustomFactionPlayer;
import java.util.*;

/** Per-owner projection. Extends the common stock data class because SkillsScreen casts to it. */
public final class SpeciesSkillTreeData extends ClientSkillTreeData {
    private final CustomFactionPlayer<?> owner;
    private final Map<Holder<ISkillTree>, Projection> cache = new HashMap<>();
    private record Projection(SkillTreeConfiguration source, boolean masteryUnlocked, SkillTreeConfiguration filtered) {}

    public SpeciesSkillTreeData(CustomFactionPlayer<?> owner) {
        super(owner.asEntity().registryAccess());
        this.owner = owner;
    }

    @Override
    public SkillTreeConfiguration getConfiguration(Holder<ISkillTree> tree) {
        var source = owner.isRemote() ? ClientSkillTreeData.instance(owner.asEntity().level()).getConfiguration(tree)
                : ServerSkillTreeData.instance().getConfiguration(tree);
        if (source == null) return null;
        var previous = cache.get(tree);
        boolean masteryUnlocked = owner.getLevel()>0 && owner.getMasteryLevel()>0;
        if (previous == null || previous.source() != source || previous.masteryUnlocked()!=masteryUnlocked) {
            previous = new Projection(source, masteryUnlocked, new SkillTreeConfiguration(tree, filterNode(source.root()), filterChildren(source.children())));
            cache.put(tree, previous);
        }
        return previous.filtered();
    }

    private Holder<ISkillNode> filterNode(Holder<ISkillNode> node) {
        var skills = node.value().skills().stream().filter(skill -> SpeciesSkillRules.allows(owner, skill.value())).toList();
        return skills.size() == node.value().skills().size() ? node
                : Holder.direct(new SkillNode(skills, node.value().lockingNodes()));
    }

    private List<SkillTreeNodeConfiguration> filterChildren(List<SkillTreeNodeConfiguration> children) {
        var result = new ArrayList<SkillTreeNodeConfiguration>();
        for (var child : children) {
            var node = filterNode(child.node());
            var descendants = filterChildren(child.children());
            if (node.value().skills().isEmpty()) result.addAll(descendants);
            else result.add(new SkillTreeNodeConfiguration(node, descendants));
        }
        return List.copyOf(result);
    }
}
