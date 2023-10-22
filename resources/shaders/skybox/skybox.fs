#version 330

in vec2 outTexCoord;

out vec4 fragColor;

uniform vec4 diffuse;
uniform sampler2D texSampler;
uniform int hasTexture;

void main()
{
    if (hasTexture == 1) fragColor = texture(texSampler, outTexCoord);
    else fragColor = diffuse;
}