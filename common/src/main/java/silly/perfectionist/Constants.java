package silly.perfectionist;

import com.mojang.logging.LogUtils;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Constants {

    public static CollectionDataManager dataManager = new CollectionDataManager();
    public static final Logger LOGGER = LogUtils.getLogger();
    public static final String MOD_ID = "perfectionist";
    public static final String MOD_NAME = "Perfectionist";
    public static final Logger LOG = LoggerFactory.getLogger(MOD_NAME);

    public static KeyMapping openKey;

    public static final int bookOffset = 150;

    public static int currentPage = 0;
    public static int itemsPerPage = 1;
    public static final int startY = 20;
    public static final int slotSize = 32;

}