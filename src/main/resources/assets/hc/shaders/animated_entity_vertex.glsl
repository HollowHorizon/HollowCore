#version 420

const int MAX_JOINTS = 50;//max joints allowed in a skeleton
const int MAX_WEIGHTS = 3;//max number of joints that can affect a vertex

layout (location=0) in vec3 in_position;
layout (location=1) in vec2 in_textureCoords;
layout (location=2) in vec3 in_normal;
layout (location=3) in ivec3 in_jointIndices;
layout (location=4) in vec3 in_weights;

out vec2 pass_textureCoords;
out vec3 pass_vertex;
flat out vec3 pass_normal;

uniform mat4 modelViewMatrix;
uniform mat4 projectionMatrix;
uniform mat4 jointTransforms[MAX_JOINTS];

void main(void){

	vec4 totalLocalPos = vec4(in_position, 1);
	vec4 totalNormal = vec4(in_normal, 1);

	for(int i=0;i<MAX_WEIGHTS;i++){
		mat4 jointTransform = jointTransforms[in_jointIndices[i]];

		vec4 posePosition = jointTransform * vec4(in_position, 1.0);
		vec4 poseNormal = jointTransform * vec4(in_normal, 1.0);

		totalLocalPos += posePosition * in_weights[i];
		totalNormal += poseNormal * in_weights[i];
	}

	gl_Position = projectionMatrix * modelViewMatrix * totalLocalPos;

	pass_vertex = vec3(modelViewMatrix * totalLocalPos);
	pass_normal = vec3(modelViewMatrix * totalNormal);
	pass_textureCoords = in_textureCoords;
}