package com.InfinityRaider.ninjagear.handler;

import com.InfinityRaider.ninjagear.NinjaGear;
import com.InfinityRaider.ninjagear.item.ItemNinjaArmor;
import com.InfinityRaider.ninjagear.network.MessageUpdateGadgetRenderMaskServer;
import com.InfinityRaider.ninjagear.network.NetworkWrapper;
import com.InfinityRaider.ninjagear.registry.ItemRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Every client checks for changes in items equipped in the inventory.
 * If a change is detected, a message is sent to the server which sends the change to all other clients.
 * This is necessary because a client is unaware of the inventory of another client.
 */
@SideOnly(Side.CLIENT)
public class NinjaGadgetHandler {
    private static final NinjaGadgetHandler INSTANCE = new NinjaGadgetHandler();

    public static NinjaGadgetHandler getInstance() {
        return INSTANCE;
    }

    private boolean hasSent = false;

    private NinjaGadgetHandler() {}

    @SubscribeEvent
    @SuppressWarnings("unused")
    public void onPlayerTick(TickEvent event) {
        if(event.side == Side.CLIENT && event.phase == TickEvent.Phase.END) {
            EntityPlayer player = NinjaGear.proxy.getClientPlayer();
            if(player == null) {
                return;
            }
            //count relevant items in player's inventory
            ItemStack chest = player.getItemStackFromSlot(EntityEquipmentSlot.CHEST);
            if(chest != null && chest.getItem() instanceof ItemNinjaArmor) {
                for(int i = 0; i < player.inventory.mainInventory.length; i++) {
                    if(i == player.inventory.currentItem) {
                        continue;
                    }
                    ItemStack stack = player.inventory.getStackInSlot(i);
                    if(stack == null) {
                        continue;
                    }
                    Gadgets gadget = Gadgets.getGadgetFromItem(stack.getItem());
                    if(gadget != null) {
                        gadget.increment();
                    }
                }
            }
            //if one gadget has changed count or it is the first tick, send the change to the server which then sends it to all clients
            boolean flag = false;
            for(Gadgets gadget : Gadgets.values()) {
                if(gadget.updateAndCheckForChanges()) {
                    flag = true;
                }
            }
            if(flag || !hasSent) {
                NetworkWrapper.getInstance().sendToServer(new MessageUpdateGadgetRenderMaskServer(Gadgets.getRenderMask()));
                hasSent = true;
            }
        }
    }

    public enum Gadgets {
        KATANA(ItemRegistry.getInstance().itemKatana),
        SAI(ItemRegistry.getInstance().itemSai),
        SHURIKEN(ItemRegistry.getInstance().itemShuriken),
        SMOKE_BOMB(ItemRegistry.getInstance().itemSmokeBomb),
        ROPE_COIL(ItemRegistry.getInstance().itemRopeCoil);

        private final Item item;

        private int counter;
        private int prevCount;

        Gadgets(Item item) {
            this.item = item;
        }

        public Item getItem() {
            return item;
        }

        public void increment() {
            this.counter++;
        }

        public boolean updateAndCheckForChanges() {
            if(counter != prevCount) {
                prevCount = counter;
                counter = 0;
                return true;
            } else {
                prevCount = counter;
                counter = 0;
                return false;
            }
        }

        public static boolean[] getRenderMask() {
            return new boolean[]{
                    KATANA.prevCount > 0,
                    shouldRenderSai(Minecraft.getMinecraft().thePlayer, SAI.prevCount, false),
                    shouldRenderSai(Minecraft.getMinecraft().thePlayer, SAI.prevCount, true),
                    SHURIKEN.prevCount > 0,
                    SMOKE_BOMB.prevCount > 0,
                    ROPE_COIL.prevCount > 0
            };
        }

        private static boolean shouldRenderSai(EntityPlayer player, int itemCount, boolean left) {
            if(itemCount <= 0) {
                return false;
            }
            if(itemCount >= 2) {
                return true;
            }
            ItemStack main = player.getItemStackFromSlot(EntityEquipmentSlot.MAINHAND);
            ItemStack off = player.getItemStackFromSlot(EntityEquipmentSlot.OFFHAND);
            boolean hasRight = main != null && main.getItem() == SAI.getItem();
            boolean hasLeft = off != null && off.getItem() == SAI.getItem();
            if(hasLeft) {
                return !left;
            } else if(hasRight){
                return  left;
            }
            return !left;
        }

        public static Gadgets getGadgetFromItem(Item item) {
            if(item == null) {
                return null;
            }
            for(Gadgets gadget : values()) {
                if(gadget.getItem() == item) {
                    return gadget;
                }
            }
            return null;
        }
    }
}
