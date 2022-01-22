#version 120

const float TWO_PI = 6.28318530718;
const float E = 2.71828182846;
const float Spread = 4.0;
float gaussian(float x, float y)
{
    const float sigmaSqu = Spread * Spread;
    return (1.0f / sqrt(TWO_PI * sigmaSqu)) * pow(E, -((x * x) + (y * y)) / (2.0f * sigmaSqu));
}

void main() {
    gl_Position = gl_ModelViewProjectionMatrix * gl_Vertex;
    gl_TexCoord[0] = gl_MultiTexCoord0;
    gl_FrontColor = gl_Color;
    texcoord = vec2(gl_MultiTexCoord0);
}