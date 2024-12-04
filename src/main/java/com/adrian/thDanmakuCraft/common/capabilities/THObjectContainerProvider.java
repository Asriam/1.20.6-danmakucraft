package com.adrian.thDanmakuCraft.common.capabilities;

import com.adrian.thDanmakuCraft.world.entity.EntityTHObjectContainer;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class THObjectContainerProvider implements ICapabilityProvider{
    private EntityTHObjectContainer container;


    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, net.minecraft.core.Direction side) {
        return null;
    }
}
