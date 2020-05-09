#version 330 core
layout (location = 0) in vec3 rawPos;

out vec3 colourPass;

uniform mat4 transform;
uniform mat4 view;
uniform mat4 projection;
uniform vec3 colour;

void main() {
    gl_Position = projection * view * transform * vec4(rawPos, 1.0);// set the final vertex position based on the raw position
    colourPass = colour;
}