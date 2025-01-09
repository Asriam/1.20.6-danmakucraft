#version 120

uniform sampler2D DiffuseTexutre;

in vec3 normal;
in vec3 viewDir;
in vec4 vertexColor;
in vec2 vertCoord;

#define RAYMARCH_STEP 40

float Far = 200.0f;
float Near = 0.001f;

float boxSDF(vec3 positon, vec3 size){
    vec3 q = abs(positon) - size;
    return length(max(q,0.0f)) + min(max(q.x,max(q.y,q.z)),0.0f);
}

vec4 raymarch(vec3 from, vec3 increment){
    float dist = 0.0;

    float lastDistEval = 1e10;
    float edge = 0.0;

    for(int i = 0; i < RAYMARCH_STEP; i++) {
        vec3 pos = (from + increment * dist);
        float distEval = distf(pos);

        if (lastDistEval < EDGE_WIDTH && distEval > lastDistEval + 0.001) {
            edge = 1.0;
        }

        if (distEval < Near) {
            break;
        }

        dist += distEval;
        if (distEval < lastDistEval) lastDistEval = distEval;
    }

    float mat = 1.0;
    if (dist >= Far) mat = 0.0;

    return vec4(dist, mat, edge, 0);
}

void main() {

}
