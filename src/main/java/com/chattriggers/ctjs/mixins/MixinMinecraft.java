package com.chattriggers.ctjs.mixins;

import com.chattriggers.ctjs.minecraft.listeners.CancellableEvent;
import com.chattriggers.ctjs.minecraft.objects.message.TextComponent;
import com.chattriggers.ctjs.minecraft.wrappers.Client;
import com.chattriggers.ctjs.minecraft.wrappers.inventory.Inventory;
import com.chattriggers.ctjs.triggers.TriggerType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.ScreenShotHelper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.File;

@Mixin(Minecraft.class)
public class MixinMinecraft {
    @Final @Shadow public File mcDataDir;
    @Shadow public int displayWidth;
    @Shadow public int displayHeight;
    @Shadow private Framebuffer framebufferMc;

    @Inject(method = "dispatchKeypresses", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiIngame;getChatGUI()Lnet/minecraft/client/gui/GuiNewChat;", ordinal = 1, shift = At.Shift.BEFORE, by = 2), cancellable = true)
    public void screenshot(CallbackInfo ci) {
        IChatComponent component = ScreenShotHelper.saveScreenshot(mcDataDir, displayWidth, displayHeight, framebufferMc);
        new TextComponent(component).chat();
        ci.cancel();
    }

    @Inject(method = "displayGuiScreen", at = @At(value = "FIELD", target = "Lnet/minecraft/client/Minecraft;currentScreen:Lnet/minecraft/client/gui/GuiScreen;", ordinal = 0), cancellable = true)
    public void displayGuiScreen(GuiScreen gui, CallbackInfo ci) {
        CancellableEvent event = new CancellableEvent();
        if (gui != null) TriggerType.GuiOpened.triggerAll(gui, event);

        if (event.isCancelled()) ci.cancel();
    }

    @Inject(method = "displayGuiScreen", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiScreen;onGuiClosed()V"))
    public void closedGuiScreen(GuiScreen gui, CallbackInfo ci) {
        TriggerType.GuiClosed.triggerAll(Client.getMinecraft().currentScreen);
    }

    @Inject(method = "startGame", at = @At("TAIL"))
    public void startGame(CallbackInfo ci) {
        TriggerType.GameLoad.triggerAll();
    }
}
