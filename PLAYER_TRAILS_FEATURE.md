// ============================================================================
// PLAYER TRAILS / AFTERIMAGES FEATURE - Code Blocks to Add to VisualsFabricClient.java
// ============================================================================

// ---------------------------------------------------------------------------
// 1. STATIC FIELDS - Add after line 131 (after photo mode fields, before Performance section)
// ---------------------------------------------------------------------------

    // New: Player Trails / Afterimages
    public static boolean playerTrailsEnabled = true;
    public static int trailLength = 20;              // Number of trail segments (5-50)
    public static float trailWidth = 0.5f;           // Trail width multiplier (0.1-2.0)
    public static float trailColorR = 1.0f;          // Trail color R
    public static float trailColorG = 0.8f;          // Trail color G
    public static float trailColorB = 0.2f;          // Trail color B
    public static float trailColorA = 0.6f;          // Trail color alpha
    public static boolean trailFadeEnabled = true;   // Fade trail over length
    public static boolean afterimageEnabled = true;  // Enable afterimage effect
    public static int afterimageCount = 5;           // Number of afterimages (1-15)
    public static float afterimageSpacing = 3f;      // Tick spacing between afterimages
    public static float afterimageAlpha = 0.3f;      // Afterimage base alpha
    public static boolean afterimageScale = true;    // Scale down afterimages over time
    public static boolean trailParticlesEnabled = false; // Emit particles along trail


// ---------------------------------------------------------------------------
// 2. GETTER METHODS - Add after line 1014 (after biome color grading getters)
// ---------------------------------------------------------------------------

    // New: Player Trails / Afterimages getters
    public static boolean isPlayerTrailsEnabled() { return playerTrailsEnabled; }
    public static int getTrailLength() { return trailLength; }
    public static float getTrailWidth() { return trailWidth; }
    public static float getTrailColorR() { return trailColorR; }
    public static float getTrailColorG() { return trailColorG; }
    public static float getTrailColorB() { return trailColorB; }
    public static float getTrailColorA() { return trailColorA; }
    public static boolean isTrailFadeEnabled() { return trailFadeEnabled; }
    public static boolean isAfterimageEnabled() { return afterimageEnabled; }
    public static int getAfterimageCount() { return afterimageCount; }
    public static float getAfterimageSpacing() { return afterimageSpacing; }
    public static float getAfterimageAlpha() { return afterimageAlpha; }
    public static boolean isAfterimageScaleEnabled() { return afterimageScale; }
    public static boolean isTrailParticlesEnabled() { return trailParticlesEnabled; }


// ---------------------------------------------------------------------------
// 3. KEY BINDINGS - Add in registerKeyBindings() after line 289 (after biome color grading keybinds)
// ---------------------------------------------------------------------------

        // New: Player Trails / Afterimages
        public static KeyBinding togglePlayerTrailsKey;
        public static KeyBinding toggleAfterimageKey;
        public static KeyBinding cycleTrailLengthKey;
        public static KeyBinding cycleAfterimageCountKey;

        togglePlayerTrailsKey = KeyBindingHelper.registerKeyBinding(
            new KeyBinding("key.visualsfabric.toggle_player_trails", GLFW.GLFW_KEY_T, "category.visualsfabric.player"));
        toggleAfterimageKey = KeyBindingHelper.registerKeyBinding(
            new KeyBinding("key.visualsfabric.toggle_afterimage", GLFW.GLFW_KEY_W, "category.visualsfabric.player"));
        cycleTrailLengthKey = KeyBindingHelper.registerKeyBinding(
            new KeyBinding("key.visualsfabric.cycle_trail_length", GLFW.GLFW_KEY_S, "category.visualsfabric.player"));
        cycleAfterimageCountKey = KeyBindingHelper.registerKeyBinding(
            new KeyBinding("key.visualsfabric.cycle_afterimage_count", GLFW.GLFW_KEY_PERIOD, "category.visualsfabric.player"));


// ---------------------------------------------------------------------------
// 4. KEY BINDING HANDLERS - Add in registerTickEvents() after line 344 (after biome color grading handlers)
// ---------------------------------------------------------------------------

            // New: Player Trails / Afterimages
            while (togglePlayerTrailsKey.wasPressed()) {
                playerTrailsEnabled = !playerTrailsEnabled;
                sendChatFeedback("Player Trails", playerTrailsEnabled);
            }
            while (toggleAfterimageKey.wasPressed()) {
                afterimageEnabled = !afterimageEnabled;
                sendChatFeedback("Afterimages", afterimageEnabled);
            }
            while (cycleTrailLengthKey.wasPressed()) {
                int[] lengths = {5, 10, 15, 20, 30, 40, 50};
                int currentIndex = 0;
                for (int i = 0; i < lengths.length; i++) {
                    if (lengths[i] == trailLength) { currentIndex = i; break; }
                }
                trailLength = lengths[(currentIndex + 1) % lengths.length];
                sendChatFeedback("Trail Length", String.valueOf(trailLength));
            }
            while (cycleAfterimageCountKey.wasPressed()) {
                int[] counts = {1, 2, 3, 4, 5, 6, 8, 10, 12, 15};
                int currentIndex = 0;
                for (int i = 0; i < counts.length; i++) {
                    if (counts[i] == afterimageCount) { currentIndex = i; break; }
                }
                afterimageCount = counts[(currentIndex + 1) % counts.length];
                sendChatFeedback("Afterimage Count", String.valueOf(afterimageCount));
            }


// ---------------------------------------------------------------------------
// 5. TRAIL DATA STRUCTURE - Add after DamageIndicator class (after line 841)
// ---------------------------------------------------------------------------

    // New: Player Trail Data
    private static final java.util.List<TrailPoint> playerTrail = new java.util.ArrayList<>();
    private static final java.util.List<Afterimage> playerAfterimages = new java.util.ArrayList<>();
    private static int trailTickCounter = 0;

    static class TrailPoint {
        Vec3d position;
        float yaw;
        float pitch;
        float age;        // 0 to 1 (0 = newest, 1 = oldest)
        float alpha;      // Current alpha
        
        TrailPoint(Vec3d position, float yaw, float pitch) {
            this.position = position;
            this.yaw = yaw;
            this.pitch = pitch;
            this.age = 0f;
            this.alpha = trailColorA;
        }
    }

    static class Afterimage {
        Vec3d position;
        float yaw;
        float pitch;
        float limbSwing;
        float limbSwingAmount;
        float age;        // 0 to 1
        float alpha;      // Current alpha
        float scale;      // Current scale
        
        Afterimage(Vec3d position, float yaw, float pitch, float limbSwing, float limbSwingAmount) {
            this.position = position;
            this.yaw = yaw;
            this.pitch = pitch;
            this.limbSwing = limbSwing;
            this.limbSwingAmount = limbSwingAmount;
            this.age = 0f;
            this.alpha = afterimageAlpha;
            this.scale = 1.0f;
        }
    }


// ---------------------------------------------------------------------------
// 6. TRAIL UPDATE LOGIC - Add in registerTickEvents() after effectTime update (around line 295)
// ---------------------------------------------------------------------------

            // Update player trails and afterimages
            updatePlayerTrails(client);


// ---------------------------------------------------------------------------
// 7. UPDATE PLAYER TRAILS METHOD - Add after updateDamageIndicators() method (after line 792)
// ---------------------------------------------------------------------------

    private void updatePlayerTrails(MinecraftClient client) {
        if (!playerTrailsEnabled && !afterimageEnabled) {
            playerTrail.clear();
            playerAfterimages.clear();
            return;
        }

        if (client.player == null) return;

        PlayerEntity player = client.player;
        Vec3d currentPos = player.getPos();
        float currentYaw = player.getYaw();
        float currentPitch = player.getPitch();

        // Update trail points - add new point every few ticks
        trailTickCounter++;
        if (trailTickCounter >= 2) { // Add point every 2 ticks
            trailTickCounter = 0;
            if (playerTrailsEnabled) {
                playerTrail.add(0, new TrailPoint(currentPos, currentYaw, currentPitch));
                
                // Limit trail length
                while (playerTrail.size() > trailLength) {
                    playerTrail.remove(playerTrail.size() - 1);
                }
            }
            
            // Add afterimage at intervals
            if (afterimageEnabled && trailTickCounter % (int)afterimageSpacing == 0) {
                playerAfterimages.add(0, new Afterimage(
                    currentPos, currentYaw, currentPitch,
                    player.limbSwing, player.limbSwingAmount
                ));
                
                // Limit afterimage count
                while (playerAfterimages.size() > afterimageCount) {
                    playerAfterimages.remove(playerAfterimages.size() - 1);
                }
            }
        }

        // Update trail point ages and alphas
        float ageIncrement = 1.0f / trailLength;
        for (int i = 0; i < playerTrail.size(); i++) {
            TrailPoint point = playerTrail.get(i);
            point.age = i * ageIncrement;
            if (trailFadeEnabled) {
                point.alpha = trailColorA * (1.0f - point.age * 0.8f);
            } else {
                point.alpha = trailColorA;
            }
            
            // Emit trail particles
            if (trailParticlesEnabled && !performanceMode && client.world != null && client.particleManager != null && Math.random() < 0.3) {
                client.particleManager.addParticle(
                    ParticleTypes.ENCHANT,
                    point.position.x, point.position.y + 0.5, point.position.z,
                    0, 0, 0
                );
            }
        }

        // Update afterimage ages, alphas, and scales
        float afterimageAgeIncrement = 1.0f / afterimageCount;
        for (int i = 0; i < playerAfterimages.size(); i++) {
            Afterimage img = playerAfterimages.get(i);
            img.age = i * afterimageAgeIncrement;
            img.alpha = afterimageAlpha * (1.0f - img.age * 0.7f);
            if (afterimageScale) {
                img.scale = 1.0f - img.age * 0.3f;
            }
        }

        // Clean up old entries
        playerTrail.removeIf(p -> p.age >= 1.0f || p.alpha <= 0.01f);
        playerAfterimages.removeIf(a -> a.age >= 1.0f || a.alpha <= 0.01f);
    }


// ---------------------------------------------------------------------------
// 8. RENDER PLAYER TRAILS - Add in renderOverlayEffects() after line 603 (after water caustics)
// ---------------------------------------------------------------------------

        if (playerTrailsEnabled) renderPlayerTrails(matrices, client.getWindow().getScaledWidth(), client.getWindow().getScaledHeight(), context.tickCounter().getTickDelta(false));
        if (afterimageEnabled) renderPlayerAfterimages(matrices, context.tickCounter().getTickDelta(false));


// ---------------------------------------------------------------------------
// 9. RENDER METHODS - Add after renderWaterCaustics() method (after line 915)
// ---------------------------------------------------------------------------

    // New: Player Trails Rendering
    private void renderPlayerTrails(MatrixStack matrices, int width, int height, float tickDelta) {
        if (performanceMode && lowQualityShaders) return;
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.gameRenderer == null || client.world == null) return;
        if (playerTrail.isEmpty()) return;

        Camera camera = client.gameRenderer.getCamera();
        Vec3d cameraPos = camera.getPos();

        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        RenderSystem.disableCull();
        RenderSystem.depthMask(false);

        Tessellator tessellator = Tessellator.getInstance();

        // Render trail as connected quads (ribbon)
        BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);

        for (int i = 0; i < playerTrail.size() - 1; i++) {
            TrailPoint p1 = playerTrail.get(i);
            TrailPoint p2 = playerTrail.get(i + 1);

            // Calculate screen positions (simplified - using world to screen)
            // For proper 3D trails, we'd need proper projection, but here's a screen-space approach
            float alpha1 = p1.alpha;
            float alpha2 = p2.alpha;
            
            if (alpha1 <= 0.01f && alpha2 <= 0.01f) continue;

            // Width tapers based on age
            float w1 = trailWidth * (1.0f - p1.age * 0.5f) * 2.0f;
            float w2 = trailWidth * (1.0f - p2.age * 0.5f) * 2.0f;

            // Perpendicular offset for ribbon
            float yawRad1 = (float)Math.toRadians(p1.yaw);
            float yawRad2 = (float)Math.toRadians(p2.yaw);
            
            double px1 = p1.position.x - cameraPos.x;
            double py1 = p1.position.y - cameraPos.y + 0.5;
            double pz1 = p1.position.z - cameraPos.z;
            
            double px2 = p2.position.x - cameraPos.x;
            double py2 = p2.position.y - cameraPos.y + 0.5;
            double pz2 = p2.position.z - cameraPos.z;

            // Simple perpendicular vectors
            double perpX1 = -Math.sin(yawRad1) * w1;
            double perpZ1 = Math.cos(yawRad1) * w1;
            double perpX2 = -Math.sin(yawRad2) * w2;
            double perpZ2 = Math.cos(yawRad2) * w2;

            // Add quad vertices
            buffer.vertex((float)(px1 + perpX1), (float)py1, (float)(pz1 + perpZ1))
                  .color(trailColorR, trailColorG, trailColorB, alpha1);
            buffer.vertex((float)(px1 - perpX1), (float)py1, (float)(pz1 - perpZ1))
                  .color(trailColorR, trailColorG, trailColorB, alpha1);
            buffer.vertex((float)(px2 - perpX2), (float)py2, (float)(pz2 - perpZ2))
                  .color(trailColorR, trailColorG, trailColorB, alpha2);
            buffer.vertex((float)(px2 + perpX2), (float)py2, (float)(pz2 + perpZ2))
                  .color(trailColorR, trailColorG, trailColorB, alpha2);
        }

        BufferRenderer.drawWithGlobalProgram(buffer.end());

        RenderSystem.depthMask(true);
        RenderSystem.enableCull();
        RenderSystem.setShaderColor(1, 1, 1, 1);
        RenderSystem.disableBlend();
    }

    // New: Player Afterimages Rendering
    private void renderPlayerAfterimages(MatrixStack matrices, float tickDelta) {
        if (performanceMode && lowQualityShaders) return;
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null) return;
        if (playerAfterimages.isEmpty()) return;

        // Render afterimages using the entity renderer with modified alpha/scale
        // This is a simplified version - full implementation would use EntityRenderDispatcher
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        RenderSystem.disableCull();

        // Note: Full afterimage rendering requires accessing EntityRenderDispatcher
        // and rendering the player entity multiple times with different transforms
        // This is a placeholder showing the pattern
        
        // For now, render simple ghost quads at afterimage positions
        Camera camera = client.gameRenderer.getCamera();
        Vec3d cameraPos = camera.getPos();

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);

        for (Afterimage img : playerAfterimages) {
            if (img.alpha <= 0.01f) continue;

            double px = img.position.x - cameraPos.x;
            double py = img.position.y - cameraPos.y;
            double pz = img.position.z - cameraPos.z;

            float size = 0.6f * img.scale;
            float halfSize = size / 2;

            // Simple billboard quad facing camera
            buffer.vertex((float)(px - halfSize), (float)(py + size), (float)(pz - halfSize))
                  .color(1, 1, 1, img.alpha);
            buffer.vertex((float)(px + halfSize), (float)(py + size), (float)(pz - halfSize))
                  .color(1, 1, 1, img.alpha);
            buffer.vertex((float)(px + halfSize), (float)(py - halfSize), (float)(pz - halfSize))
                  .color(1, 1, 1, img.alpha);
            buffer.vertex((float)(px - halfSize), (float)(py - halfSize), (float)(pz - halfSize))
                  .color(1, 1, 1, img.alpha);
        }

        BufferRenderer.drawWithGlobalProgram(buffer.end());

        RenderSystem.enableCull();
        RenderSystem.setShaderColor(1, 1, 1, 1);
        RenderSystem.disableBlend();
    }


// ---------------------------------------------------------------------------
// 10. PERFORMANCE MODE CHECK - The feature already checks performanceMode and lowQualityShaders
//     in the render methods above. No additional code needed.
// ---------------------------------------------------------------------------

// ---------------------------------------------------------------------------
// 11. EFFECT TIME USAGE - The updatePlayerTrails method uses trailTickCounter
//     which is frame-independent. For animated effects, you could also use:
//     float animOffset = (float)Math.sin(VisualsFabricClient.getEffectTime() * 2) * 0.1f;
//     in the render methods for pulsing/animating trails.
// ---------------------------------------------------------------------------