#version 420

in  vec2 outTexCoord;
in vec3 vertex;
flat in vec3 normal;
out vec4 fragColor;

layout(binding = 0) uniform sampler2D texture_sampler;
layout(binding = 1) uniform sampler2D overlay_sampler;
layout(binding = 2) uniform sampler2D lightmap_sampler;

uniform vec2 lightmap_uv;
uniform vec2 overlay_uv;
uniform vec3 ambient_light;
uniform vec3 diffuse_colors[2];
uniform vec3 diffuse_locs[2];

float getDiffuseCos(vec3 lightPos, vec3 vert, vec3 norm) {
    vec3 to_light = normalize(lightPos);
    vec3 vertex_normal = normalize(norm);
    float cos_angle = dot(vertex_normal, to_light);
    return clamp(cos_angle, 0.0, 1.0);
}


void main()
{
    vec4 texColor = texture(texture_sampler, outTexCoord);
    vec4 overlay = texture(overlay_sampler, overlay_uv);
    vec4 lightmap = texture(lightmap_sampler, lightmap_uv);
    vec3 combined = vec3(0, 0, 0);
    combined += ambient_light;
    for (int i = 0; i < 2; i++){
        float lightCos = getDiffuseCos(diffuse_locs[i], vertex, normal);
        vec3 diffuse = lightCos * diffuse_colors[i];
        combined += diffuse;
    }
    vec3 lit = combined * vec3(lightmap) * vec3(texColor);
    vec3 overlayed = vec3(overlay) * lit;

    fragColor = vec4(overlayed, texColor.w);
}