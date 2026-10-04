package com.faefluffkrist.illagerhostilities.mixin;
import com.faefluffkrist.illagerhostilities.IllagerHostilities;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(Projectile.class)
public abstract class ProjectileAimMixin {
    // Vanilla's distance * 0.2 aiming lift overshoots silverfish's tiny hitbox.
    // Use a low ballistic arc for supported archers shooting silverfish only.
    // Original arrow speed and random spread remain unchanged.
    @ModifyVariable(method="shoot", at=@At("HEAD"), argsOnly=true, ordinal=1)
    private double hostilities$smallTargetAim(double incomingY, double x, double y, double z, float speed, float spread) {
        Projectile self=(Projectile)(Object)this;
        if (!(self instanceof AbstractArrow) || !(self.getOwner() instanceof Mob mob) || !IllagerHostilities.isAttacker(mob)) return incomingY;
        LivingEntity target=mob.getTarget();
        if(target==null || target.getType()!=EntityTypes.SILVERFISH || !IllagerHostilities.isTarget(mob,target)) return incomingY;
        double dx=target.getX()-self.getX(), dz=target.getZ()-self.getZ();
        double distance=Math.sqrt(dx*dx+dz*dz), gravity=self.getGravity();
        if(distance<0.1 || gravity<=0 || speed<=0) return incomingY;
        double height=target.getY()+target.getBbHeight()*0.65-self.getY();
        double v2=speed*speed, discriminant=v2*v2-gravity*(gravity*distance*distance+2*height*v2);
        if(discriminant<0) return incomingY;
        double tangent=(v2-Math.sqrt(discriminant))/(gravity*distance);
        return Math.sqrt(x*x+z*z)*tangent;
    }
}
