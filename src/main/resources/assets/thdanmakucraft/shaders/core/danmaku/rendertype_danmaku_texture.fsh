#version 150

uniform sampler2D Sampler0;

uniform vec4 ColorModulator;
uniform vec2 ScreenSize;

in vec4 vertexColor;
in vec4 vertexColor2;
in vec2 texCoord0;

//in vec3 viewDir;

out vec4 fragColor;

vec3 RGBtoHSV(vec3 c) {
    vec4 K = vec4(0.0, -1.0 / 3.0, 2.0 / 3.0, -1.0);
    vec4 p = mix(vec4(c.bg, K.wz), vec4(c.gb, K.xy), step(c.b, c.g));
    vec4 q = mix(vec4(p.xyw, c.r), vec4(c.r, p.yzx), step(p.x, c.r));
    float d = q.x - min(q.w, q.y);
    float e = 1.0e-10;
    return vec3(abs(q.z + (q.w - q.y) / (6.0 * d + e)), d / (q.x + e), q.x);
}

vec3 HSVtoRGB(vec3 c) {
    vec4 K = vec4(1.0, 2.0 / 3.0, 1.0 / 3.0, 3.0);
    vec3 p = abs(fract(c.xxx + K.xyz) * 6.0 - K.www);
    return c.z * mix(K.xxx, clamp(p - K.xxx, 0.0, 1.0), c.y);
}

void main() {

    vec4 color = texture(Sampler0, texCoord0);
    if (color.a < 0.1) {
        discard;
    }

    vec3 hsv = RGBtoHSV(color.rgb);
    vec3 bulletHSV = RGBtoHSV(vertexColor2.rgb);
    hsv.x = bulletHSV.x;
    hsv.y *= bulletHSV.y;

    vec3 finalColor = HSVtoRGB(hsv);

    fragColor = vec4(finalColor, color.a) * vertexColor;
}
