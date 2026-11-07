#version 150

#moj_import <fog.glsl>
uniform sampler2D DepthBuffer;
uniform sampler2D ScreenBuffer;

uniform vec2 ScreenSize;
uniform float Timer;
uniform float FogStart;
uniform float FogEnd;
uniform vec4 FogColor;
uniform vec3 CameraPosition;

in vec4 vertexColor;
in vec3 normal;
in vec3 viewDir;
in float vertexDistance;
in float near;
in float far;
in mat4 uInvProjection;
in mat4 uInvModelView;
in vec2 texCoord2;

out vec4 fragColor;

//float near = FogStart;
//float far = FogEnd;

float LinearizeDepth(float depth) {
    float z = depth*2.0-1.0f;
    return (2.0f*near*far)/(far+near-z*(far-near));
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
    float senceDepth = LinearizeDepth(sencenRawDepth);
    float aruaDepth = LinearizeDepth(aruaRawDepth);
    /*if(senceDepth < aruaDepth){
        discard;
    }*/

    float distance = abs(aruaDepth-senceDepth);
    float threshold = 2.5f+sin(Timer/60.0f);
    float normalizedDistance = clamp(distance / threshold, 0.0, 1.0);
    vec4 intersection = mix(vec4(1), vec4(0), pow(normalizedDistance,3)); // white to transparent gradient
    float inversedIntersection = 1.0f-intersection.r;

    vec2 warp = vec2(sin(Timer/40.0f),cos(Timer/36.0f));
    vec2 uv2 = uvWarp(texCoord,(-normal.rg+0.2*warp)*rim*0.6*sin(Timer/120.0f)/ (vertexDistance/10.0f) *inversedIntersection*0.4f);
    float WarpedSenceDepth = LinearizeDepth(texture(DepthBuffer, uv2).r);

    vec4 screen_color = texture(ScreenBuffer, mix(texCoord,uv2,aruaRawDepth));
    vec4 intersection2 = mix(vec4(1), vec4(0), pow(clamp(abs(aruaDepth-WarpedSenceDepth) / threshold, 0.0, 1.0),3));
    vec4 outColor = overlay(FogColor,screen_color) + vec4(vertexColor.rgb*vertexColor.a*rim*(1.0f-intersection2.r),rim*(1.0f-intersection2.r));

    vec4 intersectionColor = vec4(vec3(1.0f,0.0f,0.5f)*intersection.a,intersection.a*0.6);

    if(senceDepth < aruaDepth){
        float rawDepth = texture(DepthBuffer, texCoord).r;
        vec3 viewPos = clipToView(texCoord, rawDepth);
        vec4 worldPosRaw = uInvModelView * vec4(viewPos, 1.0);
        vec3 worldPos = worldPosRaw.xyz + CameraPosition;
        worldPos.x -= Timer * 0.1;
        worldPos.yz += Timer * 0.1;

        vec3 worldNormal = mat3(uInvModelView) * reconstructNormal(viewPos, texCoord);
        float gridVal = grid(worldPos, worldNormal, 20.0f/senceDepth, 0.1);
        vec4 lineColor = vec4(vec3(vertexColor.rgb)*gridVal*0.5f,1.0f);
        vec4 backgroundColor = screen_color*vec4(vec3(0.5f),1.0f);
        fragColor = backgroundColor + lineColor;
    }else {
        vec3 viewPos = clipToView(texCoord, aruaRawDepth);
        vec4 worldPosRaw = uInvModelView * vec4(viewPos, 1.0);
        vec3 worldPos = worldPosRaw.xyz + CameraPosition;
        worldPos.x -= Timer * 0.1;
        worldPos.yz += Timer * 0.1;

        float gridVal2 = grid(worldPos, vec3(0.0f), 20.0f/aruaDepth, 0.1);
        vec4 lineColor = vec4(vec3(vertexColor.rgb)*gridVal2*0.5f,1.0f);
        fragColor = outColor + lineColor;
    }
    fragColor += intersectionColor;

    //fragColor = vec4(0.0f);
    //fragColor = vec4(vec3(senceDepth),1.0f);
}

