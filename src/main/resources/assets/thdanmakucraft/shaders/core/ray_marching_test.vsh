#version 150

in vec3 Position;
in vec4 Color;
in vec2 UV;
in vec3 Normal;

uniform mat4 ProjMat;
uniform mat4 ModelViewMat;

uniform vec2 BlurDir;

out vec3 normal;
out vec3 viewDir;
out vec4 vertexColor;
out vec2 vertCoord;

void main() {
    mat3 normalMat = mat3(transpose(inverse(ModelViewMat)));
    normal = normalize(normalMat * Normal);

    vec4 viewSpace = ModelViewMat * vec4(Position, 1.0f);
    viewDir = vec3(viewSpace);
    //real position
    gl_Position = ProjMat * viewSpace;

    vertexColor = Color;
    vertCoord = UV;
}
