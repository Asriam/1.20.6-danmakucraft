#version 150

uniform sampler2D DepthBuffer;

in vec3 normal;
in vec3 viewDir;
in vec4 vertexColor;

uniform vec2 ScreenSize;

out vec4 fragColor;

float near = 1.0f;
float far = 1000.0f;

float LinearizeDepth(float depth) {
    float zNdc = 2 * depth - 1;
    float zEye = (2 * far * near) / ((far + near) - zNdc * (far - near));
    float linearDepth = (zEye - near) / (far - near);
    return linearDepth;
}

vec4 Overlay(vec4 targetColor, vec4 overlayColor){
    return (1-overlayColor.a) * targetColor + overlayColor.a * overlayColor;
}

void main() {
    vec3 viewAngle = normalize(-viewDir);

    vec2 texCoord = gl_FragCoord.xy/ScreenSize;
    // The more orthogonal the camera is to the fragment, the stronger the rim light.
    // abs() so that the back faces get treated the same as the front, giving a rim effect.
    float rimStrength = 1 - abs(dot(viewAngle, normal)); // The more orthogonal, the stronger

    float rimFactor = pow(rimStrength, 1.0f)*1.6; // higher power = sharper rim light

    //float rimFactor = (-cos(pow(min(rimStrength,0.5f)/0.5f,3.0f)*3.1415926)+1)/2;
    vec4 rim = vec4(rimFactor);

    // - Create the intersection line -
    // Turn frag coord from screenspace -> NDC, which corresponds to the UV
    float sceneDepth = LinearizeDepth(texture(DepthBuffer, texCoord).r);
    float bubbleDepth = LinearizeDepth(gl_FragCoord.z);

    float distance = abs(bubbleDepth - sceneDepth); // linear difference in depth

    float threshold = 0.005f;
    float normalizedDistance = clamp(distance / threshold, 0.0, 1.0); // [0, threshold] -> [0, 1]

    vec4 intersection = mix(vec4(1), vec4(0), pow(normalizedDistance,2.0f)); // white to transparent gradient

    vec4 bubbleBase = vertexColor;
    vec4 coreColor = vec4(1.0f,1.0f,1.0f,bubbleBase.a);
    //fragColor = Overlay(vec4(1.0f,1.0f,1.0f,1.0f),bubbleBase*(rim));
    fragColor = vec4(1.0f,1.0f,1.0f,5.0f)*bubbleBase + max(vec4(vec3(1.0-(rim+max(intersection,0.0f))),1.0f),0.0f);
    fragColor.a *= bubbleBase.a;
    //fragColor = vertexColor;
    //fragColor = intersection;
    //fragColor = vec4(texture(DepthBuffer,texCoord).rgb, 1.0f);

   // fragColor = vec4(vec3(bubbleDepth),1.0f);
}
