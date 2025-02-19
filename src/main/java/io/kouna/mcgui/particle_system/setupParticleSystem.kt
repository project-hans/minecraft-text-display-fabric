package io.kouna.mcgui.particle_system

import io.kouna.mcgui.utilities.*
import org.bukkit.NamespacedKey
import kotlin.random.Random

fun setupParticleSystem() {
    onSpawnEntity {
        val paletteName = it.persistentDataContainer.getString(NamespacedKey.fromString("flame_particles:palette")!!) ?: return@onSpawnEntity

        val palette = when (paletteName) {
            "blue_to_orange" -> blueToOrangeFlamePalette
            "orange" -> orangeFlamePalette
            "black" -> blackFlamePalette
            else -> blackFlamePalette
        }

        for (i in 0 until 10) particles += FlameParticle(
            world = it.world,
            position = it.location.toVector(),
            palette = palette
        )
    }

    onSpawnEntity {
        val amount = it.persistentDataContainer.getInt(NamespacedKey.fromString("water_splash_particles:amount")!!) ?: 0
        val minSize = it.persistentDataContainer.getDouble(NamespacedKey.fromString("water_splash_particles:min_size")!!) ?: .1
        val maxSize = it.persistentDataContainer.getDouble(NamespacedKey.fromString("water_splash_particles:max_size")!!) ?: (minSize * 1.75)

        val minSpeed = it.persistentDataContainer.getDouble(NamespacedKey.fromString("water_splash_particles:min_speed")!!) ?: .3
        val maxSpeed = it.persistentDataContainer.getDouble(NamespacedKey.fromString("water_splash_particles:max_speed")!!) ?: (minSpeed * 2.3)

        val upAngleBias = it.persistentDataContainer.getInt(NamespacedKey.fromString("water_splash_particles:up_angle_bias")!!) ?: 4

        for (i in 0 until amount) particles += WaterParticle(
            world = it.world,
            source = it.location.toVector(),
            position = it.location.toVector().apply {
                val variance = 12/16.0
                x += Random.nextDouble(-variance, variance)
                z += Random.nextDouble(-variance, variance)
            },
            minSize = minSize,
            maxSize = maxSize,
            minSpeed = minSpeed,
            maxSpeed = maxSpeed,
            upAngleBias = upAngleBias
        )
    }

    onSpawnEntity {
        val maxFireflies = it.persistentDataContainer.getInt(NamespacedKey.fromString("firefly_particles:amount")!!) ?: return@onSpawnEntity

        val range = 7.0
        val spawnRange = .0

        val position = it.location.toVector()

        val fireflies = particles.filterIsInstance<FireFlyParticle>().filter { firefly ->
            firefly.position.distance(position) < range
        }

        for (firefly in fireflies) firefly.keepAlive()

        for (i in fireflies.size until maxFireflies) particles += FireFlyParticle(
            world = it.world,
            position = position.clone().apply {
                x += Random.nextDouble(-spawnRange, spawnRange + .0001)
                z += Random.nextDouble(-spawnRange, spawnRange + .0001)
            }
        )

    }

    onTick {
        particles.toList().forEach { it.update() }
    }
}

internal val particles = mutableListOf<Particle>()

interface Particle {
    fun update()
}