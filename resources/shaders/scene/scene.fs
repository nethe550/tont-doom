#version 330

const int MAX_POINT_LIGHTS = 16;
const float SPECULAR_POWER = 10;

in vec3 outPosition;
in vec3 outNormal;
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
struct DirectionalLight
{
    vec3 color;
    vec3 direction;
    float intensity;
};

uniform sampler2D texSampler;
uniform float timeElapsed;
uniform vec2 resolution;

uniform Material material;
uniform AmbientLight ambientLight;
uniform PointLight pointLights[MAX_POINT_LIGHTS];
uniform DirectionalLight directionalLight;

vec4 calcAmbient(AmbientLight ambientLight, vec4 ambient)
{
    return vec4(ambientLight.factor * ambientLight.color, 1.0) * ambient;
}

vec4 calcLightColor(vec4 diffuse, vec4 specular, vec3 lightColor, float light_intensity, vec3 position, vec3 to_light_dir, vec3 normal) {
    vec4 diffuseColor = vec4(0.0, 0.0, 0.0, 1.0);
    vec4 specColor = vec4(0.0, 0.0, 0.0, 1.0);

    // Diffuse Light
    float diffuseFactor = max(dot(normal, to_light_dir), 0.0);
    diffuseColor = diffuse * vec4(lightColor, 1.0) * light_intensity * diffuseFactor;

    // Specular Light
    vec3 camera_direction = normalize(-position);
    vec3 from_light_dir = -to_light_dir;
    vec3 reflected_light = normalize(reflect(from_light_dir, normal));
    float specularFactor = max(dot(camera_direction, reflected_light), 0.0);
    specularFactor = pow(specularFactor, SPECULAR_POWER);
    specColor = specular * light_intensity  * specularFactor * material.glossiness * vec4(lightColor, 1.0);

    return (diffuseColor + specColor);
}

vec4 calcPointLight(vec4 diffuse, vec4 specular, PointLight light, vec3 position, vec3 normal)
{
    vec3 light_direction = light.position - position;
    vec3 to_light_dir  = normalize(light_direction);
    vec4 light_color = calcLightColor(diffuse, specular, light.color, light.intensity, position, to_light_dir, normal);

    // Apply Attenuation
    float distance = length(light_direction);
    float attenuationInv = light.attenuation.constant + light.attenuation.linear * distance +
    light.attenuation.exponent * distance * distance;
    return light_color / attenuationInv;
}

vec4 calcDirectionalLight(vec4 diffuse, vec4 specular, DirectionalLight light, vec3 position, vec3 normal)
{
    return calcLightColor(diffuse, specular, light.color, light.intensity, position, normalize(light.direction), normal);
}

void main()
{
    vec4 texColor = texture(texSampler, outTexCoord);
    vec4 ambient = calcAmbient(ambientLight, texColor + material.ambient);
    vec4 diffuse = texColor + material.diffuse;
    vec4 specular = texColor + material.specular;

    vec4 diffuseSpecularComp = calcDirectionalLight(diffuse, specular, directionalLight, outPosition, outNormal);

    for (int i = 0; i < MAX_POINT_LIGHTS; i++) {
        if (pointLights[i].intensity > 0) {
            diffuseSpecularComp += calcPointLight(diffuse, specular, pointLights[i], outPosition, outNormal);
        }
    }

    fragColor = ambient + diffuseSpecularComp;
}