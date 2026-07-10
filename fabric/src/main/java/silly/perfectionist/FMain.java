package silly.perfectionist;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.protocol.game.ServerboundClientCommandPacket;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

public class FMain implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        Constants.openKey = new KeyMapping(
                "key.perfectionist.open_menu",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_Y,
                CommonClass.registerCategory()
        );

        KeyMappingHelper.registerKeyMapping(Constants.openKey);

        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            CommonClass.onJoin();

            if(handler != null){
                handler.send(new ServerboundClientCommandPacket(ServerboundClientCommandPacket.Action.REQUEST_STATS));
            }
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            CommonClass.onTick();
        });

        FInventoryBook.register();
    }
}
