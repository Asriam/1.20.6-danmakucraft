package com.adrian.thDanmakuCraft.init;

import com.adrian.thDanmakuCraft.THDanmakuCraftMod;
import com.adrian.thDanmakuCraft.util.ResourceLocationUtil;
import com.adrian.thDanmakuCraft.world.entity.EntitySingleTHObject;
import com.adrian.thDanmakuCraft.world.entity.EntityTHObjectContainer;
import com.adrian.thDanmakuCraft.world.entity.mount.BroomMount;
import com.adrian.thDanmakuCraft.world.entity.spellcard.EntityTHSpellCard;
import com.adrian.thDanmakuCraft.world.entity.EntityExample;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class EntityInit {
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(Registries.ENTITY_TYPE, THDanmakuCraftMod.MOD_ID);

    public static final RegistryObject<EntityType<EntityExample>> EXAMPLE_ENTITY = ENTITIES.register(
            "example_entity",
            () -> EntityType.Builder.<EntityExample>of(EntityExample::new, MobCategory.MISC)
                    .sized(1.0f,1.0f)
                    .build(ResourceLocationUtil.thdanmakucraft("example_entity").toString())
    );
    public static final RegistryObject<EntityType<EntityTHObjectContainer>> ENTITY_THOBJECT_CONTAINER = ENTITIES.register(
            "thobject_container",
            () -> EntityType.Builder.<EntityTHObjectContainer>of(EntityTHObjectContainer::new, MobCategory.MISC)
                    .sized(1.0F, 1.0F).eyeHeight(0.5F).spawnDimensionsScale(4.0F).clientTrackingRange(11)
                    .build(ResourceLocationUtil.thdanmakucraft("thobject_container").toString())
    );

    public static final RegistryObject<EntityType<EntitySingleTHObject>> ENTITY_SINGLE_THOBJECT = ENTITIES.register(
            "entity_single_thobject",
            () -> EntityType.Builder.<EntitySingleTHObject>of(EntitySingleTHObject::new, MobCategory.MISC)
                    .sized(1.0f,1.0f)
                    .build(ResourceLocationUtil.thdanmakucraft("entity_single_thobject").toString())
    );

    public static final RegistryObject<EntityType<EntityTHSpellCard>> ENTITY_THSPELLCARD = ENTITIES.register(
            "entity_thspellcard",
            () -> EntityType.Builder.<EntityTHSpellCard>of(EntityTHSpellCard::new, MobCategory.MISC)
                    .sized(1.0f,1.0f)
                    .build(ResourceLocationUtil.thdanmakucraft("entity_thspellcard").toString())
    );

    public static final RegistryObject<EntityType<BroomMount>> BROOM_MOUNT = ENTITIES.register(
            "broom_mount",
            () -> EntityType.Builder.<BroomMount>of(BroomMount::new, MobCategory.MISC)
                    .sized(1.0f,1.0f)
                    .build(ResourceLocationUtil.thdanmakucraft("broom_mount").toString())
    );
}
