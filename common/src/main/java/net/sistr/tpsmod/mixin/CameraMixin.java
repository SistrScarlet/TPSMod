package net.sistr.tpsmod.mixin;

import net.minecraft.client.render.Camera;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.BlockView;
import net.sistr.tpsmod.config.TPSModConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
public abstract class CameraMixin {

    @Shadow
    private boolean ready;

    @Shadow
    private BlockView area;

    @Shadow
    private Entity focusedEntity;

    @Shadow
    private boolean thirdPerson;

    @Shadow
    protected abstract void setRotation(float yaw, float pitch);

    @Shadow
    protected abstract void setPos(double x, double y, double z);

    @Shadow
    private float yaw;

    @Shadow
    private float pitch;

    @Shadow
    protected abstract void moveBy(double x, double y, double z);

    @Shadow
    protected abstract double clipToSpace(double desiredCameraDistance);

    @Shadow
    private float lastCameraY;

    @Shadow
    private float cameraY;

    @Inject(method = "update", at = @At("HEAD"), cancellable = true)
    public void onUpdate(BlockView area, Entity focusedEntity, boolean thirdPerson, boolean inverseView, float tickDelta, CallbackInfo ci) {
        if (!thirdPerson || inverseView) {
            return;
        }
        ci.cancel();
        this.ready = true;
        this.area = area;
        this.focusedEntity = focusedEntity;
        this.thirdPerson = thirdPerson;
        this.setRotation(focusedEntity.getYaw(tickDelta), focusedEntity.getPitch(tickDelta));
        this.setPos(
                MathHelper.lerp(tickDelta, focusedEntity.prevX, focusedEntity.getX()),
                MathHelper.lerp(tickDelta, focusedEntity.prevY, focusedEntity.getY())
                        + MathHelper.lerp(tickDelta, this.lastCameraY, this.cameraY),
                MathHelper.lerp(tickDelta, focusedEntity.prevZ, focusedEntity.getZ())
        );
        float yaw = this.yaw;
        float pitch = this.pitch;
        this.setRotation(this.yaw + TPSModConfig.CAMERA_POS_YAW, this.pitch + TPSModConfig.CAMERA_POS_PITCH);
        this.moveBy(-this.clipToSpace(TPSModConfig.CAMERA_DISTANCE), 0.0, 0.0);
        this.setRotation(yaw + TPSModConfig.CAMERA_YAW, pitch + TPSModConfig.CAMERA_PITCH);
    }
}
