package zigliix.vulkan.shaders;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.minecraft.util.Identifier;

public class VulkanShaders implements ModInitializer {
    public static final String MOD_ID = "vulkan-shaders";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        // Initialisation côté serveur / commune (rare pour des shaders)
        LOGGER.info("Vulkan Shaders Addon initialized (Common).");
    }
}