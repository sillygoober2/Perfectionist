package silly.perfectionist;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ServerboundClientCommandPacket;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;
import org.lwjgl.glfw.GLFW;

import static silly.perfectionist.Constants.dataManager;

@Mod(value = Constants.MOD_ID, dist = Dist.CLIENT)
public class NMain {
    public NMain(ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);

        container.getEventBus().addListener(this::registerBindings);

        NeoForge.EVENT_BUS.addListener(this::onJoin);
        NeoForge.EVENT_BUS.addListener(this::onClientTick);
    }

    public void registerBindings(RegisterKeyMappingsEvent event) {
        Constants.openKey = new KeyMapping(
                "key.perfectionist.open_menu",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_Y,
                CommonClass.registerCategory()
        );

        event.register(Constants.openKey);
    }

    private void onJoin(ClientPlayerNetworkEvent.LoggingIn event){
        if(dataManager.allSurvivalStacks.isEmpty()){
            dataManager.getPossibleItems();
        }
        dataManager.updateCurrentWorld();

        ClientPacketListener connection = event.getPlayer().connection;

        if(connection != null){
            connection.send(new ServerboundClientCommandPacket(ServerboundClientCommandPacket.Action.REQUEST_STATS));
        }
    }

    private void onClientTick(ClientTickEvent.Post event){
        CommonClass.onTick();
    }

}
