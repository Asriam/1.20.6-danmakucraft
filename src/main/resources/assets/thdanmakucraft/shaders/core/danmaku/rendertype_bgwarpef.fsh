#version 150

#moj_import <fog.glsl>
uniform sampler2D DepthBuffer;
uniform sampler2D ScreenBuffer;

uniform vec2 ScreenSize;
uniform float Timer;
uniform float FogStart;
uniform float FogEnd;
uniform vec4 FogColor;

in vec4 vertexColor;
in vec3 normal;
in vec3 viewDir;
in float vertexDistance;
in float near;
in float far;

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

void main() {
    vec3 viewAngle = normalize(-viewDir);
    float rimStrength = max(dot(viewAngle, normal),0.0f);
    float rim = pow(rimStrength,3.0f);

    /*if (vertexDistance > FogEnd){
        discard;
    }
    vec3 viewAngle = normalize(-viewDir);
    vec2 texCoord = gl_FragCoord.xy/ScreenSize;
    //vec4 screenColor = texture(ScreenBuffer, texCoord);
    // The more orthogonal the camera is to the fragment, the stronger the rim light.
    // abs() so that the back faces get treated the same as the front, giving a rim effect.
    float rimStrength = 1 - max(dot(viewAngle, normal),0.0f); // The more orthogonal, the stronger
    //float rimFactor = pow(rimStrength, 1.0f)*1.6; // higher power = sharper rim light
    float rimFactor  = pow(rimStrength-0.0f, 2.0f) ; // higher power = sharper rim light
    float rimFactor2 = pow(rimStrength+1.0f-0.0f, 2.0f) ; // higher power = sharper rim light

    //float rimFactor = (-cos(pow(min(rimStrength,0.5f)/0.5f,3.0f)*3.1415926)+1)/2;
    vec4 rim  = vec4(rimFactor);
    vec4 rim2 = vec4(rimFactor2);

    // - Create the intersection line -
    // Turn frag coord from screenspace -> NDC, which corresponds to the UV
    float sceneDepth  = LinearizeDepth(texture(DepthBuffer, texCoord).r);
    //float sceneDepth  = 1.0f;
    float bubbleDepth = LinearizeDepth(gl_FragCoord.z);

    float distance = abs(bubbleDepth - sceneDepth); // linear difference in depth

    float threshold = 0.01f;
    float normalizedDistance = clamp(distance / threshold, 0.0, 1.0); // [0, threshold] -> [0, 1]

    vec4 intersection = mix(vec4(1), vec4(0), pow(normalizedDistance,2)); // white to transparent gradient

    //vec4 bubbleBase = vertexColor;
    vec4 outColor = vec4(normal, 1.0f);
    outColor = vec4(0.5f,0.5f,0.5f, 1.0f);

    if(outColor.a < 0.05f){
        discard;
    }

    fragColor = linear_fog(outColor, vertexDistance, FogStart, FogEnd, FogColor);
    //fragColor =  vec4(normal, 1.0f);*/
    vec2 texCoord = gl_FragCoord.xy/ScreenSize;
    float senceDepth = texture(DepthBuffer, texCoord).r;
    if(senceDepth < gl_FragCoord.z ){
        discard;
    }
    float distance = abs(LinearizeDepth(gl_FragCoord.z)-LinearizeDepth(senceDepth));
    float threshold = 2.0f+sin(Timer/60.0f);
    float normalizedDistance = clamp(distance / threshold, 0.0, 1.0);
    vec4 intersection = mix(vec4(1), vec4(0), pow(normalizedDistance,3)); // white to transparent gradient

    float a = normalizedDistance;
    vec2 warp = vec2(sin(Timer/40.0f),cos(Timer/36.0f));
    vec2 uv2 = uvWarp(texCoord,(-normal.rg+0.2*warp)*rim*0.6*sin(Timer/120.0f)/ (vertexDistance/10.0f)*(1.0f-intersection.r)*0.4f);
    float depth = texture(DepthBuffer, uv2).r;

    vec4 screen_color = texture(ScreenBuffer, uv2);
    vec4 outColor = max(linear_fog(screen_color, LinearizeDepth(depth), FogStart, FogEnd, FogColor),0) + vec4(vertexColor.rgb*vertexColor.a*rim*(1.0f-intersection.r),rim*(1.0f*intersection.r));
    //fragColor = linear_fog(outColor, depth, FogStart, FogEnd, FogColor);
    fragColor = outColor;
    //fragColor = intersection;
}
