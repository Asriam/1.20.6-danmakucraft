#version 150

in vec3 Position;
in vec4 Color;
in vec4 Color2;
in vec2 UV0;

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;

out vec4 vertexColor;
out vec4 vertexColor2;
out vec2 texCoord0;

//out vec3 viewDir;

void main() {
    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0f);

    vertexColor = Color;
    vertexColor2 = Color2;
    texCoord0 = UV0;
}
