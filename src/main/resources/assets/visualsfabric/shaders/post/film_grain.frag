// Film Grain fragment shader with animated noise
#version 330 core

uniform sampler2D Texture;
uniform float Time;
uniform float Intensity;
uniform vec2 Resolution;

in vec2 TexCoord;
out vec4 FragColor;

// Hash function for pseudo-random noise
float hash(vec2 p) {
    return fract(sin(dot(p, vec2(127.1, 311.7))) * 43758.5453);
}

// Smooth noise
float smoothNoise(vec2 p) {
    vec2 i = floor(p);
    vec2 f = fract(p);
    f = f * f * (3.0 - 2.0 * f);

    float a = hash(i);
    float b = hash(i + vec2(1.0, 0.0));
    float c = hash(i + vec2(0.0, 1.0));
    float d = hash(i + vec2(1.0, 1.0));

    return mix(mix(a, b, f.x), mix(c, d, f.x), f.y);
}

// FBM noise for more natural grain
float fbm(vec2 p) {
    float value = 0.0;
    float amplitude = 0.5;
    for (int i = 0; i < 4; i++) {
        value += amplitude * smoothNoise(p);
        p *= 2.0;
        amplitude *= 0.5;
    }
    return value;
}

void main() {
    vec4 color = texture(Texture, TexCoord);

    // Animated noise offset
    vec2 noiseUV = TexCoord * Resolution * 0.5 + Time * 10.0;
    float grain = fbm(noiseUV) - 0.5;

    // Apply grain with intensity
    color.rgb += grain * Intensity * 0.5;

    FragColor = color;
}