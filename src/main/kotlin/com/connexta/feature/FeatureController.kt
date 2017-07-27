package com.connexta.feature

import com.connexta.feature.model.Feature
import com.connexta.feature.model.FeatureFile
import com.connexta.feature.view.FeatureFileView
import com.connexta.feature.view.FeaturesView
import org.graphstream.graph.Edge
import org.graphstream.graph.Graph
import org.graphstream.graph.Node
import org.graphstream.graph.implementations.SingleGraph
import tornadofx.Controller
import tornadofx.observable
import tornadofx.onUserSelect
import tornadofx.selectedItem
import java.io.File

class FeatureController : Controller() {

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

        val exclude = Regex("\\.\\w*|target|node|node_modules|java|webapp|schemas|test")

        val files = root.walkTopDown()
                .onEnter { !it.name.matches(exclude) }
                .filter { it.name == "features.xml" }
                .filter { namePattern.containsMatchIn(it.canonicalPath) }

        files.mapTo(featureFiles, ::parseFeatureFile)
        linkFeatures(featureFiles)
        featureFiles.sortBy(FeatureFile::name)
    }

    private fun selectedFeatureFile() {
//        featuresView.root.populate {
//            parent ->
//            if (parent == featuresView.rootNode) {
//                featureFileView.listView.selectedItem?.features
//            } else if (parent.value is Feature) {
//                (parent.value as Feature).features + (parent.value as Feature).bundles
//            } else {
//                null
//            }
//        }

        val featureFile = featureFileView.listView.selectedItem!!
        createGraph(featureFile)
    }

    private fun createFeatureNode(graph: Graph, feature: Feature, parent: Node) {
        var featureNode = graph.getNode<Node>(feature.name)

        if (featureNode == null) {
            featureNode = graph.addNode<Node>(feature.name)
            featureNode.setAttribute("name", feature.name)
            if (feature.name.matches(namePattern)) featureNode.setAttribute("ui.class", "app")
            else featureNode.setAttribute("ui.class", "feature")


            val featureEdge = graph.getEdge<Edge>("${parent.id}:${featureNode.id}")
            if (featureEdge == null) graph.addEdge<Edge>("${parent.id}:${featureNode.id}", parent.id, featureNode.id)
        }

        for (bundle in feature.bundles) {
            val bundleId = "${feature.name}:${bundle.artifactId}"
            var bundleNode = graph.getNode<Node>(bundleId)

            if (bundleNode == null) {
                bundleNode = graph.addNode(bundleId)
                bundleNode.setAttribute("name", bundle.artifactId)
                bundleNode.setAttribute("ui.class", "bundle")
            }

            val bundleEdge = graph.getEdge<Edge>(bundleId)
            if (bundleEdge == null) graph.addEdge<Edge>(bundleId, featureNode.id, bundleNode.id)
        }

        for (child in feature.features) createFeatureNode(graph, child, featureNode)
    }

    private fun createGraph(featureFile: FeatureFile) {
        val graph = SingleGraph("Feature Graph")
        graph.setAttribute("ui.stylesheet", "node{text-size: 12;}node.bundle{fill-color: blue;} node.feature{fill-color: red;} node.app{fill-color: yellow;} node.root{fill-color: green;}")
        graph.setAttribute("ui.quality")
        graph.setAttribute("ui.antialias")

        val rootNode = graph.addNode<Node>("Feature File: ${featureFile.name}")
        rootNode.setAttribute("name", featureFile.name)
        rootNode.setAttribute("ui.class", "root")

        for (feature in featureFile.features) {
            createFeatureNode(graph, feature, rootNode)
        }

        for (node in graph.getNodeSet<Node>()) {
            node.addAttribute("ui.label", node.getAttribute("name"))
        }
        val viewer = graph.display()
    }
}
