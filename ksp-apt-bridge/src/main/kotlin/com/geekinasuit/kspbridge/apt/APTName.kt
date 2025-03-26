package com.geekinasuit.kspbridge.apt

import com.google.devtools.ksp.symbol.KSName
import javax.lang.model.element.Name

class APTName(val name: String) : KSName {
  constructor(name: Name) : this(name.toString())

  override fun asString() = name

  override fun getQualifier(): String {
    val lastIndex = name.lastIndexOf('.')
    return if (lastIndex != -1) name.substring(0, lastIndex) else ""
  }

  override fun getShortName(): String {
    val lastIndex = name.lastIndexOf('.')
    return if (lastIndex != -1) name.substring(lastIndex + 1) else name
  }
}
