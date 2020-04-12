package com.sistr.rshud.datagen;

import com.google.common.collect.ImmutableSet;
import com.sistr.rshud.setup.Registration;
import net.minecraft.advancements.criterion.EnchantmentPredicate;
import net.minecraft.advancements.criterion.ItemPredicate;
import net.minecraft.advancements.criterion.MinMaxBounds;
import net.minecraft.advancements.criterion.StatePropertiesPredicate;
import net.minecraft.block.*;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DirectoryCache;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.state.IProperty;
import net.minecraft.state.properties.SlabType;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.*;
import net.minecraft.world.storage.loot.conditions.*;
import net.minecraft.world.storage.loot.functions.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

public class BlockLootTables extends BaseLootTableProvider {
    private static final ILootCondition.IBuilder SILK_TOUCH = MatchTool.builder(ItemPredicate.Builder.create().enchantment(new EnchantmentPredicate(Enchantments.SILK_TOUCH, MinMaxBounds.IntBound.atLeast(1))));
    private static final ILootCondition.IBuilder NO_SILK_TOUCH = SILK_TOUCH.inverted();
    private static final ILootCondition.IBuilder SHEARS = MatchTool.builder(ItemPredicate.Builder.create().item(Items.SHEARS));
    private static final ILootCondition.IBuilder SILK_TOUCH_OR_SHEARS = SHEARS.alternative(SILK_TOUCH);
    private static final ILootCondition.IBuilder NOT_SILK_TOUCH_OR_SHEARS = SILK_TOUCH_OR_SHEARS.inverted();
    private static final Set<Item> IMMUNE_TO_EXPLOSIONS = Stream.of(Blocks.DRAGON_EGG, Blocks.BEACON, Blocks.CONDUIT, Blocks.SKELETON_SKULL, Blocks.WITHER_SKELETON_SKULL, Blocks.PLAYER_HEAD, Blocks.ZOMBIE_HEAD, Blocks.CREEPER_HEAD, Blocks.DRAGON_HEAD, Blocks.SHULKER_BOX, Blocks.BLACK_SHULKER_BOX, Blocks.BLUE_SHULKER_BOX, Blocks.BROWN_SHULKER_BOX, Blocks.CYAN_SHULKER_BOX, Blocks.GRAY_SHULKER_BOX, Blocks.GREEN_SHULKER_BOX, Blocks.LIGHT_BLUE_SHULKER_BOX, Blocks.LIGHT_GRAY_SHULKER_BOX, Blocks.LIME_SHULKER_BOX, Blocks.MAGENTA_SHULKER_BOX, Blocks.ORANGE_SHULKER_BOX, Blocks.PINK_SHULKER_BOX, Blocks.PURPLE_SHULKER_BOX, Blocks.RED_SHULKER_BOX, Blocks.WHITE_SHULKER_BOX, Blocks.YELLOW_SHULKER_BOX).map(IItemProvider::asItem).collect(ImmutableSet.toImmutableSet());
    private static final float[] DEFAULT_SAPLING_DROP_RATES = new float[]{0.05F, 0.0625F, 0.083333336F, 0.1F};
    private static final float[] RARE_SAPLING_DROP_RATES = new float[]{0.025F, 0.027777778F, 0.03125F, 0.041666668F, 0.1F};

    protected final Map<Block, LootTable.Builder> blockLootTables = new HashMap<>();

    public BlockLootTables(DataGenerator dataGeneratorIn) {
        super(dataGeneratorIn);
    }

    @Override
    public void act(DirectoryCache cache) {
        super.act(cache);
        Map<ResourceLocation, LootTable> tables = new HashMap<>();
        for (Map.Entry<Block, LootTable.Builder> entry : blockLootTables.entrySet()) {
            tables.put(entry.getKey().getLootTable(), entry.getValue().setParameterSet(LootParameterSets.BLOCK).build());
        }
        writeTables(cache, tables);
    }

    @Override
    protected void addTables() {
        this.registerDropSelfLootTable(Registration.MOD_WORKBENCH_BLOCK.get());
    }

    protected static <T> T withExplosionDecay(IItemProvider p_218552_0_, ILootFunctionConsumer<T> p_218552_1_) {
        return !IMMUNE_TO_EXPLOSIONS.contains(p_218552_0_.asItem()) ? p_218552_1_.acceptFunction(ExplosionDecay.builder()) : p_218552_1_.cast();
    }

    protected static <T> T withSurvivesExplosion(IItemProvider p_218560_0_, ILootConditionConsumer<T> p_218560_1_) {
        return !IMMUNE_TO_EXPLOSIONS.contains(p_218560_0_.asItem()) ? p_218560_1_.acceptCondition(SurvivesExplosion.builder()) : p_218560_1_.cast();
    }

    protected static LootTable.Builder dropping(IItemProvider p_218546_0_) {
        return LootTable.builder().addLootPool(withSurvivesExplosion(p_218546_0_, LootPool.builder().rolls(ConstantRange.of(1))
                .addEntry(ItemLootEntry.builder(p_218546_0_))));
    }

    protected static LootTable.Builder dropping(Block p_218494_0_, ILootCondition.IBuilder p_218494_1_, LootEntry.Builder<?> p_218494_2_) {
        return LootTable.builder().addLootPool(LootPool.builder().rolls(ConstantRange.of(1)).addEntry(ItemLootEntry.builder(p_218494_0_)
                .acceptCondition(p_218494_1_).alternatively(p_218494_2_)));
    }

    protected static LootTable.Builder droppingWithSilkTouch(Block p_218519_0_, LootEntry.Builder<?> p_218519_1_) {
        return dropping(p_218519_0_, SILK_TOUCH, p_218519_1_);
    }

    protected static LootTable.Builder droppingWithShears(Block p_218511_0_, LootEntry.Builder<?> p_218511_1_) {
        return dropping(p_218511_0_, SHEARS, p_218511_1_);
    }

    protected static LootTable.Builder droppingWithSilkTouchOrShears(Block p_218535_0_, LootEntry.Builder<?> p_218535_1_) {
        return dropping(p_218535_0_, SILK_TOUCH_OR_SHEARS, p_218535_1_);
    }

    protected static LootTable.Builder droppingWithSilkTouch(Block p_218515_0_, IItemProvider p_218515_1_) {
        return droppingWithSilkTouch(p_218515_0_, withSurvivesExplosion(p_218515_0_, ItemLootEntry.builder(p_218515_1_)));
    }

    protected static LootTable.Builder droppingRandomly(IItemProvider p_218463_0_, IRandomRange p_218463_1_) {
        return LootTable.builder().addLootPool(LootPool.builder().rolls(ConstantRange.of(1))
                .addEntry(withExplosionDecay(p_218463_0_, ItemLootEntry.builder(p_218463_0_).acceptFunction(SetCount.builder(p_218463_1_)))));
    }

    protected static LootTable.Builder droppingWithSilkTouchOrRandomly(Block p_218530_0_, IItemProvider p_218530_1_, IRandomRange p_218530_2_) {
        return droppingWithSilkTouch(p_218530_0_, withExplosionDecay(p_218530_0_, ItemLootEntry.builder(p_218530_1_)
                .acceptFunction(SetCount.builder(p_218530_2_))));
    }

    protected static LootTable.Builder onlyWithSilkTouch(IItemProvider p_218561_0_) {
        return LootTable.builder().addLootPool(LootPool.builder().acceptCondition(SILK_TOUCH).rolls(ConstantRange.of(1))
                .addEntry(ItemLootEntry.builder(p_218561_0_)));
    }

    protected static LootTable.Builder droppingAndFlowerPot(IItemProvider p_218523_0_) {
        return LootTable.builder().addLootPool(withSurvivesExplosion(Blocks.FLOWER_POT, LootPool.builder().rolls(ConstantRange.of(1))
                .addEntry(ItemLootEntry.builder(Blocks.FLOWER_POT))))
                .addLootPool(withSurvivesExplosion(p_218523_0_, LootPool.builder().rolls(ConstantRange.of(1))
                        .addEntry(ItemLootEntry.builder(p_218523_0_))));
    }

    protected static LootTable.Builder droppingSlab(Block p_218513_0_) {
        return LootTable.builder().addLootPool(LootPool.builder().rolls(ConstantRange.of(1))
                .addEntry(withExplosionDecay(p_218513_0_, ItemLootEntry.builder(p_218513_0_)
                        .acceptFunction(SetCount.builder(ConstantRange.of(2)).acceptCondition(BlockStateProperty.builder(p_218513_0_)
                                .fromProperties(StatePropertiesPredicate.Builder.newBuilder().withProp(SlabBlock.TYPE, SlabType.DOUBLE)))))));
    }

    protected static <T extends Comparable<T> & IStringSerializable> LootTable.Builder droppingWhen(Block p_218562_0_, IProperty<T> p_218562_1_, T p_218562_2_) {
        return LootTable.builder().addLootPool(withSurvivesExplosion(p_218562_0_, LootPool.builder().rolls(ConstantRange.of(1))
                .addEntry(ItemLootEntry.builder(p_218562_0_).acceptCondition(BlockStateProperty.builder(p_218562_0_)
                        .fromProperties(StatePropertiesPredicate.Builder.newBuilder().withProp(p_218562_1_, p_218562_2_))))));
    }

    protected static LootTable.Builder droppingWithName(Block p_218481_0_) {
        return LootTable.builder().addLootPool(withSurvivesExplosion(p_218481_0_, LootPool.builder().rolls(ConstantRange.of(1))
                .addEntry(ItemLootEntry.builder(p_218481_0_).acceptFunction(CopyName.builder(CopyName.Source.BLOCK_ENTITY)))));
    }

    protected static LootTable.Builder droppingWithContents(Block p_218544_0_) {
        return LootTable.builder().addLootPool(withSurvivesExplosion(p_218544_0_, LootPool.builder().rolls(ConstantRange.of(1))
                .addEntry(ItemLootEntry.builder(p_218544_0_).acceptFunction(CopyName.builder(CopyName.Source.BLOCK_ENTITY))
                        .acceptFunction(CopyNbt.builder(CopyNbt.Source.BLOCK_ENTITY).replaceOperation("Lock", "BlockEntityTag.Lock")
                                .replaceOperation("LootTable", "BlockEntityTag.LootTable")
                                .replaceOperation("LootTableSeed", "BlockEntityTag.LootTableSeed"))
                        .acceptFunction(SetContents.func_215920_b().func_216075_a(DynamicLootEntry.func_216162_a(ShulkerBoxBlock.CONTENTS))))));
    }

    protected static LootTable.Builder droppingWithPatterns(Block p_218559_0_) {
        return LootTable.builder().addLootPool(withSurvivesExplosion(p_218559_0_, LootPool.builder().rolls(ConstantRange.of(1))
                .addEntry(ItemLootEntry.builder(p_218559_0_).acceptFunction(CopyName.builder(CopyName.Source.BLOCK_ENTITY))
                        .acceptFunction(CopyNbt.builder(CopyNbt.Source.BLOCK_ENTITY)
                                .replaceOperation("Patterns", "BlockEntityTag.Patterns")))));
    }

    private static LootTable.Builder func_229436_h_(Block p_229436_0_) {
        return LootTable.builder().addLootPool(LootPool.builder().acceptCondition(SILK_TOUCH).rolls(ConstantRange.of(1))
                .addEntry(ItemLootEntry.builder(p_229436_0_).acceptFunction(CopyNbt.builder(CopyNbt.Source.BLOCK_ENTITY).replaceOperation("Bees", "BlockEntityTag.Bees")).acceptFunction(CopyBlockState.func_227545_a_(p_229436_0_).func_227552_a_(BeehiveBlock.HONEY_LEVEL))));
    }

    private static LootTable.Builder func_229437_i_(Block p_229437_0_) {
        return LootTable.builder().addLootPool(LootPool.builder().rolls(ConstantRange.of(1)).addEntry(ItemLootEntry.builder(p_229437_0_)
                .acceptCondition(SILK_TOUCH).acceptFunction(CopyNbt.builder(CopyNbt.Source.BLOCK_ENTITY)
                        .replaceOperation("Bees", "BlockEntityTag.Bees"))
                .acceptFunction(CopyBlockState.func_227545_a_(p_229437_0_).func_227552_a_(BeehiveBlock.HONEY_LEVEL))
                .alternatively(ItemLootEntry.builder(p_229437_0_))));
    }

    protected static LootTable.Builder droppingItemWithFortune(Block p_218476_0_, Item p_218476_1_) {
        return droppingWithSilkTouch(p_218476_0_, withExplosionDecay(p_218476_0_, ItemLootEntry.builder(p_218476_1_)
                .acceptFunction(ApplyBonus.oreDrops(Enchantments.FORTUNE))));
    }

    /**
     * Creates a builder that drops the given IItemProvider in amounts between 0 and 2, most often 0. Only used in
     * vanilla for huge mushroom blocks.
     */
    protected static LootTable.Builder droppingItemRarely(Block p_218491_0_, IItemProvider p_218491_1_) {
        return droppingWithSilkTouch(p_218491_0_, withExplosionDecay(p_218491_0_, ItemLootEntry.builder(p_218491_1_)
                .acceptFunction(SetCount.builder(RandomValueRange.of(-6.0F, 2.0F)))
                .acceptFunction(LimitCount.func_215911_a(IntClamper.func_215848_a(0)))));
    }

    protected static LootTable.Builder droppingSeeds(Block p_218570_0_) {
        return droppingWithShears(p_218570_0_, withExplosionDecay(p_218570_0_, (ItemLootEntry.builder(Items.WHEAT_SEEDS)
                .acceptCondition(RandomChance.builder(0.125F)))
                .acceptFunction(ApplyBonus.uniformBonusCount(Enchantments.FORTUNE, 2))));
    }

    /**
     * Creates a builder that drops the given IItemProvider in amounts between 0 and 3, based on the AGE property. Only
     * used in vanilla for pumpkin and melon stems.
     */
    protected static LootTable.Builder droppingByAge(Block p_218475_0_, Item p_218475_1_) {
        return LootTable.builder().addLootPool(withExplosionDecay(p_218475_0_, LootPool.builder().rolls(ConstantRange.of(1))
                .addEntry(ItemLootEntry.builder(p_218475_1_).acceptFunction(SetCount.builder(BinomialRange.of(3, 0.06666667F))
                        .acceptCondition(BlockStateProperty.builder(p_218475_0_).fromProperties(StatePropertiesPredicate.Builder.newBuilder()
                                .withIntProp(StemBlock.AGE, 0))))
                        .acceptFunction(SetCount.builder(BinomialRange.of(3, 0.13333334F))
                                .acceptCondition(BlockStateProperty.builder(p_218475_0_)
                                        .fromProperties(StatePropertiesPredicate.Builder.newBuilder().withIntProp(StemBlock.AGE, 1))))
                        .acceptFunction(SetCount.builder(BinomialRange.of(3, 0.2F))
                                .acceptCondition(BlockStateProperty.builder(p_218475_0_).fromProperties(StatePropertiesPredicate.Builder.newBuilder()
                                        .withIntProp(StemBlock.AGE, 2))))
                        .acceptFunction(SetCount.builder(BinomialRange.of(3, 0.26666668F))
                                .acceptCondition(BlockStateProperty.builder(p_218475_0_)
                                        .fromProperties(StatePropertiesPredicate.Builder.newBuilder().withIntProp(StemBlock.AGE, 3))))
                        .acceptFunction(SetCount.builder(BinomialRange.of(3, 0.33333334F))
                                .acceptCondition(BlockStateProperty.builder(p_218475_0_)
                                        .fromProperties(StatePropertiesPredicate.Builder.newBuilder().withIntProp(StemBlock.AGE, 4))))
                        .acceptFunction(SetCount.builder(BinomialRange.of(3, 0.4F))
                                .acceptCondition(BlockStateProperty.builder(p_218475_0_)
                                        .fromProperties(StatePropertiesPredicate.Builder.newBuilder().withIntProp(StemBlock.AGE, 5))))
                        .acceptFunction(SetCount.builder(BinomialRange.of(3, 0.46666667F))
                                .acceptCondition(BlockStateProperty.builder(p_218475_0_)
                                        .fromProperties(StatePropertiesPredicate.Builder.newBuilder().withIntProp(StemBlock.AGE, 6))))
                        .acceptFunction(SetCount.builder(BinomialRange.of(3, 0.53333336F))
                                .acceptCondition(BlockStateProperty.builder(p_218475_0_)
                                        .fromProperties(StatePropertiesPredicate.Builder.newBuilder().withIntProp(StemBlock.AGE, 7)))))));
    }

    private static LootTable.Builder func_229435_c_(Block p_229435_0_, Item p_229435_1_) {
        return LootTable.builder().addLootPool(withExplosionDecay(p_229435_0_, LootPool.builder().rolls(ConstantRange.of(1))
                .addEntry(ItemLootEntry.builder(p_229435_1_).acceptFunction(SetCount.builder(BinomialRange.of(3, 0.53333336F))))));
    }

    protected static LootTable.Builder onlyWithShears(IItemProvider p_218486_0_) {
        return LootTable.builder().addLootPool(LootPool.builder().rolls(ConstantRange.of(1)).acceptCondition(SHEARS)
                .addEntry(ItemLootEntry.builder(p_218486_0_)));
    }

    /**
     * Used for all leaves, drops self with silk touch, otherwise drops the second Block param with the passed chances
     * for fortune levels, adding in sticks.
     */
    protected static LootTable.Builder droppingWithChancesAndSticks(Block p_218540_0_, Block p_218540_1_, float... p_218540_2_) {
        return droppingWithSilkTouchOrShears(p_218540_0_, withSurvivesExplosion(p_218540_0_, ItemLootEntry.builder(p_218540_1_))
                .acceptCondition(TableBonus.builder(Enchantments.FORTUNE, p_218540_2_)))
                .addLootPool(LootPool.builder().rolls(ConstantRange.of(1)).acceptCondition(NOT_SILK_TOUCH_OR_SHEARS)
                        .addEntry(withExplosionDecay(p_218540_0_, ItemLootEntry.builder(Items.STICK)
                                .acceptFunction(SetCount.builder(RandomValueRange.of(1.0F, 2.0F))))
                                .acceptCondition(TableBonus.builder(Enchantments.FORTUNE, 0.02F, 0.022222223F, 0.025F, 0.033333335F, 0.1F))));
    }

    /**
     * Used for oak and dark oak, same as droppingWithChancesAndSticks but adding in apples.
     */
    protected static LootTable.Builder droppingWithChancesSticksAndApples(Block p_218526_0_, Block p_218526_1_, float... p_218526_2_) {
        return droppingWithChancesAndSticks(p_218526_0_, p_218526_1_, p_218526_2_)
                .addLootPool(LootPool.builder().rolls(ConstantRange.of(1))
                        .acceptCondition(NOT_SILK_TOUCH_OR_SHEARS).addEntry(withSurvivesExplosion(p_218526_0_, ItemLootEntry.builder(Items.APPLE))
                                .acceptCondition(TableBonus.builder(Enchantments.FORTUNE, 0.005F, 0.0055555557F, 0.00625F, 0.008333334F, 0.025F))));
    }

    /**
     * Drops the first item parameter always, and the second item parameter plus more of the first when the loot
     * condition is met, applying fortune to only the second argument.
     */
    protected static LootTable.Builder droppingAndBonusWhen(Block p_218541_0_, Item p_218541_1_, Item p_218541_2_, ILootCondition.IBuilder p_218541_3_) {
        return withExplosionDecay(p_218541_0_, LootTable.builder().addLootPool(LootPool.builder().addEntry(ItemLootEntry.builder(p_218541_1_)
                .acceptCondition(p_218541_3_).alternatively(ItemLootEntry.builder(p_218541_2_)))).addLootPool(LootPool.builder()
                .acceptCondition(p_218541_3_).addEntry(ItemLootEntry.builder(p_218541_2_)
                        .acceptFunction(ApplyBonus.binomialWithBonusCount(Enchantments.FORTUNE, 0.5714286F, 3)))));
    }

    public static LootTable.Builder func_218482_a() {
        return LootTable.builder();
    }

    public void registerFlowerPot(Block flowerPot) {
        this.registerLootTable(flowerPot, (p_229438_0_) -> {
            return droppingAndFlowerPot(((FlowerPotBlock) p_229438_0_).func_220276_d());
        });
    }

    public void registerSilkTouch(Block blockIn, Block silkTouchDrop) {
        this.registerLootTable(blockIn, onlyWithSilkTouch(silkTouchDrop));
    }

    public void registerDropping(Block blockIn, IItemProvider drop) {
        this.registerLootTable(blockIn, dropping(drop));
    }

    public void registerSilkTouch(Block blockIn) {
        this.registerSilkTouch(blockIn, blockIn);
    }

    public void registerDropSelfLootTable(Block p_218492_1_) {
        this.registerDropping(p_218492_1_, p_218492_1_);
    }

    protected void registerLootTable(Block blockIn, Function<Block, LootTable.Builder> factory) {
        this.registerLootTable(blockIn, factory.apply(blockIn));
    }

    protected void registerLootTable(Block blockIn, LootTable.Builder table) {
        this.blockLootTables.put(blockIn, table);
    }

    @Override
    public String getName() {
        return "RSHUD Mod BlockLootTables";
    }

}
