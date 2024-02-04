package tomokao.utilitymod;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;

@Environment(EnvType.CLIENT)
public class UtilityUtils {
    public static Minecraft getMinecraft() {
        return ((Minecraft) FabricLoader.getInstance().getGameInstance());
    }
}
