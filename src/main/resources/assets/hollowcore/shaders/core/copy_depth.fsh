#version 330

uniform sampler2D colorTexture;
uniform sampler2D depthTexture;
in vec2 texCoords;

layout(location = 0) out vec4 fragColor;

void main() {
    float depth = texture(depthTexture, texCoords).r;
    gl_FragDepth = depth;
    fragColor = texture(colorTexture, texCoords);
}