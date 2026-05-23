package com.darkona.feathers.mixin;

import com.darkona.feathers.mount.MountExertion;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Horses, donkeys and mules: an exhausted mount can't jump. Checked on the client, which simulates the ridden jump;
 * the server side is {@link ServerGamePacketListenerImplMixin}.
 */
@Mixin(AbstractHorse.class)
public abstract class AbstractHorseMixin {

    @ModifyReturnValue(method = "canJump", at = @At("RETURN"))
    private boolean greenfeathers$canJump(boolean original) {
        return original && MountExertion.canJump((AbstractHorse) (Object) this);
    }
}
