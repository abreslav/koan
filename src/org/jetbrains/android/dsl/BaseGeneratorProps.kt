package org.jetbrains.android.dsl

import java.io.File
import java.util.HashMap

class Variable(val name: String, val typ: String) {
  override fun toString(): String {
    return "$name:$typ"
  }
}

abstract class BaseGeneratorProps() {
  open var generateImports: Boolean = true
  open var generatePackage: Boolean = true
  open var generateMavenArtifact: Boolean = true

  open var generateProperties: Boolean = true
  open var generatePropertySetters: Boolean = true
  open var generateLayoutParamsHelperClasses: Boolean = true
  open var generateViewExtensionMethods: Boolean = true
  open var generateViewHelperConstructors: Boolean = true
  open var generateViewGroupExtensionMethods: Boolean = true
  open var generateSimpleListeners: Boolean = true
  open var generateComplexListenerClasses: Boolean = true
  open var generateComplexListenerSetters: Boolean = true
  open var generateTopLevelExtensionMethods: Boolean = true
  open var generateSupport: Boolean = false
  open var generateStatic: Boolean = true

  abstract public fun getOutputFile(subsystem: Subsystem): File

  abstract val indent: String

  abstract val outputDirectory: String
  abstract val outputPackage: String

  abstract val viewGroupBaseClass: String
  abstract val viewBaseClass: String

  abstract val imports: HashMap<String, String>

  abstract val excludedClasses: Set<String>
  abstract val excludedMethods: Set<String>
  abstract val helperConstructors: Map<String, List<List<Variable>>>
  abstract val customMethodParameters: Map<String, String>
}