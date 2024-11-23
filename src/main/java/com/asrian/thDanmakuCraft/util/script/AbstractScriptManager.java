package com.asrian.thDanmakuCraft.util.script;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;

public abstract class AbstractScriptManager {
    protected String script = "";
    protected boolean shouldExecuteScript;

    public abstract Object invokeScript(String functionName, Object... args) throws Exception;

    public boolean hasScript(){
        return this.script != null && !this.script.equals("");
    }

    public void setScript(String script){
        this.script = script;
    }

    public void setShouldExecuteScript(boolean shouldExecuteScript) {
        this.shouldExecuteScript = shouldExecuteScript;
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
        /**
        CompoundTag tag = new CompoundTag();
        tag.putString("script",this.script);
        buffer.writeNbt(tag);

         */
    }

    public void readData(FriendlyByteBuf buffer){
        this.shouldExecuteScript = buffer.readBoolean();
        this.script = buffer.readUtf();
        /**
        this.script = buffer.readNbt().getString("script");
         */
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
}
