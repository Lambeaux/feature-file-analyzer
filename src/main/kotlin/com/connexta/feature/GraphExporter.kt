package com.connexta.feature

import com.connexta.feature.model.Bundle
import com.connexta.feature.model.Feature
import com.connexta.feature.model.FeatureFile
import java.io.File
import java.io.FileWriter
import java.time.LocalDateTime
import javax.xml.stream.XMLOutputFactory
import javax.xml.stream.XMLStreamWriter

val excludePattern = Regex("\\.\\w*|target|node|node_modules|java|webapp|schemas|test")

fun main(args: Array<String>) {
    val path = args.asList().getOrElse(0) { "C:\\Users\\TravisMcMahon\\Development\\ddf" }

    val resultPath = args.asList().getOrElse(1) { "C:\\Users\\TravisMcMahon\\Desktop\\test" }

    val files = File(path).walkTopDown()
            .onEnter { !it.name.matches(excludePattern) }
            .filter { it.name == "features.xml" }
            .filter { namePattern.containsMatchIn(it.canonicalPath) }

    val featureFiles = files.map(::parseFeatureFile).toList()
    linkFeatures(featureFiles)

    featureFiles.map { writeGraph(it, "$resultPath\\${it.name}.gexf") }

    val features = featureFiles.flatMap { it.features }.toMutableList()
    writeGraph(FeatureFile("DDF", features), "$resultPath\\CombinedProject.gexf")
}

fun writeGraph(featureFile: FeatureFile, path: String) {
    val writer = XMLOutputFactory.newFactory().createXMLStreamWriter(FileWriter(path))

    writer.document {
        element("gexf") {
            attribute("xmlns", "http://www.gexf.net/1.2draft")
            attribute("version", "1.1")

            element("meta") {
                attribute("lastmodifieddate", LocalDateTime.now().toString())

                element("creator", "Travis McMahon")
                element("description", "A graph file for ${featureFile.name}")
            }

            element("graph") {
                attribute("mode", "static")
                attribute("defaultedgetype", "directed")

                element("attributes") {
                    attribute("class", "node")

                    element("attribute") {
                        attribute("id", "0")
                        attribute("title", "bundle?")
                        attribute("type", "boolean")
                        element("default", "false")
                    }
                }

                element("nodes") {
                    val featureMap = hashMapOf<Feature, String>()

                    fun writeBundle(bundle: Bundle, parent: Feature) {

                        element("node") {
                            attribute("id", "${parent.name}:${bundle.groupId}/${bundle.artifactId}/${bundle.version}")
                            attribute("label", bundle.artifactId)
                            element("attvalues") {
                                element("attvalue") {
                                    attribute("for", "0")
                                    attribute("value", "true")
                                }
                            }
                        }
                    }

                    fun writeFeature(feature: Feature) {
                        if (featureMap.containsKey(feature)) return
                        featureMap.put(feature, feature.name)

                        element("node") {
                            attribute("id", feature.name)
                            attribute("label", feature.name)
                            element("attvalues") {
                                element("attvalue") {
                                    attribute("for", "0")
                                    attribute("value", "false")
                                }
                            }
                        }

                        feature.features.forEach(::writeFeature)
                        feature.bundles.forEach { writeBundle(it, feature) }
                    }


                    for (feature in featureFile.features) {
                        writeFeature(feature)
                    }
                }

                element("edges") {
                    val edgeList = arrayListOf<String>()
                    fun writeFeature(feature: Feature) {
                        for (child in feature.features) {
                            val id = "${feature.name}:${child.name}"
                            if (edgeList.contains(id)) continue
                            edgeList.add(id)
                            element("edge") {
                                attribute("id", id)
                                attribute("source", feature.name)
                                attribute("target", child.name)
                            }

                            writeFeature(child)
                        }

                        for ((artifactId, groupId, version) in feature.bundles) {
                            val id = "${feature.name}:$groupId/$artifactId/$version"
                            if (edgeList.contains(id)) continue
                            edgeList.add(id)
                            element("edge") {
                                attribute("id", "")
                                attribute("source", feature.name)
                                attribute("target", id)
                            }
                        }
                    }

                    for (feature in featureFile.features) {
                        writeFeature(feature)
                    }
                }
            }
        }
    }.flush()
}


fun XMLStreamWriter.document(init: XMLStreamWriter.() -> Unit): XMLStreamWriter {
    this.writeStartDocument()
    this.init()
    this.writeEndDocument()
    return this
}

fun XMLStreamWriter.element(name: String, init: XMLStreamWriter.() -> Unit): XMLStreamWriter {
    this.writeStartElement(name)
    this.init()
    this.writeEndElement()
    return this
}

fun XMLStreamWriter.element(name: String, content: String) {
    element(name) {
        writeCharacters(content)
    }
}

fun XMLStreamWriter.attribute(name: String, value: String) = writeAttribute(name, value)