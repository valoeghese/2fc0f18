#version 330 core

in vec3 colourPass;
in vec2 uvPass;

out vec4 fragmentColour;

uniform sampler2D textureSampler;

void main() {
    vec4 textureColour = texture(textureSampler, uvPass);
    if (textureColour.a < 0.1) {
        discard;
    }
    fragmentColour = textureColour;//vec4(textureColour.x * fragmentColour.x, textureColour.y * fragmentColour.y, textureColour.z * fragmentColour.z, 1.0);
}