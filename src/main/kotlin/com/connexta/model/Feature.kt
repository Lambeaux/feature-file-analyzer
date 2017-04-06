package com.connexta.model

data class Feature(
        val name: String,
        val install: String,
        val version: String,
        val features: MutableList<Feature> = mutableListOf(),
        val bundles: List<Bundle> = listOf())
