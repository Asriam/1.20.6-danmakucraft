package com.adrian.thDanmakuCraft.api.script;

import net.minecraft.world.phys.Vec3;

public interface IScriptTHObjectContainerAPI {

    int getTimer();
    void setTimer(int time);
    Vec3 getPosition();
}
