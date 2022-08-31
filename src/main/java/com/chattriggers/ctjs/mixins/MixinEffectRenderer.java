package com.chattriggers.ctjs.mixins;

import com.chattriggers.ctjs.minecraft.listeners.CancellableEvent;
import com.chattriggers.ctjs.minecraft.wrappers.entity.Particle;
import com.chattriggers.ctjs.triggers.TriggerType;
import net.minecraft.client.particle.EffectRenderer;
import net.minecraft.client.particle.EntityFX;
import net.minecraft.client.particle.IParticleFactory;
import net.minecraft.util.EnumParticleTypes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(EffectRenderer.class)
public class MixinEffectRenderer {
    @Inject(method = "spawnEffectParticle", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/particle/EffectRenderer;addEffect(Lnet/minecraft/client/particle/EntityFX;)V"), locals = LocalCapture.CAPTURE_FAILHARD, cancellable = true)
    @SuppressWarnings("InvalidInjectorMethodSignature")
    public void spawnEffectParticle(int particleId, double xCoord, double yCoord, double zCoord, double xSpeed, double ySpeed, double zSpeed, int[] parameters, CallbackInfoReturnable<EntityFX> cir, IParticleFactory iparticlefactory, EntityFX entityfx) {
        CancellableEvent event = new CancellableEvent();
        TriggerType.SpawnParticle.triggerAll(new Particle(entityfx), EnumParticleTypes.getParticleFromId(particleId), event);
        if (event.isCancelled()) cir.setReturnValue(null);
    }
}
