package com.sistr.rshud.block;

import com.sistr.rshud.RSHUDMod;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.inventory.container.SimpleNamedContainerProvider;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.IWorldPosCallable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class ModificationWorkbenchBlock extends Block {

    public ModificationWorkbenchBlock() {
        super(Block.Properties.create(Material.ROCK).hardnessAndResistance(3.5F));
    }

    public ActionResultType onBlockActivated(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult result) {
        if (!world.isRemote) {
            player.openContainer(state.getContainer(world, pos));
            //player.addStat(Stats.INTERACT_WITH_ANVIL);
        }
        return ActionResultType.SUCCESS;
    }

    @Nullable
    public INamedContainerProvider getContainer(BlockState state, World world, BlockPos pos) {
        return new SimpleNamedContainerProvider((windowId, inv, player) ->
                new ModificationWorkbenchContainer(windowId, inv, IWorldPosCallable.of(world, pos)), new TranslationTextComponent(RSHUDMod.MODID + ".container.modification_workbench"));
    }

}
