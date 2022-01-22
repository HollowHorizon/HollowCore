#version 120

uniform sampler2D DiffuseSampler;
uniform float intensity;
uniform int time;
uniform int windowX;
uniform int windowY;

float distsq(vec2 a, vec2 b) {
    float f = float(windowY)/float(windowX);
    float dx = (a.x-b.x);
    float dy = (a.y-b.y)*f;
    return dx*dx+dy*dy;
}

float roundToNearest(float val, float base) {
    return ceil(val/base)*base;
}

float getDistance(vec3 diff) {
    diff = abs(diff);
    return sqrt(diff.x*diff.x+diff.y*diff.y+diff.z*diff.z);
}

float getDistanceXZ(vec3 diff) {
    diff = abs(diff);
    return sqrt(diff.x*diff.x+diff.z*diff.z);
}
vec3 mod289(vec3 x) {
    return x - floor(x * (1.0 / 289.0)) * 289.0;
}

vec2 mod289(vec2 x) {
    return x - floor(x * (1.0 / 289.0)) * 289.0;
}

vec3 permute(vec3 x) {
    return mod289(((x*34.0)+1.0)*x);
}

float snoise(vec2 v) {
    const vec4 C = vec4(0.211324865405187, // (3.0-sqrt(3.0))/6.0
    0.366025403784439, // 0.5*(sqrt(3.0)-1.0)
    -0.577350269189626, // -1.0 + 2.0 * C.x
    0.024390243902439);// 1.0 / 41.0

    vec2 i  = floor(v + dot(v, C.yy));
    vec2 x0 = v -   i + dot(i, C.xx);

    vec2 i1;

    i1 = (x0.x > x0.y) ? vec2(1.0, 0.0) : vec2(0.0, 1.0);

    vec4 x12 = x0.xyxy + C.xxzz;
    x12.xy -= i1;

    i = mod289(i);
    vec3 p = permute(permute(i.y + vec3(0.0, i1.y, 1.0))
    + i.x + vec3(0.0, i1.x, 1.0));

    vec3 m = max(0.5 - vec3(dot(x0, x0), dot(x12.xy, x12.xy), dot(x12.zw, x12.zw)), 0.0);
    m = m*m;
    m = m*m;


    vec3 x = 2.0 * fract(p * C.www) - 1.0;
    vec3 h = abs(x) - 0.5;
    vec3 ox = floor(x + 0.5);
    vec3 a0 = x - ox;

    m *= 1.79284291400159 - 0.85373472095314 * (a0*a0 + h*h);

    vec3 g;
    g.x  = a0.x  * x0.x  + h.x  * x0.y;
    g.yz = a0.yz * x12.xz + h.yz * x12.yw;
    return 130.0 * dot(m, g);
}

void main() {
    vec2 texcoord = vec2(gl_TexCoord[0]);
    vec4 orig = texture2D(DiffuseSampler, texcoord);

    //float dx = min(texcoord.x, 1.0-texcoord.x)*float(windowX)/float(windowY);
    //float dy = min(texcoord.y, 1.0-texcoord.y)*float(windowY*0.0+1.0);
    //float d = min(dx, dy);
    float dx = (texcoord.x-0.5)*2.0;//*float(windowX)/float(windowY);
    float dy = (texcoord.y-0.5)*2.0;
    float d = 1.0-min(1.0, pow(dx*dx*dx*dx*dx*dx*dx*dx+dy*dy*dy*dy*dy*dy*dy*dy, 0.125));
    float df = 1+0.25*snoise(texcoord*150.0*vec2(float(windowX)/float(windowY), 1.0));
    float f = max(0.0, intensity-(d*0.018*1000.0*0.4+0.125)*df);

    vec2 pix = vec2(texcoord.x, texcoord.y);
    pix.x = roundToNearest(pix.x, 4.0/(float(windowX)));
    pix.y = roundToNearest(pix.y, 4.0/(float(windowY)));

    float t = float(time)+5000.0;
    float tx = float(t)*0.3;
    float ty = float(t)*0.3;
    //tx *= (pix.x-0.5)*0.72;
    //ty *= (pix.y-0.5)*0.72;
    //tx *= -(pix.y-0.5);
    //ty *= -(pix.x-0.5);

    float sx = 2*(0.5-pix.x);
    float sy = 2*(0.5-pix.y);
    tx *= sx;
    ty *= sy;

    /*
    if (texcoord.x > 0.5) {
        tx *= -1;
    }
    if (texcoord.y > 0.5) {
        ty *= -1;
    }*/

    vec2 pix2 = vec2(texcoord.x, texcoord.y);
    float sc = min(8.0, 1.0/max(0.01, intensity));
    pix2.x = roundToNearest(pix2.x, sc/(float(windowX)));
    pix2.y = roundToNearest(pix2.y, sc/(float(windowY)));

    float ns = snoise(pix2*vec2(windowX, windowY)*0.09+vec2(tx, ty)*0.4);
    f *= 0.5+0.5*min(1.0, ns*ns*ns*3.0);
    f = min(max(f, 0.0), 1.0);

    //color
    float r = float(255)/255.0*f;
    float g = float(255)/255.0*f;
    float b = float(255)/255.0*f;

    vec3 net = vec3(min(1.0, r+orig.r), min(1.0, g+orig.g), min(1.0, b+orig.b));

    gl_FragColor = vec4(net.x, net.y, net.z, orig.a);
}