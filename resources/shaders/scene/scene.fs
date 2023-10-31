#version 330

const int MAX_POINT_LIGHTS = 16;
const int MAX_SPOT_LIGHTS = 16;
const float SPECULAR_POWER = 10;

in vec3 outViewPosition;
in vec4 outWorldPosition;
in vec3 outNormal;
in vec3 outTangent;
in vec3 outBitangent;
in vec2 outTexCoord;

out vec4 fragColor;

struct Attenuation
{
    float constant;
    float linear;
    float exponent;
};
struct Material
{
    vec4 ambient;
    vec4 diffuse;
    vec4 specular;
    float glossiness;
    int hasNormalMap;
};
struct AmbientLight
{
    float factor;
    vec3 color;
};
struct PointLight {
    vec3 position;
    vec3 color;
    float intensity;
    Attenuation attenuation;
};
struct SpotLight {
    vec3 position;
    vec3 direction;
    vec3 color;
    float intensity;
    Attenuation attenuation;
    float innerCutoff;
    float outerCutoff;
};
struct DirectionalLight
{
    vec3 color;
    vec3 direction;
    float intensity;
};
struct Fog
{
    int fogActive;
    vec3 color;
    float density;
};

uniform sampler2D texSampler;
uniform sampler2D normalTexSampler;
uniform float timeElapsed;
uniform vec2 resolution;

uniform Material material;
uniform AmbientLight ambientLight;
uniform PointLight pointLights[MAX_POINT_LIGHTS];
uniform SpotLight spotLights[MAX_SPOT_LIGHTS];
uniform DirectionalLight directionalLight;
uniform Fog fog;

vec4 calcAmbient(AmbientLight ambientLight, vec4 ambient)
{
    return vec4(ambientLight.factor * ambientLight.color, 1.0) * ambient;
}

vec4 calcLighting(vec4 materialDiffuse, vec4 materialSpecular, vec3 lightColor, float lightIntensity, vec3 toLightDir, vec3 fragPosition, vec3 fragNormal)
{
    vec4 diffuseColor = vec4(vec3(0.0), 1.0);
    vec4 specularColor = vec4(vec3(0.0), 1.0);

    // diffuse
    float diffuseFactor = max(dot(fragNormal, toLightDir), 0.0);
    diffuseColor = materialDiffuse * vec4(lightColor, 1.0) * lightIntensity * diffuseFactor;

    // specular
    vec3 viewDirection = normalize(-fragPosition);
    vec3 reflectedLightDirection = reflect(-toLightDir, fragNormal);
    float specularFactor = max(dot(viewDirection, reflectedLightDirection), 0.0);
    specularFactor = pow(specularFactor, SPECULAR_POWER);
    specularColor = materialSpecular * lightIntensity * specularFactor * material.glossiness * vec4(lightColor, 1.0);

    return diffuseColor + specularColor;
}

vec4 calcPointLight(vec4 materialDiffuse, vec4 materialSpecular, PointLight light, vec3 fragPosition, vec3 fragNormal)
{
    vec3 toLight = light.position - fragPosition;
    vec4 lighting = calcLighting(materialDiffuse, materialSpecular, light.color, light.intensity, normalize(toLight), fragPosition, fragNormal);

    float distance = length(toLight);
    float attenuation = 1.0 / (light.attenuation.constant + light.attenuation.linear * distance + light.attenuation.exponent * (distance * distance));

    return lighting * attenuation;
}

vec4 calcSpotLight(vec4 materialDiffuse, vec4 materialSpecular, SpotLight light, vec3 fragPosition, vec3 fragNormal)
{
    vec3 toLight = light.position - fragPosition;
    float theta = dot(normalize(toLight), -light.direction);
    float intensity = 0.0;

    float inner = min(light.innerCutoff, light.outerCutoff);
    float outer = max(light.innerCutoff, light.outerCutoff);
    float cosInnerCutoff = cos(radians(inner));
    float cosOuterCutoff = cos(radians(outer));

    intensity = smoothstep(cosOuterCutoff, cosInnerCutoff, theta);

    float distance = length(toLight);
    float attenuation = 1.0 / (light.attenuation.constant + light.attenuation.linear * distance + light.attenuation.exponent * (distance * distance));

    vec4 lighting = calcLighting(materialDiffuse, materialSpecular, light.color, intensity * light.intensity, normalize(toLight), fragPosition, fragNormal);
    return lighting * attenuation;
}

vec4 calcDirectionalLight(vec4 materialDiffuse, vec4 materialSpecular, DirectionalLight light, vec3 fragPosition, vec3 fragNormal)
{
    return calcLighting(materialDiffuse, materialSpecular, light.color, light.intensity, normalize(light.direction), fragPosition, fragNormal);
}

vec4 calcFog(vec3 fragPosition, vec4 color, Fog fog, vec3 ambientLight, DirectionalLight directionalLight)
{
    vec3 fogColor = fog.color * (ambientLight + directionalLight.color * directionalLight.intensity);
    float distance = length(fragPosition);
    float fogFactor = 1.0 / exp((distance * fog.density) * (distance * fog.density));
    fogFactor = clamp(fogFactor, 0.0, 1.0);

    vec3 resultColor = mix(fogColor, color.rgb, fogFactor);
    return vec4(resultColor, color.w);
}

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
    vec4 ambient = calcAmbient(ambientLight, texColor + material.ambient);
    vec4 diffuse = texColor + material.diffuse;
    vec4 specular = texColor + material.specular;

    vec3 normal = outNormal;
    if (material.hasNormalMap > 0) normal = calcNormal(outNormal, outTangent, outBitangent, outTexCoord);

    vec4 diffuseSpecularComp = calcDirectionalLight(diffuse, specular, directionalLight, outViewPosition, normal);

    for (int i = 0; i < MAX_POINT_LIGHTS; i++) {
        if (pointLights[i].intensity > 0) {
            diffuseSpecularComp += calcPointLight(diffuse, specular, pointLights[i], outViewPosition, normal);
        }
    }

    for (int i = 0; i < MAX_SPOT_LIGHTS; i++) {
        if (spotLights[i].intensity > 0) {
            diffuseSpecularComp += calcSpotLight(diffuse, specular, spotLights[i], outViewPosition, normal);
        }
    }

    fragColor = ambient + diffuseSpecularComp;
    if (fog.fogActive == 1) fragColor = calcFog(outViewPosition, fragColor, fog, ambientLight.color, directionalLight);
}