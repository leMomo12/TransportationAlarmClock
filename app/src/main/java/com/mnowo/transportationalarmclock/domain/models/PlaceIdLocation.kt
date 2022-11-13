package com.mnowo.transportationalarmclock.domain.models

data class PlaceIdLocation(
    val result: Result,
    val status: String
) {
    data class PlaceIdLocation(
        val result: Result,
        val status: String
    )

    data class Result(
        val geometry: Geometry
    )

    data class Geometry(
        val location: Location,
    )

    data class Location(
        val lat: Double,
        val lng: Double
    )
}