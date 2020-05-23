#version 330 core
layout (location = 0) in vec3 rawPos;
layout (location = 1) in vec2 rawUV;
layout (location = 2) in float light;

out vec2 uvPass;
out float lightPass;

uniform mat4 transform;
uniform mat4 view;
uniform mat4 projection;
uniform int time;
uniform int waveMode;
uniform float lighting;

float wave1Y(float, float);

void main() {
    vec3 pos = rawPos;

    if (waveMode > 0) {
        vec4 initialPos = transform * vec4(rawPos, 1.0);
        pos.y = pos.y + 0.1 * wave1Y(initialPos.x, initialPos.z) - 0.05;
    }

    gl_Position = projection * view * transform * vec4(pos, 1.0); // set the final vertex position based on the raw position
    uvPass = rawUV;
    lightPass = light * lighting; // todo change light direction in code based on time of day
}

// todo make waves not bad
float wave1Y(float x, float z) {
    float xSample = 0.07 * (float(time) * 0.01 + x);
    float zSample = 0.07 * (float(time + 10) * 0.01 + z);
    return -abs(sin(xSample) + sin(zSample));
}