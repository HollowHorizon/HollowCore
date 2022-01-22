#version 120

varying vec2 texcoord;
uniform sampler2D bgl_RenderedTexture;
uniform int time; // Passed in, see ShaderHelper.java

uniform float grainIntensity; // Passed in via Callback

float rand(vec2 co) {
    return fract(sin(dot(co.xy, vec2(12.9898,78.233))) * 43758.5453);
}

void main() {
    vec4 color = texture2D(bgl_RenderedTexture, texcoord);

    gl_FragColor = vec4(color.r, color.g, color.b, color.a);
}