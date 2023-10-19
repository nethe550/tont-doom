#version 330

layout (location=0) in vec3 position;
layout (location=1) in vec3 color;
layout (location=2) in vec2 texcoord;

out vec3 outVertexColor;
out vec2 outTexCoord;


uniform mat4 projectionMatrix;
uniform mat4 modelMatrix;

void main()
{
    gl_Position = projectionMatrix * modelMatrix * vec4(position, 1.0);
    outVertexColor = color;
    outTexCoord = texcoord;
}