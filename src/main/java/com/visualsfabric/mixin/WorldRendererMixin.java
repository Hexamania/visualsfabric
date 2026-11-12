package com.visualsfabric.mixin;

import com.visualsfabric.VisualsFabricClient;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.RenderTickCounter;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldRenderer.class)
public class WorldRendererMixin {

    @Inject(method = "render", at = @At("HEAD"))
    private void onWorldRenderStart(LightmapTextureManager lightmapTextureManager, RenderTickCounter tickCounter, boolean tickCounterAvailable, Camera camera, GameRenderer gameRenderer, Matrix4f projectionMatrix, Matrix4f viewMatrix, CallbackInfo ci) {
        if (VisualsFabricClient.isAmbientOcclusionEnhanced()) {
            // Enhanced AO rendering setup could go here
        }
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void onWorldRenderEnd(LightmapTextureManager lightmapTextureManager, RenderTickCounter tickCounter, boolean tickCounterAvailable, Camera camera, GameRenderer gameRenderer, Matrix4f projectionMatrix, Matrix4f viewMatrix, CallbackInfo ci) {
        // Clean up
    }
}