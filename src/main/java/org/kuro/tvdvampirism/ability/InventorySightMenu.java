package org.kuro.tvdvampirism.ability;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import org.kuro.tvdvampirism.skill.CustomSkills;

/** Snapshot only: no slot/container ever references the target's live inventory. */
public final class InventorySightMenu extends ChestMenu {
    private final ServerPlayer target;
    public static void open(ServerPlayer viewer,ServerPlayer target) {
        var copy=new SimpleContainer(54);
        for(int i=0;i<target.getInventory().getContainerSize();i++) copy.setItem(i,target.getInventory().getItem(i).copy());
        viewer.openMenu(new SimpleMenuProvider((id,inventory,player)->new InventorySightMenu(id,viewer,target,copy),
                Component.translatable("menu.tvdvampirism.inventory_sight",target.getDisplayName())));
    }
    public InventorySightMenu(int id,ServerPlayer viewer,ServerPlayer target,SimpleContainer snapshot) {
        this(id,viewer.getInventory(),target,snapshot);
    }
    public InventorySightMenu(int id,net.minecraft.world.entity.player.Inventory inventory) {
        this(id,inventory,null,new SimpleContainer(54));
    }
    private InventorySightMenu(int id,net.minecraft.world.entity.player.Inventory inventory,ServerPlayer target,SimpleContainer snapshot) {
        super(CustomSkills.INVENTORY_MENU.get(),id,inventory,snapshot,6);this.target=target;
        // Identical immutable slots on both sides: even client-side click prediction cannot move items.
        for(int i=0;i<slots.size();i++) {
            var old=slots.get(i);
            var readOnly=new Slot(old.container,old.getContainerSlot(),old.x,old.y) {
                @Override public boolean mayPlace(ItemStack stack) { return false; }
                @Override public boolean mayPickup(Player player) { return false; }
            };
            readOnly.index=old.index;slots.set(i,readOnly);
        }
    }
    @Override public void clicked(int slot,int button,ClickType type,Player player) { sendAllDataToRemote(); }
    @Override public ItemStack quickMoveStack(Player player,int slot) { return ItemStack.EMPTY; }
    @Override public boolean stillValid(Player player) {
        if (player.level().isClientSide) return true;
        if (target==null) return false;
        return player.isAlive() && target.isAlive() && !target.hasDisconnected() && player.level()==target.level()
                && !org.kuro.tvdvampirism.compat.SpeciesCompatibility.rawVampire(player).isDBNO()
                && player.distanceToSqr(target)<=16 && player.hasLineOfSight(target)
                && CustomSkills.has(player,CustomSkills.Definition.INVENTORY_SIGHT);
    }
}
