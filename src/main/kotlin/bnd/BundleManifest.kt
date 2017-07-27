package bnd

data class Version(val majorInclusive: Boolean, val microInclusive: Boolean,
                   val major: Int, val minor: Int, val micro: Int) {
    operator fun compareTo(other: Version): Int {
        return major - other.major +
                minor - other.minor +
                micro - other.micro
    }
}

data class Package(val name: String, val version: Version)

data class BundleManifest(val symbolicName: String,
                          val bundlePackage: Package,
                          val importPackages: List<Package>,
                          val exportPackages: List<Package>,
                          val embeddedDependencies: List<String>)
