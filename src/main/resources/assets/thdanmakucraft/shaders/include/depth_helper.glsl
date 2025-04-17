#version 120

float LinearizeDepth(float depth, float near, float far) {
    float z = depth*2.0-1.0f;
    return (2.0f*near*far)/(far+near-z*(far-near));
}
