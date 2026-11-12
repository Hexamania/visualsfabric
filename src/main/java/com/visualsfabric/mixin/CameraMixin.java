package com.visualsfabric.mixin;

import com.visualsfabric.VisualsFabricClient;
import net.minecraft.client.render.Camera;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BlockView;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
public class CameraMixin {

    @Inject(method = "update", at = @At("TAIL"))
    private void onCameraUpdate(BlockView blockView, Entity entity, boolean thirdPerson, boolean inverseView, float tickDelta, CallbackInfo ci) {
        // Camera update hook for depth of field, motion blur, etc.
        if (VisualsFabricClient.isDepthOfFieldEnabled() || VisualsFabricClient.isMotionBlurEnabled()) {
            // Could capture camera position/rotation for effects
        }
    }
}