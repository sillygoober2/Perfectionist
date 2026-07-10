package silly.perfectionist;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ScreenEvent;

@EventBusSubscriber(modid = Constants.MOD_ID, value = Dist.CLIENT)
public class NInventoryBook {

    private static final Identifier buttonTexture =
            Identifier.fromNamespaceAndPath(Constants.MOD_ID, "button");
    private static final Identifier highlightedTexture =
            Identifier.fromNamespaceAndPath(Constants.MOD_ID, "button_highlighted");

    private static final WidgetSprites bookSprite = new WidgetSprites(buttonTexture, highlightedTexture);

    private static ImageButton bookButton;

    @SubscribeEvent
    public static void onScreenInit(ScreenEvent.Init.Post event){
        if(event.getScreen() instanceof InventoryScreen inventory){
            int height = inventory.height / 2 - 22;

            bookButton = new ImageButton(
                    inventory.getGuiLeft() + Constants.bookOffset, height,
                    20, 18,
                    bookSprite,
                    button -> {
                        Minecraft mc = Minecraft.getInstance();
                        CollectionDataManager dataManager = Constants.dataManager;
                        mc.setScreenAndShow(new CommonScreen(dataManager));
                    }
            );
            event.addListener(bookButton);

        }
    }

    @SubscribeEvent
    public static void onScreenRenderPre(ScreenEvent.Render.Pre event) {
        if (bookButton == null) return;

        Screen screen = event.getScreen();

        if (screen instanceof InventoryScreen inventory) {
            int xPos = inventory.getGuiLeft() + Constants.bookOffset;

            bookButton.setX(xPos);
        }
    }
}
