#version 330 core
layout (location = 0) in vec3 rawPos;
layout (location = 1) in vec2 rawUV;

out vec2 uvPass;

uniform mat4 projection;

void main() {
    gl_Position = projection * vec4(rawPos, 1.0);
    uvPass = rawUV;
}