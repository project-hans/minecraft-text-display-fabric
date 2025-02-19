package io.kouna.mcgui.utilities

import org.bukkit.Location
import org.bukkit.util.Vector
import org.joml.*
import java.lang.Math
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.sign
import kotlin.math.sqrt

val DOWN_VECTOR; get () = Vector(0, -1, 0)
val UP_VECTOR; get () = Vector(0, 1, 0)
val FORWARD_VECTOR; get () = Vector(0, 0, 1)
val BACKWARD_VECTOR; get () = Vector(0, 0, -1)
val LEFT_VECTOR; get () = Vector(-1, 0, 0)
val RIGHT_VECTOR; get () = Vector(1, 0, 0)


fun Vector3f.toVector4f() = Vector4f(x, y, z, 1f)
fun Vector4f.toVector3f() = Vector3f(x, y, z)

fun Vector.copy(vector: Vector3d): Vector {
    this.x = vector.x
    this.y = vector.y
    this.z = vector.z
    return this
}

fun Vector.copy(vector: Vector3f): Vector {
    this.x = vector.x.toDouble()
    this.y = vector.y.toDouble()
    this.z = vector.z.toDouble()
    return this
}

fun Vector.pitch(): Float {
    return atan2(y, sqrt(x * x + z * z)).toFloat()
}

fun Vector.yaw(): Float {
    return -atan2(-x, z).toFloat()
}

fun Vector.rotate(quaternion: Quaterniond) = copy(Vector3d(x, y, z).rotate(quaternion))

fun Location.yawRadians(): Float {
    return -yaw.toRadians()
}

fun Location.pitchRadians(): Float {
    return pitch.toRadians()
}

fun Location.getQuaternion(): Quaternionf {
    return Quaternionf().rotateYXZ(yawRadians(), pitchRadians(), 0f)
}

fun Quaterniond.transform(vector: Vector): Vector {
    vector.copy(this.transform(vector.toVector3d()))
    return vector
}


fun Double.lerp(other: Double, t: Double): Double {
    return this * (1 - t) + other * t
}

fun Float.lerp(other: Float, t: Float): Float {
    return this * (1 - t) + other * t
}

fun Int.lerpSafely(other: Int, t: Double): Int {
    val result = this.toDouble().lerp(other.toDouble(), t).toInt()
    if (result == this && t != .0) return this.moveTowards(other, 1)
    return result
}

fun Double.moveTowards(target: Double, speed: Double): Double {
    val distance = target - this
    return if (abs(distance) < speed) target else this + speed * distance.sign
}

fun Float.moveTowards(target: Float, speed: Float): Float {
    val distance = target - this
    return if (abs(distance) < speed) target else this + speed * distance.sign
}

fun Int.moveTowards(target: Int, speed: Int): Int {
    val distance = target - this
    return if (abs(distance) < speed) target else this + speed * distance.sign
}

fun Double.toRadians(): Double {
    return Math.toRadians(this)
}

fun Float.toRadians(): Float {
    return Math.toRadians(this.toDouble()).toFloat()
}