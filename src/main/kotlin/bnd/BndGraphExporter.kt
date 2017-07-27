//package bnd
//
//import java.io.File
//import java.util.jar.Manifest
//
//val excludePattern = Regex("\\.\\w*|node|node_modules|java|webapp|schemas|test|src")
//
//fun main(args: Array<String>) {
//    val path = args.asList().getOrElse(0) { "C:\\Users\\TravisMcMahon\\Development\\ddf" }
//
//    val files = File(path).walkTopDown()
//            .onEnter { !it.name.matches(excludePattern) }
//            .filter { it.name == "MANIFEST.MF" }
//
//    val bndManifests = files.map { Manifest(it.inputStream()) }
//            .filter { it.mainAttributes.getValue("Bundle-SymbolicName") != null }
//            .map {
//                BundleManifest(
//                        it.mainAttributes.getValue("Bundle-SymbolicName"),
//                        parsePackage(it.mainAttributes.)
//                )
//            }
//
//    println(bndManifests.count())
//}
//
//fun parsePackage(s: String): Package {
//
//}
