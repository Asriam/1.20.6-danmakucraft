package com.adrian.thDanmakuCraft.init;

import com.adrian.thDanmakuCraft.registries.THDanmakuCraftRegistries;
import com.adrian.thDanmakuCraft.world.entity.danmaku.THBullet;
import com.adrian.thDanmakuCraft.world.entity.danmaku.laser.THCurvedLaser;
import com.adrian.thDanmakuCraft.THDanmakuCraftCore;
import com.adrian.thDanmakuCraft.world.entity.danmaku.THObject;
import com.adrian.thDanmakuCraft.world.entity.danmaku.THObjectType;
import com.adrian.thDanmakuCraft.world.entity.danmaku.laser.THLaser;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class THObjectInit {
    public static final DeferredRegister<THObjectType> TH_OBJECTS = DeferredRegister.create(THDanmakuCraftRegistries.Keys.THOBJECT_TYPE, THDanmakuCraftCore.MOD_ID);

    public static final RegistryObject<THObjectType<THObject>> TH_OBJECT = TH_OBJECTS.register("object", () -> THObjectType.Builder.<THObject>of(THObject::new).build());
    public static final RegistryObject<THObjectType<THBullet>> TH_BULLET = TH_OBJECTS.register("bullet", () -> THObjectType.Builder.<THBullet>of(THBullet::new).build());
    public static final RegistryObject<THObjectType<THLaser>> TH_LASER = TH_OBJECTS.register("laser", () -> THObjectType.Builder.<THLaser>of(THLaser::new).build());
    public static final RegistryObject<THObjectType<THCurvedLaser>> TH_CURVED_LASER = TH_OBJECTS.register("curved_laser", () -> THObjectType.Builder.<THCurvedLaser>of(THCurvedLaser::new).build());


}
