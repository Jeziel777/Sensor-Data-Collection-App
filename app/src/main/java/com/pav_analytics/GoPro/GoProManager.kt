package com.pav_analytics.GoPro

// Define a data class to hold the command name and its corresponding byte array
data class Command(val name: String, val bytes: UByteArray)

// Create an object to hold the commands
object CommandStore {
    @OptIn(ExperimentalUnsignedTypes::class)
    val commands = listOf(
        Command("Load Video Preset Group", ubyteArrayOf(0x04U, 0x3EU, 0x02U, 0x03U, 0xE8U)),
        Command("Load Photo Preset Group", ubyteArrayOf(0x04U, 0x3EU, 0x02U, 0x03U, 0xE9U)),
        Command("Load Timelapse Preset Group", ubyteArrayOf(0x04U, 0x3EU, 0x02U, 0x03U, 0xEAU)),
        Command("Set Shutter Off", ubyteArrayOf(0x03U, 0x01U, 0x01U, 0x00U)),
        Command("Set Shutter On", ubyteArrayOf(0x03U, 0x01U, 0x01U, 0x01U)),
        Command("Set Video Resolution to 1080", ubyteArrayOf(0x03U, 0x02U, 0x01U, 0x09U)),
        Command("Set Video Resolution to 2.7K", ubyteArrayOf(0x03U, 0x02U, 0x01U, 0x04U)),
        Command("Set Video Resolution to 5K", ubyteArrayOf(0x03U, 0x02U, 0x01U, 0x18U)),
        Command("Set FPS to 24", ubyteArrayOf(0x03U, 0x03U, 0x01U, 0x0AU)),
        Command("Set FPS to 60", ubyteArrayOf(0x03U, 0x03U, 0x01U, 0x05U)),
        Command("Set FPS to 240", ubyteArrayOf(0x03U, 0x03U, 0x01U, 0x00U))
    )

    // Function to get a command by name
    fun getCommandByName(name: String): Command? {
        return commands.find { it.name == name }
    }

    // Function to get the byte array of a command by name
    @OptIn(ExperimentalUnsignedTypes::class)
    fun getCommandBytesByName(name: String): UByteArray? {
        return getCommandByName(name)?.bytes
    }

}

// Usage example
fun main() {
    val videoCommandBytes = CommandStore.getCommandBytesByName("Load Video Preset Group")
    videoCommandBytes?.let {
        println("Bytes: ${it.joinToString(" ") { byte -> "0x%02X".format(byte) }}")
    }
}
