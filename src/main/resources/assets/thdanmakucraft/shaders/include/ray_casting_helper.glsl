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

// -----------------------------------------------------------------------------
// Cube (box) ray casting
// -----------------------------------------------------------------------------

/** 1/x，把 0 換成極小值，避免 slab test 出現 NaN。 */
vec3 RayCastingSafeInverse(vec3 v) {
    vec3 safe = v;
    if (abs(safe.x) < 1.0e-8) safe.x = safe.x < 0.0 ? -1.0e-8 : 1.0e-8;
    if (abs(safe.y) < 1.0e-8) safe.y = safe.y < 0.0 ? -1.0e-8 : 1.0e-8;
    if (abs(safe.z) < 1.0e-8) safe.z = safe.z < 0.0 ? -1.0e-8 : 1.0e-8;
    return 1.0 / safe;
}

/**
 * 軸對齊 box 的 slab test。
 *
 * @param tNear 近交點距離（可能為負，代表在射線起點後方）
 * @param tFar  遠交點距離
 * @return 射線與 box 有沒有交集（射線起點在 box 內也算有）
 */
bool RayCastingCubeAabb(vec3 ro, vec3 rd, vec3 boxMin, vec3 boxMax,
                        out float tNear, out float tFar) {
    vec3 invRd = RayCastingSafeInverse(rd);
    vec3 t0 = (boxMin - ro) * invRd;
    vec3 t1 = (boxMax - ro) * invRd;
    vec3 tSmall = min(t0, t1);
    vec3 tBig = max(t0, t1);

    tNear = max(max(tSmall.x, tSmall.y), tSmall.z);
    tFar = min(min(tBig.x, tBig.y), tBig.z);
    return tFar >= max(tNear, 0.0);
}

/** 單位 cube（半邊長 = 1）求交；tHit / normalLocal / hitLocal 都是 local space。 */
bool RayCastingUnitCube(vec3 ro, vec3 rd,
                        out float tHit, out vec3 normalLocal, out vec3 hitLocal) {
    float tNear;
    float tFar;
    if (!RayCastingCubeAabb(ro, rd, vec3(-1.0), vec3(1.0), tNear, tFar)) {
        return false;
    }

    // 起點在 cube 外就用近交點，在裡面就用遠交點（離開的那一面）
    tHit = tNear >= 0.0 ? tNear : tFar;
    hitLocal = ro + rd * tHit;

    // 用「最靠近哪個面」決定法線
    vec3 unit = clamp(hitLocal, vec3(-1.0), vec3(1.0));
    vec3 absUnit = abs(unit);
    normalLocal = vec3(0.0);
    if (absUnit.x >= absUnit.y && absUnit.x >= absUnit.z) {
        normalLocal.x = unit.x < 0.0 ? -1.0 : 1.0;
    } else if (absUnit.y >= absUnit.z) {
        normalLocal.y = unit.y < 0.0 ? -1.0 : 1.0;
    } else {
        normalLocal.z = unit.z < 0.0 ? -1.0 : 1.0;
    }
    return true;
}

/** 依命中面把 local 座標（[-1,1]）攤平成 0..1 UV，六個面各自使用完整貼圖。 */
vec2 RayCastingCubeUv(vec3 localUnit, vec3 normalLocal) {
    vec3 p = localUnit * 0.5f + 0.5f;
    vec3 n = abs(normalLocal);

    if (n.x >= n.y && n.x >= n.z) {
        return vec2(normalLocal.x < 0.0 ? 1.0 - p.z : p.z, p.y);
    }
    if (n.y >= n.z) {
        return vec2(p.x, normalLocal.y < 0.0 ? 1.0 - p.z : p.z);
    }
    return vec2(normalLocal.z < 0.0 ? 1.0 - p.x : p.x, p.y);
}

/**
 * Cube 版 ray casting，參數與回傳都跟 {@code RayCastingElliposid} 一致。
 *
 * <p>{@code _size} 是 cube 的「半邊長」（各軸可分開，所以也能畫長方體）；
 * 回傳的 uv 是命中面攤平後的 0..1 座標，depth 是 [0,1] 的深度，
 * normal 是 view space 的法線。</p>
 */
UvDepthNormal RayCastingCube(mat4 ModelViewMat, mat4 ProjectionMat, vec3 vRayDirection,
                             vec3 centerPos, vec3 rotation, vec3 _size, bool shouldDiscard) {
    mat3 viewRot = mat3(ModelViewMat[0].xyz, ModelViewMat[1].xyz, ModelViewMat[2].xyz);
    mat3 R = viewRot * rotationMatrix(rotation);
    vec3 centerVS = (ModelViewMat * vec4(centerPos, 1.0)).xyz;

    vec3 roVS = vec3(0.0);
    vec3 rdVS = normalize(vRayDirection);

    // View -> Cube Local
    vec3 roLocal = transpose(R) * (roVS - centerVS);
    vec3 rdLocal = normalize(transpose(R) * rdVS);

    vec3 halfSize = max(abs(_size), vec3(1.0e-4));

    // Cube -> Unit Cube（slab test 在單位 cube 上做）
    vec3 ro = roLocal / halfSize;
    vec3 rd = rdLocal / halfSize;

    float tHit;
    vec3 normalLocal;
    vec3 hitLocal;
    bool hit = RayCastingUnitCube(ro, rd, tHit, normalLocal, hitLocal);

    if (shouldDiscard && !hit) {
        discard;
    }

    // 沒打到的時候給一組安全值，避免 NaN 流出去
    if (!hit) {
        tHit = 0.0;
        normalLocal = vec3(0.0, 0.0, 1.0);
        hitLocal = ro;
    }

    // 回到 cube 的 local 空間（世界單位）
    vec3 hitLocalWorld = hitLocal * halfSize;
    vec3 normalVS = normalize(R * normalLocal);
    vec3 hitVS = centerVS + R * hitLocalWorld;

    vec2 cubeUV = RayCastingCubeUv(hitLocal, normalLocal);

    // Depth
    vec4 clipPos = ProjectionMat * vec4(hitVS, 1.0);

    if (shouldDiscard && clipPos.w <= 0.0) {
        discard;
    }

    float depth = clipPos.z / clipPos.w;

    UvDepthNormal result;
    result.uv = cubeUV;
    result.depth = clamp(depth * 0.5 + 0.5, 0.0, 1.0);
    result.normal = normalVS;
    return result;
}

// -----------------------------------------------------------------------------
// Analytic 5-point star ray casting（圓潤輪廓、精確求交）
// -----------------------------------------------------------------------------

/**
 * 星形輪廓在 theta 方向的半徑。
 *
 * @param roundness 0 = 星角較尖（餘弦輪廓），1 = 非常圓潤（平滑的花瓣狀星形）
 */
float StarRadiusAt(float theta, float outerRadius, float innerRatio, float roundness) {
    float base = 0.5 + 0.5 * cos(5.0 * theta);   // 1 = 尖端方向，0 = 內凹方向
    float shaped = mix(base, smoothstep(0.0, 1.0, base), clamp(roundness, 0.0, 1.0));
    return outerRadius * (innerRatio + (1.0 - innerRatio) * shaped);
}

/** 星形輪廓上一點（local xy）。 */
vec2 StarBoundary(float theta, float outerRadius, float innerRatio, float roundness) {
    float r = StarRadiusAt(theta, outerRadius, innerRatio, roundness);
    return vec2(cos(theta), sin(theta)) * r;
}

/** Möller–Trumbore 射線-三角形交集。 */
bool RayCastingTriangle(vec3 ro, vec3 rd, vec3 a, vec3 b, vec3 c,
                        out float tHit, out vec3 normal) {
    vec3 e1 = b - a;
    vec3 e2 = c - a;
    vec3 p = cross(rd, e2);
    float det = dot(e1, p);
    if (abs(det) < 1.0e-8) {
        return false;
    }
    float invDet = 1.0 / det;
    vec3 tv = ro - a;
    float u = dot(tv, p) * invDet;
    if (u < 0.0 || u > 1.0) {
        return false;
    }
    vec3 q = cross(tv, e1);
    float v = dot(rd, q) * invDet;
    if (v < 0.0 || u + v > 1.0) {
        return false;
    }
    float t = dot(e2, q) * invDet;
    if (t <= 0.0) {
        return false;
    }
    tHit = t;
    normal = normalize(cross(e1, e2));
    return true;
}

/** 依命中位置直接算錐面的平滑法線（避免看到一片片三角面）。 */
vec3 StarSmoothNormal(vec3 hitLocal, float outerRadius, float innerRatio, float roundness,
                      float capHeight, vec3 rayDir) {
    float theta = atan(hitLocal.y, hitLocal.x);
    float zSign = hitLocal.z < 0.0 ? -1.0 : 1.0;
    vec3 apex = vec3(0.0, 0.0, capHeight * zSign);

    float r = StarRadiusAt(theta, outerRadius, innerRatio, roundness);
    const float e = 0.001;
    float dr = (StarRadiusAt(theta + e, outerRadius, innerRatio, roundness)
              - StarRadiusAt(theta - e, outerRadius, innerRatio, roundness)) / (2.0 * e);

    float c = cos(theta);
    float s = sin(theta);
    vec3 p = vec3(c * r, s * r, 0.0);
    vec3 dp = vec3(c * dr - s * r, s * dr + c * r, 0.0);

    vec3 n = normalize(cross(p - apex, dp));
    if (dot(n, rayDir) > 0.0) {
        n = -n;
    }
    return n;
}

/**
 * 五角星 ray casting：解析求交（不用 ray marching）。
 *
 * <p>輪廓是「圓潤的五角星」曲線（半徑隨角度連續變化），前後各一個頂點在
 * z = ±{@code _size.z}，用 32 段扇形三角面逼近；法線用錐面解析解算，
 * 所以輪廓平滑、也不會看到一片片平面。</p>
 *
 * @param _size      (星形半徑, 星形半徑, 前後頂點高度)
 * @param innerRatio 內外半徑比（0.45 左右）
 * @param roundness  輪廓圓潤度：0 = 星角較尖，1 = 很圓潤
 */
UvDepthNormal RayCastingStar(mat4 ModelViewMat, mat4 ProjectionMat, vec3 vRayDirection,
                             vec3 centerPos, vec3 rotation, vec3 _size,
                             float innerRatio, float roundness, bool shouldDiscard) {
    mat3 viewRot = mat3(ModelViewMat[0].xyz, ModelViewMat[1].xyz, ModelViewMat[2].xyz);
    mat3 R = viewRot * rotationMatrix(rotation);
    vec3 centerVS = (ModelViewMat * vec4(centerPos, 1.0)).xyz;

    // 相機射線（view space，相機在原點）-> star local
    vec3 roLocal = transpose(R) * (vec3(0.0) - centerVS);
    vec3 rdLocal = normalize(transpose(R) * normalize(vRayDirection));

    vec3 halfSize = max(abs(_size), vec3(1.0e-4));
    float outer = halfSize.x;
    float capHeight = halfSize.z;
    float ratio = clamp(innerRatio, 0.05, 0.95);
    float round_ = clamp(roundness, 0.0, 1.0);

    const int SEGMENTS = 32;

    float bestT = 1.0e30;
    bool hit = false;

    // 包圍球快速剔除
    float boundRadius = length(vec3(outer, outer, capHeight));
    float b = dot(roLocal, rdLocal);
    float cc = dot(roLocal, roLocal) - boundRadius * boundRadius;
    float hh = b * b - cc;

    if (hh >= 0.0) {
        // 先算好輪廓取樣點，避免每個三角面重複計算
        vec2 boundary[SEGMENTS + 1];
        for (int j = 0; j <= SEGMENTS; j++) {
            float theta = float(j) * PI * 2.0 / float(SEGMENTS);
            boundary[j] = StarBoundary(theta, outer, ratio, round_);
        }

        for (int cap = 0; cap < 2; cap++) {
            float zSign = (cap == 0) ? 1.0 : -1.0;
            vec3 apex = vec3(0.0, 0.0, capHeight * zSign);

            for (int j = 0; j < SEGMENTS; j++) {
                float tHit;
                vec3 normal;
                if (RayCastingTriangle(roLocal, rdLocal, apex,
                                       vec3(boundary[j], 0.0), vec3(boundary[j + 1], 0.0),
                                       tHit, normal) && tHit < bestT) {
                    bestT = tHit;
                    hit = true;
                }
            }
        }
    }

    if (shouldDiscard && !hit) {
        discard;
    }
    if (!hit) {
        bestT = 0.0;
    }

    vec3 hitLocal = roLocal + rdLocal * bestT;
    vec3 normalLocal = hit
            ? StarSmoothNormal(hitLocal, outer, ratio, round_, capHeight, rdLocal)
            : vec3(0.0, 0.0, 1.0);

    vec3 normalVS = normalize(R * normalLocal);
    vec3 hitVS = centerVS + R * hitLocal;

    // ------------------------------------------------------------------ UV
    // 平面投影；背面做鏡像，貼圖方向才不會反
    vec2 starUV = hitLocal.xy / (outer * 2.0) + 0.5;
    if (normalLocal.z < 0.0) {
        starUV.x = 1.0 - starUV.x;
    }

    // Depth
    vec4 clipPos = ProjectionMat * vec4(hitVS, 1.0);

    if (shouldDiscard && clipPos.w <= 0.0) {
        discard;
    }

    float depth = clipPos.z / clipPos.w;

    UvDepthNormal result;
    result.uv = starUV;
    result.depth = clamp(depth * 0.5 + 0.5, 0.0, 1.0);
    result.normal = normalVS;
    return result;
}
