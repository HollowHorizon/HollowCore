#version 150

uniform sampler2D DiffuseSampler;

in vec2 texCoord;

uniform vec2 InSize;
uniform float Time;

out vec4 fragColor;

#define antialiasing(n) n/min(InSize.y, InSize.x)
#define S(d, b) smoothstep(antialiasing(3.0), b, d)
#define B(p, s) max(abs(p).x-s.x, abs(p).y-s.y)

float random (vec2 p) {
    return fract(sin(dot(p.xy, vec2(12.9898, 78.233)))* 43758.5453123);
}

void main() {
    vec2 p = gl_FragCoord.xy;
    vec2 prevP = p;

    gl_FragColor = texture(DiffuseSampler, p);

    return;

    vec3 col = texture(DiffuseSampler, p).rgb;

    p*=10.;
    vec2 id = floor(p);
    vec2 gr = fract(p)-0.5;
    float rnd = random(id);
    if (rnd>=0.6){
        float s = sin((10.*Time*rnd)+rnd)*0.75;
        vec2 size = vec2(s, s*rnd);
        if (rnd>=0.8 && rnd<0.9){
            size = vec2((s*rnd)*0.5, s*rnd);
        } else if (rnd>=0.9){
            size = vec2(s*rnd, s);
        }
        float d = B(gr,size);

        vec2 ruv = prevP+vec2(sin(rnd+Time*60.)*0.02, 0.0);
        float r = random(id+vec2(sin(Time*0.5)));
        float g = random(id+20.0+vec2(sin(Time*0.5)));
        float b = random(id+37.0+vec2(sin(Time*0.5)));

        col = mix(col, texture(DiffuseSampler, ruv).rgb*vec3(r, g, b), S(d,0.));
    }

    fragColor = vec4(col, 1.0);
}