package com.sistr.tpscamera;

import com.sistr.tpscamera.client.CustomThirdPersonRender;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.MovementInput;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.client.event.InputUpdateEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ForgeEventHandlers {
    private static final CustomThirdPersonRender THIRD_PERSON_RENDER = new CustomThirdPersonRender();

    //移動方向をカメラ基準に
    //ただしダッシュ中は無視
    @SubscribeEvent
    public static void onInputUpdate(InputUpdateEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.gameSettings.thirdPersonView == 1 && !mc.gameSettings.keyBindSprint.isKeyDown()) {
            PlayerEntity player = TPSCameraMod.proxy.getClientPlayer();
            if (player.isSprinting() || player.isShiftKeyDown()) {
                return;
            }
            MovementInput input = event.getMovementInput();
            float tempForward = input.moveForward;
            float tempStrife = input.moveStrafe;
            if (tempForward == 0 && tempStrife == 0) {
                return;
            }
            float yaw = (float) (MathHelper.atan2(tempStrife, tempForward) * (180 / Math.PI));
            yaw += player.rotationYaw - THIRD_PERSON_RENDER.cameraYaw;
            Vec3d move = Vec3d.fromPitchYaw(0, -yaw);
            if (((ClientPlayerEntity)player).func_228354_I_()) {
                move = move.scale(0.3);
            }
            input.moveForward = (float) move.z;
            input.moveStrafe = (float) move.x;
        }
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            THIRD_PERSON_RENDER.tick();
        }
    }

    @SubscribeEvent
    public static void onDisplayRender(EntityViewRenderEvent.CameraSetup event) {
        ActiveRenderInfo info = event.getInfo();
        if (info.isThirdPerson() && Minecraft.getInstance().gameSettings.thirdPersonView == 1) {
            float partialTicks = (float) event.getRenderPartialTicks();
            THIRD_PERSON_RENDER.render(info.getRenderViewEntity(), info, partialTicks);
            event.setPitch(MathHelper.lerp(partialTicks, THIRD_PERSON_RENDER.cameraPrevPitch, THIRD_PERSON_RENDER.cameraPitch));
            event.setYaw(MathHelper.lerp(partialTicks, THIRD_PERSON_RENDER.cameraPrevYaw, THIRD_PERSON_RENDER.cameraYaw));
        } else {
            THIRD_PERSON_RENDER.reset();
        }
    }


}
