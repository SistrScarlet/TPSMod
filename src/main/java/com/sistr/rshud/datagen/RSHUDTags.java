package com.sistr.rshud.datagen;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.Tag;
import net.minecraft.util.ResourceLocation;

public class RSHUDTags {

    public static class Blocks {
        public static final Tag<Block> SAMPLE = tag("sample");

        private static Tag<Block> tag(String name) {
            return new BlockTags.Wrapper(new ResourceLocation("rshud", name));
        }
    }

    public static class Items {
        public static final Tag<Item> RSHUD_MOUNTED = tag("rshud_mounted");

        private static Tag<Item> tag(String name) {
            return new ItemTags.Wrapper(new ResourceLocation("rshud", name));
        }
    }
}
