#version 330

layout (location=0) in vec3 pos;
layout (location=1) in vec2 textureCoords;

uniform sampler2D depthTexture;
uniform sampler2D colorTexture;
out vec2 texCoords;

void main() {
    texCoords = textureCoords;
    gl_Position = vec4(pos, 1.0);
}