#version 150

uniform vec2 ScreenSize;

in vec3 normal;

out vec4 fragColor;

void main() {
    fragColor = vec4(normal,1.0f);
}
