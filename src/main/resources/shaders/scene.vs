#version 330

layout (location=0) in vec3 position;
layout (location=1) in vec3 color;

out vec3 outColor;

uniform mat4 projectionMatrix;
uniform mat4 modelMatrix;
uniform float timeElapsed;

void main()
{
    gl_Position = projectionMatrix * modelMatrix * vec4(position, 1.0);
    outColor = color * (sin(timeElapsed * 0.001) * 0.4 + 0.6);
}