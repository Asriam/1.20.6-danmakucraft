#version 120

struct UvDepthNormal{
    vec2 uv;
    float depth;
    vec3 normal;
};

const float PI = 3.14159265359;
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

UvDepthNormal RayCastingElliposid(mat4 ModelViewMat, mat4 ProjectionMat, vec3 vRayDirection, vec3 centerPos, vec3 rotation, vec3 _radius, bool shouldDiscard){
    mat3 viewRot = mat3(ModelViewMat[0].xyz, ModelViewMat[1].xyz, ModelViewMat[2].xyz);
    mat3 R = viewRot * rotationMatrix(rotation) ;
    vec3 centerVS = (ModelViewMat * vec4(centerPos, 1.0)).xyz;

    // 相機射線（view space，相機在原點）
    vec3 roVS = vec3(0.0);
    vec3 rdVS = normalize(vRayDirection);

    // View -> Ellipsoid Local
    vec3 roLocal = transpose(R) * (roVS - centerVS);
    vec3 rdLocal = transpose(R) * rdVS;

    // 避免半徑為 0 造成除以零（NaN 會讓整個 ellipsoid 消失）
    vec3 radius = max(abs(_radius), vec3(1.0e-4));

    // Ellipsoid -> Unit Sphere
    vec3 ro = roLocal / radius;
    vec3 rd = rdLocal / radius;

    // Ray Intersection（rd 除以 radius 後長度不再是 1，所以要用一般二次式
    // a*t^2 + 2b*t + c = 0，不能假設 a = 1）
    float a = dot(rd, rd);
    float b = dot(ro, rd);
    float c = dot(ro, ro) - 1.0;

    float h = b * b - a * c;

    if (shouldDiscard && h < 0.0)
    discard;

    h = sqrt(h);

    float t = (-b - h) / a;

    if (t < 0.0)
    t = (-b + h) / a;

    if (shouldDiscard && t < 0.0)
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

    // ------------------------------------------------------------------ UV
    // ellipsoid 是「單位球」經過半徑縮放 + 旋轉的結果，所以把命中點除以半徑
    // 就得到單位球上的方向，再轉成經緯度（equirectangular）UV：
    //   u = 0..1 繞 Y 軸一圈（經度）
    //   v = 0..1 從 +Y 到 -Y（緯度）
    // 這裡用 local space，所以紋理會跟著橢圓一起旋轉。
    vec3 unitDir = normalize(hitLocal / radius);
    float u = atan(unitDir.y, unitDir.x) * (0.5 / PI) + 0.5;
    float v = clamp(acos(clamp(unitDir.z, -1.0, 1.0)) / PI, 0.0, 1.0);
    vec2 ellipsoidUV = vec2(u, v);

    // 之後要貼圖時可以直接用：
    //   vec4 tex = texture(Sampler0, ellipsoidUV);
    // （記得在 .json 的 samplers 加上 Sampler0，並在 Java 端 setShaderTexture(0, ...)）

    // Depth
    vec4 clipPos = ProjectionMat * vec4(hitVS, 1.0);

    if (shouldDiscard && clipPos.w <= 0.0)
    discard;

    float depth = clipPos.z / clipPos.w;

    UvDepthNormal result;
    result.uv = ellipsoidUV;
    result.depth = clamp(depth * 0.5 + 0.5, 0.0, 1.0);
    result.normal = normalVS;
    return result;
}