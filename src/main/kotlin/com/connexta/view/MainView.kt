package com.connexta.view

import com.connexta.FeatureController
import tornadofx.*
import java.io.File

class MainView : View() {
    val featureController: FeatureController by inject()

    override val root = borderpane {
        top {
            menubar {
                menu("File") {
                    menuitem("Open") {
                        val folder = chooseDirectory(
                                title = "Select Root Directory",
                                initialDirectory = File("C:\\Users\\TravisMcMahon\\Development\\ddf"))
                        if (folder != null) {
                            featureController.loadFeatures(folder)
                        }
                    }
                }
            }
        }
        left(FeatureFileView::class)
        center(FeatureTableView::class)
    }
}