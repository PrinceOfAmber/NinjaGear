package com.InfinityRaider.ninjagear.item;

import com.InfinityRaider.ninjagear.api.v1.IHiddenItem;
import com.InfinityRaider.ninjagear.block.BlockRope;
import com.InfinityRaider.ninjagear.handler.ConfigurationHandler;
import com.InfinityRaider.ninjagear.reference.Reference;
import com.InfinityRaider.ninjagear.registry.BlockRegistry;
import com.InfinityRaider.ninjagear.registry.ItemRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;

import java.util.ArrayList;
import java.util.List;

public class ItemRope extends ItemBase implements IItemWithModel, IHiddenItem, IItemWithRecipe {
    private final BlockRope block;

    public ItemRope() {
        super("ropeItem");
        this.block = (BlockRope) BlockRegistry.getInstance().blockRope;
        this.setCreativeTab(ItemRegistry.getInstance().creativeTab());
        ItemRegistry.getInstance().items.add(this);
    }

    @Override
    public EnumActionResult onItemUse(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing face, float hitX, float hitY, float hitZ) {
        IBlockState state = world.getBlockState(pos);
        Block block = state.getBlock();
        if(block instanceof BlockRope) {
            return EnumActionResult.PASS;
        }
        if (!block.isReplaceable(world, pos)) {
            pos = pos.offset(face);
        }
        if (stack.stackSize != 0 && player.canPlayerEdit(pos, face, stack) && world.canBlockBePlaced(this.block, pos, false, face, null, stack)) {
            int i = this.getMetadata(stack.getMetadata());
            IBlockState newState = this.block.onBlockPlaced(world, pos, face, hitX, hitY, hitZ, i, player);
            if (this.block.canPlaceBlockAt(world, pos) && placeBlockAt(stack, player, world, pos, newState)) {
                SoundType soundtype = this.block.getStepSound();
                world.playSound(player, pos, soundtype.getPlaceSound(), SoundCategory.BLOCKS, (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);
                --stack.stackSize;
            }
            return EnumActionResult.SUCCESS;
        } else {
            return EnumActionResult.FAIL;
        }
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(ItemStack stack, World world, EntityPlayer player, EnumHand hand) {
        if(player.isSneaking()) {
            if(world.isRemote) {
                return new ActionResult<>(EnumActionResult.PASS, stack);
            } else {
                this.attemptToCreateRopeCoil(player);
                return new ActionResult<>(EnumActionResult.SUCCESS, stack);
            }
        }
        return new ActionResult<>(EnumActionResult.PASS, stack);
    }

    public void attemptToCreateRopeCoil(EntityPlayer player) {
        ItemStack stack = player.inventory.getCurrentItem();
        if(stack != null && stack.getItem() instanceof ItemRope && stack.stackSize >= ConfigurationHandler.getInstance().ropeCoilLength) {
            ItemStack coil = new ItemStack(ItemRegistry.getInstance().itemRopeCoil, 1, 0);
            if(player.inventory.addItemStackToInventory(coil) && !player.capabilities.isCreativeMode) {
                player.inventory.decrStackSize(player.inventory.currentItem, ConfigurationHandler.getInstance().ropeCoilLength);
            }
        }
    }

    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, EntityPlayer playerIn, List<String> tooltip, boolean advanced) {
        tooltip.add(I18n.translateToLocal(Reference.MOD_ID + ".tooltip:" + this.getInternalName() + "_L1"));
        tooltip.add(I18n.translateToLocal(Reference.MOD_ID + ".tooltip:" + this.getInternalName() + "_L2"));
        tooltip.add(I18n.translateToLocal(Reference.MOD_ID + ".tooltip:" + this.getInternalName() + "_L3"));
        tooltip.add(I18n.translateToLocal(Reference.MOD_ID + ".tooltip:" + this.getInternalName() + "_L4"));
        tooltip.add(I18n.translateToLocal(Reference.MOD_ID + ".tooltip:" + this.getInternalName() + "_L5"));
    }

    public boolean placeBlockAt(ItemStack stack, EntityPlayer player, World world, BlockPos pos, IBlockState newState) {
        if (!world.setBlockState(pos, newState, 3)) {
            return false;
        }
        IBlockState state = world.getBlockState(pos);
        if (state.getBlock() == this.block) {
            this.block.onBlockPlacedBy(world, pos, state, player, stack);
        }
        return true;
    }

    @Override
    public boolean shouldRevealPlayerWhenEquipped(EntityPlayer entity, ItemStack stack) {
        return false;
    }


    @Override
    public List<IRecipe> getRecipes() {
        List<IRecipe> list = new ArrayList<>();
        list.add(new ShapedOreRecipe(new ItemStack(this, 8),"s  ", "sss", "  s",
                's', "string"));
        list.add(new ShapelessOreRecipe(new ItemStack(this, ConfigurationHandler.getInstance().ropeCoilLength),
                ItemRegistry.getInstance().itemRopeCoil));
        return list;
    }
}
