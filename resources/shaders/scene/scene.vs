#version 330

const int MAX_WEIGHTS = 4;
const int MAX_BONES = 128;

layout (location=0) in vec3 position;
layout (location=1) in vec3 normal;
layout (location=2) in vec3 tangent;
layout (location=3) in vec3 bitangent;
layout (location=4) in vec2 texcoord;
layout (location=5) in vec4 boneWeights;
layout (location=6) in ivec4 boneIndices;

out vec3 outViewPosition;
out vec4 outWorldPosition;
out vec3 outNormal;
out vec3 outTangent;
out vec3 outBitangent;
out vec2 outTexCoord;

uniform mat4 projectionMatrix;
uniform mat4 viewMatrix;
uniform mat4 modelMatrix;
uniform mat4 boneMatrices[MAX_BONES];

void main()
{
    vec4 initPos = vec4(0.0, 0.0, 0.0, 0.0);
    vec4 initNormal = vec4(0.0, 0.0, 0.0, 0.0);
    vec4 initTangent = vec4(0.0, 0.0, 0.0, 0.0);
    vec4 initBitangent = vec4(0.0, 0.0, 0.0, 0.0);

    int count = 0;
    for (int i = 0; i < MAX_WEIGHTS; i++) {
        float weight = boneWeights[i];
        if (weight > 0) {
            count++;
            int boneIndex = boneIndices[i];
            vec4 tmpPos = boneMatrices[boneIndex] * vec4(position, 1.0);
            initPos += weight * tmpPos;

            vec4 tmpNormal = boneMatrices[boneIndex] * vec4(normal, 0.0);
            initNormal += weight * tmpNormal;

            vec4 tmpTangent = boneMatrices[boneIndex] * vec4(tangent, 0.0);
            initTangent += weight * tmpTangent;

            vec4 tmpBitangent = boneMatrices[boneIndex] * vec4(bitangent, 0.0);
            initBitangent += weight * tmpBitangent;
        }
    }

    if (count == 0) {
        initPos = vec4(position, 1.0);
        initNormal = vec4(normal, 0.0);
        initTangent = vec4(tangent, 0.0);
        initBitangent = vec4(bitangent, 0.0);
    }

    mat4 modelViewMatrix = viewMatrix * modelMatrix;
    vec4 mvPosition = modelViewMatrix * initPos;

    gl_Position = projectionMatrix * mvPosition;

    outViewPosition = mvPosition.xyz;
    outWorldPosition = modelMatrix * initPos;
    outNormal = normalize(modelViewMatrix * initNormal).xyz;
    outTangent = normalize(modelViewMatrix * initTangent).xyz;
    outBitangent = normalize(modelViewMatrix * initBitangent).xyz;
    outTexCoord = texcoord;
}