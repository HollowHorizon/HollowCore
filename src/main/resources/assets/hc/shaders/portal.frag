#version 120

#import inputs

float rand(vec2 n) {
    return fract(sin(dot(n, vec2(12.9898,12.1414))) * 83758.5453);
}

float noise(vec2 n) {
    const vec2 d = vec2(0.0, 1.0);
    vec2 b = floor(n);
    vec2 f = fract(n);
    return mix(mix(rand(b), rand(b + d.yx), f.x), mix(rand(b + d.xy), rand(b + d.yy), f.x), f.y);
}

float fire(vec2 n) {
    return noise(n) + noise(n * 2.1) * .6 + noise(n * 5.4) * .42;
}

////////////////////////////

float square_tunnel(vec2 uv) {
    vec2 p = -1.0 + 2.0 * uv;
    vec2 t = p*p*p*p;
    return max(t.x, t.y);
}

/////////////////////////////

void main() {
    float t = time+0.0;
    vec2 uv = vec2(gl_TexCoord[0]);

    // create a fire texture
    float q = noise(uv + t);
    float fire = fire(uv - (t * .934) - q);

    float square_tunnel = square_tunnel(uv);

    // apply fire everywhere to only select pixels
    float grad = fire * (1. - (square_tunnel * 2. -1.));

    // can't really explain what this is doing
    grad = max(0., (1.5 - grad)/grad);

    // adjust fade
    grad /= (0.5 + grad);

    grad = grad * 1.1;

    // add colour
    vec3 portal = vec3((grad), (grad * grad), grad * grad * grad);

    // add your portal onto whatever
    vec3 bg = texture2D(bgl_RenderedTexture, uv + (q * .1 - .05)).rgb;

    gl_FragColor = vec4(bg + portal, 1.);
}