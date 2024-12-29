package com.adrian.thDanmakuCraft.world.entity;

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

public class PlayerTHObjectContainerProvider implements ICapabilityProvider, INBTSerializable<CompoundTag> {

    public static Capability<PlayerTHObjectContainerProvider> PLAYER_THOBJECT_CONTAINER_CAPABILITY = CapabilityManager.get(new CapabilityToken<PlayerTHObjectContainerProvider>() { });
    private EntityTHObjectContainer container = null;
    private final LazyOptional<EntityTHObjectContainer> optional = LazyOptional.of(this::createTHObjectContainer);

    public EntityTHObjectContainer createTHObjectContainer() {
        return null;
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == PLAYER_THOBJECT_CONTAINER_CAPABILITY) {
            return optional.cast();
        }
        return LazyOptional.empty();
    }

    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        return tag;
    }

    public void deserializeNBT(CompoundTag tag) {

    }


}
