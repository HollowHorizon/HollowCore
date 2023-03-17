#version 420

layout (location=0) in vec3 position;
layout (location=1) in vec2 texCoord;
layout (location=2) in vec3 vertexNormal;

out vec2 outTexCoord;
out vec3 vertex;
flat out vec3 normal;

uniform mat4 model_view;
uniform mat4 proj_mat;

void main()
{
    gl_Position = proj_mat * model_view *  vec4(position, 1.0);
    outTexCoord = texCoord;
    vertex = vec3(model_view * vec4(position, 1.0));
    normal = vec3(model_view * vec4(vertexNormal, 0.0));
}