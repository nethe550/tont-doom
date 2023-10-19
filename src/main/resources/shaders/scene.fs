#version 330

in vec3 outColor;
in vec2 outTexCoord;

out vec4 fragColor;

uniform float timeElapsed;
uniform vec2 resolution;

uniform sampler2D texSampler;

void main()
{
    fragColor = texture(texSampler, outTexCoord);
}