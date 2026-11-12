#version 150

uniform mat4 ProjMat;
uniform mat4 ModelViewMat;
uniform vec2 ScreenSize;

in vec3 Position;
in vec3 Position2;
in vec3 Rotation;
in vec3 Scale;
in vec4 Color;
in vec4 Color2;
in vec4 VEC4;
in int Shape;

// 傳給 fragment shader 的資料
out vec3 EllipsoidPosition;
out vec3 EllipsoidRotation;
out vec3 EllipsoidRadius;
out vec4 BulletColor;
out vec4 CoreColor;
// 相機到這個頂點的射線（view space）
out vec3 vRayDirection;
out vec4 params;
out mat4 inversedPojMat;
flat out int ShapeType;

void main() {
    vec4 viewPos = ModelViewMat * vec4(Position, 1.0);
    gl_Position = ProjMat * viewPos;
    vRayDirection = viewPos.xyz;
    EllipsoidPosition = Position2;
    EllipsoidRotation = Rotation;
    EllipsoidRadius = Scale;
    BulletColor = Color;
    CoreColor = Color2;
    params = VEC4;
    inversedPojMat = inverse(ProjMat);
    ShapeType = Shape;
}
