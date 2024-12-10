package com.adrian.thDanmakuCraft.world.entity.danmaku;

import com.google.common.collect.Lists;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BooleanSupplier;

public class THTasker {
    //public LinkedList<BooleanSupplier> SUPPLIERS = Lists.newLinkedList();
    private final List<BooleanSupplier> SUPPLIERS = Lists.newArrayList();
    private int tick = 0;

    public void add(final Runnable runnable){
        AtomicInteger tick = new AtomicInteger(this.tick);
        SUPPLIERS.add(()->{
            if(tick.decrementAndGet() <= 0) {
                runnable.run();
                return true;
            }
            return false;
        });
    }

    public void wait(int tick){
        if(tick <= 0) return;
        this.tick += tick;
    }

    public void resume(){
        SUPPLIERS.removeIf(BooleanSupplier::getAsBoolean);
        tick = Math.max(0, tick - 1);
    }
}
