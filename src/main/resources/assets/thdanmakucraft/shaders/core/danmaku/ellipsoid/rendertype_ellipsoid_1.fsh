#version 150

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;

in vec3 EllipsoidPosition;
in vec3 EllipsoidRotation;
in vec3 EllipsoidRadius;

in vec3 vRayDirection;

out vec4 fragColor;

mat3 rotationMatrix(vec3 r)
{
    vec3 c = cos(r);
    vec3 s = sin(r);

    mat3 Rx = mat3(
    1.0, 0.0, 0.0,
    0.0, c.x, s.x,
    0.0, -s.x, c.x
    );

    mat3 Ry = mat3(
    c.y, 0.0, -s.y,
    0.0, 1.0, 0.0,
    s.y, 0.0, c.y
    );

    mat3 Rz = mat3(
    c.z, s.z, 0.0,
    -s.z, c.z, 0.0,
    0.0, 0.0, 1.0
    );

    return Rz * Ry * Rx;
}

void main()
{
    // Object Rotation
    mat3 R = rotationMatrix(EllipsoidRotation);

    // Transform Position to View Space
    vec3 centerVS = (ModelViewMat *vec4(EllipsoidPosition, 1.0)).xyz;

    // Camera Ray
    vec3 roVS = vec3(0.0);
    vec3 rdVS = normalize(vRayDirection);

    // View -> Ellipsoid Local
    vec3 roLocal = transpose(R) * (roVS - centerVS);
    vec3 rdLocal = transpose(R) * rdVS;

    // Ellipsoid -> Unit Sphere
    vec3 ro = roLocal / EllipsoidRadius;
    vec3 rd = rdLocal / EllipsoidRadius;

    // Ray Intersection
    float b = dot(ro, rd);
    float c = dot(ro, ro) - 1.0;

    float h = b * b - c;

    if (h < 0.0)
    discard;

    h = sqrt(h);

    float t = -b - h;

    if (t < 0.0)
    t = -b + h;

    if (t < 0.0)
    discard;

    // Local Hit Position
    vec3 hitLocal = roLocal + rdLocal * t;

    // Local Normal
    vec3 normalLocal = normalize(
    hitLocal /
    (EllipsoidRadius * EllipsoidRadius)
    );

    // Local -> View Normal
    vec3 normalVS = normalize(R * normalLocal);

    // Local -> View Hit Position
    vec3 hitVS = centerVS + R * hitLocal;

    // Depth
    vec4 clipPos = ProjMat * vec4(hitVS, 1.0);

    float depth = clipPos.z / clipPos.w;

    gl_FragDepth = depth * 0.5 + 0.5;

    fragColor = vec4(vec3(gl_FragDepth), 1.0f);

    //fragColor = vec4(1.0f);
}
