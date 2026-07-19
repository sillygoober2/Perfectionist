package silly.perfectionist;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.minecraft.client.Minecraft;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.*;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.*;

public class CollectionDataManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private final Set<String> unlockedItems = new HashSet<>();
    public static final List<Item> allSurvivalStacks = new ArrayList<>();

    private String currentWorld = "";

    public void getPossibleItems(){
        Minecraft mc = Minecraft.getInstance();

        allSurvivalStacks.clear();

        CreativeModeTab searchTab = CreativeModeTabs.searchTab();
        if (searchTab == null) return;

        FeatureFlagSet featureFlags = mc.player.connection.enabledFeatures();
        boolean hasOpPermissions = mc.options.operatorItemsTab().get() && mc.player.canUseGameMasterBlocks();
        HolderLookup.Provider registries = mc.level.registryAccess();

        CreativeModeTab.ItemDisplayParameters params = new CreativeModeTab.ItemDisplayParameters(
                featureFlags,
                false,
                registries
        );

        for (CreativeModeTab tab : CreativeModeTabs.allTabs()) {
            if (tab != searchTab) {
                tab.buildContents(params);
            }
        }

        searchTab.buildContents(params);

        Collection<ItemStack> searchDisplayItems = searchTab.getDisplayItems();
        if (searchDisplayItems == null || searchDisplayItems.isEmpty()) return;

        for(ItemStack stack : searchDisplayItems) {
            Item item = stack.getItem();
            System.out.println(item.toString());

            if(BlacklistedItems.isBlacklisted(item)) continue;
            if(allSurvivalStacks.contains(item)) continue;

            String itemStringId = BuiltInRegistries.ITEM.getKey(item).toString();
            boolean isUnlocked = hasItem(itemStringId);

            allSurvivalStacks.add(item);

            if (isUnlocked) {
                unlockedItems.add(itemStringId);
            }
        }
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