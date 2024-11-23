package com.asrian.thDanmakuCraft.init;

import com.asrian.thDanmakuCraft.THDanmakuCraftCore;
import com.asrian.thDanmakuCraft.world.entity.EntityExample;
import com.asrian.thDanmakuCraft.world.entity.EntityTHSpellCard;
import com.asrian.thDanmakuCraft.world.entity.EntityTHObjectContainer;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class EntityInit {
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(Registries.ENTITY_TYPE, THDanmakuCraftCore.MODID);

    public static final RegistryObject<EntityType<EntityExample>> EXAMPLE_ENTITY = ENTITIES.register("example_entity",
            () -> EntityType.Builder.<EntityExample>of(EntityExample::new, MobCategory.MISC)
                    .sized(1.0f,1.0f)
                    .build(new ResourceLocation(THDanmakuCraftCore.MODID,"example_entity").toString())
    );
    public static final RegistryObject<EntityType<EntityTHObjectContainer>> ENTITY_THDANMAKU_CONTAINER = ENTITIES.register("entity_thdanmaku_container",
            () -> EntityType.Builder.<EntityTHObjectContainer>of(EntityTHObjectContainer::new, MobCategory.MISC)
                    .sized(1.0f,1.0f)
                    .build(new ResourceLocation(THDanmakuCraftCore.MODID,"entity_thdanmaku_container").toString())
    );
    public static final RegistryObject<EntityType<EntityTHSpellCard>> ENTITY_THSPELLCARD = ENTITIES.register("entity_thspellcard",
            () -> EntityType.Builder.<EntityTHSpellCard>of(EntityTHSpellCard::new, MobCategory.MISC)
                    .sized(1.0f,1.0f)
                    .build(new ResourceLocation(THDanmakuCraftCore.MODID,"entity_thspellcard").toString())
    );
}
