package zigliix.vulkan.shaders.mixin.client;

import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.DeltaTracker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Vrais imports de VulkanMod
import net.vulkanmod.vulkan.Renderer;
import net.vulkanmod.vulkan.shader.GraphicsPipeline;
import net.vulkanmod.vulkan.shader.Pipeline;
import net.vulkanmod.vulkan.shader.SPIRVUtils;
import net.vulkanmod.vulkan.shader.SPIRVUtils.ShaderKind;
import net.vulkanmod.render.vertex.CustomVertexFormat;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK10;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;

import java.nio.ByteBuffer;
import java.util.ArrayList;

/**
 * Mixin sur le GameRenderer de Minecraft (Mappings: Mojmap).
 * Pourquoi ici ?
 * Le `renderLevel` est la méthode principale appelée à chaque frame
 * pour dessiner le monde 3D. C'est l'endroit parfait pour :
 * 1. Injecter nos variables globales pour nos shaders (ex: le temps pour notre effet de chaleur).
 * 2. Dire au moteur de rendu VulkanMod de lier (bind) notre pipeline de shader personnalisé
 *    juste avant ou après certains rendus.
 */
@Mixin(GameRenderer.class)
public class GameRendererMixin {

    private static GraphicsPipeline heatPipeline = null;

    // Utilisation de DeltaTracker en 1.21.1+ au lieu du vieux float tickDelta
    @Inject(
        method = "renderLevel",
        at = @At("HEAD") // On injecte au tout début du rendu de la frame (HEAD)
    )
    private void onRenderLevelStart(DeltaTracker tracker, CallbackInfo ci) {
        // 1. Calcul du temps (en secondes) pour l'animation du shader.
        // On utilise System.currentTimeMillis() pour avoir un écoulement du temps constant.
        float timeInSeconds = (System.currentTimeMillis() % 100000L) / 1000.0f;

        // 2. Interaction avec VulkanMod API (Code 100% réel)
        
        // Initialisation paresseuse (lazy load) du pipeline
        if (heatPipeline == null) {
            try {
                // Lecture des sources GLSL depuis le resource pack
                String vertSource = new String(Minecraft.getInstance().getResourceManager()
                        .getResourceOrThrow(Identifier.fromNamespaceAndPath("vulkan-shaders", "shaders/heat.vert"))
                        .open().readAllBytes());
                
                String fragSource = new String(Minecraft.getInstance().getResourceManager()
                        .getResourceOrThrow(Identifier.fromNamespaceAndPath("vulkan-shaders", "shaders/heat.frag"))
                        .open().readAllBytes());

                // Compilation à la volée en SPIR-V via Shaderc (intégré à VulkanMod)
                SPIRVUtils.SPIRV vertSpirv = SPIRVUtils.compileShader("heat.vert", vertSource, ShaderKind.VERTEX_SHADER);
                SPIRVUtils.SPIRV fragSpirv = SPIRVUtils.compileShader("heat.frag", fragSource, ShaderKind.FRAGMENT_SHADER);

                // Configuration du builder de pipeline VulkanMod
                Pipeline.Builder builder = new Pipeline.Builder(CustomVertexFormat.COMPRESSED_TERRAIN, "heat");
                builder.setVertShaderSPIRV(vertSpirv);
                builder.setFragShaderSPIRV(fragSpirv);
                
                // VulkanMod exige que UBOs et Samplers soient initialisés (même vides)
                builder.setUniforms(new ArrayList<>(), new ArrayList<>());

                // Création du GraphicsPipeline
                heatPipeline = builder.createGraphicsPipeline();

            } catch (Exception e) {
                System.err.println("Erreur lors du chargement du pipeline heat: " + e.getMessage());
                e.printStackTrace();
                return; // On annule l'injection si le pipeline a échoué
            }
        }
        
        // 3. Bind du pipeline et Push du Float "time"
        try (MemoryStack stack = MemoryStack.stackPush()) {
            // Bind notre pipeline personnalisé au Renderer de VulkanMod
            Renderer.getInstance().bindGraphicsPipeline(heatPipeline);

            // Préparation des données PushConstants (1 float = 4 bytes)
            ByteBuffer pushData = stack.malloc(4);
            pushData.putFloat(0, timeInSeconds);

            // Injection manuelle de la variable "time" via les commandes Vulkan
            // VK_SHADER_STAGE_VERTEX_BIT | VK_SHADER_STAGE_FRAGMENT_BIT (Selon où est utilisé le PushConstant)
            VK10.vkCmdPushConstants(
                Renderer.getCommandBuffer(),
                heatPipeline.getLayout(),
                VK10.VK_SHADER_STAGE_VERTEX_BIT | VK10.VK_SHADER_STAGE_FRAGMENT_BIT,
                64, // L'offset défini dans le layout GLSL (ex: layout(offset=64))
                pushData
            );
        }
        
        // Note : Dans un cas réel, VulkanMod va re-binder son propre pipeline de terrain juste après.
        // C'est pourquoi un effet post-process ou un shader spécifique nécessiterait de s'injecter
        // plus profondément dans le rendu (RenderPass de VulkanMod), mais ceci montre la logique Vulkan directe.
    }
}