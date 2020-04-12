package com.sistr.tpscamera.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.*;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.function.Predicate;

@OnlyIn(Dist.CLIENT)
public class CustomThirdPersonRender {
    public Entity followTarget;
    public Vec3d cameraPos;
    public Vec3d cameraPrevPos;
    public float cameraPitch;
    public float cameraYaw;
    public float cameraPrevPitch;
    public float cameraPrevYaw;
    public float prevPlayerPitch;
    public float prevPlayerYaw;
    public boolean shouldPlayerLookChange;
    public boolean cameraRight;
    public boolean cameraLock;

    //まずカメラ向きとプレイヤーの向きは直接関係ない
    //ただし、前回のプレイヤーの向きとの差異によってカメラの向きが変更される
    public void render(Entity entity, ActiveRenderInfo info, float partialTicks) {
        this.followTarget = entity;
        World world = entity.getEntityWorld();
        Vec3d entityPos = entity.getEyePosition(1F);

        if (cameraPos == null) {
            cameraPos = entityPos;
            cameraPrevPos = new Vec3d(entity.prevPosX, entity.prevPosY, entity.prevPosZ);
            cameraPitch = entity.rotationPitch;
            cameraYaw = entity.rotationYaw;
            cameraPrevPitch = entity.prevRotationPitch;
            cameraPrevYaw = entity.prevRotationYaw;
            prevPlayerPitch = cameraPitch;
            prevPlayerYaw = cameraYaw;
        }

        float pitchDiff = entity.rotationPitch - prevPlayerPitch;
        float yawDiff = entity.rotationYaw - prevPlayerYaw;
        cameraPitch += pitchDiff;
        cameraYaw += yawDiff;

        //プレイヤーの向きを変更
        //1tickに一回更新
        //それならtickでやれって？いや、そうすると描画がガタガタするのよ
        //ダッシュ中はブロック走査をスキップして常に正面を向かせる
        if (shouldPlayerLookChange && (entity.isSprinting() || Minecraft.getInstance().gameSettings.keyBindSprint.isKeyDown())) {
            entity.rotationYaw = cameraYaw;
            entity.rotationPitch = cameraPitch;
        } else if (shouldPlayerLookChange) {
            Vec3d cameraDirection = Vec3d.fromPitchYaw(cameraPitch, cameraYaw);
            double toLength = 64;
            Vec3d start = cameraPos.add(cameraDirection.scale(3));
            Vec3d end = start.add(cameraDirection.scale(toLength));
            RayTraceResult sightResult = world.rayTraceBlocks(new RayTraceContext(start, end, RayTraceContext.BlockMode.COLLIDER, RayTraceContext.FluidMode.NONE, entity));
            Vec3d lookPos = end;
            if (sightResult.getType() != RayTraceResult.Type.MISS) {
                lookPos = sightResult.getHitVec();
            }
            toLength = lookPos.subtract(cameraPos).length();
            Vec3d lookVec = cameraDirection.scale(toLength);
            AxisAlignedBB axis = new AxisAlignedBB(start, start.add(lookVec)).grow(1.0D);
            EntityRayTraceResult entityResult = CustomRayTraceEntities(entity, start, start.add(lookVec), axis, (target) ->
                    !target.isSpectator() && target.canBeCollidedWith(), toLength * toLength, new Vec3d(0.5, 8, 0.5));
            if (entityResult != null && entityResult.getType() != RayTraceResult.Type.MISS) {
                lookPos = entityResult.getEntity().getEyePosition(1F);
                double distanceSq = entityPos.squareDistanceTo(lookPos);
                if (6 * 6 < distanceSq) {
                    lookPos = lookPos.mul(1, 0, 1).add(0, entityResult.getHitVec().y, 0);
                }
            }
            Vec2f direction = getYawPitch(lookPos.subtract(entityPos));
            entity.rotationYaw = -direction.x;
            entity.rotationPitch = -direction.y;
        }
        shouldPlayerLookChange = false;

        info.update(world, entity, true, false, partialTicks);
        double x = MathHelper.lerp(partialTicks, cameraPrevPos.getX(), cameraPos.getX());
        double y = MathHelper.lerp(partialTicks, cameraPrevPos.getY(), cameraPos.getY());
        double z = MathHelper.lerp(partialTicks, cameraPrevPos.getZ(), cameraPos.getZ());
        info.setPosition(x, y, z);

        prevPlayerPitch = entity.rotationPitch;
        prevPlayerYaw = entity.rotationYaw;

        cameraPrevPitch = cameraPitch;
        cameraPrevYaw = cameraYaw;
    }

    //クライアントサイドのtick処理の一番最初に動く
    public void tick() {
        if (this.followTarget != null && cameraPos != null && Minecraft.getInstance().gameSettings.thirdPersonView == 1) {
            //一定以上離れたらリセット
            //この処理を入れないとテレポした時や死亡時にフリーズする
            if (32 * 32 < this.followTarget.getDistanceSq(cameraPos)) {
                reset();
                return;
            }
            cameraPrevPos = cameraPos;
            shouldPlayerLookChange = true;

            //カメラ位置を移動
            Entity entity = this.followTarget;
            World world = entity.getEntityWorld();
            Vec3d playerPos = entity.getPositionVec().add(0, entity.getEyeHeight(), 0);

            //todo ここら辺コンフィグで自由に弄れるようにする
            double length = 3;
            float rotate = 160;
            double followGradualness = 50;
            if (entity.isSprinting() || Minecraft.getInstance().gameSettings.keyBindSprint.isKeyDown()) {
                length = 2;
                rotate = 170;
                followGradualness = 25;
            }
            if (entity instanceof PlayerEntity && ((PlayerEntity) entity).isHandActive()
                    || entity.isShiftKeyDown() || Minecraft.getInstance().gameSettings.keyBindSneak.isKeyDown()) {
                length = 1;
                rotate = 120;
                followGradualness = 75;
                cameraLock = true;
            }
            boolean right = true;
            boolean left = true;
            //カメラロック中は片側だけ処理
            if (cameraLock) {
                right = cameraRight;
                left = !cameraRight;
            }
            Vec3d rightCameraPos = null;
            RayTraceResult rightResult;
            Vec3d leftCameraPos = null;
            RayTraceResult leftResult;
            if (right) {
                rightCameraPos = playerPos.add(Vec3d.fromPitchYaw(-cameraPitch, cameraYaw + rotate).scale(length));
                rightResult = world.rayTraceBlocks(new RayTraceContext(playerPos, rightCameraPos, RayTraceContext.BlockMode.COLLIDER, RayTraceContext.FluidMode.NONE, entity));
                if (rightResult.getType() != RayTraceResult.Type.MISS) {
                    rightCameraPos = rightResult.getHitVec().subtract(playerPos).scale(0.7).add(playerPos);
                }
            }
            if (left) {
                leftCameraPos = playerPos.add(Vec3d.fromPitchYaw(-cameraPitch, cameraYaw - rotate).scale(length));
                leftResult = world.rayTraceBlocks(new RayTraceContext(playerPos, leftCameraPos, RayTraceContext.BlockMode.COLLIDER, RayTraceContext.FluidMode.NONE, entity));
                if (leftResult.getType() != RayTraceResult.Type.MISS) {
                    leftCameraPos = leftResult.getHitVec().subtract(playerPos).scale(0.7).add(playerPos);
                }
            }

            //基本的に左右どちらか現在のカメラ位置が近い方にカメラ位置を定める
            //ただしカメラ位置がプレイヤー位置に超接近した場合は、マシな方へ移動する
            Vec3d newCameraPos;
            if (cameraLock) {
                if (right) {
                    newCameraPos = rightCameraPos;
                } else {
                    newCameraPos = leftCameraPos;
                }
            } else if (cameraPos.squareDistanceTo(rightCameraPos) < cameraPos.squareDistanceTo(leftCameraPos)) {
                double distanceSq = playerPos.squareDistanceTo(rightCameraPos);
                if (distanceSq < 1.5 * 1.5 && distanceSq < playerPos.squareDistanceTo(leftCameraPos)) {
                    newCameraPos = leftCameraPos;
                    cameraRight = false;
                } else {
                    newCameraPos = rightCameraPos;
                    cameraRight = true;
                }
            } else {
                double distanceSq = playerPos.squareDistanceTo(leftCameraPos);
                if (distanceSq < 1.5 * 1.5 && distanceSq < playerPos.squareDistanceTo(rightCameraPos)) {
                    newCameraPos = rightCameraPos;
                    cameraRight = true;
                } else {
                    newCameraPos = leftCameraPos;
                    cameraRight = false;
                }
            }

            Vec3d cameraToCamera = newCameraPos.subtract(cameraPos);
            cameraPos = cameraPos.add(cameraToCamera.scale((float) MathHelper.clamp(newCameraPos.squareDistanceTo(cameraPos) / followGradualness, 0.1, 1)));
            //todo 新カメラ地点とプレイヤー位置とのレイトレース、埋まり防止
            cameraLock = false;
        }
    }

    public void reset() {
        this.followTarget = null;
        cameraPos = null;
    }

    @Nullable
    public static EntityRayTraceResult CustomRayTraceEntities(Entity shooter, Vec3d startVec, Vec3d endVec, AxisAlignedBB boundingBox, Predicate<Entity> filter, double distance, Vec3d grow) {
        World world = shooter.world;
        double tempDist = distance;
        Entity target = null;
        Vec3d targetPos = null;

        for (Entity inRangeEntity : world.getEntitiesInAABBexcluding(shooter, boundingBox, filter)) {
            AxisAlignedBB bb = inRangeEntity.getBoundingBox().grow(inRangeEntity.getCollisionBorderSize()).grow(grow.getX(), grow.getY(), grow.getZ());
            Optional<Vec3d> optional = bb.rayTrace(startVec, endVec);
            if (bb.contains(startVec)) {
                if (tempDist >= 0.0D) {
                    target = inRangeEntity;
                    targetPos = optional.orElse(startVec);
                    tempDist = 0.0D;
                }
            } else if (optional.isPresent()) {
                Vec3d hitPos = optional.get();
                double d1 = startVec.squareDistanceTo(hitPos);
                if (d1 < tempDist || tempDist == 0.0D) {
                    if (inRangeEntity.getLowestRidingEntity() == shooter.getLowestRidingEntity() && !inRangeEntity.canRiderInteract()) {
                        if (tempDist == 0.0D) {
                            target = inRangeEntity;
                            targetPos = hitPos;
                        }
                    } else {
                        target = inRangeEntity;
                        targetPos = hitPos;
                        tempDist = d1;
                    }
                }
            }
        }

        return target == null ? null : new EntityRayTraceResult(target, targetPos);
    }

    public static Vec2f getYawPitch(Vec3d vec3d) {
        float f = MathHelper.sqrt(vec3d.x * vec3d.x + vec3d.z * vec3d.z);
        float theta = (float) (MathHelper.atan2(vec3d.x, vec3d.z) * (180 / Math.PI));
        float phi = (float) (MathHelper.atan2(vec3d.y, f) * (180 / Math.PI));
        return new Vec2f(theta, phi);
    }

}
