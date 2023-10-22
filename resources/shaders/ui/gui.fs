#version 330

in vec2 fragTexCoords;
in vec4 fragColor;

uniform sampler2D texSampler;

out vec4 outColor;

void main()
{
    outColor = fragColor * texture(texSampler, fragTexCoords);
}