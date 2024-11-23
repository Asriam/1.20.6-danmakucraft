class Vec3 {

    x: number;
    y: number;
    z: number;

    constructor(x: number, y: number, z: number);

    vectorTo(vec3: Vec3): Vec3;

    normalize(): Vec3;

    dot(vec3: Vec3): Vec3;

    cross(vec3: Vec3): Vec3;

    subtract(vec3: Vec3): Vec3;

    subtract(x: number, y: number, z: number): Vec3;

    add(vec3): Vec3;

    add(x, y, z): Vec3;

    distanceTo(vec3): number;

    distanceToSqr(vec3): number;

    distanceToSqr(x, y, z): number;

    scale(num): Vec3;

    reverse(): Vec3;

    multiply(vec3): Vec3;

    multiply(x, y, z): Vec3;

    length(): number;

    lengthSqr(): number;

    horizontalDistance(): number;

    horizontalDistanceSqr(): number;

    equals(vec3: Vec3): boolean;

    lerp(vec3: Vec3, pct: number): Vec3;

    xRot(num: number): Vec3;

    yRot(num: number): Vec3;

    zRot(num: number): Vec3;

    directionFromRotation(vec2: Vec2): Vec3;

    directionFromRotation(xRot: number, yRot: number): Vec3;

    x(): number;

    y(): number;

    z(): number;
}



