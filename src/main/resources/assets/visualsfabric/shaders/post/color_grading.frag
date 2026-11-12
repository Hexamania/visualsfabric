// Color Grading fragment shader - ACES tone mapping + ASC CDL
#version 330 core

uniform sampler2D Texture;
uniform float Saturation;
uniform float Contrast;
uniform float Brightness;
uniform float Gamma;

in vec2 TexCoord;
out vec4 FragColor;

// ACES tone mapping
vec3 acesToneMapping(vec3 x) {
    const float a = 2.51;
    const float b = 0.03;
    const float c = 2.43;
    const float d = 0.59;
    const float e = 0.14;
    return clamp((x * (a * x + b)) / (x * (c * x + d) + e), 0.0, 1.0);
}

// ASC CDL (Color Decision List) - Slope, Offset, Power
vec3 applyCDL(vec3 color, float slope, float offset, float power) {
    return pow(color * slope + offset, vec3(power));
}

void main() {
    vec4 color = texture(Texture, TexCoord);
    vec3 c = color.rgb;

    // Brightness
    c += Brightness - 1.0;

    // Contrast
    c = (c - 0.5) * Contrast + 0.5;

    // Saturation
    float luminance = dot(c, vec3(0.2126, 0.7152, 0.0722));
    c = mix(vec3(luminance), c, Saturation);

    // ASC CDL style grading (simplified)
    c = applyCDL(c, Contrast, Brightness - 1.0, Gamma);

    // ACES tone mapping for HDR-like look
    c = acesToneMapping(c);

    // Gamma correction
    c = pow(c, vec3(1.0 / Gamma));

    FragColor = vec4(c, color.a);
}