#version 330

const int MAX_POINT_LIGHTS = 16;
const int MAX_SPOT_LIGHTS = 16;
const float SPECULAR_POWER = 10.0;

in vec2 outTexCoord;

out vec4 fragColor;

struct Attenuation
{
    float constant;
    float linear;
    float exponent;
};

struct AmbientLight
{
    float intensity;
    vec3 color;
};

struct PointLight
{
    vec3 position;
    vec3 color;
    float intensity;
    Attenuation attenuation;
};

struct SpotLight
{
    PointLight pl;
    vec3 direction;
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
    int activeFog;
    vec3 color;
    float density;
};

uniform sampler2D albedoSampler;
uniform sampler2D normalSampler;
uniform sampler2D specularSampler;
uniform sampler2D depthSampler;

uniform mat4 inverseProjectionMatrix;

uniform AmbientLight ambientLight;
uniform PointLight pointLights[MAX_POINT_LIGHTS];
uniform SpotLight spotLights[MAX_SPOT_LIGHTS];
uniform DirectionalLight directionalLight;
uniform Fog fog;

vec4 calcAmbient(AmbientLight ambientLight, vec4 ambientColor)
{
    return vec4(ambientLight.intensity * ambientLight.color, 1.0) * ambientColor;
}

vec4 calcLightColor(vec4 diffuse, vec4 specular, float glossiness, vec3 lightColor, float lightIntensity, vec3 toLightDirection, vec3 position, vec3 normal)
{
    vec4 diffuseColor = vec4(0.0, 0.0, 0.0, 1.0);
    vec4 specularColor = vec4(0.0, 0.0, 0.0, 1.0);

    float diffuseFactor = max(dot(normal, toLightDirection), 0.0);
    diffuseColor = diffuse * vec4(lightColor, 1.0) * lightIntensity * diffuseFactor;

    vec3 cameraDirection = normalize(-position);
    vec3 fromLightDirection = -toLightDirection;
    vec3 reflected = normalize(reflect(fromLightDirection, normal));
    float specularFactor = max(dot(cameraDirection, reflected), 0.0);
    specularFactor = pow(specularFactor, SPECULAR_POWER);
    specularColor = specular * lightIntensity * specularFactor * glossiness * vec4(lightColor, 1.0);

    return diffuseColor + specularColor;
}

vec4 calcPointLight(vec4 diffuse, vec4 specular, float glossiness, PointLight light, vec3 position, vec3 normal)
{
    vec3 lightDirection = light.position - position;
    vec3 toLightDirection = normalize(lightDirection);
    vec4 lightColor = calcLightColor(diffuse, specular, glossiness, light.color, light.intensity, toLightDirection, position, normal);

    float distance = length(lightDirection);
    float inverseAttenuation = light.attenuation.constant + light.attenuation.linear * distance + light.attenuation.exponent * distance * distance;
    return lightColor / inverseAttenuation;
}

vec4 calcSpotLight(vec4 diffuse, vec4 specular, float glossiness, SpotLight light, vec3 position, vec3 normal)
{
    vec3 toLight = light.pl.position - position;
    float theta = dot(normalize(toLight), -light.direction);
    float intensity = 0.0;

    float inner = min(light.innerCutoff, light.outerCutoff);
    float outer = max(light.innerCutoff, light.outerCutoff);
    float cosInner = cos(radians(inner));
    float cosOuter = cos(radians(outer));

    intensity = smoothstep(cosOuter, cosInner, theta);

    float distance = length(toLight);
    float inverseAttenuation = light.pl.attenuation.constant + light.pl.attenuation.linear * distance + light.pl.attenuation.exponent * distance * distance;

    vec4 lighting = calcLightColor(diffuse, specular, glossiness, light.pl.color, intensity * light.pl.intensity, normalize(toLight), position, normal);
    return lighting / inverseAttenuation;
}

vec4 calcDirectionalLight(vec4 diffuse, vec4 specular, float glossiness, DirectionalLight light, vec3 position, vec3 normal)
{
    return calcLightColor(diffuse, specular, glossiness, light.color, light.intensity, normalize(light.direction), position, normal);
}

vec4 calcFog(vec3 position, vec4 color, Fog fog, vec3 ambientLight, DirectionalLight directionalLight)
{
    vec3 fogColor = fog.color * (ambientLight + directionalLight.color * directionalLight.intensity);
    float dist = length(position);
    float fogFactor = 1.0 / exp((dist * fog.density) * (dist * fog.density));
    fogFactor = clamp(fogFactor, 0.0, 1.0);

    vec3 resultColor = mix(fogColor, color.rgb, fogFactor);
    return vec4(resultColor, color.w);
}

vec3 calcNormal(vec3 normal, vec3 tangent, vec3 bitangent, vec2 texCoords)
{
    mat3 TBN = mat3(tangent, bitangent, normal);
    vec3 n = texture(normalSampler, texCoords).rgb;
    n = normalize(n * 2.0 - 1.0);
    n = normalize(TBN * n);
    return n;
}

void main()
{
    vec4 albedoSamplerValue = texture(albedoSampler, outTexCoord);
    vec3 albedo = albedoSamplerValue.rgb;
    vec4 diffuse = vec4(albedo, 1.0);

    float glossiness = albedoSamplerValue.a;
    vec3 normal = normalize(texture(normalSampler, outTexCoord).rgb * 2.0 - 1.0);
    vec4 specular = texture(specularSampler, outTexCoord);

    float depth = texture(depthSampler, outTexCoord).x * 2.0 - 1.0;
    if (depth == 1.0) discard;

    vec4 clip = vec4(outTexCoord.x * 2.0 - 1.0, outTexCoord.y * 2.0 - 1.0, depth, 1.0);
    vec4 viewW = inverseProjectionMatrix * clip;
    vec3 viewPosition = viewW.xyz / viewW.w;

    vec4 diffuseSpecularComp = calcDirectionalLight(diffuse, specular, glossiness, directionalLight, viewPosition, normal);

    for (int i=0; i<MAX_POINT_LIGHTS; i++) {
        if (pointLights[i].intensity > 0) {
            diffuseSpecularComp += calcPointLight(diffuse, specular, glossiness, pointLights[i], viewPosition, normal);
        }
    }

    for (int i=0; i<MAX_SPOT_LIGHTS; i++) {
        if (spotLights[i].pl.intensity > 0) {
            diffuseSpecularComp += calcSpotLight(diffuse, specular, glossiness, spotLights[i], viewPosition, normal);
        }
    }

    vec4 ambient = calcAmbient(ambientLight, diffuse);
    fragColor = ambient + diffuseSpecularComp;
    if (fog.activeFog == 1) fragColor = calcFog(viewPosition, fragColor, fog, ambientLight.color, directionalLight);
}