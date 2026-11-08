#version 150

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;

in vec3 EllipsoidPosition;
in vec3 EllipsoidRotation;
in vec3 EllipsoidRadius;

in vec3 vRayDirection;

out vec4 fragColor;

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

void main()
{
    // 把 view 的旋轉抽出來：Rotation 是物件在世界座標的角度，
    // 要轉到 view space 才能跟 centerVS / 射線在同一個座標系
    mat3 viewRot = mat3(ModelViewMat[0].xyz, ModelViewMat[1].xyz, ModelViewMat[2].xyz);
    mat3 R = viewRot * rotationMatrix(EllipsoidRotation) * transpose(viewRot);

    // 物件中心（view space）
    vec3 centerVS = (ModelViewMat * vec4(EllipsoidPosition, 1.0)).xyz;

    // 相機射線（view space，相機在原點）
    vec3 roVS = vec3(0.0);
    vec3 rdVS = normalize(vRayDirection);

    // View -> Ellipsoid Local
    vec3 roLocal = transpose(R) * (roVS - centerVS);
    vec3 rdLocal = transpose(R) * rdVS;

    // 避免半徑為 0 造成除以零（NaN 會讓整個 ellipsoid 消失）
    vec3 radius = max(abs(EllipsoidRadius), vec3(1.0e-4));

    // Ellipsoid -> Unit Sphere
    vec3 ro = roLocal / radius;
    vec3 rd = rdLocal / radius;

    // Ray Intersection
    float b = dot(ro, rd);
    float c = dot(ro, ro) - 1.0;

    float h = b * b - c;

    if (h < 0.0)
    discard;

    h = sqrt(h);

    float t = -b - h;

    if (t < 0.0)
    t = -b + h;

    if (t < 0.0)
    discard;

    // Local Hit Position
    vec3 hitLocal = roLocal + rdLocal * t;

    // Local Normal
    vec3 normalLocal = normalize(
    hitLocal /
    (radius * radius)
    );

    // Local -> View Normal
    vec3 normalVS = normalize(R * normalLocal);

    // Local -> View Hit Position
    vec3 hitVS = centerVS + R * hitLocal;

    // Depth
    vec4 clipPos = ProjMat * vec4(hitVS, 1.0);

    if (clipPos.w <= 0.0)
    discard;

    float depth = clipPos.z / clipPos.w;

    gl_FragDepth = clamp(depth * 0.5 + 0.5, 0.0, 1.0);

    // 這個 pass 把深度寫進顏色（除錯用）：之後可改成固定顏色
    fragColor = vec4(vec3(gl_FragDepth), 1.0);
}
