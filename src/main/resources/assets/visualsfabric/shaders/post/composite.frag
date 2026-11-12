// Composite shader - combines all post-processing effects in single pass
#version 330 core

uniform sampler2D Texture;
uniform float Time;
uniform vec2 Resolution;

// Effect toggles
uniform bool BloomEnabled;
uniform bool ChromaticAberrationEnabled;
uniform bool FilmGrainEnabled;
uniform bool VignetteEnabled;
uniform bool ColorGradingEnabled;

// Effect parameters
uniform float BloomIntensity;
uniform float BloomThreshold;
uniform float ChromaticAberrationIntensity;
uniform float FilmGrainIntensity;
uniform float VignetteIntensity;
uniform float Saturation;
uniform float Contrast;
uniform float Brightness;
uniform float Gamma;

in vec2 TexCoord;
out vec4 FragColor;

// Hash and noise functions
float hash(vec2 p) {
    return fract(sin(dot(p, vec2(127.1, 311.7))) * 43758.5453);
}

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

// ACES tone mapping
vec3 acesToneMapping(vec3 x) {
    const float a = 2.51;
    const float b = 0.03;
    const float c = 2.43;
    const float d = 0.59;
    const float e = 0.14;
    return clamp((x * (a * x + b)) / (x * (c * x + d) + e), 0.0, 1.0);
}

vec3 applyCDL(vec3 color, float slope, float offset, float power) {
    return pow(color * slope + offset, vec3(power));
}

// Bloom extraction
vec3 extractBloom(vec3 color) {
    float brightness = dot(color, vec3(0.2126, 0.7152, 0.0722));
    if (brightness < BloomThreshold) return vec3(0.0);
    return color * pow((brightness - BloomThreshold) / (1.0 - BloomThreshold), 2.0);
}

void main() {
    vec4 color = texture(Texture, TexCoord);
    vec3 c = color.rgb;

    // 1. Bloom (additive)
    if (BloomEnabled) {
        vec3 bloom = extractBloom(c);
        c += bloom * BloomIntensity;
    }

    // 2. Chromatic Aberration
    if (ChromaticAberrationEnabled) {
        vec2 offset = ChromaticAberrationIntensity * vec2(1.0, 0.0);
        float r = texture(Texture, TexCoord + offset).r;
        float g = texture(Texture, TexCoord).g;
        float b = texture(Texture, TexCoord - offset).b;
        c = vec3(r, g, b);
    }

    // 3. Color Grading
    if (ColorGradingEnabled) {
        // Brightness
        c += Brightness - 1.0;
        // Contrast
        c = (c - 0.5) * Contrast + 0.5;
        // Saturation
        float luminance = dot(c, vec3(0.2126, 0.7152, 0.0722));
        c = mix(vec3(luminance), c, Saturation);
        // CDL
        c = applyCDL(c, Contrast, Brightness - 1.0, Gamma);
        // ACES
        c = acesToneMapping(c);
        // Gamma
        c = pow(c, vec3(1.0 / Gamma));
    }

    // 4. Film Grain
    if (FilmGrainEnabled) {
        vec2 noiseUV = TexCoord * Resolution * 0.5 + Time * 10.0;
        float grain = fbm(noiseUV) - 0.5;
        c += grain * FilmGrainIntensity * 0.5;
    }

    // 5. Vignette
    if (VignetteEnabled) {
        vec2 center = vec2(0.5, 0.5);
        float dist = distance(TexCoord, center) * 1.414;
        float vignette = smoothstep(0.3, 1.0, dist);
        vignette = pow(vignette, 2.0);
        c = mix(c, c * (1.0 - VignetteIntensity), vignette);
    }

    FragColor = vec4(c, color.a);
}