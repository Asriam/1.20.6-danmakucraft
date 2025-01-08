#version 150

in vec3 Position;
in vec4 Color;
in vec4 Color2;
//in vec2 UV;
in vec2 UV2;
in vec2 UV3;
in vec3 Normal;

uniform mat4 ProjMat;
uniform mat4 ModelViewMat;

uniform vec2 BlurDir;

out vec3 normal;
out vec3 viewDir;
out vec4 vertexColor;
out vec4 coreColor;
out vec2 vertCoord;
out vec4 params;

void main() {
    mat3 normalMat = mat3(transpose(inverse(ModelViewMat)));
    normal = normalize(normalMat * Normal);

    vec4 viewSpace = ModelViewMat * vec4(Position, 1.0f);
    viewDir = vec3(viewSpace);
    //real position
    gl_Position = ProjMat * viewSpace;

    vertexColor = Color;
    coreColor = Color2;
    //normal = Normal;
    //vertCoord = UV;
    vertCoord = vec2(0.0f,0.0f);
    params = vec4(UV2,UV3);
}
