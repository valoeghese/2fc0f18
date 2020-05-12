#version 330 core

in vec2 uvPass;

out vec4 fragmentColour;

uniform sampler2D textureSampler;

void main() {
    vec4 textureColour = texture(textureSampler, vec2(uvPass.x, 1.0 - uvPass.y));
    if (textureColour.a < 0.1) {
        discard;
    }
    fragmentColour = textureColour;
}