package silly.perfectionist;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CollectionDataManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private final Set<String> unlockedItems = new HashSet<>();
    public static final List<ItemStack> allSurvivalStacks = new ArrayList<>();

    private String currentWorld = "";

    public void getPossibleItems(){
        for(Item item : BuiltInRegistries.ITEM) {
            if(item instanceof SpawnEggItem) continue;

            String itemStringId = BuiltInRegistries.ITEM.getKey(item).toString();

            if(BlacklistedItems.isBlacklisted(item)) continue;

            boolean isUnlocked = hasItem(itemStringId);

            allSurvivalStacks.add(new ItemStack(item));

            if (isUnlocked) {
                unlockedItems.add(itemStringId);
            }
        }
        System.out.println("Possible Items Amount: "+allSurvivalStacks.size());
        System.out.println("ALL Minecraft Items: "+BuiltInRegistries.ITEM.size());
    }

    public void updateCurrentWorld() {
        Minecraft mc = Minecraft.getInstance();

        if (mc.isLocalServer() && mc.getSingleplayerServer() != null) {
            this.currentWorld = "singeplayer_"+mc.getSingleplayerServer().getWorldData().getLevelName();
        } else if (mc.getCurrentServer() != null) {
            this.currentWorld = "multiplayer_"+mc.getCurrentServer().ip.replaceAll("[^a-zA-Z0-9]", "_");
        } else {
            this.currentWorld = "unknown_world";
        }

        loadCollectionLog();
    }

    public boolean hasItem(String itemRegistryName) {
        return unlockedItems.contains(itemRegistryName);
    }

    public int getUnlockedAmount() {
        return unlockedItems.size();
    }

    public boolean addDiscoveredItem(Item item) {
        if(BlacklistedItems.isBlacklisted(item)){
            return false;
        }

        if (unlockedItems.add(item.toString())) {
            saveCollectionLog();
            return true;
        }
        return false;
    }

    private File getSaveFile() {
        java.io.File dir = new java.io.File(Minecraft.getInstance().gameDirectory, "config/perfectionist");
        if (!dir.exists()) dir.mkdirs();
        return new File(dir, "storage_"+ currentWorld +".json");
    }

    private void loadCollectionLog() {
        unlockedItems.clear();
        File file = getSaveFile();
        if(!file.exists()) return;

        try (FileReader reader = new FileReader(file)) {
            Type setType = new TypeToken<HashSet<String>>(){}.getType();
            Set<String> loadedData = GSON.fromJson(reader, setType);

            if (loadedData != null){
                unlockedItems.addAll(loadedData);
            }
        } catch (IOException e) {
            Constants.LOGGER.error("Failed to load collection data", e);
        }
    }

    private void saveCollectionLog() {
        if (currentWorld.equals("unknown_world")) return;

        File file = getSaveFile();
        try (FileWriter writer = new FileWriter(file)) {
            GSON.toJson(unlockedItems, writer);
        } catch (IOException e) {
            Constants.LOGGER.error("Failed to save collection data", e);
        }
    }
    
}
