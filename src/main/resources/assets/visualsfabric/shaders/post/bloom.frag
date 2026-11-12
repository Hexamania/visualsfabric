// Bloom fragment shader - separable Gaussian blur
#version 330 core

uniform sampler2D Texture;
uniform vec2 Resolution;
uniform float Intensity;
uniform float Threshold;
uniform bool Horizontal;

in vec2 TexCoord;
out vec4 FragColor;

const float WEIGHTS[5] = float[](0.227027, 0.1945946, 0.1216216, 0.054054, 0.016216);

vec3 getBloom(vec2 uv) {
    vec3 color = texture(Texture, uv).rgb;
    // Extract bright areas
    float brightness = dot(color, vec3(0.2126, 0.7152, 0.0722));
    if (brightness < Threshold) return vec3(0.0);
    return color * pow((brightness - Threshold) / (1.0 - Threshold), 2.0);
}

void main() {
    vec2 texelSize = 1.0 / Resolution;
    vec3 result = getBloom(TexCoord) * WEIGHTS[0];

    if (Horizontal) {
        for (int i = 1; i < 5; i++) {
            result += getBloom(TexCoord + vec2(texelSize.x * float(i), 0.0)) * WEIGHTS[i];
            result += getBloom(TexCoord - vec2(texelSize.x * float(i), 0.0)) * WEIGHTS[i];
        }
    } else {
        for (int i = 1; i < 5; i++) {
            result += getBloom(TexCoord + vec2(0.0, texelSize.y * float(i))) * WEIGHTS[i];
            result += getBloom(TexCoord - vec2(0.0, texelSize.y * float(i))) * WEIGHTS[i];
        }
    }

    FragColor = vec4(result * Intensity, 1.0);
}