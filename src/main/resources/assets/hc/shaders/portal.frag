#version 120

uniform int time;
uniform vec2 TextureSize;
uniform sampler2D TextureChannel;

void main() {
    // distance from center of image, used to adjust blur
    vec2 uv = vec2(gl_TexCoord[0]);
    float d = length(uv - vec2(0.5,0.5));

    // blur amount
    float blur = 0.0;
    blur = (1.0 + sin(time*6.0)) * 0.5;
    blur *= 1.0 + sin(time*16.0) * 0.5;
    blur = pow(blur, 3.0);
    blur *= 0.05;
    // reduce blur towards center
    blur *= d;

    // final color
    vec3 col;
    col.r = texture2D( TextureChannel, vec2(uv.x+blur,uv.y) ).r;
    col.g = texture2D( TextureChannel, uv ).g;
    col.b = texture2D( TextureChannel, vec2(uv.x-blur,uv.y) ).b;

    // scanline
    float scanline = sin(uv.y*800.0)*0.04;
    col -= scanline;

    // vignette
    col *= 1.0 - d * 0.5;

    gl_FragColor = vec4(col,1.0);
}