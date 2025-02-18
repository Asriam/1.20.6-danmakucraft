#version 150

#moj_import <fog.glsl>

in vec3 Position;
in vec4 Color;
in vec4 Color2;
in vec2 UV0;

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;
uniform int FogShape;

out vec4 vertexColor;
out vec4 vertexColor2;
out vec2 texCoord0;
out float vertexDistance;

void main() {
    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0f);

    vertexColor = Color;
    vertexColor2 = Color2;
    texCoord0 = UV0;

    vertexDistance = fog_distance(Position, FogShape);
}
