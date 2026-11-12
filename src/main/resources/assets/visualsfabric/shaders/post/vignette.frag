// Vignette fragment shader
#version 330 core

uniform sampler2D Texture;
uniform float Intensity;

in vec2 TexCoord;
out vec4 FragColor;

void main() {
    vec4 color = texture(Texture, TexCoord);

    // Calculate distance from center (0.0 to sqrt(2) ~ 1.414)
    vec2 center = vec2(0.5, 0.5);
    float dist = distance(TexCoord, center) * 1.414;

    // Smooth vignette falloff
    float vignette = smoothstep(0.3, 1.0, dist);
    vignette = pow(vignette, 2.0);

    // Apply vignette
    color.rgb = mix(color.rgb, color.rgb * (1.0 - Intensity), vignette);

    FragColor = color;
}