#version 330

in vec2 frgTexCoords;
in vec4 frgColor;

uniform sampler2D texSampler;

out vec4 outColor;

void main()
{
    outColor = frgColor * texture(texSampler, frgTexCoords);
}