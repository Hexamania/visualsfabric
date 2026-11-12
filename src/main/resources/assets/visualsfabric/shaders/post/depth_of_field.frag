// Depth of Field fragment shader
#version 330 core

uniform sampler2D Texture;
uniform sampler2D DepthTexture;
uniform float FocusDistance;
uniform float FocusRange;
uniform float Aperture;

in vec2 TexCoord;
out vec4 FragColor;

float linearizeDepth(float depth) {
    // Linearize depth from projection matrix
    // This is a simplified version
    return depth;
}

float coc(float depth) {
    // Circle of Confusion calculation
    float depthDiff = abs(depth - FocusDistance);
    return clamp((depthDiff - FocusRange) * Aperture, 0.0, 1.0);
}

void main() {
    vec4 color = texture(Texture, TexCoord);
    float depth = texture(DepthTexture, TexCoord).r;
    depth = linearizeDepth(depth);

    float blurAmount = coc(depth);

    if (blurAmount > 0.001) {
        // Simple blur approximation - in practice would use separable blur
        vec2 texelSize = 1.0 / textureSize(Texture, 0);
        vec3 sum = color.rgb * 0.2;
        sum += texture(Texture, TexCoord + vec2(texelSize.x, 0.0)).rgb * 0.1;
        sum += texture(Texture, TexCoord - vec2(texelSize.x, 0.0)).rgb * 0.1;
        sum += texture(Texture, TexCoord + vec2(0.0, texelSize.y)).rgb * 0.1;
        sum += texture(Texture, TexCoord - vec2(0.0, texelSize.y)).rgb * 0.1;
        sum += texture(Texture, TexCoord + vec2(texelSize.x, texelSize.y)).rgb * 0.05;
        sum += texture(Texture, TexCoord - vec2(texelSize.x, texelSize.y)).rgb * 0.05;
        sum += texture(Texture, TexCoord + vec2(-texelSize.x, texelSize.y)).rgb * 0.05;
        sum += texture(Texture, TexCoord - vec2(-texelSize.x, texelSize.y)).rgb * 0.05;

        color.rgb = mix(color.rgb, sum, blurAmount);
    }

    FragColor = color;
}