#version 150

#moj_import <fog.glsl>

in vec3 Position;
in vec3 Normal;
in vec4 Color;

uniform mat4 ProjMat;
uniform mat4 ModelViewMat;
uniform int FogShape;

out vec4 vertexColor;
out vec3 normal;
out vec3 viewDir;
out float vertexDistance;

void main() {
    mat3 normalMat = mat3(transpose(inverse(ModelViewMat)));
    vec4 viewSpace = ModelViewMat * vec4(Position, 1.0f);
    viewDir = vec3(viewSpace);
    gl_Position = ProjMat * viewSpace;
    vertexColor = Color;
    normal = normalize(normalMat * Normal);
    vertexDistance = fog_distance(Position, FogShape);
}
