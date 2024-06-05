#version 150

uniform sampler2D DiffuseSampler;

in vec2 texCoord;
in vec2 oneTexel;

uniform vec2 InSize;

out vec4 fragColor;

vec3 Sepia(vec3 color) {
    vec3 outputColor;
    outputColor.r = (color.r * 0.393) + (color.g * 0.769) + (color.b * 0.189);
    outputColor.g = (color.r * 0.349) + (color.g * 0.686) + (color.b * 0.168);
    outputColor.b = (color.r * 0.272) + (color.g * 0.534) + (color.b * 0.131);
    return outputColor;
}

void main() {
    vec3 col = texture(DiffuseSampler, texCoord).rgb;
    col = Sepia(col);
    fragColor = vec4(col, 1.0);
}
