package net.ccbluex.liquidbounce.features.module.modules.movement.longjumps.vulcan

import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.event.JumpEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.longjumps.LongJumpMode
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.minecraft.network.play.client.C03PacketPlayer.C04PacketPlayerPosition
import net.minecraft.network.play.server.S08PacketPlayerPosLook

class Vulcan2Longjump : LongJumpMode("Vulcan2") {
    private var ticks = 0
    private var cancelFlag = false
    private var jumped = false
    //Vulcan LongJump Bypass - by FDP Team
    
    override fun onEnable() {
        longjump.noTimerModify = true
        if(!mc.thePlayer.onGround) {
            onAttemptDisable()
        }
        jumped = false
        mc.timer.timerSpeed = 0.5f
        ticks = 0
    }
    
    override fun onDisable() {
        longjump.noTimerModify = false
        MovementUtils.resetMotion(false)
        mc.timer.timerSpeed = 1.0f
    }
    
    override fun onUpdate(event: UpdateEvent) {
        if (!jumped)
            return

        mc.timer.timerSpeed = 0.5f
        if (mc.thePlayer.fallDistance > 0 && ticks % 2 == 0 && mc.thePlayer.fallDistance < 2.2) 
            mc.thePlayer.motionY += 0.14
        
        ticks++
        
        when (ticks) {
            1 -> {
                mc.netHandler.addToSendQueue(C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, true))
                mc.netHandler.addToSendQueue(C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY - 0.0784, mc.thePlayer.posZ, false))
                mc.netHandler.addToSendQueue(C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, true))
                MovementUtils.strafe(7.9f)
                mc.thePlayer.motionY = 0.41999998688698
                cancelFlag = true
            }
            
            2 -> {
                mc.thePlayer.motionY += 0.1
                MovementUtils.strafe(2.79f)
            }
            
            3 -> MovementUtils.strafe(2.56f)
            
            4 -> {
                MovementUtils.strafe(0.49f)
                mc.thePlayer.onGround = true
            }
            
            5 -> MovementUtils.strafe(0.59f)
            
            6 -> MovementUtils.strafe(0.3f)
        }
    }
    
    override fun onPacket(event: PacketEvent) {
        val packet = event.packet
        if (packet is S08PacketPlayerPosLook && cancelFlag) {
            cancelFlag = false
            event.cancelEvent()
        }

    }
    
    override fun onAttemptDisable() {
        mc.timer.timerSpeed = 1.0f
        longjump.state = false
    }
    
    override fun onAttemptJump() {
        ticks = 0
        jumped = true
    }
    
    override fun onJump(event: JumpEvent) {
        event.cancelEvent()
        ticks = 0
        jumped = true
    }
}
