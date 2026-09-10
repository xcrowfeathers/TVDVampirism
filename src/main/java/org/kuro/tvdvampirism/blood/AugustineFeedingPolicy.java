package org.kuro.tvdvampirism.blood;

import de.teamlapen.vampirism.api.entity.player.vampire.IDrinkBloodContext;
import de.teamlapen.vampirism.core.ModItems;
import de.teamlapen.werewolves.items.LiverItem;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.kuro.tvdvampirism.config.ServerConfig;
import org.kuro.tvdvampirism.faction.VampireFamily;
import org.kuro.tvdvampirism.faction.player.CustomFactionPlayer;
import org.kuro.tvdvampirism.player.SpeciesRules;

public final class AugustineFeedingPolicy {
    private AugustineFeedingPolicy() {}

    public static boolean isAugustine(Player player) { return SpeciesRules.isAugustine(player); }
    public static boolean isVampire(LivingEntity target) { return VampireFamily.isVampireDerived(target); }
    public static boolean canFeedOn(Player player, LivingEntity target) {
        return !isVampire(target) || isAugustine(player);
    }

    /** Future control skill belongs here; no skill is granted in this phase */
    public static boolean canControlVampireFeeding(Player player) { return !isAugustine(player); }

    public static boolean storeVampireOverflow(CustomFactionPlayer<?> owner, IDrinkBloodContext context) {
        if (!isAugustine(owner.asEntity()) || context.getEntity().filter(AugustineFeedingPolicy::isVampire).isEmpty())
            return false;
        var player = owner.asEntity();
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            ItemStack empty = player.getInventory().getItem(slot);
            if (!empty.is(Items.GLASS_BOTTLE)) continue;
            if (owner.advanceVampireOverflow()) {
                empty.shrink(1);
                ItemStack filled = new ItemStack(ModItems.VAMPIRE_BLOOD_BOTTLE.get());
                if (!player.getInventory().add(filled)) player.drop(filled, false);
                player.getInventory().setChanged();
            }
            break;
        }
        return true;
    }

    public static int intake(CustomFactionPlayer<?> owner, int amount, IDrinkBloodContext context) {
        if (!isAugustine(owner.asEntity())) return amount;
        var stack = context.getStack();
        if (stack.isPresent()) {
            if (stack.get().getItem() instanceof LiverItem) return 0;
            if (stack.get().is(ModItems.VAMPIRE_BLOOD_BOTTLE))
                return ServerConfig.AUGUSTINE_VAMPIRE_BOTTLE_GAIN.get();
            if (stack.get().is(ModItems.HUMAN_HEART) || stack.get().is(ModItems.WEAK_HUMAN_HEART))
                return owner.getBloodData().quarterHeartGain(amount);
        }
        if (amount > 0 && context.getEntity().isPresent()) {
            if (isVampire(context.getEntity().get()))
                amount = (int) Math.floor(amount * ServerConfig.AUGUSTINE_VAMPIRE_BLOOD_MULTIPLIER.get());
            else amount = owner.getBloodData().normalFeedingGain(amount,
                    ServerConfig.AUGUSTINE_HUMAN_BLOOD_MULTIPLIER.get());
            return owner.getFeedingTargetId() == context.getEntity().get().getId()
                    ? owner.limitFeedingGain(amount) : amount;
        }
        return amount;
    }
}
