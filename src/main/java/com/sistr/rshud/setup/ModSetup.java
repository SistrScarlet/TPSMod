package com.sistr.rshud.setup;

import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

import static com.sistr.rshud.RSHUDMod.MODID;

@Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ModSetup {

    public static final ItemGroup ITEM_GROUP = new ItemGroup("rshud") {
        @Override
        public ItemStack createIcon() {
            return new ItemStack(Registration.GOGGLE_ITEM.get());
        }
    };

    public static void init(final FMLCommonSetupEvent event) {

    }

}
