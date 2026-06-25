package com.liquidglass.render;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;

import java.util.Set;

/**
 * Набор «руд» для XRay вынесен сюда, а не в миксин:
 * статическое поле в миксине вмёрдживается в {@code Block.<clinit>} и вызывает
 * циклическую инициализацию (краш). Здесь инициализация ленивая — после загрузки игры.
 */
public final class XRayBlocks {
    private static Set<Block> ores;
    private static Set<Block> valuable;

    private XRayBlocks() {
    }

    public static boolean isOre(Block block) {
        if (ores == null) {
            ores = Set.of(
                    Blocks.COAL_ORE, Blocks.DEEPSLATE_COAL_ORE,
                    Blocks.IRON_ORE, Blocks.DEEPSLATE_IRON_ORE,
                    Blocks.COPPER_ORE, Blocks.DEEPSLATE_COPPER_ORE,
                    Blocks.GOLD_ORE, Blocks.DEEPSLATE_GOLD_ORE,
                    Blocks.REDSTONE_ORE, Blocks.DEEPSLATE_REDSTONE_ORE,
                    Blocks.LAPIS_ORE, Blocks.DEEPSLATE_LAPIS_ORE,
                    Blocks.DIAMOND_ORE, Blocks.DEEPSLATE_DIAMOND_ORE,
                    Blocks.EMERALD_ORE, Blocks.DEEPSLATE_EMERALD_ORE,
                    Blocks.NETHER_GOLD_ORE, Blocks.NETHER_QUARTZ_ORE,
                    Blocks.ANCIENT_DEBRIS
            );
        }
        return ores.contains(block);
    }

    /** Только дорогое: алмазы, изумруды, золото, древние обломки. */
    public static boolean isValuable(Block block) {
        if (valuable == null) {
            valuable = Set.of(
                    Blocks.DIAMOND_ORE, Blocks.DEEPSLATE_DIAMOND_ORE,
                    Blocks.EMERALD_ORE, Blocks.DEEPSLATE_EMERALD_ORE,
                    Blocks.GOLD_ORE, Blocks.DEEPSLATE_GOLD_ORE,
                    Blocks.NETHER_GOLD_ORE, Blocks.ANCIENT_DEBRIS
            );
        }
        return valuable.contains(block);
    }
}
