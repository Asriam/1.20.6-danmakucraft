package com.adrian.thDanmakuCraft.script;

import com.adrian.thDanmakuCraft.util.ResourceLoader;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;

public abstract class ScriptManager {
    protected String script = "";
    protected boolean shouldExecuteScript = false;

    public ScriptManager(){
        this.disableScript();
    }

    public abstract Object invokeScript(String functionName, Object... args) throws Exception;

    public abstract Object invokeScript(String functionName, ResourceLoader.RunnableWithException whenException, Object... args);

    public boolean hasScript(){
        return this.script != null && !this.script.equals("");
    }

    public void setScript(String script){
        this.script = script;
    }

    public void enableScript(){
        this.shouldExecuteScript = true;
    }

    public void disableScript(){
        this.shouldExecuteScript = false;
    }

    public void writeData(FriendlyByteBuf buffer){
        buffer.writeBoolean(this.shouldExecuteScript);
        buffer.writeUtf(this.script);
    }

    public void readData(FriendlyByteBuf buffer){
        this.shouldExecuteScript = buffer.readBoolean();
        this.script = buffer.readUtf();
    }

    public CompoundTag save(CompoundTag tag){
        tag.putBoolean("ShouldExecuteScript",this.shouldExecuteScript);
        tag.putString("Script",this.script);
        return tag;
    }

    public void load(CompoundTag tag){
        this.shouldExecuteScript = tag.getBoolean("ShouldExecuteScript");
        this.script = tag.getString("Script");
    }

    public abstract ScriptType type();

    public enum ScriptType{
        JAVASCRIPT,LUA
    }
}
