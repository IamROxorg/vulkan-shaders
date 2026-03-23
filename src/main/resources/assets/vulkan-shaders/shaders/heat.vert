#version 450

// Attributs d'entrée du sommet (Vertex Attributes)
layout(location = 0) in vec3 inPosition;
layout(location = 1) in vec2 inTexCoord;

// Sortie vers le Fragment Shader
layout(location = 0) out vec2 outTexCoord;

// Structure des Push Constants attendue par VulkanMod (offset 0)
// VulkanMod pousse généralement la matrice MVP (Model-View-Projection)
// La matrice mat4 occupe 64 octets. Le float 'time' commencera donc à l'offset 64.
layout(push_constant) uniform PushConstants {
    layout(offset = 0) mat4 mvp;   // Offset 0 (64 bytes)
    layout(offset = 64) float time; // Offset 64 (4 bytes) - Utilisé pour l'animation
} pc;

void main() {
    // Calcul classique de la position du sommet dans l'espace projeté
    gl_Position = pc.mvp * vec4(inPosition, 1.0);
    
    // Transmission des coordonnées de texture au Fragment Shader
    outTexCoord = inTexCoord;
}