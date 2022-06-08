package higtools.modules.player

import higtools.MeteorModule
import higtools.modules.HIGTools
import meteordevelopment.meteorclient.events.packets.PacketEvent
import meteordevelopment.orbit.EventHandler
import net.minecraft.client.gui.screen.ConnectScreen
import net.minecraft.client.network.ServerAddress
import net.minecraft.client.network.ServerInfo
import net.minecraft.network.packet.c2s.handshake.HandshakeC2SPacket
import net.minecraft.network.packet.s2c.play.DisconnectS2CPacket

class AutoReconnect:MeteorModule(HIGTools.HIG, "AutoReconnect", "Automatically reconnect") {

    private lateinit var serverInfo:ServerInfo

    @EventHandler
    fun pack(event:PacketEvent.Send) {
        if (event.packet is HandshakeC2SPacket) {
            if (mc.currentServerEntry != null) {
                serverInfo = mc.currentServerEntry!!
            }
        }
    }

    @EventHandler
    fun pack1(event:PacketEvent.Receive) {
        if (event.packet is DisconnectS2CPacket) {
            Thread {
                if (this@AutoReconnect::serverInfo.isInitialized) {
                    Thread.sleep(50L)
                    mc.submit {
                        ConnectScreen.connect(mc.currentScreen,
                                              mc,
                                              ServerAddress.parse(serverInfo.address),
                                              serverInfo)
                    }
                }
            }.start()
        }
    }

}
