#version 150

uniform sampler2D DepthBuffer;

uniform vec2 ScreenSize;
//uniform mat4 ModelViewMat;
uniform mat4 ProjMat;

in vec3 EllipsoidPosition;
in vec3 EllipsoidRotation;
in vec3 EllipsoidRadius;

in vec4 BulletColor;
in vec4 CoreColor;
in vec4 params;

in vec3 vRayDirection;
in mat3 R;
// 物件中心（view space）
in vec3 centerVS;

in mat4 inversedPojMat;

out vec4 fragColor;

const float PI = 3.14159265359;

// 想看 UV 長什麼樣子時把這行註解打開（會直接把 UV 當顏色輸出）
//#define DEBUG_UV

float GetCameraDistance(vec2 uv, float rawDepth)
{
    // Raw Depth: [0, 1] -> NDC: [-1, 1]
    vec4 clipPos = vec4(
    uv * 2.0 - 1.0,
    rawDepth * 2.0 - 1.0,
    1.0
    );

    // Reconstruct View Space Position
    vec4 viewPos = inversedPojMat * clipPos;

    viewPos.xyz /= viewPos.w;

    // Euclidean Distance
    return length(viewPos.xyz);
}

#define test_flag 1

void main()
{
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

    // Ray Intersection（rd 除以 radius 後長度不再是 1，所以要用一般二次式
    // a*t^2 + 2b*t + c = 0，不能假設 a = 1）
    float a = dot(rd, rd);
    float b = dot(ro, rd);
    float c = dot(ro, ro) - 1.0;

    float h = b * b - a * c;

    #if (test_flag == 1)
    if (h < 0.0)
    discard;
    #endif

    h = sqrt(h);

    float t = (-b - h) / a;

    if (t < 0.0)
    t = (-b + h) / a;

    #if (test_flag == 1)
    if (t < 0.0)
    discard;
    #endif

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
    float u = atan(unitDir.z, unitDir.x) * (0.5 / PI) + 0.5;
    float v = clamp(acos(clamp(unitDir.y, -1.0, 1.0)) / PI, 0.0, 1.0);
    vec2 ellipsoidUV = vec2(u, v);

    // 之後要貼圖時可以直接用：
    //   vec4 tex = texture(Sampler0, ellipsoidUV);
    // （記得在 .json 的 samplers 加上 Sampler0，並在 Java 端 setShaderTexture(0, ...)）

    // Depth
    vec4 clipPos = ProjMat * vec4(hitVS, 1.0);

    #if (test_flag == 1)
    if (clipPos.w <= 0.0)
    discard;
    #endif

    float depth = clipPos.z / clipPos.w;

    gl_FragDepth = clamp(depth * 0.5 + 0.5, 0.0, 1.0);

    // 這個 pass 把深度寫進顏色（除錯用）：之後可改成固定顏色
    //fragColor = vec4(normalLocal,1.0f);


    vec3 viewAngle = normalize(-vRayDirection);
    vec2 texCoord = gl_FragCoord.xy/ScreenSize;
    float rimStrength = 1 - max(dot(viewAngle, normalVS),0.0f);
    //vec4 params = vec4(0.5f,1.5f,0.2f,0.3f);
    float rimFactor  = pow(rimStrength-params.z, params.w);
    float rimFactor2 = pow(rimStrength+1.0f-params.x, params.y);
    vec4 rim  = vec4(rimFactor);
    vec4 rim2 = vec4(rimFactor2);
    float sceneDepth  = GetCameraDistance(texCoord,texture(DepthBuffer, texCoord).r);
    float bubbleDepth = GetCameraDistance(texCoord,gl_FragDepth);
    float distance = abs(bubbleDepth - sceneDepth);
    float threshold = 0.3f;
    float normalizedDistance = clamp(distance / threshold, 0.0, 1.0);
    vec4 intersection = mix(vec4(1), vec4(0), pow(normalizedDistance,2));
    vec4 bubbleBase = BulletColor;
    vec4 outColor = max(vec4(1.0-(clamp(rim,0.0f,1.0f)+max(intersection,0.0f))),0.0f)*1.6f*bubbleBase + max(vec4(1.0-(rim2+clamp(intersection,0.0f,1.0f))),0.0f)*1.6f*(CoreColor*2.0f-1.0f);
    fragColor = outColor;

    #ifdef DEBUG_UV
    fragColor = vec4(ellipsoidUV, 0.0, 1.0);
    #endif
    //fragColor = vec4(vec3(texture(DepthBuffer, texCoord).r),1.0f);
    #if (test_flag != 1)
    fragColor = vec4(1.0f);
    #endif
}
