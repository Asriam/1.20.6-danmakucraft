class Vec2 {
    x : number;
    y : number;

    constructor(x: number, y: number, z: number);

    scale(num: number): Vec2;

    dot(vec2 : Vec2): Vec2;

    add(vec2 : Vec2): Vec2;

    add(num : number): Vec2;

    equals(): boolean;

    normalized(): Vec2;

    length(): number;

    lengthSquared(): number;

    distanceToSqr(vec2 : Vec2): number;

    negated(): number;
}