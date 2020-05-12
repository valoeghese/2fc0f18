#version 330 core
layout (location = 0) in vec3 rawPos;
layout (location = 1) in vec2 rawUV;

out vec2 uvPass;

uniform mat4 transform;
uniform mat4 view;
uniform mat4 projection;

void main() {
    gl_Position = projection * view * transform * vec4(rawPos, 1.0);// set the final vertex position based on the raw position
    uvPass = rawUV;
}