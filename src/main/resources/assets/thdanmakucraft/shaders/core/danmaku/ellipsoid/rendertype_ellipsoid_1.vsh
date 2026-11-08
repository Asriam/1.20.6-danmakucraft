#version 150

uniform mat4 ProjMat;
uniform mat4 ModelViewMat;

in vec3 Position;
in vec3 Position2;
in vec3 Rotation;
in vec3 Scale;

// 傳給 fragment shader 的資料
out vec3 EllipsoidPosition;
out vec3 EllipsoidRotation;
out vec3 EllipsoidRadius;
// 相機到這個頂點的射線（view space）
out vec3 vRayDirection;

void main() {
    // Position 是「相機相對世界座標」的螢幕面向四邊形頂點
    vec4 viewPos = ModelViewMat * vec4(Position, 1.0);

    gl_Position = ProjMat * viewPos;

    // 相機在 view space 的原點，所以頂點位置本身就是射線方向
    vRayDirection = viewPos.xyz;

    // 另外三個通道原樣傳下去（fragment 會自己做 view space 轉換）
    EllipsoidPosition = Position2;
    EllipsoidRotation = Rotation;
    EllipsoidRadius = Scale;
}
