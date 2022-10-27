package highwaytools

import meteordevelopment.meteorclient.MeteorClient.mc
import meteordevelopment.meteorclient.settings.*
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import kotlin.math.abs
import kotlin.math.sign
import kotlin.reflect.KProperty

// sad attempt to adapt Llama code to Meteor Addon code

typealias MeteorModule = meteordevelopment.meteorclient.systems.modules.Module

operator fun <T> Setting<T>.setValue(thisRef:Any?, property:KProperty<*>, value:T) {
    this.set(value)
}

operator fun <T> Setting<T>.getValue(thisRef:Any?, property:KProperty<*>):T {
    return this.get()
}

val MeteorModule.mainGroup:SettingGroup
    get() = this.settings.defaultGroup

fun IValue(
    name:String,
    value:Int,
    description:String = "",
    range:IntRange,
    step:Int,
    visibility:() -> Boolean = { true },
    consumer:(input:Int) -> Unit = { },
):IntSetting {
    return IntSetting.Builder()
        .name(name)
        .defaultValue(value)
        .description(description)
        .range(range.first, range.last)
        .sliderRange(range.first, range.last)
        .visible(visibility)
        .onChanged(consumer)
        .build()
}

fun DValue(
    name:String,
    value:Double,
    description:String = "",
    range:ClosedFloatingPointRange<Double>,
    step:Double,
    visibility:() -> Boolean = { true },
    consumer:(input:Double) -> Unit = { },
):DoubleSetting {
    return DoubleSetting.Builder()
        .name(name)
        .defaultValue(value)
        .description(description)
        .range(range.start, range.endInclusive)
        .sliderRange(range.start, range.endInclusive)
        .visible(visibility)
        .onChanged(consumer)
        .build()
}

fun BValue(
    name:String,
    value:Boolean,
    description:String = "",
    visibility:() -> Boolean = { true },
    consumer:(input:Boolean) -> Unit = { },
):BoolSetting {
    return BoolSetting.Builder()
        .name(name)
        .defaultValue(value)
        .description(description)
        .visible(visibility)
        .onChanged(consumer)
        .build()
}

fun <T:Enum<T>> EValue(
    name:String,
    value:T,
    description:String = "",
    visibility:() -> Boolean = { true },
    consumer:(input:T) -> Unit = { },
):EnumSetting<T> {
    return EnumSetting.Builder<T>()
        .name(name)
        .defaultValue(value)
        .description(description)
        .visible(visibility)
        .onChanged(consumer)
        .build()
}

fun SValue(
    name:String,
    value:String,
    description:String = "",
    visibility:() -> Boolean = { true },
    consumer:(input:String) -> Unit = { },
):StringSetting {
    return StringSetting.Builder()
        .name(name)
        .defaultValue(value)
        .description(description)
        .visible(visibility)
        .onChanged(consumer)
        .build()
}

fun BlockPos.left(int:Int):BlockPos {
    return when (mc.player!!.horizontalFacing) {
        Direction.SOUTH -> this.east(int)
        Direction.NORTH -> this.west(int)
        Direction.WEST -> this.south(int)
        else -> this.north(int)
    }
}

fun BlockPos.right(int:Int):BlockPos {
    return when (mc.player!!.horizontalFacing) {
        Direction.SOUTH -> this.west(int)
        Direction.NORTH -> this.east(int)
        Direction.WEST -> this.north(int)
        else -> this.south(int)
    }
}

fun BlockPos.forward(int:Int):BlockPos {
    return when (mc.player!!.horizontalFacing) {
        Direction.SOUTH -> this.south(int)
        Direction.NORTH -> this.north(int)
        Direction.WEST -> this.west(int)
        else -> this.east(int)
    }
}

fun BlockPos.backward(int:Int):BlockPos {
    return when (mc.player!!.horizontalFacing) {
        Direction.SOUTH -> this.north(int)
        Direction.NORTH -> this.south(int)
        Direction.WEST -> this.east(int)
        else -> this.west()
    }
}

fun BlockPos.mutX(x:Int):BlockPos {
    return BlockPos(this.x + x, this.y, this.z)
}

fun BlockPos.mutY(y:Int):BlockPos {
    return BlockPos(this.x, this.y + y, this.z)
}

fun BlockPos.mutZ(z:Int):BlockPos {
    return BlockPos(this.x, this.y, this.z + z)
}

fun BlockPos.mutXZ(x:Int, z:Int):BlockPos {
    return BlockPos(this.x + x, this.y, this.z + z)
}

val highway:Int
    get() {
        val x = abs(mc.player!!.z) < 5
        val z = abs(mc.player!!.x) < 5
        val xp = sign(mc.player!!.x) == 1.0
        val zp = sign(mc.player!!.z) == 1.0
        val diag = abs(abs(mc.player!!.x) - abs(mc.player!!.z)) < 5
        return if (x && xp) 1 else if (x && !xp) 2 else if (z && zp) 3 else if (z && !zp) 4 else if (diag && xp && zp) 5 else if (diag && !xp && zp) 6 else if (diag && xp && !zp) 7 else if (diag && !xp && !zp) 8 else -1
    }

private val packetToClient = HashMap<Int, Int>()
private val clientToPacket = HashMap<Int, Int>()

fun csToPs(clientSlot:Int):Int {
    return clientToPacket.getOrDefault(clientSlot, -1)
}

fun psToCs(packetSlot:Int):Int {
    return packetToClient.getOrDefault(packetSlot, -1)
}

fun setCs2Ps() {
    packetToClient[5] = 39
    clientToPacket[39] = 5
    packetToClient[6] = 38
    clientToPacket[38] = 6
    packetToClient[7] = 37
    clientToPacket[37] = 7
    packetToClient[8] = 36
    clientToPacket[36] = 8
    packetToClient[45] = 40
    clientToPacket[40] = 45
    for (i in 9..35) {
        packetToClient[i] = i
        clientToPacket[i] = i
    }
    for (i in 0..8) {
        packetToClient[i + 36] = i
        clientToPacket[i] = i + 36
    }
}
