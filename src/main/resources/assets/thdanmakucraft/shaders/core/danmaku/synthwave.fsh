#version 150

uniform float U_GameTime;
uniform vec2 ScreenSize;
uniform sampler2D MainDepthSampler;
uniform mat4 U_InverseProjectionMatrix;
uniform mat4 U_InverseViewMatrix;
uniform vec3 U_CameraPosition;

in vec2 texCoord;
out vec4 fragColor;

vec3 clipToView(vec2 uv, float depth) {
    vec4 clipPos = vec4(uv * 2.0 - 1.0, depth * 2.0 - 1.0, 1.0);
    vec4 viewPos = U_InverseProjectionMatrix * clipPos;
    return viewPos.xyz / viewPos.w;
}

vec3 fetchViewPos(vec2 uv) {
    if(uv.x < 0.0 || uv.x > 1.0 || uv.y < 0.0 || uv.y > 1.0)
    return vec3(0.0, 0.0, -1000.0);
    float rawDepth = texture(MainDepthSampler, uv).r;
    return clipToView(uv, rawDepth);
}

// 重建法线
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

float synthwaveSun(vec2 uv, float time) {
    float val = smoothstep(0.3, 0.29, length(uv));
    float bloom = smoothstep(0.5, 0.0, length(uv));
    float cut = 5.0 * sin((uv.y + time * 0.2) * 60.0);
    cut += clamp(uv.y * 15.0, -6.0, 6.0);
    cut = clamp(cut, 0.0, 1.0);
    return clamp(val * cut, 0.0, 1.0) + bloom * 0.6;
}

void main() {
    float rawDepth = texture(MainDepthSampler, texCoord).r;

    vec3 synthColor = vec3(0.0);
    float pixelMetricForMask = 0.0;

    if (rawDepth >= 1.0) {
        //  渲染天空
        vec3 viewPos = clipToView(texCoord, 1.0);
        vec3 worldViewDir = mat3(U_InverseViewMatrix) * normalize(viewPos);
        worldViewDir = normalize(worldViewDir);

        vec3 sunDir = normalize(vec3(-1.0, 0.15, 0.0));
        float facingSun = dot(worldViewDir, sunDir);
        float sunShape = 0.0;
        vec2 sunUV = vec2(0.0);
        if(facingSun > 0.0) {
            vec3 sunRight = normalize(cross(sunDir, vec3(0.0, 1.0, 0.0)));
            vec3 sunUp = cross(sunRight, sunDir);
            sunUV = vec2(dot(worldViewDir, sunRight), dot(worldViewDir, sunUp)) * 1.0;
            sunShape = synthwaveSun(sunUV, U_GameTime * 1.0);
        }

        vec3 sunColorTop = vec3(1.0, 0.8, 0.0);
        vec3 sunColorBottom = vec3(1.0, 0.0, 0.5);
        vec3 finalSunColor = mix(sunColorBottom, sunColorTop, smoothstep(-0.3, 0.3, sunUV.y));

        float skyGradient = texCoord.y;
        vec3 skyTop = vec3(0.05, 0.0, 0.1);
        vec3 skyBottom = vec3(0.4, 0.0, 0.4);
        vec3 skyColor = mix(skyBottom, skyTop, skyGradient);
        skyColor += finalSunColor * sunShape;

        synthColor = skyColor;

    } else {
        // 渲染场景
        vec3 viewPos = clipToView(texCoord, rawDepth);

        vec3 normal = reconstructNormal(viewPos, texCoord);
        vec3 viewDir = normalize(-viewPos);
        float NdotV = dot(normal, viewDir);

        vec4 worldPosRaw = U_InverseViewMatrix * vec4(viewPos, 1.0);
        vec3 worldPos = worldPosRaw.xyz;
        worldPos += U_CameraPosition;

        vec3 worldNormal = mat3(U_InverseViewMatrix) * normal;
        worldNormal = normalize(worldNormal);

        vec3 baseColor = vec3(0.08, 0.05, 0.15);

        //边缘光
        float rimIntensity = 1.0 - max(NdotV, 0.0);
        rimIntensity = pow(rimIntensity, 3.0);
        vec3 rimColor = vec3(0.0, 1.0, 1.0) * rimIntensity;

        vec3 absN = abs(worldNormal);
        float axisAlignment = max(max(absN.x, absN.y), absN.z);
        float flatSurfaceMask = smoothstep(0.9, 0.98, axisAlignment);

        float gridVal = 0.0;
        if (flatSurfaceMask > 0.01) {
            // 网格移动
            worldPos.x -= U_GameTime * 2;
            worldPos.yz += U_GameTime * 2;
            gridVal = grid(worldPos, worldNormal, 3.0, 0.5);
            float depthDelta = fwidth(viewPos.z);
            float edgeDisconnectMask = 1.0 - smoothstep(0.2, 0.8, depthDelta);
            gridVal *= edgeDisconnectMask;
            gridVal *= flatSurfaceMask;
        }
        // 网格呼吸效果
        float pulse = sin(U_GameTime * 2.0) * 0.4 + 0.6;
        gridVal *= pulse;

        vec3 gridColorVec = vec3(1.0, 0.0, 0.8) * gridVal * 2.0;

        float dist = length(viewPos);
        float fogFactor = exp(-dist * 0.015);
        fogFactor = clamp(fogFactor, 0.0, 1.0);

        vec3 finalColor = baseColor + rimColor + gridColorVec;
        vec3 fogColor = vec3(0.1, 0.0, 0.2);
        finalColor = mix(fogColor, finalColor, fogFactor);

        synthColor = finalColor;
    }

    // 扩散
    float timeCycle = U_GameTime;

    float baseSpeed = 1.0;
    float exponent = 5.0;
    float mainRadius = pow(timeCycle * baseSpeed, exponent);

    if(timeCycle < 0.1) mainRadius = 0.0;

    float alphaMask = 0.0;
    float scanMetric = 0.0;
    float scanThreshold = 0.0;
    float scanSoftness = 0.0;

    if (rawDepth >= 1.0) {
        // 天空部分的扫描
        float skyStartRad = 0.0;
        float skyEndRad = 1500.0;
        float skyProgress = smoothstep(skyStartRad, skyEndRad, mainRadius);

        vec3 skyViewDir = normalize(clipToView(texCoord, 1.0));
        float pixelHeightY = skyViewDir.y;

        float currentThresholdY = mix(-0.8, 1.1, skyProgress);

        scanSoftness = 0.15;
        alphaMask = 1.0 - smoothstep(currentThresholdY, currentThresholdY + scanSoftness, pixelHeightY);

        scanMetric = pixelHeightY;
        scanThreshold = currentThresholdY;

    } else {
        // 场景部分的扫描逻辑
        vec3 viewPos = clipToView(texCoord, rawDepth);
        float pixelDistance = length(viewPos);

        scanSoftness = 10.0;
        alphaMask = 1.0 - smoothstep(mainRadius, mainRadius + scanSoftness, pixelDistance);

        scanMetric = pixelDistance;
        scanThreshold = mainRadius;
    }

    float scanWidthOuter = (rawDepth >= 1.0) ? scanSoftness + 0.05 : scanSoftness + 2.0;
    float scanWidthInner = (rawDepth >= 1.0) ? 0.02 : 1.0;

    float scanLine = smoothstep(scanThreshold, scanThreshold + scanSoftness, scanMetric) * smoothstep(scanThreshold + scanWidthOuter, scanThreshold + scanWidthInner, scanMetric);

    vec3 scanColor = vec3(0.5, 1.0, 1.2) * scanLine * 3.0;

    synthColor += scanColor;

    fragColor = vec4(synthColor, alphaMask);
}