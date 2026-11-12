// Motion Blur fragment shader
#version 330 core

uniform sampler2D Texture;
uniform sampler2D VelocityTexture;
uniform float Intensity;
uniform int Samples;

in vec2 TexCoord;
out vec4 FragColor;

void main() {
    vec4 color = texture(Texture, TexCoord);
    vec2 velocity = texture(VelocityTexture, TexCoord).rg;

    if (length(velocity) < 0.001) {
        FragColor = color;
        return;
    }

    vec3 sum = color.rgb;
    vec2 step = velocity / float(Samples);

    for (int i = 1; i < Samples; i++) {
        vec2 offset = step * float(i);
        sum += texture(Texture, TexCoord + offset).rgb;
        sum += texture(Texture, TexCoord - offset).rgb;
    }

    float count = float(Samples * 2 - 1);
    color.rgb = mix(color.rgb, sum / count, Intensity);

    FragColor = color;
}