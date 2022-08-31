package com.chattriggers.ctjs.mixins;

import com.chattriggers.ctjs.minecraft.listeners.CancellableEvent;
import com.chattriggers.ctjs.minecraft.objects.message.TextComponent;
import com.chattriggers.ctjs.minecraft.wrappers.inventory.Item;
import com.chattriggers.ctjs.triggers.TriggerType;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IChatComponent;
import org.lwjgl.input.Keyboard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;

@Mixin(GuiScreen.class)
public class MixinGuiScreen {
    @Inject(method = "sendChatMessage(Ljava/lang/String;Z)V", at = @At("HEAD"), cancellable = true)
    public void sendChatMessage(String msg, boolean addToChat, CallbackInfo ci) {
        CancellableEvent event = new CancellableEvent();
        TriggerType.MessageSent.triggerAll(msg, event);

        if (event.isCancelled()) ci.cancel();
    }

    @Inject(method = "handleKeyboardInput", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiScreen;keyTyped(CI)V"), cancellable = true)
    public void handleKeyboardInput(CallbackInfo ci) {
        CancellableEvent event = new CancellableEvent();
        TriggerType.GuiKey.triggerAll(Keyboard.getEventCharacter(), Keyboard.getEventKey(), this, event);

        if (event.isCancelled()) ci.cancel();
    }

    @Inject(method = "handleMouseInput", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiScreen;mouseClicked(III)V"), locals = LocalCapture.CAPTURE_FAILHARD, cancellable = true)
    public void handleMouseInput(CallbackInfo ci, int mouseX, int mouseY, int button) {
        CancellableEvent event = new CancellableEvent();
        TriggerType.GuiMouseClick.triggerAll(mouseX, mouseY, button, this, event);

        if (event.isCancelled()) ci.cancel();
    }

    @Inject(method = "handleMouseInput", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiScreen;mouseReleased(III)V"), locals = LocalCapture.CAPTURE_FAILHARD)
    public void handleMouseRelease(CallbackInfo ci, int mouseX, int mouseY, int button) {
        TriggerType.GuiMouseRelease.triggerAll(mouseX, mouseY, button, this);
    }

    @Inject(method = "handleMouseInput", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiScreen;mouseClickMove(IIIJ)V"), locals = LocalCapture.CAPTURE_FAILHARD, cancellable = true)
    public void handleMouseDrag(CallbackInfo ci, int mouseX, int mouseY, int button) {
        CancellableEvent event = new CancellableEvent();
        TriggerType.GuiMouseDrag.triggerAll(mouseX, mouseY, button, this, event);

        if (event.isCancelled()) ci.cancel();
    }

    @Inject(method = "handleComponentClick", at = @At("HEAD"), cancellable = true)
    public void handleComponentClick(IChatComponent component, CallbackInfoReturnable<Boolean> cir) {
        if (component != null) {
            CancellableEvent event = new CancellableEvent();
            TriggerType.ChatComponentClicked.triggerAll(new TextComponent(component), event);

            if (event.isCancelled()) cir.setReturnValue(false);
        }
    }

    @Inject(method = "handleComponentHover", at = @At("HEAD"), cancellable = true)
    public void handleComponentHover(IChatComponent component, int x, int y, CallbackInfo ci) {
        CancellableEvent event = new CancellableEvent();

        if (component == null) TriggerType.ChatComponentHovered.triggerAll(null, x, y, event);
        else TriggerType.ChatComponentHovered.triggerAll(new TextComponent(component), x, y, event);

        if (event.isCancelled()) ci.cancel();
    }

    @Inject(method = "renderToolTip", at = @At(value = "INVOKE", target = "Ljava/util/List;size()I"), locals = LocalCapture.CAPTURE_FAILHARD, cancellable = true)
    public void renderToolTip(ItemStack stack, int x, int y, CallbackInfo ci, List<String> list) {
        CancellableEvent event = new CancellableEvent();
        TriggerType.Tooltip.triggerAll(list, new Item(stack), event);

        if (event.isCancelled()) ci.cancel();
    }

    @Inject(method = "drawDefaultBackground", at = @At("HEAD"), cancellable = true)
    public void handleComponentHover(CallbackInfo ci) {
        TriggerType.GuiDrawBackground.triggerAll(this);
    }
}