package com.darkona.feathers.mixin;

import com.darkona.feathers.mount.MountExertion;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.world.entity.animal.camel.Camel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Camels override the horse's jump with their dash: an exhausted camel can't dash either.
 */
@Mixin(Camel.class)
public abstract class CamelMixin {

    @ModifyReturnValue(method = "canJump", at = @At("RETURN"))
    private boolean greenfeathers$canDash(boolean original) {
        return original && MountExertion.canJump((Camel) (Object) this);
    }
}
