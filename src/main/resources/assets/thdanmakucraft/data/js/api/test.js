function onTick(object) {
    object.setVelocity(0.2, new Vec2(0.0, 1 * 360 / 16 + 60 * Math.cos(object.getTimer() * 0.3)), true, true);
    //print("aaaaaaaaaaaaaaaaaaaaaaaaaa");
}

