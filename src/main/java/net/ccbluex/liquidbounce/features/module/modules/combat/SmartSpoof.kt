/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.combat

import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.utils.PacketUtils
import net.ccbluex.liquidbounce.value.IntegerValue
import net.minecraft.network.play.server.S12PacketEntityVelocity
import net.minecraft.network.play.server.S08PacketPlayerPosLook
import net.ccbluex.liquidbounce.utils.extensions.getDistanceToEntityBox
import net.minecraft.network.Packet
import net.minecraft.network.play.INetHandlerPlayClient
import java.util.concurrent.LinkedBlockingQueue

@ModuleInfo(name = "SmartSpoof", category = ModuleCategory.COMBAT)
object SmartSpoof : Module() {

    private val btDelay = IntegerValue("BacktrackDelay", 300, 0, 500)
    private val velocityDelay = IntegerValue("VelocityDelay", 50, 0, 500)

    private val packets = LinkedBlockingQueue<Packet<INetHandlerPlayClient>>()
    private val times = ArrayList<Long>()

    private var delay = 0L
    private var targetDelay = 0L

    override fun onEnable() {
        packets.clear()
        times.clear()
    }

    override fun onDisable() {
        while (!packets.isEmpty()) {
            PacketUtils.handlePacket(packets.take() as Packet<INetHandlerPlayClient?>)
        }
        times.clear()
    }

    @EventTarget
    fun onAttack(event: AttackEvent) {
        if (mc.thePlayer.getDistanceToEntityBox(event.targetEntity) > 2.6f) {
            targetDelay = btDelay.get().toLong()
        }
    }


    @EventTarget
    fun onPacket(event: PacketEvent) {
        val packet = event.packet
        if (packet.javaClass.simpleName.startsWith("S", ignoreCase = true)) {
            if (packet is S12PacketEntityVelocity) targetDelay = velocityDelay.get().toLong()
            if (packet is S08PacketPlayerPosLook || mc.thePlayer.ticksExisted < 20) {
                targetDelay = 0L
                while (!packets.isEmpty()) {
                    PacketUtils.handlePacket(packets.take() as Packet<INetHandlerPlayClient?>)
                }
                times.clear()
                return
            }
            event.cancelEvent()
            times.add(System.currentTimeMillis())
            packets.add(packet as Packet<INetHandlerPlayClient>)
        }
    }

    @EventTarget
    fun onWorld(event: WorldEvent) {
        times.clear()
        packets.clear()
    }

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        delay = targetDelay
        if (!packets.isEmpty()) {
            while (times.first() < System.currentTimeMillis() - delay) {
                PacketUtils.handlePacket(packets.take() as Packet<INetHandlerPlayClient?>)
                times.remove(times.first())
            }
        }
    }
}