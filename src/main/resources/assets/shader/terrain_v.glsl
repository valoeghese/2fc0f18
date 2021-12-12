#version 330 core
layout (location = 0) in vec3 rawPos;
layout (location = 1) in vec2 rawUV;
layout (location = 2) in int packedLight;

out vec2 uvPass;
flat out float lightPass;

uniform mat4 transform;
uniform mat4 view;
uniform mat4 projection;
uniform float time;
uniform int waveMode;
uniform float skylight;

float wave1Y(float, float);

void main() {
    vec3 pos = rawPos;

    if (waveMode > 0) {
        vec4 initialPos = transform * vec4(rawPos, 1.0);
        pos.y = pos.y + 0.15 * wave1Y(initialPos.x, initialPos.z) - 0.05;
    }

    gl_Position = projection * view * transform * vec4(pos, 1.0); // set the final vertex position based on the raw position
    uvPass = rawUV;

    if (skylight < 0) {
        lightPass = 1;
    } else {
        // 0-1 for block and sky base values
        float blockLight = float(packedLight >> 7) / 15.0;
        float skyLight = skylight * float((packedLight >> 3) & 0xF) / 15.0; // have fun with skyLight and skylight being different
        int face = packedLight & 7;
        float lightMultiplier;

        switch (face) {
        case 0: // south
        case 3: // north
            lightMultiplier = 0.925;
            break;
        case 1: // up
            lightMultiplier = 0.95;
            break;
        case 2: // east
            lightMultiplier = 0.85;
            break;
        case 4: // down
            lightMultiplier = 0.9;
            break;
        case 5: // west
            lightMultiplier = 1.0;
            break;
        }

        lightPass = max(blockLight, skyLight) * lightMultiplier;
        // todo change light direction in code based on time of day
    }
}

float wave1Y(float x, float z) {
    float xSample = 0.035* (float(time) + x);
    float zSample = 0.035 * (float(time + 10) + z);
    return -abs(sin(xSample) + sin(zSample));
}