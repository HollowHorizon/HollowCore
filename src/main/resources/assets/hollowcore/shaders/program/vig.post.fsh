#version 150

uniform sampler2D DiffuseSampler;

in vec2 texCoord;
in vec2 oneTexel;

uniform vec2 InSize;

out vec4 fragColor;

void main() {
    vec2 uv0 = texCoord;
    vec2 uv = uv0 * (1.0 - uv0.yx);
    float vig = uv.x*uv.y * 15.0;
    vig = pow(vig, 0.25);
    fragColor = texture(DiffuseSampler, uv0) * vec4(vig);
}
