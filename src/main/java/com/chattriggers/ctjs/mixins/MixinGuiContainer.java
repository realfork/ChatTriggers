package com.chattriggers.ctjs.mixins;

import com.chattriggers.ctjs.minecraft.listeners.CancellableEvent;
import com.chattriggers.ctjs.minecraft.wrappers.inventory.Slot;
import com.chattriggers.ctjs.triggers.TriggerType;
import gg.essential.lib.mixinextras.injector.WrapWithCondition;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiContainer.class)
public class MixinGuiContainer {
    @Shadow private net.minecraft.inventory.Slot theSlot;

    @Inject(method = "drawSlot", at = @At("HEAD"), cancellable = true)
    public void drawSlot(net.minecraft.inventory.Slot slotIn, CallbackInfo ci) {
        CancellableEvent event = new CancellableEvent();

        GlStateManager.pushMatrix();
        TriggerType.RenderSlot.triggerAll(new Slot(slotIn), this, event);
        GlStateManager.popMatrix();

        if (event.isCancelled()) ci.cancel();
    }

    @Inject(method = "drawScreen", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/inventory/GuiContainer;drawGuiContainerForegroundLayer(II)V"))
    public void drawScreen(int mouseX, int mouseY, float partialTicks, CallbackInfo ci) {
        if (theSlot != null) {
            GlStateManager.pushMatrix();
            TriggerType.PreItemRender.triggerAll(mouseX, mouseY, theSlot, this);
            GlStateManager.popMatrix();
        }
    }

    @WrapWithCondition(method = "drawScreen", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/inventory/GuiContainer;drawGradientRect(IIIIII)V"))
    private boolean drawScreen(GuiContainer instance, int left, int top, int right, int bottom, int startColor, int endColor, int mouseX, int mouseY, float partialTicks) {
        if (theSlot != null) {
            CancellableEvent event = new CancellableEvent();

            GlStateManager.pushMatrix();
            TriggerType.RenderSlotHighlight.triggerAll(mouseX, mouseY, theSlot, this, event);
            GlStateManager.popMatrix();

            return !event.isCancelled();
        }
        return true;
    }
}
