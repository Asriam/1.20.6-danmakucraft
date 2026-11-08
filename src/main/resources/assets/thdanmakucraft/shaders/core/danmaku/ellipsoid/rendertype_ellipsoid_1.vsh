#version 150

uniform mat4 ProjMat;
uniform mat4 ModelViewMat;

in vec3 Position;
in vec3 Position2;
in vec3 Rotation;
in vec3 Scale;
in vec4 Color;
in vec4 Color2;
in vec4 VEC4;

// 傳給 fragment shader 的資料
out vec3 EllipsoidPosition;
out vec3 EllipsoidRotation;
out vec3 EllipsoidRadius;
// 相機到這個頂點的射線（view space）
out vec3 vRayDirection;
out vec4 BulletColor;
out vec4 CoreColor;
out float near;
out float far;

out mat3 R;
out vec3 centerVS;
out vec4 params;

mat3 rotationMatrix(vec3 r)
{
    vec3 c = cos(r);
    vec3 s = sin(r);

    mat3 Rx = mat3(
    1.0, 0.0, 0.0,
    0.0, c.x, s.x,
    0.0, -s.x, c.x
    );

    mat3 Ry = mat3(
    c.y, 0.0, -s.y,
    0.0, 1.0, 0.0,
    s.y, 0.0, c.y
    );

    mat3 Rz = mat3(
    c.z, s.z, 0.0,
    -s.z, c.z, 0.0,
    0.0, 0.0, 1.0
    );

    return Rz * Ry * Rx;
}

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

    BulletColor = Color;
    CoreColor = Color2;

    near = ProjMat[3][2]/(ProjMat[2][2]-1.0);
    far = ProjMat[3][2]/(ProjMat[2][2]+1.0);

    // 把 view 的旋轉抽出來：Rotation 是物件在世界座標的角度，
    // 要轉到 view space 才能跟 centerVS / 射線在同一個座標系
    mat3 viewRot = mat3(ModelViewMat[0].xyz, ModelViewMat[1].xyz, ModelViewMat[2].xyz);
    R = viewRot * rotationMatrix(EllipsoidRotation) ;
    // 物件中心（view space）
    centerVS = (ModelViewMat * vec4(EllipsoidPosition, 1.0)).xyz;
    params = VEC4;
}
