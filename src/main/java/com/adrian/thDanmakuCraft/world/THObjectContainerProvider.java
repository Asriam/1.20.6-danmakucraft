package com.adrian.thDanmakuCraft.world;

import com.adrian.thDanmakuCraft.world.danmaku.THObjectContainer;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class THObjectContainerProvider implements ICapabilityProvider, INBTSerializable<CompoundTag> {

    public static Capability<THObjectContainer> PLAYER_THOBJECT_CONTAINER_CAPABILITY = CapabilityManager.get(new CapabilityToken<THObjectContainer>() { });
    private THObjectContainer container = null;
    private final LazyOptional<THObjectContainer> optional = LazyOptional.of(this::createTHObjectContainer);

    private THObjectContainer createTHObjectContainer() {
        if (this.container == null) {
            this.container = new THObjectContainer(null);
        }
        return this.container;
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == PLAYER_THOBJECT_CONTAINER_CAPABILITY) {
            return optional.cast();
        }
        return LazyOptional.empty();
    }

    public THObjectContainer getContainer(){
        return this.createTHObjectContainer();
    }

    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        //createTHObjectContainer().save(tag);
        return tag;
    }

    public void deserializeNBT(CompoundTag tag) {
        createTHObjectContainer().load(tag);
    }


}
