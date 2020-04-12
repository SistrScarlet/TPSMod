package com.sistr.rshud.client;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.TransformationMatrix;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.opengl.GL11;

import java.util.Iterator;

public class RSHUDRenderer {
    public static final float rad = 1 / 180F * (float) Math.PI;

    //Yが反転しており、画面の上がY0である点に注意
    public void render(PlayerEntity player, int width, int height) {
        RenderSystem.disableTexture();
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.disableAlphaTest();

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder builder = tessellator.getBuffer();
        float scale = height / 240F;
        float centerX = width / 2F;
        float centerY = height / 2F;
        float radius = 55 * scale;
        float thick = 1.4F * scale;
        float offset = 2.0F * scale;
        float frameOuterRadius = radius + thick;
        float frameInnerRadius = radius - thick;

        int whiteColor = 0xFFFFFF;
        int mainColor = 0x60FFC4;
        int subColor = 0x00FFD8;
        int redColor = 0xFF0000;
        int mainAlpha = 128;
        int subAlpha = 224;

        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        GL11.glEnable(GL11.GL_POLYGON_SMOOTH);

        float outLineThick = thick * 1.5F;
        float outerRadius = radius + 5 * scale;

        //右下 *90-180
        float hungerVal = (player.getFoodStats().getFoodLevel() + player.getFoodStats().getSaturationLevel()) / 40;
        drawArch(tessellator, builder, centerX + offset, centerY, outerRadius, thick, 90 + 90 * hungerVal, 180, mainColor, mainAlpha);
        //フレーム
        {
            //外側の太線
            drawArchThickLine(tessellator, builder, centerX + offset, centerY,
                    outerRadius - thick, outLineThick, 90, 180, subColor, subAlpha);
            //外側の太線の横の端
            drawThickLine(tessellator, builder, centerX + offset + outerRadius - thick + thick * 4, centerY,
                    centerX + offset + outerRadius - thick, centerY, outLineThick, subColor, subAlpha);
            //外側の太線の下の端
            drawThickLine(tessellator, builder, centerX + offset, centerY + frameOuterRadius, centerX + offset, centerY + frameOuterRadius + thick * 4, outLineThick, subColor, subAlpha);
        }

        //左下 180-*270
        float healthVal = Math.max(player.getHealth() / player.getMaxHealth(), 0);
        drawArch(tessellator, builder, centerX - offset, centerY, outerRadius, thick, 180, 180 + 90 * healthVal, mainColor, mainAlpha);
        //フレーム
        {
            //外側の太線
            drawArchThickLine(tessellator, builder, centerX - offset, centerY,
                    outerRadius - thick, outLineThick, 180, 270, subColor, subAlpha);
            //外側の太線の端
            drawThickLine(tessellator, builder, centerX - offset - outerRadius + thick - thick * 4, centerY,
                    centerX - offset - outerRadius + thick, centerY, outLineThick, subColor, subAlpha);
            drawThickLine(tessellator, builder, centerX - offset, centerY + frameOuterRadius, centerX - offset, centerY + frameOuterRadius + thick * 4, outLineThick, subColor, subAlpha);
        }

        float spikeThick = 0.6F * scale;
        float spikeLength = 6 * scale;

        //右下 *105-180
        float speedVal = (float) player.getPositionVec().subtract(player.prevPosX, player.prevPosY, player.prevPosZ).length();
        drawArch(tessellator, builder, centerX + offset, centerY, radius, thick, 105 + 75 * (1 - Math.min(MathHelper.sqrt(speedVal) / 2, 1)), 180, mainColor, mainAlpha);
        //フレーム
        {
            //フレームの外円弧と内円弧
            drawArchLine(tessellator, builder, centerX + offset, centerY, frameOuterRadius, 100, 180, whiteColor, mainAlpha);
            drawArchLine(tessellator, builder, centerX + offset, centerY, frameInnerRadius, 100, 180, whiteColor, mainAlpha);
            //フレームの両端
            float startX = MathHelper.sin(100 * rad) * (frameOuterRadius);
            float startY = MathHelper.cos(100 * rad) * (frameOuterRadius);
            drawLine(tessellator, builder, centerX + offset + startX, centerY - startY, centerX + offset + startX - thick * 5, centerY - startY, whiteColor, mainAlpha);
            float endX = MathHelper.sin(180 * rad) * (frameOuterRadius);
            float endY = MathHelper.cos(180 * rad) * (frameOuterRadius);
            drawLine(tessellator, builder, centerX + offset + endX, centerY - endY, centerX + offset + endX, centerY - endY - thick * 5, whiteColor, mainAlpha);
            //フレーム中腹のとげ
            float spikeSin = MathHelper.sin(135 * rad);
            float spikeCos = MathHelper.cos(135 * rad);
            float spikeBottomX = centerX + offset + spikeSin * (frameInnerRadius);
            float spikeBottomY = centerY - spikeCos * (frameInnerRadius);
            builder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
            vertex(builder, spikeBottomX - spikeThick, spikeBottomY + spikeThick, -90, redColor, mainAlpha);
            vertex(builder, spikeBottomX + spikeThick, spikeBottomY - spikeThick, -90, redColor, mainAlpha);
            float spikeTopX = centerX + offset + spikeSin * (frameInnerRadius - spikeLength);
            float spikeTopY = centerY - spikeCos * (frameInnerRadius - spikeLength);
            vertex(builder, spikeTopX + spikeThick, spikeTopY - spikeThick, -90, redColor, mainAlpha);
            vertex(builder, spikeTopX - spikeThick, spikeTopY + spikeThick, -90, redColor, mainAlpha);
            tessellator.draw();
            //とげの先っぽ
            float spikeTopTopX = centerX + offset + spikeSin * (frameInnerRadius - spikeLength * 2);
            float spikeTopTopY = centerY - spikeCos * (frameInnerRadius - spikeLength * 2);
            drawLine(tessellator, builder, spikeTopX, spikeTopY, spikeTopTopX, spikeTopTopY, redColor, mainAlpha);
        }

        //左下 180-*255
        float posHeightVal = (float) player.getPosY();
        drawArch(tessellator, builder, centerX - offset, centerY, radius, thick, 180, 180 + 75 * MathHelper.clamp(posHeightVal, 0, 256) / 256, mainColor, mainAlpha);
        //フレーム
        {
            //フレームの外円弧と内円弧
            drawArchLine(tessellator, builder, centerX - offset, centerY, frameOuterRadius, 180, 260, whiteColor, mainAlpha);
            drawArchLine(tessellator, builder, centerX - offset, centerY, frameInnerRadius, 180, 260, whiteColor, mainAlpha);
            //フレームの両端
            float startX = MathHelper.sin(180 * rad) * (frameOuterRadius);
            float startY = MathHelper.cos(180 * rad) * (frameOuterRadius);
            drawLine(tessellator, builder, centerX - offset + startX, centerY - startY, centerX - offset + startX, centerY - startY - thick * 5, whiteColor, mainAlpha);
            float endX = MathHelper.sin(260 * rad) * (frameOuterRadius);
            float endY = MathHelper.cos(260 * rad) * (frameOuterRadius);
            drawLine(tessellator, builder, centerX - offset + endX, centerY - endY, centerX - offset + endX + thick * 5, centerY - endY, whiteColor, mainAlpha);
            //フレーム中腹のとげ
            float spikeSin = MathHelper.sin(225 * rad);
            float spikeCos = MathHelper.cos(225 * rad);
            float spikeBottomX = centerX - offset + spikeSin * (frameInnerRadius);
            float spikeBottomY = centerY - spikeCos * (frameInnerRadius);
            builder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
            vertex(builder, spikeBottomX - spikeThick, spikeBottomY - spikeThick, -90, redColor, mainAlpha);
            vertex(builder, spikeBottomX + spikeThick, spikeBottomY + spikeThick, -90, redColor, mainAlpha);
            float spikeTopX = centerX - offset + spikeSin * (frameInnerRadius - spikeLength);
            float spikeTopY = centerY - spikeCos * (frameInnerRadius - spikeLength);
            vertex(builder, spikeTopX + spikeThick, spikeTopY + spikeThick, -90, redColor, mainAlpha);
            vertex(builder, spikeTopX - spikeThick, spikeTopY - spikeThick, -90, redColor, mainAlpha);
            tessellator.draw();
            //とげの先っぽ
            float spikeTopTopX = centerX - offset + spikeSin * (frameInnerRadius - spikeLength * 2);
            float spikeTopTopY = centerY - spikeCos * (frameInnerRadius - spikeLength * 2);
            drawLine(tessellator, builder, spikeTopX, spikeTopY, spikeTopTopX, spikeTopTopY, redColor, mainAlpha);
        }

        //右上 0-*75
        ItemStack mainHand = player.getHeldItemMainhand();
        int heldDurableVal = mainHand.getMaxDamage() - mainHand.getDamage();
        if (0 < mainHand.getMaxDamage()) {
            drawArch(tessellator, builder, centerX + offset, centerY, radius, thick, 0, 0 + 75F * heldDurableVal / mainHand.getMaxDamage(), mainColor, mainAlpha);

            //弾数
        }
        //フレーム
        {
            //フレームの外円弧と内円弧
            drawArchLine(tessellator, builder, centerX + offset, centerY, frameOuterRadius, 0, 80, whiteColor, mainAlpha);
            drawArchLine(tessellator, builder, centerX + offset, centerY, frameInnerRadius, 0, 80, whiteColor, mainAlpha);
            //フレームの両端
            float startX = MathHelper.sin(0 * rad) * (frameOuterRadius);
            float startY = MathHelper.cos(0 * rad) * (frameOuterRadius);
            drawLine(tessellator, builder, centerX + offset + startX, centerY - startY, centerX + offset + startX, centerY - startY + thick * 5, whiteColor, mainAlpha);
            float endX = MathHelper.sin(80 * rad) * (frameOuterRadius);
            float endY = MathHelper.cos(80 * rad) * (frameOuterRadius);
            drawLine(tessellator, builder, centerX + offset + endX, centerY - endY, centerX + offset + endX - thick * 5, centerY - endY, whiteColor, mainAlpha);
            //フレーム中腹のとげ
            float spikeSin = MathHelper.sin(45 * rad);
            float spikeCos = MathHelper.cos(45 * rad);
            float spikeBottomX = centerX + offset + spikeSin * (frameInnerRadius);
            float spikeBottomY = centerY - spikeCos * (frameInnerRadius);
            builder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
            vertex(builder, spikeBottomX - spikeThick, spikeBottomY - spikeThick, -90, redColor, mainAlpha);
            vertex(builder, spikeBottomX + spikeThick, spikeBottomY + spikeThick, -90, redColor, mainAlpha);
            float spikeTopX = centerX + offset + spikeSin * (frameInnerRadius - spikeLength);
            float spikeTopY = centerY - spikeCos * (frameInnerRadius - spikeLength);
            vertex(builder, spikeTopX + spikeThick, spikeTopY + spikeThick, -90, redColor, mainAlpha);
            vertex(builder, spikeTopX - spikeThick, spikeTopY - spikeThick, -90, redColor, mainAlpha);
            tessellator.draw();
            //とげの先っぽ
            float spikeTopTopX = centerX + offset + spikeSin * (frameInnerRadius - spikeLength * 2);
            float spikeTopTopY = centerY - spikeCos * (frameInnerRadius - spikeLength * 2);
            drawLine(tessellator, builder, spikeTopX, spikeTopY, spikeTopTopX, spikeTopTopY, redColor, mainAlpha);
        }

        //左上 *275-355
        float armorVal = player.getTotalArmorValue() / 20F;
        if (0 < armorVal) {
            drawArch(tessellator, builder, centerX - offset, centerY, radius, thick, 285 + 75 * (1 - armorVal), 360, mainColor, mainAlpha);
        }
        //フレーム
        {
            //フレームの外円弧と内円弧
            drawArchLine(tessellator, builder, centerX - offset, centerY, frameOuterRadius, 280, 360, whiteColor, mainAlpha);
            drawArchLine(tessellator, builder, centerX - offset, centerY, frameInnerRadius, 280, 360, whiteColor, mainAlpha);
            //フレームの両端
            float startX = MathHelper.sin(280 * rad) * (frameOuterRadius);
            float startY = MathHelper.cos(280 * rad) * (frameOuterRadius);
            drawLine(tessellator, builder, centerX - offset + startX, centerY - startY, centerX - offset + startX + thick * 5, centerY - startY, whiteColor, mainAlpha);
            float endX = MathHelper.sin(360 * rad) * (frameOuterRadius);
            float endY = MathHelper.cos(360 * rad) * (frameOuterRadius);
            drawLine(tessellator, builder, centerX - offset + endX, centerY - endY, centerX - offset + endX, centerY - endY + thick * 5, whiteColor, mainAlpha);
            //フレーム中腹のとげ
            float spikeSin = MathHelper.sin(315 * rad);
            float spikeCos = MathHelper.cos(315 * rad);
            float spikeBottomX = centerX - offset + spikeSin * (frameInnerRadius);
            float spikeBottomY = centerY - spikeCos * (frameInnerRadius);
            builder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
            vertex(builder, spikeBottomX - spikeThick, spikeBottomY + spikeThick, -90, redColor, mainAlpha);
            vertex(builder, spikeBottomX + spikeThick, spikeBottomY - spikeThick, -90, redColor, mainAlpha);
            float spikeTopX = centerX - offset + spikeSin * (frameInnerRadius - spikeLength);
            float spikeTopY = centerY - spikeCos * (frameInnerRadius - spikeLength);
            vertex(builder, spikeTopX + spikeThick, spikeTopY - spikeThick, -90, redColor, mainAlpha);
            vertex(builder, spikeTopX - spikeThick, spikeTopY + spikeThick, -90, redColor, mainAlpha);
            tessellator.draw();
            //とげの先っぽ
            float spikeTopTopX = centerX - offset + spikeSin * (frameInnerRadius - spikeLength * 2);
            float spikeTopTopY = centerY - spikeCos * (frameInnerRadius - spikeLength * 2);
            drawLine(tessellator, builder, spikeTopX, spikeTopY, spikeTopTopX, spikeTopTopY, redColor, mainAlpha);
        }

        //残りのフレーム
        {
            drawLine(tessellator, builder, centerX - offset, centerY - frameOuterRadius, centerX + offset, centerY - frameOuterRadius, whiteColor, 128);
            drawLine(tessellator, builder, centerX - offset, centerY + frameOuterRadius, centerX + offset, centerY + frameOuterRadius, whiteColor, 128);
        }
        //三角
        {
            float yaw = -player.rotationYaw;
            float triBaseX = MathHelper.sin(yaw * rad) * (radius + 12.5F * scale);
            float triBaseY = MathHelper.cos(yaw * rad) * (radius + 12.5F * scale);
            float triRightX = MathHelper.sin((yaw + 2) * rad) * (radius + 15F * scale);
            float triRightY = MathHelper.cos((yaw + 2) * rad) * (radius + 15F * scale);
            float triLeftX = MathHelper.sin((yaw - 2) * rad) * (radius + 15F * scale);
            float triLeftY = MathHelper.cos((yaw - 2) * rad) * (radius + 15F * scale);
            builder.begin(GL11.GL_TRIANGLES, DefaultVertexFormats.POSITION_COLOR);
            vertex(builder, centerX + triBaseX, centerY - triBaseY, -90, whiteColor, 128);
            vertex(builder, centerX + triRightX, centerY - triRightY, -90, whiteColor, 128);
            vertex(builder, centerX + triLeftX, centerY - triLeftY, -90, whiteColor, 128);
            tessellator.draw();
            builder.begin(GL11.GL_TRIANGLES, DefaultVertexFormats.POSITION_COLOR);
            vertex(builder, centerX - triBaseX, centerY + triBaseY, -90, whiteColor, 128);
            vertex(builder, centerX - triRightX, centerY + triRightY, -90, whiteColor, 128);
            vertex(builder, centerX - triLeftX, centerY + triLeftY, -90, whiteColor, 128);
            tessellator.draw();
        }

        GL11.glDisable(GL11.GL_LINE_SMOOTH);
        GL11.glDisable(GL11.GL_POLYGON_SMOOTH);

        FontRenderer fontRenderer = Minecraft.getInstance().fontRenderer;
        float fontHeight = fontRenderer.FONT_HEIGHT;

        //デフォルトは左上詰め
        //下詰めはYにfontHeightを引く
        //右詰めはXにStringWidthを引く

        //左下詰め
        String hungerStr = String.format("%06.2f", hungerVal * 100);
        //fontRenderer.drawString(hungerStr, centerX + 65 * scale, centerY - fontHeight, subColor);
        IRenderTypeBuffer.Impl impl = IRenderTypeBuffer.getImpl(builder);
        fontRenderer.renderString(hungerStr, centerX + 60 * scale, centerY - fontHeight, subColor, false,
                TransformationMatrix.identity().getMatrix(), impl, true, 0, 15728880);
        impl.finish();

        //右下詰め
        String healthStr = String.format("%06.2f", healthVal * 100);
        fontRenderer.drawString(healthStr, centerX - 60 * scale - fontRenderer.getStringWidth(healthStr), centerY - fontHeight, subColor);

        //右下詰め
        String speedStr = String.format("%.2f", speedVal * 20 * 60);
        fontRenderer.drawString(speedStr, centerX + 50 * scale - fontRenderer.getStringWidth(speedStr), centerY + 25 * scale - fontHeight, subColor);

        //左下詰め
        String posHeightStr = String.format("%06.2f", posHeightVal);
        fontRenderer.drawString(posHeightStr, centerX - 50 * scale, centerY + 25 * scale - fontHeight, subColor);

        //右上詰め
        if (0 < mainHand.getMaxDamage()) {
            String length = String.valueOf(String.valueOf(mainHand.getMaxDamage()).length());
            String durableStr = String.format("%0" + length + "d", heldDurableVal);
            fontRenderer.drawString(durableStr, centerX + 50 * scale - fontRenderer.getStringWidth(durableStr), centerY - 25 * scale, subColor);
        }

        //左上詰め
        if (0 < armorVal) {
            String armorStr = String.format("%06.2f", armorVal * 100F);
            fontRenderer.drawString(armorStr, centerX - 50 * scale, centerY - 25 * scale, subColor);
        }
        //右下詰め
        Iterator<ItemStack> armorIterator = player.getArmorInventoryList().iterator();
        int armorInv = 0;
        while (armorIterator.hasNext()) {
            ItemStack stack = armorIterator.next();
            armorInv++;
            if (0 < stack.getMaxDamage()) {
                int durableVal = stack.getMaxDamage() - stack.getDamage();
                String length = String.valueOf(String.valueOf(stack.getMaxDamage()).length());
                String durableStr = String.format("%0" + length + "d", durableVal);
                fontRenderer.drawString(durableStr, centerX - (15 * (5 - armorInv) + 10) * scale - fontRenderer.getStringWidth(durableStr), centerY - (15 * armorInv + 10) * scale - fontHeight, subColor);
            }
        }

        RenderSystem.enableTexture();
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
        RenderSystem.enableAlphaTest();
        RenderSystem.disableBlend();
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
    }

    public static void drawArchThickLine(Tessellator tessellator, BufferBuilder builder, float centerX, float centerY,
                                         float radius, float thick, float startAngle, float endAngle, int rgb, int alpha) {
        GlStateManager.lineWidth(thick);
        drawArchLine(tessellator, builder, centerX, centerY, radius, startAngle, endAngle, rgb, alpha);
        GlStateManager.lineWidth(1);
    }

    public static void drawThickLine(Tessellator tessellator, BufferBuilder builder,
                                     float startX, float startY, float endX, float endY, float thick, int rgb, int alpha) {
        GlStateManager.lineWidth(thick);
        drawLine(tessellator, builder, startX, startY, endX, endY, rgb, alpha);
        GlStateManager.lineWidth(1);
    }

    public static void drawArch(Tessellator tessellator, BufferBuilder builder, float centerX, float centerY,
                                float radius, float thick, float startAngle, float endAngle, int rgb, int alpha) {
        builder.begin(GL11.GL_QUAD_STRIP, DefaultVertexFormats.POSITION_COLOR);
        float outer = radius + thick;
        float inner = radius - thick;
        startAngle *= rad;
        endAngle *= rad;
        if (endAngle < startAngle) {
            float temp = startAngle;
            startAngle = endAngle;
            endAngle = temp;
        }
        for (float angle = startAngle; angle < endAngle; angle += rad * 5) {
            float x = MathHelper.sin(angle);
            float y = MathHelper.cos(angle);
            vertex(builder, centerX + x * outer, centerY - y * outer, -90, rgb, alpha);
            vertex(builder, centerX + x * inner, centerY - y * inner, -90, rgb, alpha);
        }
        float x = MathHelper.sin(endAngle);
        float y = MathHelper.cos(endAngle);
        vertex(builder, centerX + x * outer, centerY - y * outer, -90, rgb, alpha);
        vertex(builder, centerX + x * inner, centerY - y * inner, -90, rgb, alpha);
        tessellator.draw();
    }

    public static void drawArchLine(Tessellator tessellator, BufferBuilder builder, float centerX, float centerY,
                                    float radius, float startAngle, float endAngle, int rgb, int alpha) {
        builder.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION_COLOR);
        startAngle *= rad;
        endAngle *= rad;
        if (endAngle < startAngle) {
            float temp = startAngle;
            startAngle = endAngle;
            endAngle = temp;
        }
        for (float angle = startAngle; angle < endAngle; angle += rad * 5) {
            float x = MathHelper.sin(angle) * radius;
            float y = MathHelper.cos(angle) * radius;
            vertex(builder, centerX + x, centerY - y, -90, rgb, alpha);
        }
        float x = MathHelper.sin(endAngle) * radius;
        float y = MathHelper.cos(endAngle) * radius;
        vertex(builder, centerX + x, centerY - y, -90, rgb, alpha);
        tessellator.draw();
    }

    public static void drawLine(Tessellator tessellator, BufferBuilder builder,
                                float startX, float startY, float endX, float endY, int rgb, int alpha) {
        builder.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);
        vertex(builder, startX, startY, -90, rgb, alpha);
        vertex(builder, endX, endY, -90, rgb, alpha);
        tessellator.draw();
    }

    public static void vertex(BufferBuilder builder, double x, double y, double z, int rgb, int alpha) {
        vertex(builder, x, y, z, rgb >> 16 & 255, rgb >> 8 & 255, rgb & 255, alpha);
    }

    public static void vertex(BufferBuilder builder, double x, double y, double z,
                              int red, int green, int blue, int alpha) {
        builder.pos(x, y, z).color(red, green, blue, alpha).endVertex();
    }

}
