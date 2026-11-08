#version 150

#moj_import <fog.glsl>

uniform sampler2D DepthBuffer;
uniform sampler2D ScreenBuffer;
uniform sampler2D AuraDepthBuffer;

uniform vec2 ScreenSize;
uniform float Timer;
uniform vec3 CameraPosition;

in vec4 vertexColor;
in vec3 normal;
in vec3 normal2;
in vec3 viewDir;
in float vertexDistance;
in float near;
in float far;
in mat4 uInvProjection;
in mat4 uInvModelView;
in vec2 texCoord2;

out vec4 fragColor;

// ------------------------------------------------------------------ 基本工具

float LinearizeDepth(float depth) {
    float z = depth * 2.0 - 1.0;
    return (2.0 * near * far) / (far + near - z * (far - near));
}

vec3 clipToView(vec2 uv, float depth) {
    vec4 clipPos = vec4(uv * 2.0 - 1.0, depth * 2.0 - 1.0, 1.0);
    vec4 viewPos = uInvProjection * clipPos;
    return viewPos.xyz / viewPos.w;
}

vec3 fetchViewPos(vec2 uv) {
    if (uv.x < 0.0 || uv.x > 1.0 || uv.y < 0.0 || uv.y > 1.0)
    return vec3(0.0, 0.0, -1000.0);
    float rawDepth = texture(DepthBuffer, uv).r;
    return clipToView(uv, rawDepth);
}

// 重建法線
vec3 reconstructNormal(vec3 centerPos, vec2 uv) {
    float EDGE_THRESHOLD = 0.003;
    vec2 offset = 1.0 / ScreenSize;
    vec3 posLeft  = fetchViewPos(uv + vec2(-offset.x, 0.0));
    vec3 posRight = fetchViewPos(uv + vec2( offset.x, 0.0));
    vec3 posUp    = fetchViewPos(uv + vec2(0.0,  offset.y));
    vec3 posDown  = fetchViewPos(uv + vec2(0.0, -offset.y));

    vec3 l = centerPos - posLeft;
    vec3 r = posRight - centerPos;
    vec3 d = centerPos - posDown;
    vec3 u = posUp - centerPos;

    float p = pow(centerPos.z, 2);

    if (abs(l.z / p) > EDGE_THRESHOLD || abs(r.z / p) > EDGE_THRESHOLD ||
    abs(d.z / p) > EDGE_THRESHOLD || abs(u.z / p) > EDGE_THRESHOLD) {
        return vec3(0.0, 0.0, 1.0);
    }
    vec3 hVec = (abs(l.z) < abs(r.z)) ? l : r;
    vec3 vVec = (abs(d.z) < abs(u.z)) ? d : u;
    return normalize(cross(hVec, vVec));
}

float grid(vec3 worldPos, vec3 normal, float lineWidth, float scale) {
    vec3 coord = worldPos * scale;
    vec3 gridDeriv = fwidth(coord);
    vec3 gridPattern = abs(fract(coord - 0.5) - 0.5);
    vec3 lineDist = gridPattern / (gridDeriv * lineWidth);
    vec3 gridLines = 1.0 - min(lineDist, 1.0);
    vec3 weight = abs(normal);
    float result = gridLines.x * (1.0 - weight.x) +
    gridLines.y * (1.0 - weight.y) +
    gridLines.z * (1.0 - weight.z);
    return clamp(result, 0.0, 1.0);
}

// ------------------------------------------------------------------ 花樣

float hash12(vec2 p) {
    vec3 p3 = fract(vec3(p.xyx) * 0.1031);
    p3 += dot(p3, p3.yzx + 33.33);
    return fract((p3.x + p3.y) * p3.z);
}

float valueNoise(vec2 p) {
    vec2 i = floor(p);
    vec2 f = fract(p);
    f = f * f * (3.0 - 2.0 * f);
    float a = hash12(i);
    float b = hash12(i + vec2(1.0, 0.0));
    float c = hash12(i + vec2(0.0, 1.0));
    float d = hash12(i + vec2(1.0, 1.0));
    return mix(mix(a, b, f.x), mix(c, d, f.x), f.y);
}

vec3 sampleChromatic(vec2 uv, vec2 dir, float amount) {
    vec2 r = clamp(uv + dir * (1.0 + amount), vec2(0.0015), vec2(0.9985));
    vec2 g = clamp(uv, vec2(0.0015), vec2(0.9985));
    vec2 b = clamp(uv - dir * (1.0 - amount), vec2(0.0015), vec2(0.9985));
    return vec3(texture(ScreenBuffer, r).r,
                texture(ScreenBuffer, g).g,
                texture(ScreenBuffer, b).b);
}

void main() {
    vec2 texCoord = gl_FragCoord.xy / ScreenSize;

    float sceneRawDepth = texture(DepthBuffer, texCoord).r;
    float auraRawDepth = gl_FragCoord.z;
    float auraSceneRawDepth = texture(AuraDepthBuffer, texCoord).r;

    // 被場景擋住就不要畫
    if (auraSceneRawDepth != 1.0 && sceneRawDepth < auraSceneRawDepth) {
        discard;
    }

    float sceneDepth = LinearizeDepth(sceneRawDepth);
    float auraDepth = LinearizeDepth(auraRawDepth);
    bool sceneBehindAura = sceneDepth < auraDepth;

    // ------------------------------------------------------------------ 顏色

    vec3 auraColor = vertexColor.rgb;
    // 保險：Color 屬性若沒綁定（舊格式）就用預設桃紅
    if (dot(auraColor, vec3(0.299, 0.587, 0.114)) < 0.02) {
        auraColor = vec3(1.0, 0.12, 0.78);
    }
    vec3 accent = 0.5 + 0.5 * cos(Timer * 0.55 + vec3(0.0, 2.1, 4.2));
    vec3 highlight = mix(vec3(1.0), auraColor, 0.35);

    // ------------------------------------------------------------------ 幾何量

    vec3 viewAngle = normalize(-viewDir);
    float facing = max(dot(viewAngle, normal), 0.0);
    float facingPow = pow(facing, 3.0);
    float edge = 1.0 - facingPow;
    float edgeGlow = pow(edge, 2.5);

    float distScale = 10.0 / max(vertexDistance, 1.0);
    vec2 wobble = vec2(sin(Timer * 0.025), cos(Timer * 0.028));

    // ------------------------------------------------------------------ 折射 / 螺旋

    vec2 refraction = (-normal.rg + 0.2 * wobble) * facingPow * 0.06
                    * sin(Timer / 120.0) * distScale;

    vec2 centered = texCoord - 0.5;
    float swirlAngle = Timer * 0.30 + edge * 2.4;
    mat2 swirlRot = mat2(cos(swirlAngle), -sin(swirlAngle),
                         sin(swirlAngle),  cos(swirlAngle));
    vec2 swirl = (swirlRot * centered - centered) * (0.10 * edge * distScale);

    vec2 uv2 = sceneBehindAura ? texCoord : texCoord + refraction + swirl;
    vec2 caDir = normalize(refraction + swirl + vec2(1.0e-5));

    float caAmount = 0.0035 + 0.0065 * edge;
    vec3 refracted = sceneBehindAura
            ? texture(ScreenBuffer, texCoord).rgb
            : sampleChromatic(uv2, caDir, caAmount);

    // ------------------------------------------------------------------ 交界 / 網格

    float warpedRawDepth = sceneBehindAura ? sceneRawDepth : texture(DepthBuffer, uv2).r;
    float warpedDepth = LinearizeDepth(warpedRawDepth);

    float threshold = 1.5;
    float inter = 1.0 - smoothstep(0.0, threshold, abs(auraDepth - warpedDepth));
    float pulse = 0.65 + 0.35 * sin(Timer * 6.0 + edge * 6.0);
    vec3 interColor = highlight * pow(inter, 2.0) * pulse;

    vec3 viewPos = clipToView(texCoord, sceneBehindAura ? sceneRawDepth : auraRawDepth);
    vec4 worldPosRaw = uInvModelView * vec4(viewPos, 1.0);
    vec3 worldPos = worldPosRaw.xyz + CameraPosition;
    vec3 worldNormal = sceneBehindAura
            ? mat3(uInvModelView) * reconstructNormal(viewPos, texCoord)
            : normal2;

    float lineWidth = 20.0 / max(sceneBehindAura ? sceneDepth : auraDepth, 1.0);
    float g1 = grid(worldPos + vec3(-Timer * 0.10,  Timer * 0.10, -Timer * 0.10),
                    worldNormal, lineWidth, 0.20);
    float g2 = grid(worldPos + vec3( Timer * 0.08,  Timer * 0.08,  Timer * 0.08),
                    worldNormal, lineWidth * 0.8, 0.30);
    float g3 = grid(worldPos + vec3( Timer * 0.03, -Timer * 0.06,  Timer * 0.05),
                    worldNormal, lineWidth * 1.4, 0.55);

    float gridMask = clamp(g1 + g2 * 0.55 + g3 * 0.25, 0.0, 1.0);
    vec3 gridColor = mix(auraColor, accent, 0.45) * gridMask * (0.85 + 0.15 * sin(Timer * 2.0));

    // ------------------------------------------------------------------ 合成

    vec3 base = sceneBehindAura
            ? refracted * 0.55
            : refracted + auraColor * (0.22 * facingPow + 0.08);

    vec3 color = base;
    color += gridColor * 0.95;
    color += interColor * 1.7;
    color += accent * edgeGlow * 0.5 * (0.7 + 0.3 * sin(Timer * 3.0));

    // 內圈能量波紋（由球心往外）
    float ripple = 0.5 + 0.5 * sin(length(centered) * 60.0 - Timer * 4.0);
    color += auraColor * pow(ripple, 6.0) * edge * 0.35;

    // 掃描線 + 雜訊
    float scan = 0.95 + 0.05 * sin(texCoord.y * ScreenSize.y * 1.4 + Timer * 3.0);
    color *= scan;
    color += (valueNoise(gl_FragCoord.xy * 0.5 + fract(Timer) * 97.0) - 0.5) * (2.0 / 255.0);

    // 暗角
    float vignette = 1.0 - smoothstep(0.25, 1.05, length(centered * vec2(1.0, 0.9)));
    color *= mix(0.78, 1.0, vignette);

    fragColor = vec4(max(color, vec3(0.0)), 1.0);
}
