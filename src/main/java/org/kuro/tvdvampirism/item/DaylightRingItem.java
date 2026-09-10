package org.kuro.tvdvampirism.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.kuro.tvdvampirism.compat.DaylightRingAccess;

import java.util.List;

public final class DaylightRingItem extends Item {
    public DaylightRingItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(
            ItemStack stack,
            TooltipContext context,
            List<Component> tooltip,
            TooltipFlag flag
    ) {
        super.appendHoverText(stack, context, tooltip, flag);
        if (!DaylightRingAccess.isCuriosLoaded()) {
            tooltip.add(Component.translatable(
                    "item.tvdvampirism.daylight_ring.curios_missing"
            ).withStyle(ChatFormatting.RED));
        }
    }
}
