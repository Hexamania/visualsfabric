// Full-screen quad vertex shader for post-processing
#version 330 core

in vec2 VertexPosition;
in vec2 VertexUV;

out vec2 TexCoord;

void main() {
    TexCoord = VertexUV;
    gl_Position = vec4(VertexPosition, 0.0, 1.0);
}