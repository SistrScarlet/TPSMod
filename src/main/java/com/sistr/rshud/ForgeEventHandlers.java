package com.sistr.rshud;

import com.sistr.rshud.client.CustomThirdPersonRender2;
import com.sistr.rshud.client.RSHUDRenderer;
import com.sistr.rshud.datagen.RSHUDTags;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.MovementInput;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.client.event.InputUpdateEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ForgeEventHandlers {
    private static final RSHUDRenderer RSHUD_RENDERER = new RSHUDRenderer();
    private static final CustomThirdPersonRender2 THIRD_PERSON_RENDER = new CustomThirdPersonRender2();

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onDisplayRender(RenderGameOverlayEvent.Pre event) {
        if (event.getType() != RenderGameOverlayEvent.ElementType.CROSSHAIRS) {
            return;
        }
        PlayerEntity player = RSHUDMod.proxy.getClientPlayer();
        ItemStack stack = player.getItemStackFromSlot(EquipmentSlotType.HEAD);
        if (!Config.RSHUD_ALWAYS.get()) {
            if (stack.isEmpty()) {
                return;
            }
            CompoundNBT tag = stack.getOrCreateTag();
            if (!tag.getBoolean(RSHUDMod.MODID + "CanRender") && !(RSHUDTags.Items.RSHUD_MOUNTED.contains(stack.getItem()))) {
                return;
            }
        }
        RSHUD_RENDERER.render(player, event.getWindow().getScaledWidth(), event.getWindow().getScaledHeight());
    }

    @SubscribeEvent
    public static void onGetToolTip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        CompoundNBT tag = stack.getOrCreateTag();
        if (tag.getBoolean(RSHUDMod.MODID + "CanRender")) {
            event.getToolTip().add(new TranslationTextComponent(RSHUDMod.MODID + ".modified.tooltip"));
        }
    }

    //移動方向をカメラ基準に
    //ただしダッシュ中は無視
    @SubscribeEvent
    public static void onInputUpdate(InputUpdateEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.gameSettings.thirdPersonView == 1 && !mc.gameSettings.keyBindSprint.isKeyDown()) {
            PlayerEntity player = RSHUDMod.proxy.getClientPlayer();
            if (player.isSprinting()) {
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
