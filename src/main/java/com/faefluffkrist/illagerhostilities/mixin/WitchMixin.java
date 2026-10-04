package com.faefluffkrist.illagerhostilities.mixin;
import com.faefluffkrist.illagerhostilities.IllagerHostilities;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Witch;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Witch.class)
public abstract class WitchMixin {
    // Keep vanilla aim, cooldowns, sounds and projectile creation; only replace
    // the offensive potion for this mod's undead targets. Healing damages undead.
    @Redirect(method="performRangedAttack", at=@At(value="INVOKE",
        target="Lnet/minecraft/world/item/alchemy/PotionContents;createItemStack(Lnet/minecraft/world/item/Item;Lnet/minecraft/core/Holder;)Lnet/minecraft/world/item/ItemStack;"))
    private ItemStack hostilities$undeadPotion(Item item, Holder<Potion> potion, LivingEntity target, float distanceFactor) {
        Witch self = (Witch)(Object)this;
        if (IllagerHostilities.isAttacker(self) && IllagerHostilities.isTarget(self, target)) {
            if (IllagerHostilities.isUndead(target)) potion=Potions.HEALING;
            else if (!target.canBeAffected(new MobEffectInstance(MobEffects.POISON, 200))) potion=Potions.HARMING;
        }
        return PotionContents.createItemStack(item, potion);
    }
}
