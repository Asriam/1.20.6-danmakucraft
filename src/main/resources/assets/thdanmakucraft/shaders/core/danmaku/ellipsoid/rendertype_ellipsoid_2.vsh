#version 150

uniform mat4 ProjectionMat;
uniform mat4 ModelViewMat;
uniform vec2 ScreenSize;

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

out mat3 R;
out vec3 centerVS;
out vec4 params;

out mat4 inversedPojMat;

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
    /*vec2 size = vec2(2.0 / ProjectionMat[0][0],2.0 / ProjectionMat[1][1]);
    vec3 screenPos = vec3((Position.xy * 2.0 - 1.0)/2* size * 1.0f, -1.0f);
    vec4 viewPos = vec4(screenPos, 1.0);*/
    vec4 viewPos = vec4(Position, 1.0);
    gl_Position = ProjectionMat * viewPos;
    vRayDirection = viewPos.xyz;
    EllipsoidPosition = Position2;
    EllipsoidRotation = Rotation;
    EllipsoidRadius = Scale;
    BulletColor = Color;
    CoreColor = Color2;
    mat3 viewRot = mat3(ModelViewMat[0].xyz, ModelViewMat[1].xyz, ModelViewMat[2].xyz);
    R = viewRot * rotationMatrix(EllipsoidRotation) ;
    centerVS = (ModelViewMat * vec4(EllipsoidPosition, 1.0)).xyz;
    params = VEC4;
    inversedPojMat = inverse(ProjectionMat);
}
