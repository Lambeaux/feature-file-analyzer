package com.connexta

import com.connexta.model.Bundle
import com.connexta.model.Feature
import com.connexta.model.FeatureFile
import com.connexta.view.FeatureFileView
import com.connexta.view.FeaturesView
import org.w3c.dom.Element
import tornadofx.*
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory

class FeatureController : Controller() {
    private val documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder()

    // Views
    val featureFileView: FeatureFileView by inject()
    val featuresView: FeaturesView by inject()

    // Models
    val featureFiles = mutableListOf<FeatureFile>().observable()

    init {
        featureFileView.listView.items = featureFiles
        featureFileView.listView.onUserSelect { selectedFeatureFile() }
    }

    fun loadFeatures(root: File) {
        featureFiles.clear()

        val namePattern = Regex("(\\w+-)+app")
        val exclude = Regex("\\.\\w*|target|node|node_modules|java|webapp|schemas|test")

        val files = root.walkTopDown()
                .onEnter { !it.name.matches(exclude) }
                .filter { it.name == "features.xml" }
                .filter { namePattern.containsMatchIn(it.canonicalPath) }

        val appNames = files.map(File::getCanonicalPath)
                .map { namePattern.find(it) }
                .filterNotNull()
                .map { it.value }
                .toList()

        files.mapIndexedTo(featureFiles, {
            i, file ->
            FeatureFile(appNames[i], parseFeatureFile(file))
        })

        linkFeatures()
    }

    private fun parseFeatureFile(file: File): MutableList<Feature> {
        val document = documentBuilder.parse(file)
        val rootFeature = document.getElementsByTagName("features").item(0)

        val features = ArrayList<Feature>()
        (0 until rootFeature.childNodes.length)
                .map { rootFeature.childNodes.item(it) }
                .filterIsInstance<Element>()
                .filter { it.tagName == "feature" }
                .mapTo(features) {
                    Feature(
                            it.getAttribute("name").trim(),
                            it.getAttribute("install").trim(),
                            it.getAttribute("version").trim(),
                            getTempFeatures(it),
                            getBundlesFromFeature(it))
                }

        return features
    }

    private fun getTempFeatures(feature: Element): MutableList<Feature> {

        return (0 until feature.childNodes.length)
                .map { feature.childNodes.item(it) }
                .filterIsInstance<Element>()
                .filter { it.tagName == "feature" }
                .map {
                    Feature(
                            name = it.textContent.trim(),
                            install = it.getAttribute("install").trim(),
                            version = it.getAttribute("version").trim())
                }.toMutableList()

    }

    private fun getBundlesFromFeature(feature: Element): List<Bundle> {
        return (0 until feature.childNodes.length)
                .map { feature.childNodes.item(it) }
                .filterIsInstance<Element>()
                .filter { it.tagName == "bundle" }
                .map {
                    it.textContent.trim()
                            .removePrefix("wrap:")
                            .removePrefix("mvn:")
                            .removePrefix("file:")
                            .split("/")
                }
                .map { Bundle(it[0], it[1], it[2]) }
    }

    private fun selectedFeatureFile() {
        featuresView.root.populate {
            parent ->
            if (parent == featuresView.rootNode) {
                featureFileView.listView.selectedItem?.features
            } else if (parent.value is Feature) {
                (parent.value as Feature).features + (parent.value as Feature).bundles
            } else {
                null
            }
        }
    }

    private fun linkFeatures() {
        val features = featureFiles.flatMap { it.features }

        for (feature in features) {
            for (i in 0 until feature.features.size) {
                val childFeature = feature.features[i]
                feature.features[i] = features.find { it.name == childFeature.name } ?: childFeature
            }
            feature.features.sortBy(Feature::name)
        }

        featureFiles.forEach {
            for (i in 0 until it.features.size) {
                val feature = it.features[i]
                it.features[i] = features.find { it.name == feature.name } ?: feature
            }
            it.features.sortBy(Feature::name)
        }

        featureFiles.sortBy(FeatureFile::name)
    }
}
