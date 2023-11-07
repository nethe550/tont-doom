#version 330

in vec4 outViewPosition;
in vec4 outWorldPosition;
in vec3 outNormal;
in vec3 outTangent;
in vec3 outBitangent;
in vec2 outTexCoord;

layout (location=0) out vec4 bufAlbedo;
layout (location=1) out vec4 bufNormal;
layout (location=2) out vec4 bufSpecular;

out vec4 fragColor;

struct Material
{
    vec4 ambient;
    vec4 diffuse;
    vec4 specular;
    float glossiness;
    int hasNormalMap;
};

uniform sampler2D texSampler;
uniform sampler2D normalTexSampler;
uniform Material material;
uniform int billboard;

vec3 calcNormal(vec3 normal, vec3 tangent, vec3 bitangent, vec2 texCoords)
{
    mat3 TBN = mat3(tangent, bitangent, normal);
    vec3 n = texture(normalTexSampler, texCoords).rgb;
    n = normalize(n * 2.0 - 1.0);
    n = normalize(TBN * n);
    return n;
}

void main()
{
    vec4 texColor = texture(texSampler, outTexCoord);
    vec4 diffuse = texColor + material.diffuse;
    if (billboard == 1 ? diffuse.a < 0.1 : billboard == 0 ? diffuse.a < 0.5 : false) discard;
    vec4 specular = texColor + material.specular;

    vec3 normal = outNormal;
    if (material.hasNormalMap > 0) normal = calcNormal(outNormal, outTangent, outBitangent, outTexCoord);

    bufAlbedo = vec4(diffuse.rgb, material.glossiness);
    bufNormal = vec4(normal * 0.5 + 0.5, 1.0);
    bufSpecular = specular;
}