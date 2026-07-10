package silly.perfectionist;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.InfestedBlock;

import java.util.Arrays;

public class BlacklistedItems {
    public static final Class<?>[] BLACKLISTINSTANCES = {
            AirItem.class,
            DebugStickItem.class,
            SpawnEggItem.class,
            InfestedBlock.class,
            GameMasterBlockItem.class,
            KnowledgeBookItem.class,
    };

    public static final String[] BLACKLISTNAMES = {
            "minecraft:bedrock",
            "minecraft:barrier",
            "minecraft:light",
            "minecraft:structure_void",

            "minecraft:spawner",
            "minecraft:trial_spawner",

            "minecraft:vault",

            "minecraft:farmland",
            "minecraft:dirt_path",
            "minecraft:frogspawn",

            "minecraft:command_block_minecart",

            "minecraft:infested_stone",
            "minecraft:infested_cobblestone",
            "minecraft:infested_stone_bricks",
            "minecraft:infested_mossy_stone_bricks",
            "minecraft:infested_cracked_stone_bricks",
            "minecraft:infested_chiseled_stone_bricks",
    };

    public static boolean isBlacklisted(Item item) {
        Class<?> itemClass = item.getClass();
        if(Arrays.asList(BLACKLISTINSTANCES).contains(itemClass)){
            return true;
        }

        String itemStringId = BuiltInRegistries.ITEM.getKey(item).toString();

        if(Arrays.asList(BLACKLISTNAMES).contains(itemStringId)){
            return true;
        }

        return false;
    }
}
