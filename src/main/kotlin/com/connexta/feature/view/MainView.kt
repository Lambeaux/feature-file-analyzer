package com.connexta.feature.view

import com.connexta.feature.FeatureController
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
        center(FeaturesView::class)
    }
}