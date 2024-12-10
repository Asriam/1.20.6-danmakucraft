function onTick(object) {
    object.setVelocityFromRotation(0.2,
        new Vec2(
            360 / 16 + 60 * Math.cos(object.getTimer() * 0.1),
            360 / 16 + 60 * Math.sin(object.getTimer() * 0.1)
        ),
        true,
        true);
}

//new Vec3(1.0,1.0,1.0).add();