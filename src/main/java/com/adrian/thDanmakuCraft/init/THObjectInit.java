package com.adrian.thDanmakuCraft.init;

import com.adrian.thDanmakuCraft.registries.THDanmakuCraftRegistries;
import com.adrian.thDanmakuCraft.world.danmaku.thobject.bullet.THBullet;
import com.adrian.thDanmakuCraft.world.danmaku.thobject.laser.THCurvyLaser;
import com.adrian.thDanmakuCraft.THDanmakuCraftMod;
import com.adrian.thDanmakuCraft.world.danmaku.thobject.THObject;
import com.adrian.thDanmakuCraft.world.danmaku.thobject.THObjectType;
import com.adrian.thDanmakuCraft.world.danmaku.thobject.laser.THLaser;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

public class THObjectInit {
    public static final DeferredRegister<THObjectType> TH_OBJECTS = DeferredRegister.create(THDanmakuCraftRegistries.Keys.THOBJECT_TYPE, THDanmakuCraftMod.MOD_ID);

    public static final DeferredHolder<THObjectType, THObjectType<THObject>> TH_OBJECT = TH_OBJECTS.register("object",
            () -> THObjectType.Builder.<THObject>of(THObject::new)
                    .build());
    public static final DeferredHolder<THObjectType, THObjectType<THBullet>> TH_BULLET = TH_OBJECTS.register("bullet",
            () -> THObjectType.Builder.<THBullet>of(THBullet::new)
                    .build());
    public static final DeferredHolder<THObjectType, THObjectType<THLaser>>  TH_LASER  = TH_OBJECTS.register("laser",
            () -> THObjectType.Builder.<THLaser>of(THLaser::new)
                    .build());
    public static final DeferredHolder<THObjectType, THObjectType<THCurvyLaser>> TH_CURVY_LASER = TH_OBJECTS.register("curvy_laser",
            () -> THObjectType.Builder.<THCurvyLaser>of(THCurvyLaser::new)
                    .build());


}
