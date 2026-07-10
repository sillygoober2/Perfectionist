package silly.perfectionist;

import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.resources.Identifier;

import java.lang.reflect.Field;
import java.util.List;

public class FInventoryBook {

    private static final Identifier buttonTexture =
            Identifier.fromNamespaceAndPath(Constants.MOD_ID, "button");
    private static final Identifier highlightedTexture =
            Identifier.fromNamespaceAndPath(Constants.MOD_ID, "button_highlighted");

    private static final WidgetSprites bookSprite = new WidgetSprites(buttonTexture, highlightedTexture);

    private static ImageButton bookButton;

    private static Field leftPosField;

    private static int getLeftPos(Screen screen){
        int currentLeftPos = 0;
        try {
            currentLeftPos = leftPosField.getInt(screen);

        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return currentLeftPos;
    }

    public static void register() {
        try {
            // doing this to get the ACTUAL left position of inventory since fabric doesnt have a library thing for that
            // 1 Neoforge - 0 Fabric
            leftPosField = AbstractContainerScreen.class.getDeclaredField("leftPos");
            leftPosField.setAccessible(true);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }

        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if(screen instanceof InventoryScreen inventory){

                int height = inventory.height / 2 - 22;
                bookButton = new ImageButton(
                        getLeftPos(inventory) + Constants.bookOffset, height,
                        20, 18,
                        bookSprite,
                        button -> {
                            Minecraft mc = Minecraft.getInstance();
                            CollectionDataManager dataManager = Constants.dataManager;
                            mc.setScreenAndShow(new CommonScreen(dataManager));

                        }
                );

                Screens.getWidgets(inventory).add(bookButton);

                ScreenEvents.beforeTick(inventory).register((delta) -> {
                    bookButton.setX(getLeftPos(inventory) + Constants.bookOffset);
                });

            }
        });
    }

}
