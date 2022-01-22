#version 120

varying vec2 texcoord;

void main() {
    vec4 vert = gl_Vertex;
	gl_Position = gl_ModelViewProjectionMatrix * vert;
    texcoord = vec2(gl_MultiTexCoord0);
}