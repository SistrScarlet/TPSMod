package com.sistr.rshud.setup;

import com.sistr.rshud.block.ModificationWorkbenchBlock;
import com.sistr.rshud.block.ModificationWorkbenchContainer;
import com.sistr.rshud.item.GoggleItem;
import net.minecraft.block.Block;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraftforge.common.extensions.IForgeContainerType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import static com.sistr.rshud.RSHUDMod.MODID;

public class Registration {
    private static final DeferredRegister<Block> BLOCKS = new DeferredRegister<>(ForgeRegistries.BLOCKS, MODID);
    private static final DeferredRegister<Item> ITEMS = new DeferredRegister<>(ForgeRegistries.ITEMS, MODID);
    private static final DeferredRegister<ContainerType<?>> CONTAINERS = new DeferredRegister<>(ForgeRegistries.CONTAINERS, MODID);

    public static void init() {
        BLOCKS.register(FMLJavaModLoadingContext.get().getModEventBus());
        ITEMS.register(FMLJavaModLoadingContext.get().getModEventBus());
        CONTAINERS.register(FMLJavaModLoadingContext.get().getModEventBus());
    }

    private static final Item.Properties properties = new Item.Properties().group(ModSetup.ITEM_GROUP);

    //アイテムレジストリ
    public static final RegistryObject<Item> GOGGLE_ITEM = ITEMS.register("goggle", GoggleItem::new);

    //機能ブロック
    public static final RegistryObject<ModificationWorkbenchBlock> MOD_WORKBENCH_BLOCK = BLOCKS.register("modification_workbench", ModificationWorkbenchBlock::new);
    public static final RegistryObject<Item> MOD_WORKBENCH_ITEM = ITEMS.register("modification_workbench", () ->
            new BlockItem(MOD_WORKBENCH_BLOCK.get(), properties));
    public static final RegistryObject<ContainerType<ModificationWorkbenchContainer>> MOD_WORKBENCH_CONTAINER =
            CONTAINERS.register("modification_workbench", () -> IForgeContainerType.create((windowId, inv, data) ->
                    new ModificationWorkbenchContainer(windowId, inv)));



}
