#version 150

#moj_import <thdanmakucraft:rgb_to_hsv.glsl>

uniform sampler2D Sampler0;

uniform vec4 ColorModulator;
uniform vec2 ScreenSize;

in vec4 vertexColor;
in vec4 vertexColor2;
in vec2 texCoord0;

//in vec3 viewDir;

out vec4 fragColor;

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
