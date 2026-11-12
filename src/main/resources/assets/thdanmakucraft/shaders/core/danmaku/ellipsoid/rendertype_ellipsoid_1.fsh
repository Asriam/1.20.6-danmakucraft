#version 150

#moj_import <thdanmakucraft:ray_casting_helper.glsl>
uniform sampler2D DepthBuffer;

uniform vec2 ScreenSize;
uniform mat4 ModelViewMat;
uniform mat4 ProjMat;

in vec3 EllipsoidPosition;
in vec3 EllipsoidRotation;
in vec3 EllipsoidRadius;
in vec4 BulletColor;
in vec4 CoreColor;
in vec3 vRayDirection;
in mat4 inversedPojMat;
in vec4 params;
flat in int ShapeType;

out vec4 fragColor;

vec3 clipToView(vec2 uv, float depth) {
    vec4 clipPos = vec4(uv * 2.0 - 1.0, depth * 2.0 - 1.0, 1.0);
    vec4 viewPos = inversedPojMat * clipPos;
    return viewPos.xyz / viewPos.w;
}

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

void main()
{
    vec2 texCoord = gl_FragCoord.xy/ScreenSize;
    UvDepthNormal e1;
    switch (ShapeType) {
        case 1: e1 = RayCastingElliposid(ModelViewMat, ProjMat, vRayDirection, EllipsoidPosition, EllipsoidRotation, EllipsoidRadius, true) ; break;
        case 2: e1 = RayCastingElliposid(ModelViewMat, ProjMat, vRayDirection, EllipsoidPosition, EllipsoidRotation, EllipsoidRadius, true); break;
        case 3: e1 = RayCastingCube(ModelViewMat, ProjMat, vRayDirection, EllipsoidPosition, EllipsoidRotation, EllipsoidRadius, true); break;
        case 4: e1 = RayCastingStar(ModelViewMat, ProjMat, vRayDirection, EllipsoidPosition, EllipsoidRotation, EllipsoidRadius, 0.55f, 0.85f, true); break;
        default : e1 = RayCastingElliposid(ModelViewMat, ProjMat, vRayDirection, EllipsoidPosition, EllipsoidRotation, EllipsoidRadius, true) ; break;
    }
    gl_FragDepth = e1.depth;
    vec3 viewAngle = normalize(-vRayDirection);
    float rimStrength = 1 - max(dot(viewAngle, e1.normal),0.0f);
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
}

