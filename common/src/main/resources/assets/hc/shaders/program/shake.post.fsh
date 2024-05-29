#version 150

uniform sampler2D DiffuseSampler;

in vec2 texCoord;
in vec2 oneTexel;

uniform vec2 InSize;
uniform float Time;

out vec4 fragColor;

float random (in vec2 st) {
    return fract(sin(dot(st.xy, vec2(12.9898,78.233)))* 43758.5453123);
}

float noise(in vec2 st) {
    vec2 i = floor(st);
    vec2 f = fract(st);
    float a = random(i);
    float b = random(i + vec2(1.0, 0.0));
    float c = random(i + vec2(0.0, 1.0));
    float d = random(i + vec2(1.0, 1.0));
    vec2 u = f * f * (3.0 - 2.0 * f);
    return mix(a, b, u.x) + (c - a)* u.y * (1.0 - u.x) + (d - b) * u.x * u.y;
}

float fbm(in vec2 p) {
    float value = 0.0;
    float freq = 1.13;
    float amp = 0.57;
    for (int i = 0; i < 3; i++) {
        value += amp * (noise((p - vec2(1.0)) * freq));
        freq *= 1.61;
        amp *= 0.47;
    }
    return value;
}

float pat(in vec2 p) {
    float time = Time*0.75;
    vec2 aPos = vec2(sin(time * 0.035), sin(time * 0.05)) * 3.;
    vec2 aScale = vec2(3.25);
    float a = fbm(p * aScale + aPos);
    vec2 bPos = vec2(sin(time * 0.09), sin(time * 0.11)) * 1.2;
    vec2 bScale = vec2(0.75);
    float b = fbm((p + a) * bScale + bPos);
    vec2 cPos = vec2(-0.6, -0.5) + vec2(sin(-time * 0.01), sin(time * 0.1)) * 1.9;
    vec2 cScale = vec2(1.25);
    float c = fbm((p + b) * cScale + cPos);
    return c;
}

vec2 Shake(float maxshake, float mag) {
    float speed = 20.0*mag;
    float shakescale = maxshake * mag;

    float time = Time*speed;

    vec2 p1 = vec2(0.25,0.25);
    vec2 p2 = vec2(0.75,0.75);
    p1 += time;
    p2 += time;

    float val1 = pat(p1);
    float val2 = pat(p2);
    val1 = clamp(val1,0.0,1.0);
    val2 = clamp(val2,0.0,1.0);

    return vec2(val1*shakescale,val2*shakescale);
}

void main() {
    float maxshake = 0.05;
    float mag = 1.5;

    vec2 shakexy = Shake(maxshake,mag);

    vec2 uv = texCoord;
    uv *= 1.0-(maxshake*mag);
    vec3 col = texture(DiffuseSampler, uv + shakexy).xyz;

    fragColor = vec4(col, 1.0);
}
