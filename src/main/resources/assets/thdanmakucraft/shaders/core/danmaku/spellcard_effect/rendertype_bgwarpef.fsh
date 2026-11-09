#version 150

#moj_import <fog.glsl>
uniform sampler2D DepthBuffer;
uniform sampler2D ScreenBuffer;
uniform sampler2D AuraDepthBuffer;

uniform vec2 ScreenSize;
uniform float Timer;
uniform float FogStart;
uniform float FogEnd;
uniform vec4 FogColor;
uniform vec3 CameraPosition;

in vec4 vertexColor;
in vec3 normal;
in vec3 normal2;
in vec3 viewDir;
in float vertexDistance;
in mat4 uInvProjection;
in mat4 uInvModelView;
in vec2 texCoord2;

out vec4 fragColor;

//float near = FogStart;
//float far = FogEnd;

float GetCameraDistance(vec2 uv, float rawDepth)
{
    // Raw Depth: [0, 1] -> NDC: [-1, 1]
    vec4 clipPos = vec4(
    uv * 2.0 - 1.0,
    rawDepth * 2.0 - 1.0,
    1.0
    );

    // Reconstruct View Space Position
    vec4 viewPos = uInvProjection * clipPos;

    viewPos.xyz /= viewPos.w;

    // Euclidean Distance
    return length(viewPos.xyz);
}

vec2 uvWarp(vec2 uv, vec2 warpSize){
    vec2 xy = uv;
    xy += warpSize;
    return xy;
}

vec4 overlay(vec4 targetColor, vec4 overlayColor){
    return (1.0-overlayColor.a) * targetColor + overlayColor.a * overlayColor;
}

vec3 clipToView(vec2 uv, float depth) {
    vec4 clipPos = vec4(uv * 2.0 - 1.0, depth * 2.0 - 1.0, 1.0);
    vec4 viewPos = uInvProjection * clipPos;
    return viewPos.xyz / viewPos.w;
}

vec3 fetchViewPos(vec2 uv) {
    if(uv.x < 0.0 || uv.x > 1.0 || uv.y < 0.0 || uv.y > 1.0)
    return vec3(0.0, 0.0, -1000.0);
    float rawDepth = texture(DepthBuffer, uv).r;
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

vec3 getViewPosition(vec2 uv, float depth)
{
    vec4 clipPos = vec4(
    uv * 2.0 - 1.0,
    depth * 2.0 - 1.0,
    1.0
    );

    vec4 viewPos = uInvProjection * clipPos;

    return viewPos.xyz / viewPos.w;
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

void main() {
    vec3 viewAngle = normalize(-viewDir);
    float rimStrength = max(dot(viewAngle, normal),0.0f);
    float rim = pow(rimStrength,3.0f);

    vec2 texCoord = gl_FragCoord.xy/ScreenSize;
    float sencenRawDepth = texture(DepthBuffer, texCoord).r;
    float aruaRawDepth = gl_FragCoord.z;
    float arua2RawDepth = texture(AuraDepthBuffer, texCoord).r;
    float senceDepth = GetCameraDistance(texCoord,sencenRawDepth);
    float aruaDepth = GetCameraDistance(texCoord,aruaRawDepth);

    if(arua2RawDepth != 1.0f && sencenRawDepth < arua2RawDepth){
        discard;
    }

    float distance = abs(aruaDepth-senceDepth);
    float threshold = 5.0f;
    float normalizedDistance = clamp(distance / threshold, 0.0, 1.0);
    vec4 intersection = mix(vec4(1), vec4(0), pow(normalizedDistance,2)); // white to transparent gradient
    float inversedIntersection = 1.0f-intersection.r;

    vec2 warp = vec2(sin(Timer/40.0f),cos(Timer/36.0f));
    bool flag = senceDepth < aruaDepth;
    vec2 uv2 = flag ? texCoord : uvWarp(texCoord,(-normal.rg+0.2*warp)*rim*0.6*sin(Timer/120.0f)/ (vertexDistance/10.0f) *inversedIntersection*0.4f);
    float WarpedSenceDepthRaw = texture(DepthBuffer, uv2).r;
    float WarpedSenceDepth = GetCameraDistance(texCoord,WarpedSenceDepthRaw);
    gl_FragDepth = WarpedSenceDepth;
    flag = WarpedSenceDepth < aruaDepth;

    vec4 screen_color = texture(ScreenBuffer, mix(texCoord,uv2,aruaRawDepth));
    vec4 intersection2 = mix(vec4(1), vec4(0), pow(clamp(abs(aruaDepth-WarpedSenceDepth) / threshold, 0.0, 1.0),3));
    vec4 ccc = vec4(vertexColor.rgb*vertexColor.a*rim*(1.0f-intersection2.r),rim*(1.0f-intersection2.r));
    vec4 aruaColor = screen_color + (1.0f-screen_color) * ccc * 0.6f;

    vec4 intersectionColor = vec4(vec3(1.0f,0.0f,0.5f)*intersection2.a,intersection2.a*0.6);

    float rawDepth = texture(DepthBuffer, texCoord).r;
    vec3 viewPos = clipToView(texCoord, flag ? rawDepth : aruaRawDepth);
    vec4 worldPosRaw = uInvModelView * vec4(viewPos, 1.0);
    vec3 worldPos = worldPosRaw.xyz + CameraPosition;
    vec3 worldNormal = flag ? mat3(uInvModelView) * reconstructNormal(viewPos, texCoord) : normal2;
    float aaa = 16.0f/(flag ? WarpedSenceDepth : aruaDepth);
    float squareSize = 0.4f;
    float gridVal = grid(worldPos+vec3(-Timer * 0.1,Timer * 0.1,-Timer * 0.1), worldNormal, aaa, squareSize);
    float gridVal2 = grid(worldPos+vec3(Timer * 0.08,Timer * 0.08,Timer * 0.08), worldNormal, aaa, squareSize);
    float pulse = sin(Timer/10.0f) * 0.2 + 0.8;
    vec4 lineColor = vec4(vec3(vertexColor.rgb)*(gridVal+gridVal2*0.4f)*0.6f * pulse,1.0f);
    vec4 backgroundColor = screen_color*vec4(vec3(min(1.0f,0.5f+intersection2.a)),1.0f);
    vec4 outColor = (flag ? backgroundColor : aruaColor);
    outColor = outColor + (1.0f-outColor) * lineColor;
    outColor = outColor + (1.0f-outColor) * intersectionColor*0.6f;

    fragColor = outColor;
    //fragColor = linear_fog(fragColor, distance, FogStart, FogEnd, FogColor);
    //fragColor = vec4(arua2RawDepth);

    //fragColor = vec4(0.0f);
    //fragColor = vec4(vec3(senceDepth),1.0f);
}

