package com.sistr.rshud.block;

import com.sistr.rshud.RSHUDMod;
import com.sistr.rshud.setup.Registration;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.CraftResultInventory;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.IWorldPosCallable;
import net.minecraftforge.common.Tags;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;

public class ModificationWorkbenchContainer extends Container {
    private final InvWrapper playerInventory;
    private final IWorldPosCallable posCallable;
    private final IInventory inputSlots = new Inventory(2) {
        /**
         * For tile entities, ensures the chunk containing the tile entity is saved to disk later - the game won't think
         * it hasn't changed and skip it.
         */
        public void markDirty() {
            super.markDirty();
            ModificationWorkbenchContainer.this.onCraftMatrixChanged(this);
        }
    };
    private final IInventory outputSlot = new CraftResultInventory();;

    public ModificationWorkbenchContainer(int id, PlayerInventory playerInventory) {
        this(id, playerInventory, IWorldPosCallable.DUMMY);
    }

    public ModificationWorkbenchContainer(int id, PlayerInventory playerInventory, final IWorldPosCallable posCallable) {
        super(Registration.MOD_WORKBENCH_CONTAINER.get(), id);
        this.playerInventory = new InvWrapper(playerInventory);
        this.posCallable = posCallable;

        this.addSlot(new Slot(this.inputSlots, 0, 27, 47));
        this.addSlot(new Slot(this.inputSlots, 1, 76, 47));
        this.addSlot(new Slot(this.outputSlot, 2, 134, 47) {
            /**
             * Check if the stack is allowed to be placed in this slot, used for armor slots as well as furnace fuel.
             */
            public boolean isItemValid(ItemStack stack) {
                return false;
            }

            public ItemStack onTake(PlayerEntity thePlayer, ItemStack stack) {
                ModificationWorkbenchContainer.this.inputSlots.setInventorySlotContents(0, ItemStack.EMPTY);
                ItemStack itemstack = ModificationWorkbenchContainer.this.inputSlots.getStackInSlot(1);
                if (!itemstack.isEmpty() && itemstack.getCount() > 0) {
                    itemstack.shrink(1);
                    ModificationWorkbenchContainer.this.inputSlots.setInventorySlotContents(1, itemstack);
                } else {
                    ModificationWorkbenchContainer.this.inputSlots.setInventorySlotContents(1, ItemStack.EMPTY);
                }

                posCallable.consume((world, pos) -> world.playEvent(1030, pos, 0));
                return stack;
            }
        });

        layoutPlayerInventorySlots(8, 84);
    }

    /**
     * Callback for when the crafting matrix is changed.
     * クラフトマトリクスが変更されたときのコールバック。
     */
    public void onCraftMatrixChanged(IInventory inventoryIn) {
        super.onCraftMatrixChanged(inventoryIn);
        if (inventoryIn == this.inputSlots) {
            this.updateOutput();
        }

    }

    /**
     * It is called when the input slot is changed and calculates the new result and puts it in the output slot.
     * 入力スロットが変更されたときに呼び出され、新しい結果を計算して出力スロットに入れます。
     */
    public void updateOutput() {
        ItemStack mainSlot = this.inputSlots.getStackInSlot(0);
        ItemStack subSlot = this.inputSlots.getStackInSlot(1);
        if (mainSlot.isEmpty() || subSlot.isEmpty()
                || MobEntity.getSlotForItemStack(mainSlot) != EquipmentSlotType.HEAD
                || !Tags.Items.INGOTS.contains(subSlot.getItem())) {
            this.outputSlot.setInventorySlotContents(0, ItemStack.EMPTY);
            return;
        }
        ItemStack output = mainSlot.copy();
        CompoundNBT tag = output.getOrCreateTag();
        if (tag.getBoolean(RSHUDMod.MODID + "CanRender")) {
            this.outputSlot.setInventorySlotContents(0, ItemStack.EMPTY);
            return;
        }
        tag.putBoolean(RSHUDMod.MODID + "CanRender", true);
        output.setTag(tag);

        this.outputSlot.setInventorySlotContents(0, output);
    }

    /**
     * Called when the container is closed.
     * コンテナが閉じられたときに呼び出されます。
     */
    public void onContainerClosed(PlayerEntity playerIn) {
        super.onContainerClosed(playerIn);
        this.posCallable.consume((world, pos) -> {
            this.clearContainer(playerIn, world, this.inputSlots);
        });
    }

    /**
     * Determines whether supplied player can use this container
     * 供給されたプレイヤーがこのコンテナを使用できるかどうかを決定します。
     */
    public boolean canInteractWith(PlayerEntity playerIn) {
        return this.posCallable.applyOrElse((world, pos) ->
                world.getBlockState(pos).getBlock() instanceof ModificationWorkbenchBlock/*isIn(BlockTags.ANVIL)*/
                        && playerIn.getDistanceSq(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D) <= 64.0D, true);
    }

    /**
     * Handle when the stack in slot {@code index} is shift-clicked. Normally this moves the stack between the player
     * inventory and the other inventory(s).
     * スロット{@code index}内のスタックがシフトクリックされたときの処理です。
     * 通常、これはプレイヤーのインベントリと他のインベントリの間でスタックを移動させます。
     */
    public ItemStack transferStackInSlot(PlayerEntity playerIn, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.inventorySlots.get(index);
        if (slot != null && slot.getHasStack()) {
            ItemStack itemstack1 = slot.getStack();
            itemstack = itemstack1.copy();
            if (index == 2) {
                if (!this.mergeItemStack(itemstack1, 3, 39, true)) {
                    return ItemStack.EMPTY;
                }

                slot.onSlotChange(itemstack1, itemstack);
            } else if (index != 0 && index != 1) {
                if (index < 39 && !this.mergeItemStack(itemstack1, 0, 2, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.mergeItemStack(itemstack1, 3, 39, false)) {
                return ItemStack.EMPTY;
            }

            if (itemstack1.isEmpty()) {
                slot.putStack(ItemStack.EMPTY);
            } else {
                slot.onSlotChanged();
            }

            if (itemstack1.getCount() == itemstack.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(playerIn, itemstack1);
        }

        return itemstack;
    }

    private int addSlotRange(IItemHandler handler, int index, int x, int y, int amount, int dx) {
        for (int i = 0; i < amount; i++) {
            addSlot(new SlotItemHandler(handler, index, x, y));
            x += dx;
            index++;
        }
        return index;
    }

    private int addSlotBox(IItemHandler handler, int index, int x, int y, int horAmount, int dx, int verAmount, int dy) {
        for (int j = 0; j < verAmount; j++) {
            index = addSlotRange(handler, index, x, y, horAmount, dx);
            y += dy;
        }
        return index;
    }

    private void layoutPlayerInventorySlots(int leftCol, int topRow) {
        // Player inventory
        addSlotBox(playerInventory, 9, leftCol, topRow, 9, 18, 3, 18);

        // Hotbar
        topRow += 58;
        addSlotRange(playerInventory, 0, leftCol, topRow, 9, 18);
    }

}
