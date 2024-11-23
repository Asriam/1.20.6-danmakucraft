var Mth = Java.type('net.minecraft.util.Mth');
var Vec2 = Java.type('net.minecraft.world.phys.Vec2');
var Vec3 = Java.type('net.minecraft.world.phys.Vec3');
var THObject = Java.type('com.asrian.thDanmakuCraft.world.entity.danmaku.THObject');
var THBullet = Java.type('com.asrian.thDanmakuCraft.world.entity.danmaku.THBullet');
var THCurvedLaser = Java.type('com.asrian.thDanmakuCraft.world.entity.danmaku.laser.THCurvedLaser');
function onTick(object) {
    object.setVelocity(0.2, new Vec2(0.0, 1 * 360 / 16 + 60 * Mth.cos(object.getTimer() * 0.3)), true, true);
}