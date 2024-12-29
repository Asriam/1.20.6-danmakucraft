package com.adrian.thDanmakuCraft.init;

import com.adrian.thDanmakuCraft.world.entity.EntitySingleTHObject;
import com.adrian.thDanmakuCraft.world.entity.EntityTHObjectContainer;
import com.adrian.thDanmakuCraft.world.entity.spellcard.EntityTHSpellCard;
import com.adrian.thDanmakuCraft.THDanmakuCraftCore;
import com.adrian.thDanmakuCraft.world.entity.EntityExample;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class EntityInit {
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(Registries.ENTITY_TYPE, THDanmakuCraftCore.MOD_ID);

    public static final RegistryObject<EntityType<EntityExample>> EXAMPLE_ENTITY = ENTITIES.register("example_entity",
            () -> EntityType.Builder.<EntityExample>of(EntityExample::new, MobCategory.MISC)
                    .sized(1.0f,1.0f)
                    .build(new ResourceLocation(THDanmakuCraftCore.MOD_ID,"example_entity").toString())
    );
    public static final RegistryObject<EntityType<EntityTHObjectContainer>> ENTITY_THOBJECT_CONTAINER = ENTITIES.register("entity_thobject_container",
            () -> EntityType.Builder.<EntityTHObjectContainer>of(EntityTHObjectContainer::new, MobCategory.MISC)
                    .sized(1.0f,1.0f)
                    .build(new ResourceLocation(THDanmakuCraftCore.MOD_ID,"entity_thobject_container").toString())
    );

    public static final RegistryObject<EntityType<EntitySingleTHObject>> ENTITY_SINGLE_THOBJECT = ENTITIES.register("entity_single_thobject",
            () -> EntityType.Builder.<EntitySingleTHObject>of(EntitySingleTHObject::new, MobCategory.MISC)
                    .sized(1.0f,1.0f)
                    .build(new ResourceLocation(THDanmakuCraftCore.MOD_ID,"entity_single_thobject").toString())
    );

    public static final RegistryObject<EntityType<EntityTHSpellCard>> ENTITY_THSPELLCARD = ENTITIES.register("entity_thspellcard",
            () -> EntityType.Builder.<EntityTHSpellCard>of(EntityTHSpellCard::new, MobCategory.MISC)
                    .sized(1.0f,1.0f)
                    .build(new ResourceLocation(THDanmakuCraftCore.MOD_ID,"entity_thspellcard").toString())
    );
}
