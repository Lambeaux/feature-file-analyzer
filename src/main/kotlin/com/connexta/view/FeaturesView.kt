package com.connexta.view

import com.connexta.model.Bundle
import com.connexta.model.Feature
import javafx.scene.control.TreeItem
import tornadofx.View
import tornadofx.cellFormat
import tornadofx.singleAssign
import tornadofx.treeview

class FeaturesView : View() {
    var rootNode: TreeItem<*> by singleAssign<TreeItem<*>>()

    override val root = treeview<Any> {
        root = TreeItem(Feature("Features", "", ""))
        rootNode = root

        cellFormat {
            text = when (it) {
                is Feature -> {
                    if (it.name != "Features" && it.features.isEmpty() && it.bundles.isEmpty())
                        "[External] ${it.name}"
                    else
                        it.name
                }
                is Bundle -> "Bundle: ${it.groupId}/${it.artifactId}/${it.version}"
                else -> throw IllegalArgumentException("Invalid value type")
            }
        }
    }
}

