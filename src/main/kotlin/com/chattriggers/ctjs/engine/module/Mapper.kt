package com.chattriggers.ctjs.engine.module

import org.mozilla.javascript.IRFactory
import org.mozilla.javascript.ast.AstNode
import org.mozilla.javascript.ast.FunctionCall
import org.mozilla.javascript.ast.Name
import org.mozilla.javascript.ast.NodeVisitor
import java.io.File
import java.io.FileReader
import java.nio.charset.Charset

object Mapper {
    val mappings = mutableMap()

    fun loadMappings() {
        // Convert to kt since IJ is being shit
        try (scanner = new Scanner(File("essentials mappings file"))) {
            scanner.nextLine();
            while (scanner.hasNext()) {
                String mappingLine = scanner.nextLine()
                int commaIndex = mappingLine.indexOf(',')
                String srgName = mappingLine.substring(0, commaIndex)
                String mcpName = mappingLine.substring(commaIndex + 1, commaIndex + 1 + mappingLine.substring(commaIndex + 1).indexOf(','));
                mappings.put(srgName, mcpName)
            }
        }
    }

    fun checkMappings(module: Module) {
        // Some sort of check to see if it needs to be done

        buildMappings(module)
    }

    fun buildMappings(module: Module) {
        val entryFile = File(module.folder, module.metadata.entry!!).toURI()

        val buildFolder = File(module.folder, "build").also {
            it.deleteRecursively()
            it.mkdirs()
        }

        // Run remapper
        module.folder.walk().filter {
            it.isFile && it.extension == "js"
        }.forEach {
            val source = IRFactory().parse(FileReader(it), null, 1).also { it.visit(Remapper()) }.toSource()

            val path = buildFolder.path + it.path.split(module.name).drop(1).joinToString()
            File(path).writeText(source, Charset.defaultCharset())
        }

        module.folder = buildFolder
    }

    internal class Remapper : NodeVisitor {
        override fun visit(node: AstNode): Boolean {
            when (node) {
                is FunctionCall -> {
                    node.target.visit {
                        if (it is Name) {
                            // This is just for testing to see if the concept works
                            if (it.identifier == "chat") it.identifier = "actionBar"

                            println(it.identifier)
                        }

                        return@visit true
                    }
                }
            }

            return true
        }
    }
}

