package zigliix.vulkan.shaders;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Environment(EnvType.CLIENT)
public class VulkanShadersClient implements ClientModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger(VulkanShaders.MOD_ID + "-client");

    @Override
    public void onInitializeClient() {
        // Chargement du module client
        LOGGER.info("Vulkan Shaders Addon initialized (Client).");
        // C'est ici que tu pourrais enregistrer tes propres pipelines Vulkan,
        // si VulkanMod fournit une API publique (actuellement, on injectera).
    }
}