// ============================================================================
// BLOCK HIGHLIGHT/OUTLINE FEATURE - Code Blocks to Add to VisualsFabricClient.java
// ============================================================================

// 1. STATIC FIELDS (add after Performance section, around line 132-136)
// ----------------------------------------------------------------------------
    // New: Block Highlight/Outline
    public static boolean blockHighlightEnabled = true;
    public static float blockHighlightR = 1.0f, blockHighlightG = 1.0f, blockHighlightB = 0.0f, blockHighlightA = 0.6f;
    public static float blockOutlineWidth = 2.0f;
    public static boolean blockHighlightPulseEnabled = true;
    public static float blockHighlightPulseSpeed = 2.0f;
    public static float blockHighlightPulseMinAlpha = 0.3f;
    public static float blockHighlightPulseMaxAlpha = 0.8f;
    public static boolean blockHighlightOres = true;
    public static boolean blockHighlightChests = true;
    public static boolean blockHighlightMobs = false;
    public static boolean blockHighlightPlayers = false;
    public static float blockHighlightRange = 64.0f;
    public static int blockHighlightMode = 0; // 0=Outline, 1=Fill, 2=Both
    public static boolean blockHighlightThroughWalls = false;

// 2. GETTER METHODS (add after Biome Color Grading getters, around line 1014)
// ----------------------------------------------------------------------------
    public static boolean isBlockHighlightEnabled() { return blockHighlightEnabled; }
    public static float getBlockHighlightR() { return blockHighlightR; }
    public static float getBlockHighlightG() { return blockHighlightG; }
    public static float getBlockHighlightB() { return blockHighlightB; }
    public static float getBlockHighlightA() { return blockHighlightA; }
    public static float getBlockOutlineWidth() { return blockOutlineWidth; }
    public static boolean isBlockHighlightPulseEnabled() { return blockHighlightPulseEnabled; }
    public static float getBlockHighlightPulseSpeed() { return blockHighlightPulseSpeed; }
    public static float getBlockHighlightPulseMinAlpha() { return blockHighlightPulseMinAlpha; }
    public static float getBlockHighlightPulseMaxAlpha() { return blockHighlightPulseMaxAlpha; }
    public static boolean isBlockHighlightOresEnabled() { return blockHighlightOres; }
    public static boolean isBlockHighlightChestsEnabled() { return blockHighlightChests; }
    public static boolean isBlockHighlightMobsEnabled() { return blockHighlightMobs; }
    public static boolean isBlockHighlightPlayersEnabled() { return blockHighlightPlayers; }
    public static float getBlockHighlightRange() { return blockHighlightRange; }
    public static int getBlockHighlightMode() { return blockHighlightMode; }
    public static boolean isBlockHighlightThroughWallsEnabled() { return blockHighlightThroughWalls; }

// 3. KEY BINDING FIELDS (add after line 245, after Biome Color Grading keybinds)
// ----------------------------------------------------------------------------
    // New: Block Highlight/Outline
    public static KeyBinding toggleBlockHighlightKey;
    public static KeyBinding toggleBlockHighlightOresKey;
    public static KeyBinding toggleBlockHighlightChestsKey;
    public static KeyBinding toggleBlockHighlightModeKey;
    public static KeyBinding toggleBlockHighlightPulseKey;
    public static KeyBinding cycleBlockHighlightColorKey;
    public static KeyBinding increaseBlockOutlineWidthKey;
    public static KeyBinding decreaseBlockOutlineWidthKey;
    public static KeyBinding toggleBlockHighlightThroughWallsKey;

// 4. KEY BINDING REGISTRATION (add in registerKeyBindings() method after line 289)
// ----------------------------------------------------------------------------
        // New: Block Highlight/Outline
        toggleBlockHighlightKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.visualsfabric.toggle_block_highlight", GLFW.GLFW_KEY_T, "category.visualsfabric.world"));
        toggleBlockHighlightOresKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.visualsfabric.toggle_block_highlight_ores", GLFW.GLFW_KEY_PERIOD, "category.visualsfabric.world"));
        toggleBlockHighlightChestsKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.visualsfabric.toggle_block_highlight_chests", GLFW.GLFW_KEY_SLASH, "category.visualsfabric.world"));
        toggleBlockHighlightModeKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.visualsfabric.cycle_block_highlight_mode", GLFW.GLFW_KEY_LEFT_BRACKET, "category.visualsfabric.world"));
        toggleBlockHighlightPulseKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.visualsfabric.toggle_block_highlight_pulse", GLFW.GLFW_KEY_RIGHT_BRACKET, "category.visualsfabric.world"));
        cycleBlockHighlightColorKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.visualsfabric.cycle_block_highlight_color", GLFW.GLFW_KEY_BACKSLASH, "category.visualsfabric.world"));
        increaseBlockOutlineWidthKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.visualsfabric.increase_block_outline_width", GLFW.GLFW_KEY_EQUAL, "category.visualsfabric.world"));
        decreaseBlockOutlineWidthKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.visualsfabric.decrease_block_outline_width", GLFW.GLFW_KEY_MINUS, "category.visualsfabric.world"));
        toggleBlockHighlightThroughWallsKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.visualsfabric.toggle_block_highlight_through_walls", GLFW.GLFW_KEY_INSERT, "category.visualsfabric.world"));

// 5. KEY HANDLING (add in registerTickEvents() after line 344, after Biome Color Grading)
// ----------------------------------------------------------------------------
            // New: Block Highlight/Outline
            while (toggleBlockHighlightKey.wasPressed()) { blockHighlightEnabled = !blockHighlightEnabled; sendChatFeedback("Block Highlight", blockHighlightEnabled); }
            while (toggleBlockHighlightOresKey.wasPressed()) { blockHighlightOres = !blockHighlightOres; sendChatFeedback("Block Highlight Ores", blockHighlightOres); }
            while (toggleBlockHighlightChestsKey.wasPressed()) { blockHighlightChests = !blockHighlightChests; sendChatFeedback("Block Highlight Chests", blockHighlightChests); }
            while (toggleBlockHighlightModeKey.wasPressed()) { blockHighlightMode = (blockHighlightMode + 1) % 3; sendChatFeedback("Block Highlight Mode", getBlockHighlightModeName(blockHighlightMode)); }
            while (toggleBlockHighlightPulseKey.wasPressed()) { blockHighlightPulseEnabled = !blockHighlightPulseEnabled; sendChatFeedback("Block Highlight Pulse", blockHighlightPulseEnabled); }
            while (cycleBlockHighlightColorKey.wasPressed()) { cycleBlockHighlightColor(); sendChatFeedback("Block Highlight Color", getBlockHighlightColorName()); }
            while (increaseBlockOutlineWidthKey.wasPressed()) { blockOutlineWidth = Math.min(5.0f, blockOutlineWidth + 0.5f); sendChatFeedback("Block Outline Width", String.format("%.1f", blockOutlineWidth)); }
            while (decreaseBlockOutlineWidthKey.wasPressed()) { blockOutlineWidth = Math.max(0.5f, blockOutlineWidth - 0.5f); sendChatFeedback("Block Outline Width", String.format("%.1f", blockOutlineWidth)); }
            while (toggleBlockHighlightThroughWallsKey.wasPressed()) { blockHighlightThroughWalls = !blockHighlightThroughWalls; sendChatFeedback("Block Highlight Through Walls", blockHighlightThroughWalls); }

// 6. HELPER METHODS (add after getCrosshairStyleName() around line 370)
// ----------------------------------------------------------------------------
    private String getBlockHighlightModeName(int mode) {
        return switch (mode) {
            case 0 -> "Outline";
            case 1 -> "Fill";
            case 2 -> "Both";
            default -> "Outline";
        };
    }

    private void cycleBlockHighlightColor() {
        // Cycle through preset colors: Yellow, Red, Green, Blue, Cyan, Magenta, White, Orange
        float[][] colors = {
            {1.0f, 1.0f, 0.0f}, // Yellow
            {1.0f, 0.0f, 0.0f}, // Red
            {0.0f, 1.0f, 0.0f}, // Green
            {0.0f, 0.0f, 1.0f}, // Blue
            {0.0f, 1.0f, 1.0f}, // Cyan
            {1.0f, 0.0f, 1.0f}, // Magenta
            {1.0f, 1.0f, 1.0f}, // White
            {1.0f, 0.5f, 0.0f}  // Orange
        };
        float[] current = {blockHighlightR, blockHighlightG, blockHighlightB};
        int currentIndex = 0;
        for (int i = 0; i < colors.length; i++) {
            if (Math.abs(colors[i][0] - current[0]) < 0.01f &&
                Math.abs(colors[i][1] - current[1]) < 0.01f &&
                Math.abs(colors[i][2] - current[2]) < 0.01f) {
                currentIndex = i;
                break;
            }
        }
        int nextIndex = (currentIndex + 1) % colors.length;
        blockHighlightR = colors[nextIndex][0];
        blockHighlightG = colors[nextIndex][1];
        blockHighlightB = colors[nextIndex][2];
    }

    private String getBlockHighlightColorName() {
        if (Math.abs(blockHighlightR - 1.0f) < 0.01f && Math.abs(blockHighlightG - 1.0f) < 0.01f && Math.abs(blockHighlightB - 0.0f) < 0.01f) return "Yellow";
        if (Math.abs(blockHighlightR - 1.0f) < 0.01f && Math.abs(blockHighlightG - 0.0f) < 0.01f && Math.abs(blockHighlightB - 0.0f) < 0.01f) return "Red";
        if (Math.abs(blockHighlightR - 0.0f) < 0.01f && Math.abs(blockHighlightG - 1.0f) < 0.01f && Math.abs(blockHighlightB - 0.0f) < 0.01f) return "Green";
        if (Math.abs(blockHighlightR - 0.0f) < 0.01f && Math.abs(blockHighlightG - 0.0f) < 0.01f && Math.abs(blockHighlightB - 1.0f) < 0.01f) return "Blue";
        if (Math.abs(blockHighlightR - 0.0f) < 0.01f && Math.abs(blockHighlightG - 1.0f) < 0.01f && Math.abs(blockHighlightB - 1.0f) < 0.01f) return "Cyan";
        if (Math.abs(blockHighlightR - 1.0f) < 0.01f && Math.abs(blockHighlightG - 0.0f) < 0.01f && Math.abs(blockHighlightB - 1.0f) < 0.01f) return "Magenta";
        if (Math.abs(blockHighlightR - 1.0f) < 0.01f && Math.abs(blockHighlightG - 1.0f) < 0.01f && Math.abs(blockHighlightB - 1.0f) < 0.01f) return "White";
        if (Math.abs(blockHighlightR - 1.0f) < 0.01f && Math.abs(blockHighlightG - 0.5f) < 0.01f && Math.abs(blockHighlightB - 0.0f) < 0.01f) return "Orange";
        return "Custom";
    }

// 7. RENDER EVENT REGISTRATION (add in registerRenderEvents() after line 412)
// ----------------------------------------------------------------------------
        WorldRenderEvents.BEFORE_ENTITIES.register(this::renderBlockHighlight);

// 8. BLOCK HIGHLIGHT RENDER METHOD (add as new method in class, after renderSkyEffects)
// ----------------------------------------------------------------------------
    private void renderBlockHighlight(WorldRenderContext context) {
        if (!blockHighlightEnabled) return;
        if (performanceMode && reducedParticles) return;

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null || client.player == null) return;

        MatrixStack matrices = context.matrixStack();
        float tickDelta = context.tickCounter().getTickDelta(false);

        // Calculate pulse alpha if enabled (uses effectTime for animation)
        float pulseAlpha = blockHighlightA;
        if (blockHighlightPulseEnabled) {
            float pulse = (float)Math.sin(effectTime * blockHighlightPulseSpeed) * 0.5f + 0.5f;
            pulseAlpha = blockHighlightPulseMinAlpha + (blockHighlightPulseMaxAlpha - blockHighlightPulseMinAlpha) * pulse;
        }

        // Get camera position for distance culling
        Camera camera = client.gameRenderer.getCamera();
        Vec3d cameraPos = camera.getPos();

        // Collect blocks to highlight
        java.util.List<BlockPos> highlightBlocks = new java.util.ArrayList<>();
        int range = (int)blockHighlightRange;

        // Check blocks in range around player
        BlockPos playerPos = client.player.getBlockPos();
        for (int x = -range; x <= range; x++) {
            for (int y = -range; y <= range; y++) {
                for (int z = -range; z <= range; z++) {
                    BlockPos pos = playerPos.add(x, y, z);
                    if (pos.getSquaredDistance(playerPos) > range * range) continue;

                    BlockState blockState = client.world.getBlockState(pos);
                    if (blockState.isAir()) continue;

                    boolean shouldHighlight = false;

                    // Check for ores
                    if (blockHighlightOres && isOreBlock(blockState)) {
                        shouldHighlight = true;
                    }
                    // Check for chests
                    else if (blockHighlightChests && isChestBlock(blockState)) {
                        shouldHighlight = true;
                    }

                    if (shouldHighlight) {
                        // Distance culling if not through walls
                        if (!blockHighlightThroughWalls) {
                            if (!hasLineOfSight(client.world, cameraPos, pos)) continue;
                        }
                        highlightBlocks.add(pos);
                    }
                }
            }
        }

        if (highlightBlocks.isEmpty()) return;

        // Render highlights
        matrices.push();

        // Translate to camera-relative coordinates
        matrices.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);

        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        RenderSystem.disableCull();
        RenderSystem.lineWidth(blockOutlineWidth);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer;

        for (BlockPos pos : highlightBlocks) {
            BlockState blockState = client.world.getBlockState(pos);
            Box box = blockState.getOutlineShape(client.world, pos).getBoundingBox()
                .expand(0.002, 0.002, 0.002) // Slightly larger than block
                .offset(pos.getX(), pos.getY(), pos.getZ());

            // Render based on mode
            if (blockHighlightMode == 0 || blockHighlightMode == 2) { // Outline or Both
                RenderSystem.setShaderColor(blockHighlightR, blockHighlightG, blockHighlightB, pulseAlpha);

                buffer = tessellator.begin(VertexFormat.DrawMode.LINES, VertexFormats.POSITION);
                // 12 edges of a cube - Bottom face
                buffer.vertex((float)box.minX, (float)box.minY, (float)box.minZ);
                buffer.vertex((float)box.maxX, (float)box.minY, (float)box.minZ);
                buffer.vertex((float)box.maxX, (float)box.minY, (float)box.minZ);
                buffer.vertex((float)box.maxX, (float)box.minY, (float)box.maxZ);
                buffer.vertex((float)box.maxX, (float)box.minY, (float)box.maxZ);
                buffer.vertex((float)box.minX, (float)box.minY, (float)box.maxZ);
                buffer.vertex((float)box.minX, (float)box.minY, (float)box.maxZ);
                buffer.vertex((float)box.minX, (float)box.minY, (float)box.minZ);
                // Top face
                buffer.vertex((float)box.minX, (float)box.maxY, (float)box.minZ);
                buffer.vertex((float)box.maxX, (float)box.maxY, (float)box.minZ);
                buffer.vertex((float)box.maxX, (float)box.maxY, (float)box.minZ);
                buffer.vertex((float)box.maxX, (float)box.maxY, (float)box.maxZ);
                buffer.vertex((float)box.maxX, (float)box.maxY, (float)box.maxZ);
                buffer.vertex((float)box.minX, (float)box.maxY, (float)box.maxZ);
                buffer.vertex((float)box.minX, (float)box.maxY, (float)box.maxZ);
                buffer.vertex((float)box.minX, (float)box.maxY, (float)box.minZ);
                // Vertical edges
                buffer.vertex((float)box.minX, (float)box.minY, (float)box.minZ);
                buffer.vertex((float)box.minX, (float)box.maxY, (float)box.minZ);
                buffer.vertex((float)box.maxX, (float)box.minY, (float)box.minZ);
                buffer.vertex((float)box.maxX, (float)box.maxY, (float)box.minZ);
                buffer.vertex((float)box.maxX, (float)box.minY, (float)box.maxZ);
                buffer.vertex((float)box.maxX, (float)box.maxY, (float)box.maxZ);
                buffer.vertex((float)box.minX, (float)box.minY, (float)box.maxZ);
                buffer.vertex((float)box.minX, (float)box.maxY, (float)box.maxZ);

                BufferRenderer.drawWithGlobalProgram(buffer.end());
            }

            if (blockHighlightMode == 1 || blockHighlightMode == 2) { // Fill or Both
                RenderSystem.setShaderColor(blockHighlightR, blockHighlightG, blockHighlightB, pulseAlpha * 0.15f);

                buffer = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION);
                // Bottom
                buffer.vertex((float)box.minX, (float)box.minY, (float)box.minZ);
                buffer.vertex((float)box.maxX, (float)box.minY, (float)box.minZ);
                buffer.vertex((float)box.maxX, (float)box.minY, (float)box.maxZ);
                buffer.vertex((float)box.minX, (float)box.minY, (float)box.maxZ);
                // Top
                buffer.vertex((float)box.minX, (float)box.maxY, (float)box.minZ);
                buffer.vertex((float)box.minX, (float)box.maxY, (float)box.maxZ);
                buffer.vertex((float)box.maxX, (float)box.maxY, (float)box.maxZ);
                buffer.vertex((float)box.maxX, (float)box.maxY, (float)box.minZ);
                // North
                buffer.vertex((float)box.minX, (float)box.minY, (float)box.minZ);
                buffer.vertex((float)box.minX, (float)box.maxY, (float)box.minZ);
                buffer.vertex((float)box.maxX, (float)box.maxY, (float)box.minZ);
                buffer.vertex((float)box.maxX, (float)box.minY, (float)box.minZ);
                // South
                buffer.vertex((float)box.maxX, (float)box.minY, (float)box.maxZ);
                buffer.vertex((float)box.maxX, (float)box.maxY, (float)box.maxZ);
                buffer.vertex((float)box.minX, (float)box.maxY, (float)box.maxZ);
                buffer.vertex((float)box.minX, (float)box.minY, (float)box.maxZ);
                // West
                buffer.vertex((float)box.minX, (float)box.minY, (float)box.maxZ);
                buffer.vertex((float)box.minX, (float)box.maxY, (float)box.maxZ);
                buffer.vertex((float)box.minX, (float)box.maxY, (float)box.minZ);
                buffer.vertex((float)box.minX, (float)box.minY, (float)box.minZ);
                // East
                buffer.vertex((float)box.maxX, (float)box.minY, (float)box.minZ);
                buffer.vertex((float)box.maxX, (float)box.maxY, (float)box.minZ);
                buffer.vertex((float)box.maxX, (float)box.maxY, (float)box.maxZ);
                buffer.vertex((float)box.maxX, (float)box.minY, (float)box.maxZ);

                BufferRenderer.drawWithGlobalProgram(buffer.end());
            }
        }

        RenderSystem.setShaderColor(1, 1, 1, 1);
        RenderSystem.enableCull();
        RenderSystem.lineWidth(1.0f);
        RenderSystem.disableBlend();

        matrices.pop();
    }

// 9. HELPER METHODS FOR BLOCK DETECTION (add at end of class before closing brace)
// ----------------------------------------------------------------------------
    private boolean isOreBlock(BlockState blockState) {
        Block block = blockState.getBlock();
        // Check for common ore blocks using tags or direct comparison
        return block instanceof net.minecraft.block.OreBlock ||
               block instanceof net.minecraft.block.ExperienceDroppingBlock ||
               block == Blocks.DIAMOND_ORE ||
               block == Blocks.DEEPSLATE_DIAMOND_ORE ||
               block == Blocks.EMERALD_ORE ||
               block == Blocks.DEEPSLATE_EMERALD_ORE ||
               block == Blocks.GOLD_ORE ||
               block == Blocks.DEEPSLATE_GOLD_ORE ||
               block == Blocks.IRON_ORE ||
               block == Blocks.DEEPSLATE_IRON_ORE ||
               block == Blocks.COPPER_ORE ||
               block == Blocks.DEEPSLATE_COPPER_ORE ||
               block == Blocks.COAL_ORE ||
               block == Blocks.DEEPSLATE_COAL_ORE ||
               block == Blocks.REDSTONE_ORE ||
               block == Blocks.DEEPSLATE_REDSTONE_ORE ||
               block == Blocks.LAPIS_ORE ||
               block == Blocks.DEEPSLATE_LAPIS_ORE ||
               block == Blocks.NETHER_GOLD_ORE ||
               block == Blocks.NETHER_QUARTZ_ORE ||
               block == Blocks.ANCIENT_DEBRIS ||
               blockState.getBlock().getTranslationKey().contains("ore");
    }

    private boolean isChestBlock(BlockState blockState) {
        Block block = blockState.getBlock();
        return block instanceof net.minecraft.block.ChestBlock ||
               block instanceof net.minecraft.block.EnderChestBlock ||
               block instanceof net.minecraft.block.TrappedChestBlock ||
               block == Blocks.CHEST ||
               block == Blocks.ENDER_CHEST ||
               block == Blocks.TRAPPED_CHEST ||
               block == Blocks.BARREL ||
               block == Blocks.SHULKER_BOX ||
               block == Blocks.WHITE_SHULKER_BOX ||
               block == Blocks.ORANGE_SHULKER_BOX ||
               block == Blocks.MAGENTA_SHULKER_BOX ||
               block == Blocks.LIGHT_BLUE_SHULKER_BOX ||
               block == Blocks.YELLOW_SHULKER_BOX ||
               block == Blocks.LIME_SHULKER_BOX ||
               block == Blocks.PINK_SHULKER_BOX ||
               block == Blocks.GRAY_SHULKER_BOX ||
               block == Blocks.LIGHT_GRAY_SHULKER_BOX ||
               block == Blocks.CYAN_SHULKER_BOX ||
               block == Blocks.PURPLE_SHULKER_BOX ||
               block == Blocks.BLUE_SHULKER_BOX ||
               block == Blocks.BROWN_SHULKER_BOX ||
               block == Blocks.GREEN_SHULKER_BOX ||
               block == Blocks.RED_SHULKER_BOX ||
               block == Blocks.BLACK_SHULKER_BOX;
    }

    private boolean hasLineOfSight(World world, Vec3d from, BlockPos to) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (world == null || client.player == null) return true;
        Vec3d toVec = new Vec3d(to.getX() + 0.5, to.getY() + 0.5, to.getZ() + 0.5);
        HitResult result = world.raycast(new RaycastContext(from, toVec,
            RaycastContext.ShapeType.OUTLINE, RaycastContext.FluidHandling.NONE, client.player));
        return result.getType() == HitResult.Type.MISS ||
               (result.getType() == HitResult.Type.BLOCK &&
                ((BlockHitResult)result).getBlockPos().equals(to));
    }

// 10. ADDITIONAL IMPORTS NEEDED (add at top of file with other imports)
// ----------------------------------------------------------------------------
// import net.minecraft.block.Block;
// import net.minecraft.block.BlockState;
// import net.minecraft.block.Blocks;
// import net.minecraft.util.hit.BlockHitResult;
// import net.minecraft.util.hit.HitResult;
// import net.minecraft.util.math.Box;
// import net.minecraft.util.math.RaycastContext;
// import net.minecraft.util.shape.VoxelShape;
// import net.minecraft.world.World;
// import java.util.List;
// import java.util.ArrayList;