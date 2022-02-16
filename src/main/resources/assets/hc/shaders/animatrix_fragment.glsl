#version 420

layout(binding = 0) uniform sampler2D textureSampler;
layout(binding = 1) uniform sampler2D overlaySampler;
layout(binding = 2) uniform sampler2D lightmapSampler;

uniform vec2 lightMapTextureCoords;
uniform vec2 overlayTextureCoords;

in vec2 pass_textureCoords;

out vec4 out_fragmentColor;

void main() {
    vec4 textureColor = texture2D(textureSampler, pass_textureCoords);
    vec4 overlayColor = texture(overlaySampler, overlayTextureCoords);
    vec4 lightMapColor = texture(lightmapSampler, lightMapTextureCoords);
    vec3 combinedColor = vec3(1,1,1);

    vec3 litColor = combinedColor * vec3(lightMapColor) * vec3(textureColor);
    vec3 overlayedColor = vec3(overlayColor) * litColor;

    out_fragmentColor = vec4(overlayedColor, textureColor.w);
}