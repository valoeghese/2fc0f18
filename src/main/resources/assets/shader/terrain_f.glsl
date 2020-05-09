#version 330 core

in vec3 colourPass;
out vec4 fragmentColour;

void main() {
    fragmentColour = vec4(colourPass, 1.0f); // rgba
}