#version 150

uniform sampler2D DepthBuffer;
uniform vec2 ScreenSize;
//uniform float Near;
//uniform float Far;

in vec3 normal;
in vec3 viewDir;
in vec4 vertexColor;
in vec4 coreColor;
in vec2 vertCoord;

out vec4 fragColor;

/*
float near = Near;
float far = Far;
*/

float near = -1.0f;
float far = 1000.0f;

float LinearizeDepth(float depth) {
    float zNdc = 2 * depth - 1;
    float zEye = (2 * far * near) / ((far + near) - zNdc * (far - near));
    float linearDepth = (zEye - near) / (far - near);
    return linearDepth;
}

/*
vec4 Overlay(vec4 targetColor, vec4 overlayColor){
    return (1-overlayColor.a) * targetColor + overlayColor.a * overlayColor;
}*/

void main() {
    float coreSize = vertCoord.x;
    float feather  = vertCoord.y;

    vec3 viewAngle = normalize(-viewDir);

    vec2 texCoord = gl_FragCoord.xy/ScreenSize;
    // The more orthogonal the camera is to the fragment, the stronger the rim light.
    // abs() so that the back faces get treated the same as the front, giving a rim effect.
    float rimStrength = 1 - max(dot(viewAngle, normal),0.0f); // The more orthogonal, the stronger

    //float rimFactor = pow(rimStrength, 1.0f)*1.6; // higher power = sharper rim light
    float rimFactor  = pow(max(rimStrength-feather,0.0f), 2) ; // higher power = sharper rim light
    float rimFactor2 = pow(rimStrength+1.0f-coreSize, 2) ; // higher power = sharper rim light

    //float rimFactor = (-cos(pow(min(rimStrength,0.5f)/0.5f,3.0f)*3.1415926)+1)/2;
    vec4 rim  = vec4(rimFactor);
    vec4 rim2 = vec4(rimFactor2);

    // - Create the intersection line -
    // Turn frag coord from screenspace -> NDC, which corresponds to the UV
    float sceneDepth  = LinearizeDepth(texture(DepthBuffer, texCoord).r);
    float bubbleDepth = LinearizeDepth(gl_FragCoord.z);

    float distance = abs(bubbleDepth - sceneDepth); // linear difference in depth

    float threshold = 0.01f;
    float normalizedDistance = clamp(distance / threshold, 0.0, 1.0); // [0, threshold] -> [0, 1]

    vec4 intersection = mix(vec4(1), vec4(0), pow(normalizedDistance,2)); // white to transparent gradient

    vec4 bubbleBase = vertexColor;
    //fragColor = max(vec4(vec3(1.0-(clamp(rim,0.0f,1.0f)+max(intersection,0.0f))),1.0f),0.0f)*1.6f*bubbleBase + max(vec4(1.0-(rim2+clamp(intersection,0.0f,1.0f))),0.0f)*1.6f*(coreColor*2.0f-1.0f);
    fragColor = max(vec4(1.0-(clamp(rim,0.0f,1.0f)+max(intersection,0.0f))),0.0f)*1.6f*bubbleBase + max(vec4(1.0-(rim2+clamp(intersection,0.0f,1.0f))),0.0f)*1.6f*(coreColor*2.0f-1.0f);
    //fragColor = vec4(normal,fragColor.a);
    //fragColor = vec4(vec3(sceneDepth),1.0f);
    //fragColor = texture(DepthBuffer, texCoord)*2.0f;
}
