package com.asrian.thDanmakuCraft;

import com.asrian.thDanmakuCraft.util.JSLoader;
import com.asrian.thDanmakuCraft.util.script.JavaScript;
import net.minecraft.resources.ResourceLocation;

import javax.script.ScriptEngine;

public class jsTest {

    public static void main(String[] args){
        ScriptEngine engine = new JavaScript().getEngine();
        JSLoader jsLoader = new JSLoader();
        jsLoader.getResource(new ResourceLocation(THDanmakuCraftCore.MODID,"data/js/test.js"));
        //engine.eval();
    }
}
