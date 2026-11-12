// Chromatic Aberration fragment shader
#version 330 core

uniform sampler2D Texture;
uniform float Intensity;

in vec2 TexCoord;
out vec4 FragColor;

void main() {
    vec2 offset = Intensity * vec2(1.0, 0.0);

    float r = texture(Texture, TexCoord + offset).r;
    float g = texture(Texture, TexCoord).g;
    float b = texture(Texture, TexCoord - offset).b;

    FragColor = vec4(r, g, b, 1.0);
}