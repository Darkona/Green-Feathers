package com.darkona.feathers.mixin;

import com.darkona.feathers.mount.MountExertion;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Prevents exhausted horses, donkeys, and mules from jumping. The client checks this during ridden jump simulation.
 * {@link ServerGamePacketListenerImplMixin} performs the server-side check.
 */
@Mixin(AbstractHorse.class)
public abstract class AbstractHorseMixin {

    @ModifyReturnValue(method = "canJump", at = @At("RETURN"))
    private boolean greenfeathers$canJump(boolean original) {
        return original && MountExertion.canJump((AbstractHorse) (Object) this);
    }
}
