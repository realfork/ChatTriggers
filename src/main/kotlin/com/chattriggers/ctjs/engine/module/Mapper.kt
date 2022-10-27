package com.chattriggers.ctjs.engine.module

import com.chattriggers.ctjs.engine.langs.js.JSLoader
import org.mozilla.classfile.ClassFileWriter
import org.mozilla.javascript.CompilerEnvirons
import org.mozilla.javascript.EcmaError
import org.mozilla.javascript.ErrorReporter
import org.mozilla.javascript.Parser
import org.mozilla.javascript.Scriptable
import org.mozilla.javascript.ast.AstNode
import org.mozilla.javascript.ast.AstRoot
import org.mozilla.javascript.ast.NodeVisitor
import org.mozilla.javascript.ast.PropertyGet
import org.mozilla.javascript.commonjs.module.ModuleScript
import org.mozilla.javascript.optimizer.ClassCompiler
import scala.reflect.runtime.ReflectionUtils
import java.io.File
import java.io.FileOutputStream
import java.io.FileReader
import java.nio.charset.Charset
import java.util.*
import kotlin.jvm.internal.Reflection
import kotlin.reflect.full.declaredFunctions


object Mapper {
    private val methodMappings = mutableMapOf<String, String>()
    private val fieldMappings = mutableMapOf<String, String>()

    fun loadMappings(): Boolean {
        fun parseCsv(scanner: Scanner): MutableMap<String, String> {
            val returnMap = mutableMapOf<String, String>()

            scanner.nextLine()
            while (scanner.hasNext()) {
                val mapping = scanner.nextLine().split(",")
                returnMap[mapping[1]] = mapping[0]
            }

            return returnMap
        }

        // Add mappings
        val methodsStream = javaClass.getResourceAsStream("/mappings/methods.csv") ?: return false
        val fieldsStream = javaClass.getResourceAsStream("/mappings/fields.csv") ?: return false

        methodMappings.putAll(parseCsv(Scanner(methodsStream)))
        fieldMappings.putAll(parseCsv(Scanner(fieldsStream)))

        println("Successfully loaded mappings!")
        return true
    }

    fun checkMappings(module: Module) {
        // Some sort of check to see if the module is already mapped

        buildMappings(module)
    }

    private fun buildMappings(module: Module) {
        val buildFolder = File(module.folder, "build").also {
            it.deleteRecursively()
            it.mkdirs()
        }

        // Reflect into ESM parsing method
        val compilerEnvirons = CompilerEnvirons().apply { initFromContext(JSLoader.moduleContext) }
        val parseMethod = JSLoader.moduleContext::class.java.getDeclaredMethod("parse", String.javaClass, String.javaClass, java.lang.Integer::class.java, compilerEnvirons.javaClass, ErrorReporter::class.java, java.lang.Boolean::class.java).also {
            it.isAccessible = true
        }

        // Run remapper
        module.folder.walk().filter {
            it.isFile && it.extension == "js"
        }.forEach {
            try {
                val source = parseMethod.invoke(JSLoader.moduleContext, FileReader(it).readText(), it.path, 1, compilerEnvirons, compilerEnvirons.errorReporter, false) as AstRoot
                //val source = Parser().parse(FileReader(it), null, 1)
                source.visit(Remapper())

                val path = buildFolder.path + it.path.split(module.name).drop(1).joinToString()
                File(path).writeText(source.toSource(), Charset.defaultCharset())
            } catch (e: Exception) {}
        }

        module.folder = buildFolder
    }

    internal class Remapper : NodeVisitor {
        override fun visit(node: AstNode): Boolean {
            if (node is PropertyGet) {
                if (methodMappings.containsKey(node.property.identifier) || fieldMappings.containsKey(node.property.identifier)) {
                    // This is just for testing to see if the concept works

                    // Get LHS class (goofy)
                    var className = ""
                    try { className = JSLoader.eval("${node.left.toSource()}.class").replace("class ", "")
                    } catch (_: EcmaError) {}

                    // Remap if it is an mc class
                    if (className.startsWith("net.minecraft") || className.isEmpty()) {
                        // Remap
                        if (methodMappings.containsKey(node.property.identifier)) node.property.identifier = methodMappings[node.property.identifier]
                        else node.property.identifier = fieldMappings[node.property.identifier]

                        println("Remapped to " + node.property.identifier)
                    }
                }
            }

            return true
        }
    }
}