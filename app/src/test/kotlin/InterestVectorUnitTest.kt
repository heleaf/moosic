package com.dev.moosic
import com.dev.moosic.models.SongFeatures
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.*

class InterestVectorUnitTest {
    val vec1 = mapOf("acousticness" to 0.9,
        "danceability" to 0.4, "energy" to 0.7,
        "instrumentalness" to 0.1, "liveness" to 0.03,
        "speechiness" to 0.082, "valence" to 0.2)
    val vec2 = mapOf("acousticness" to 0.9,
        "danceability" to 0.0, "energy" to 0.0,
        "instrumentalness" to 0.0, "liveness" to 0.0,
        "speechiness" to 0.0, "valence" to 0.0)

    val epsilon = (10.0).pow(-6)

    @Test
    fun dotProduct_isCorrect() {
        assertTrue(SongFeatures.dot(vec1, vec1) - getTestDot(vec1, vec1) < epsilon)
        assertTrue(SongFeatures.dot(vec2, vec2) - getTestDot(vec2, vec2) < epsilon)
        assertTrue(SongFeatures.dot(vec1, vec2) - getTestDot(vec1, vec2) < epsilon)
        assertTrue(SongFeatures.dot(vec1, vec2) - SongFeatures.dot(vec2, vec1) < epsilon)
        assertTrue(SongFeatures.dot(vec1, vec2) - getTestDot(vec2, vec1) < epsilon)
    }

    @Test
    fun magnitude_isCorrect() {
        assertTrue(SongFeatures.magnitude(vec2) - getTestMagnitude(vec2) < epsilon)
        assertTrue(SongFeatures.magnitude(vec1) - getTestMagnitude(vec1) < epsilon)
        assertTrue(SongFeatures.magnitude(vec1) >= 0.0)
        assertTrue(SongFeatures.magnitude(vec2) >= 0.0)
    }

    fun getTestMagnitude(vec : Map<String, Double>): Double {
        val values = vec.values
        val magnitude = sqrt(
            values.fold(
                0.0
            ) { accumulator, newval -> accumulator + newval.pow(2) }
        )
        return magnitude
    }

    fun getTestDot(v1 : Map<String, Double>, v2 : Map<String, Double>) : Double {
        val v1values = v1.values.toList()
        val v2values = v2.values.toList()
        return (v1values.indices.toList()).fold(
            0.0
        ) { accumulator, index -> accumulator + (v1values.get(index) * v2values.get(index))}

    }
}