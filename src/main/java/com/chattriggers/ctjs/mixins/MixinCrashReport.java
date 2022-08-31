package com.chattriggers.ctjs.mixins;

import com.chattriggers.ctjs.engine.module.ModuleManager;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CrashReport.class)
public class MixinCrashReport {
    @Shadow @Final private CrashReportCategory theReportCategory;

    @Inject(method = "populateEnvironment", at = @At("HEAD"))
    public void populateEnvironment(CallbackInfo ci) {
        theReportCategory.addCrashSection("ChatTriggers modules", ModuleManager.INSTANCE.getCachedModules());
    }
}
