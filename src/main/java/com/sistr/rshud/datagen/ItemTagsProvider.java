package com.sistr.rshud.datagen;

import com.sistr.rshud.setup.Registration;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.common.data.ForgeItemTagsProvider;

public class ItemTagsProvider extends ForgeItemTagsProvider {

    public ItemTagsProvider(DataGenerator gen) {
        super(gen);
    }

    @Override
    public void registerTags() {
        getBuilder(RSHUDTags.Items.RSHUD_MOUNTED).add(Registration.GOGGLE_ITEM.get());
    }
}
