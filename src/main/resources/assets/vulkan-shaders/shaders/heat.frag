#version 450

// Coordonnées de texture interpolées depuis le Vertex Shader
layout(location = 0) in vec2 inTexCoord;

// Couleur finale à écrire dans le Framebuffer
layout(location = 0) out vec4 outColor;

// Vulkan Descriptor Set Binding 0 : La texture principale (ex: un bloc de Netherrack ou l'écran)
// VulkanMod utilise par défaut le binding 0 pour la texture de base du chunk / de l'entité
layout(binding = 0) uniform sampler2D baseSampler;

// Push Constants contenant la matrice et le temps pour animer la chaleur
// Doit correspondre exactement à la définition du Vertex Shader
layout(push_constant) uniform PushConstants {
    layout(offset = 0) mat4 mvp;
    layout(offset = 64) float time; // Notre variable personnalisée injectée via le Mixin
} pc;

void main() {
    // Fréquence (taille des vagues) et Vitesse (rapidité de l'animation)
    float frequency = 50.0;
    float speed = 3.0;
    float intensity = 0.005;

    // Calcul de la distorsion de chaleur avec des ondes sinusoïdales basées sur l'UV et le temps
    vec2 offset = vec2(
        sin(inTexCoord.y * frequency + pc.time * speed) * intensity,
        cos(inTexCoord.x * frequency + pc.time * speed * 1.5) * intensity
    );

    // Applique l'offset pour tordre l'image et créer l'effet "mirage" de la chaleur
    vec2 distortedUV = inTexCoord + offset;

    // Échantillonne la texture déformée
    vec4 texColor = texture(baseSampler, distortedUV);

    // Optionnel : Légère teinte orangée/chaude sur les pixels affectés
    vec3 heatTint = vec3(1.0, 0.9, 0.8);
    outColor = vec4(texColor.rgb * heatTint, texColor.a);
}