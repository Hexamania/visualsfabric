package com.visualsfabric.mixin;

import com.visualsfabric.VisualsFabricClient;
import net.minecraft.client.render.Camera;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BlockView;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
public abstract class CameraMixin {
    @Shadow
    protected abstract void setPos(Vec3d pos);

    @Inject(method = "update", at = @At("TAIL"))
    private void onCameraUpdate(BlockView blockView, Entity entity, boolean thirdPerson, boolean inverseView, float tickDelta, CallbackInfo ci) {
        Vec3d shake = VisualsFabricClient.getScreenShakeOffset();
        if (!shake.equals(Vec3d.ZERO)) {
            Camera camera = (Camera) (Object) this;
            setPos(camera.getPos().add(shake));
        }

        if (VisualsFabricClient.isDepthOfFieldEnabled() || VisualsFabricClient.isMotionBlurEnabled()) {
            // Could capture camera position/rotation for effects
        }
    }
}