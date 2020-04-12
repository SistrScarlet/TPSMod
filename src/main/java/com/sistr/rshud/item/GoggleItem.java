package com.sistr.rshud.item;

import com.sistr.rshud.setup.ModSetup;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import javax.annotation.Nullable;

public class GoggleItem extends Item {

    public GoggleItem() {
        super(new Item.Properties()
                .maxDamage(0)
                .group(ModSetup.ITEM_GROUP));
    }

    @Nullable
    @Override
    public EquipmentSlotType getEquipmentSlot(ItemStack stack) {
        return EquipmentSlotType.HEAD;
    }
}
