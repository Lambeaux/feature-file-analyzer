package com.connexta.feature.view

import com.connexta.feature.model.FeatureFile
import javafx.scene.control.ListView
import javafx.scene.layout.Priority
import tornadofx.*

class FeatureFileView : View() {
    var listView: ListView<FeatureFile> by singleAssign<ListView<FeatureFile>>()

    override val root = vbox {
        label("Feature Files")
        listView = listview<FeatureFile> {
            vgrow = Priority.ALWAYS
            cellCache {
                label(it.name)
            }
        }
    }
}