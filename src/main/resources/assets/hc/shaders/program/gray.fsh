#version 150

uniform sampler2D DiffuseSampler;

in vec2 texCoord;
in vec2 oneTexel;

uniform vec2 InSize;

out vec4 fragColor;

vec3 SingleColorChannelGrayScale(vec3 color) {
    return vec3(color.r);
}

void main() {
    vec3 col = texture(DiffuseSampler, texCoord).rgb;
    col = SingleColorChannelGrayScale(col);
    fragColor = vec4(col, 1.0);
}
