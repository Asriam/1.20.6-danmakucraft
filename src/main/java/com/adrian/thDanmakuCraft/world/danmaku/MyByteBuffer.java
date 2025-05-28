package com.adrian.thDanmakuCraft.world.danmaku;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.Utf8String;

public class MyByteBuffer {

    public final ByteBuf buffer;

    public MyByteBuffer(ByteBuf buffer)
    {
        this.buffer = new FriendlyByteBuf(buffer);
    }

    public MyByteBuffer(){
        this.buffer = new FriendlyByteBuf(Unpooled.buffer());
    }

    public void write(ByteBuf buffer)
    {
        this.buffer.writeBytes(buffer);
    }

    public ByteBuf read(){
        return this.buffer.readBytes(this.buffer.readableBytes());
    }

    public void write(byte[] bytes){
        this.buffer.writeBytes(bytes);
    }

    public void writeUTF(String string){
        Utf8String.write(buffer, string, 32767);
    }

    public String readUTF(String string){
        return Utf8String.read(buffer, 32767);
    }
}
