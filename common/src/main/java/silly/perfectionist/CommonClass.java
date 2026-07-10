package silly.perfectionist;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.stats.StatsCounter;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import silly.perfectionist.platform.Services;

import static silly.perfectionist.CollectionDataManager.allSurvivalStacks;
import static silly.perfectionist.Constants.dataManager;
import static silly.perfectionist.Constants.openKey;

public class CommonClass {
    public static boolean statsChecked = false;
    private static boolean requestedStats = false;

    public static void init() {
        if (Services.PLATFORM.isModLoaded(Constants.MOD_ID)) {

            Constants.LOG.info("Hello to "+Constants.MOD_NAME);
        }
    }

    public static KeyMapping.Category registerCategory(){
        return new KeyMapping.Category(
                Identifier.fromNamespaceAndPath("perfectionist", "perfectionist")
        );
    }

    private void playClientSound(){
        Player player = Minecraft.getInstance().player;

        if(player == null) return;

        player.level().playSound(player,
                player.getX(), player.getY(), player.getZ(),
                SoundEvents.EXPERIENCE_ORB_PICKUP,
                SoundSource.MASTER, 0.5F, 0.9F - (player.level().getRandom().nextFloat() * 0.4F));
    }

    public static void onJoin(){
        dataManager.updateCurrentWorld();
        statsChecked = false;
        requestedStats = true;

        if(dataManager.allSurvivalStacks.isEmpty()){
            dataManager.getPossibleItems();
        }
    }

    public static void onTick(){
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;

        if(player == null || mc.level == null) return;

        if (requestedStats && !statsChecked) {
            StatsCounter stats = mc.player.getStats();
            if (stats != null && stats.getValue(Stats.CUSTOM.get(Stats.LEAVE_GAME)) > 0) {
                checkStats(stats);
            }
        }

        while(openKey.consumeClick()){
            if(mc.gui.screen() instanceof CommonScreen){
                mc.setScreenAndShow(null);
            } else{
                mc.setScreenAndShow(new CommonScreen(dataManager));
            }
        }

        if(player.gameMode() == GameType.CREATIVE || player.gameMode() == GameType.SPECTATOR) return;

        ItemStack mouseItem = player.containerMenu.getCarried();
        if(!mouseItem.isEmpty()){
            String itemRegistryName = mouseItem.getItem().toString();

            if(!dataManager.hasItem(itemRegistryName)){

                if(dataManager.addDiscoveredItem(mouseItem.getItem())){
                    alertDiscover(mouseItem);
                    //playClientSound();
                }
            }
        }

        for (int i = 0; i < player.getInventory().getContainerSize(); i++){
            ItemStack stack = player.getInventory().getItem(i);

            if(!stack.isEmpty()) {
                String itemRegistryName = stack.getItem().toString();

                if(!dataManager.hasItem(itemRegistryName)){

                    if(dataManager.addDiscoveredItem(stack.getItem())){
                        alertDiscover(stack);
                        //playClientSound();
                    }
                }

            }
        }

    }

    private static void alertDiscover(ItemStack stack){
        Player player = Minecraft.getInstance().player;
        assert player != null;
        player.sendOverlayMessage(Component.literal("Discovered: " + stack.getItemName().getString()).withColor(TextColor.GREEN));
    }

    private static void checkStats(StatsCounter stats) {
        if(stats != null){
            for (ItemStack stack : allSurvivalStacks) {
                if (dataManager.hasItem(stack.getItemName().toString())) continue;

                Item item = stack.getItem();

                boolean hasInteracted = stats.getValue(Stats.ITEM_PICKED_UP.get(item)) > 0 ||
                        stats.getValue(Stats.ITEM_CRAFTED.get(item)) > 0 ||
                        stats.getValue(Stats.ITEM_USED.get(item)) > 0 ||
                        stats.getValue(Stats.ITEM_BROKEN.get(item)) > 0;

                if (hasInteracted) {
                    dataManager.addDiscoveredItem(item);
                }
            }
            statsChecked = true;

        }
    }
}