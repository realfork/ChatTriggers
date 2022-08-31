package com.chattriggers.ctjs.launch

import com.chattriggers.ctjs.engine.module.ModuleManager
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin
import org.spongepowered.asm.launch.MixinBootstrap
import org.spongepowered.asm.mixin.MixinEnvironment
import org.spongepowered.asm.mixin.Mixins

class CTJSLoadingPlugin : IFMLLoadingPlugin {
    init {
        ModuleManager.setup()
        ModuleManager.asmPass()

        // Mixin Loading
        MixinBootstrap.init()
        Mixins.addConfiguration("mixins.chattriggers.json")
        MixinEnvironment.getCurrentEnvironment().obfuscationContext = "searge"
    }

    override fun getASMTransformerClass(): Array<String> = emptyArray()

    override fun getModContainerClass(): String? = null

    override fun getSetupClass(): String? = null

    override fun injectData(data: MutableMap<String, Any>?) { }

    override fun getAccessTransformerClass(): String? = null
}