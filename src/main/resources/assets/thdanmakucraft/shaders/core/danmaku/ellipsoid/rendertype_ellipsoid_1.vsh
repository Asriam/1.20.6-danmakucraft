#version 330 core

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

// 相機到這個頂點的射線（view space）
out vec3 vRayDirection;
// 傳給 fragment shader 的資料
flat out vec3 EllipsoidPosition;
flat out vec3 EllipsoidRotation;
flat out vec3 EllipsoidRadius;
flat out vec4 BulletColor;
flat out vec4 CoreColor;
flat out vec4 params;
flat out mat4 inversedPojMat;
flat out int ShapeType;

void main() {
    vec4 viewPos = ModelViewMat * vec4(Position, 1.0);
    gl_Position = ProjMat * viewPos;
    vRayDirection = viewPos.xyz;
    if (gl_VertexID == 0){
        EllipsoidPosition = Position2;
        EllipsoidRotation = Rotation;
        EllipsoidRadius = Scale;
        BulletColor = Color;
        CoreColor = Color2;
        params = VEC4;
        inversedPojMat = inverse(ProjMat);
        ShapeType = Shape;
    }
}
