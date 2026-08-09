package com.grakepch.storagedrawersvt.mixin;

import com.jaquadro.minecraft.storagedrawers.block.BlockDrawers;
import com.jaquadro.minecraft.storagedrawers.client.renderer.BlockEntityDrawersRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransform;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import com.mojang.math.Axis;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Replaces StorageDrawers' flat "sprite" rendering of the item shown on a drawer's front
 * with a vanilla item-frame style 3D render: the item is scaled to a fixed size and floats
 * in front of the drawer face (ItemDisplayContext.FIXED).
 *
 * StorageDrawers draws the item by scaling Z to 0.001 (flattening it) inside
 * renderFastItem(). We cancel that method at the HEAD and draw our own version instead,
 * leaving the rest of the BER (indicator fill bar, quantity text) untouched.
 */
@Mixin(BlockEntityDrawersRenderer.class)
public abstract class BlockEntityDrawersRendererMixin
{
    @Shadow(remap = false)
    private void alignRendering (PoseStack matrix, Direction side) { }

    @Inject(method = "renderFastItem", remap = false, at = @At("HEAD"), cancellable = true)
    private void storagedrawersvt$renderLikeItemFrame (ItemStack itemStack, BlockState state, int slot,
        PoseStack matrix, MultiBufferSource buffer, int combinedLight, int combinedOverlay,
        Direction side, CallbackInfo ci)
    {
        if (itemStack == null || itemStack.isEmpty() || !(state.getBlock() instanceof BlockDrawers block))
            return;

        AABB labelGeometry = block.labelGeometry[slot];
        if (labelGeometry == null)
            return;

        float centerX = (float) (labelGeometry.minX + labelGeometry.getXsize() / 2);
        float centerY = 16f - (float) (labelGeometry.minY + labelGeometry.getYsize() / 2);
        // The slot area starts 1px (0.0625) inside the drawer face — the item sits in this
        // recessed slot, so its back face should align with the slot opening, not the outer face.
        float slotZ = 1f - (float) (labelGeometry.minZ * .0625f);

        double scale = 0.5;

        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
        BakedModel model = itemRenderer.getModel(itemStack, null, null, 0);

        // StorageDrawers sizes each rendered item to fill its own slot area (scaleX = slotWidth/16,
        // scaleY = slotHeight/16). Match that: 1-slot drawers have 8x8 slots (0.5), while 2/4-slot
        // and compacting drawers have 4x4 slots (0.25). ITEM_SCALE is calibrated relative to a
        // 1-slot drawer, so normalize by the 1-slot width (8 px -> 0.5) to keep 1-slot visuals
        // unchanged while shrinking multi-slot items to fit their slot.
        float slotWidth = (float) (labelGeometry.getXsize() / 16.0);
        float slotHeight = (float) (labelGeometry.getYsize() / 16.0);
        float slotScale = Math.min(slotWidth, slotHeight) * 2.0f;
        scale *= slotScale;

        // Block-family models (oak log, buttons, ...) inherit block/block's FIXED display
        // transform with scale 0.5, which ItemRenderer applies on top of our scale. That would
        // shrink blocks to half size on the drawer. Compensate by inverting the fixed scale —
        // but only on multi-slot drawers: on a 1x1 drawer (slotScale == 1.0) we keep the
        // original scale so blocks look exactly like they do in a vanilla item frame.
        // Flat/generated items and custom models (e.g. the book from IconicEnchantments, which
        // defines fixed rotation+translation but no scale) keep their vanilla presentation.
        ItemTransforms transforms = model.getTransforms();
        float fixedScale = 1.0f;
        if (transforms != null) {
            ItemTransform fixed = transforms.getTransform(ItemDisplayContext.FIXED);
            if (fixed != null)
                fixedScale = fixed.scale.x();
        }
        if (fixedScale != 0.0f && slotScale < 1.0f)
            scale /= fixedScale;

        matrix.pushPose();

        try {
            alignRendering(matrix, side);
            // Push the item's centre out by its rendered depth (v) so its BACK face sits
            // exactly on the slot opening (which sits 1px inside the drawer face), like a vanilla
            // item frame. Vanilla item models are 1px-thick slices (0.0625 blocks), so v is a
            // uniform 0.0625 * scale for every item (blocks render with the same flat offset).
            double v = 0.0625 * scale;
            matrix.translate(centerX / 16f, 1f - centerY / 16f, slotZ + v);
            matrix.scale((float) scale, (float) scale, (float) scale);
            // FIXED context renders models facing the opposite way from the drawer-aligned
            // coordinate system StorageDrawers' alignRendering sets up (which assumes the
            // flat GUI-style render). Rotate 180 degrees around Y so the item faces the viewer.
            matrix.mulPose(Axis.YP.rotationDegrees(180.0F));

            itemRenderer.render(itemStack, ItemDisplayContext.FIXED, false, matrix, buffer, combinedLight, combinedOverlay, model);
        } catch (Exception e) {
            // Never let a bad item model crash the whole drawer render pass.
        }

        matrix.popPose();
        ci.cancel();
    }
}
