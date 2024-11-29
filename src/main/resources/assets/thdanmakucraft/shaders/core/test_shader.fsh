#version 150

uniform sampler2D Sampler0;

in vec4 vertexColor;
in vec2 texCoord0;
in vec2 texCoord1;
in vec2 texCoord2;

out vec4 fragColor;

void main() {
/*
    vec4 color = texture(Sampler0, texCoord0);
    discard;
    if (color.a < vertexColor.a) {
        discard;
    }*/
    fragColor = vec4(1.0f, 0.0f, 0.0f, 1.0f);
}
