package com.geekinasuit.kspbridge.apt

import com.google.devtools.ksp.symbol.KSNode
import javax.lang.model.element.Element

interface APTNode : KSNode {
  val element: Element
}
