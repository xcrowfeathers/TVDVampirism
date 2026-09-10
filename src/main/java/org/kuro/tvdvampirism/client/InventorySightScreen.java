package org.kuro.tvdvampirism.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import org.kuro.tvdvampirism.ability.InventorySightMenu;
import org.kuro.tvdvampirism.skill.CustomSkills;

@EventBusSubscriber(modid="tvdvampirism",value=Dist.CLIENT)
public final class InventorySightScreen extends AbstractContainerScreen<InventorySightMenu> {
    private static final ResourceLocation BACKGROUND=ResourceLocation.withDefaultNamespace("textures/gui/container/generic_54.png");
    public InventorySightScreen(InventorySightMenu menu,Inventory inventory,Component title) {
        super(menu,inventory,title);imageHeight=222;inventoryLabelY=128;
    }
    @SubscribeEvent public static void register(RegisterMenuScreensEvent event) { event.register(CustomSkills.INVENTORY_MENU.get(),InventorySightScreen::new); }
    @Override protected void renderBg(GuiGraphics graphics,float partialTick,int mouseX,int mouseY) {
        graphics.blit(BACKGROUND,leftPos,topPos,0,0,imageWidth,125);
        graphics.blit(BACKGROUND,leftPos,topPos+125,0,126,imageWidth,96);
    }
    @Override public void render(GuiGraphics graphics,int x,int y,float partialTick) {
        super.render(graphics,x,y,partialTick);renderTooltip(graphics,x,y);
    }
}
