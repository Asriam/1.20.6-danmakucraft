#version 150

in vec3 Position;
in vec3 Normal;

uniform mat4 ProjMat;
uniform mat4 ModelViewMat;
uniform int FogShape;

out vec3 normal;
out vec3 viewDir;

void main() {
    mat3 normalMat = mat3(transpose(inverse(ModelViewMat)));
    vec4 viewSpace = ModelViewMat * vec4(Position, 1.0f);
    viewDir = vec3(viewSpace);
    gl_Position = ProjMat * viewSpace;
    normal = mat3(inverse(ModelViewMat)) * normalize(normalMat * Normal);
}
