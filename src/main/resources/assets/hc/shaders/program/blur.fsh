#version 150

uniform sampler2D DiffuseSampler;

uniform float BlurStrength;
uniform float BlurAccuracy;
uniform float BlurSurexposition;

in vec2 texCoord;

out vec4 fragColor;

void main()
{
    vec4 col = vec4(0);

    for (int x = 0; x < BlurAccuracy; ++x)
    {
        for (int y = 0; y < BlurAccuracy; ++y)
        {
            vec2 uv = texCoord + vec2(x / ( BlurAccuracy - 1) - 0.5, y / ( BlurAccuracy - 1 ) - 0.5) * BlurStrength;
            col += texture(DiffuseSampler, uv);
        }
    }

    float m = BlurAccuracy * BlurAccuracy * (1 - BlurSurexposition);
    fragColor = col / m;
}