package com.faefluffkrist.illagerhostilities;

import com.faefluffkrist.illagerhostilities.mixin.MobAccessor;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

public final class IllagerHostilities implements ModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("illager-hostilities");
    private static final Set<Mob> INSTALLED = Collections.newSetFromMap(new WeakHashMap<>());
    @Override public void onInitialize() {
        HostilitiesConfig.load();
        ServerLifecycleEvents.SERVER_STARTING.register(server -> { HostilitiesConfig.load(); INSTALLED.clear(); });
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> INSTALLED.clear());
        ServerEntityEvents.ENTITY_LOAD.register((entity, level) -> {
            if (!(entity instanceof Mob mob) || !isAttacker(mob) || !INSTALLED.add(mob)) return;
            // Below retaliation/player goals: existing raids and combat remain functional.
            ((MobAccessor)mob).hostilities$targets().addGoal(4,
                new NearestAttackableTargetGoal<>(mob, LivingEntity.class, 10, true, false,
                    (target, world) -> isTarget(mob, target)));
        });
    }
    public static boolean isAttacker(Mob mob) {
        HostilitiesConfig c = HostilitiesConfig.active;
        if (!c.enabled) return false;
        String id = BuiltInRegistries.ENTITY_TYPE.getKey(mob.getType()).toString();
        if (id.startsWith("friendsandfoes:") && (!c.friendsAndFoes || !FabricLoader.getInstance().isModLoaded("friendsandfoes"))) return false;
        if (id.startsWith("takesapillage:") && (!c.takesAPillage || !FabricLoader.getInstance().isModLoaded("takesapillage"))) return false;
        return Boolean.TRUE.equals(c.attackers.get(id));
    }
    public static boolean isUndead(LivingEntity e) { return e.typeHolder().is(EntityTypeTags.UNDEAD); }
    public static boolean isTarget(Mob attacker, LivingEntity target) {
        if (!target.isAlive() || target == attacker || target.isAlliedTo(attacker)) return false;
        EntityType<?> type = target.getType();
        // Always excluded, regardless of tags or configuration.
        if (type == EntityTypes.ZOMBIE_HORSE || type == EntityTypes.SKELETON_HORSE) return false;
        HostilitiesConfig c = HostilitiesConfig.active;
        return (c.undead && isUndead(target)) || (c.creepers && type == EntityTypes.CREEPER)
            || (c.spiders && (type == EntityTypes.SPIDER || type == EntityTypes.CAVE_SPIDER))
            || (c.silverfish && type == EntityTypes.SILVERFISH);
    }
}
