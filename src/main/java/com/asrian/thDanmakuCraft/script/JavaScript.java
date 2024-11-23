package com.asrian.thDanmakuCraft.script;

import com.asrian.thDanmakuCraft.world.entity.danmaku.THBullet;
import com.asrian.thDanmakuCraft.world.entity.danmaku.laser.THCurvedLaser;
import com.asrian.thDanmakuCraft.world.entity.danmaku.THObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.openjdk.nashorn.api.scripting.NashornScriptEngineFactory;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import static com.asrian.thDanmakuCraft.THDanmakuCraftCore.MODID;

public class JavaScript {
    public static final String ENGINE_NAME = new ResourceLocation(MODID,"js").toString();
    private final ScriptEngineManager scriptEngineManager = new ScriptEngineManager();
    private final ScriptEngine scriptEngine;

    public JavaScript() {
        this.scriptEngineManager.registerEngineName(ENGINE_NAME,new NashornScriptEngineFactory());
        this.scriptEngine = scriptEngineManager.getEngineByName(ENGINE_NAME);
        this.putAPI();
    }

    public final ScriptEngine getEngine(){
        return this.scriptEngine;
    }

    public final void putAPI(){
        this.scriptEngine.put("Mth", Mth.class);
        this.scriptEngine.put("Vec3", Vec3.class);
        this.scriptEngine.put("Vec2", Vec2.class);
        this.scriptEngine.put("THObject", THObject.class);
        this.scriptEngine.put("THObject", THBullet.class);
        this.scriptEngine.put("THCurvedLaser", THCurvedLaser.class);
    }
}
