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

out vec4 fragColor;

float near = -1.0f;
float far = 1000.0f;

float LinearizeDepth(float depth) {
    float zNdc = 2 * depth - 1;
    float zEye = (2 * far * near) / ((far + near) - zNdc * (far - near));
    float linearDepth = (zEye - near) / (far - near);
    return linearDepth;
}

vec2 uvWarp(vec2 uv, vec2 warpSize){
    vec2 xy = uv;
    xy += warpSize;
    return xy;
}

void main() {
    vec3 viewAngle = normalize(-viewDir);
    float rimStrength = max(dot(viewAngle, normal),0.0f);
    float rim = pow(rimStrength,6.0f);

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
    vec2 uv2 = uvWarp(texCoord,(-normal.rg+0.2*vec2(sin(Timer/40.0f),cos(Timer/36.0f)))*rim*0.6*sin(Timer/120.0f)/ (vertexDistance/20.0f));
    float depth = texture(DepthBuffer, uv2).r;

    if(texture(DepthBuffer, texCoord).r < gl_FragCoord.z ){
        discard;
    }

    vec4 screen_color = texture(ScreenBuffer, uv2);
    vec4 outColor = max(linear_fog(screen_color, depth, FogStart, FogEnd, FogColor),0) + vec4(vertexColor.rgb*vertexColor.a*rim,rim);
    //fragColor = linear_fog(outColor, depth, FogStart, FogEnd, FogColor);
    fragColor = outColor;
}
