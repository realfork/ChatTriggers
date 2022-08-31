package com.chattriggers.ctjs.mixins;

import com.chattriggers.ctjs.minecraft.listeners.CancellableEvent;
import com.chattriggers.ctjs.triggers.TriggerType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiIngame;
import net.minecraftforge.client.GuiIngameForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiIngameForge.class)
public class MixinTitle extends GuiIngame {
    public MixinTitle(Minecraft minecraft) {
        super(minecraft);
    }

    @Inject(method = "renderTitle", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GlStateManager;pushMatrix()V"), cancellable = true)
    public void renderTitle(int width, int height, float partialTicks, CallbackInfo ci) {
        if (!displayedTitle.isEmpty() || !displayedSubTitle.isEmpty()) {
            CancellableEvent event = new CancellableEvent();

            TriggerType.RenderTitle.triggerAll(displayedTitle, displayedSubTitle, event);

            if (event.isCancelled()) ci.cancel();
        }
    }
}
