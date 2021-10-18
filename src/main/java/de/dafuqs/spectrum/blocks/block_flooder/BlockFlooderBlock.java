package de.dafuqs.spectrum.blocks.block_flooder;

import com.google.common.collect.ImmutableList;
import de.dafuqs.spectrum.InventoryHelper;
import de.dafuqs.spectrum.Support;
import de.dafuqs.spectrum.interfaces.PlayerOwned;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.tag.BlockTags;
import net.minecraft.tag.Tag;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class BlockFlooderBlock extends BlockWithEntity {

    public short MAX_DISTANCE = 10;
    public BlockState DEFAULT_BLOCK_STATE = Blocks.COBBLESTONE.getDefaultState();

    // when replacing blocks there may be cases when there is a good reason to use replacement blocks
    // like using dirt instead of grass, because grass will be growing anyways and silk touching grass
    // is absolutely not worth it / fun
    public static final HashMap<Tag<Block>, Block> exchangeableBlocks = new HashMap<>() {{
       put(BlockTags.DIRT, Blocks.DIRT); // grass, podzol, mycelium, ...
       put(BlockTags.BASE_STONE_OVERWORLD, Blocks.STONE);
       put(BlockTags.BASE_STONE_NETHER, Blocks.NETHERRACK);
       put(BlockTags.SAND, Blocks.SAND);
    }};
    public static final List<Tag<Block>> exchangeBlockTags = ImmutableList.copyOf(exchangeableBlocks.keySet()); // for quick lookup

    public BlockFlooderBlock(Settings settings) {
        super(settings);
    }

    @Override
    public void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify) {
        super.onBlockAdded(state, world, pos, oldState, notify);
        if (!world.isClient) {
            world.getBlockTickScheduler().schedule(pos, state.getBlock(), 4);
        }
    }

    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new BlockFlooderBlockEntity(pos, state);
    }

    @Override
    public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        super.scheduledTick(state, world, pos, random);

        BlockEntity blockEntity = world.getBlockEntity(pos);
        if(blockEntity instanceof BlockFlooderBlockEntity blockFlooderBlockEntity) {
            PlayerEntity owner = PlayerOwned.getPlayerEntityIfOnline(world, blockFlooderBlockEntity.getOwnerUUID());
            if(owner == null) {
                world.setBlockState(pos, DEFAULT_BLOCK_STATE, 3);
                return;
            }

            Hashtable<Block, Integer> neighboringBlockAmounts = new Hashtable<>();
            for(Direction direction : Direction.values()) {
                BlockPos targetBlockPos = pos.offset(direction);
                BlockState currentBlockState = world.getBlockState(targetBlockPos);
                BlockEntity currentBlockEntity = world.getBlockEntity(targetBlockPos);

                if(currentBlockState.isOf(this) || currentBlockEntity != null) {
                    continue;
                } else if(isReplaceableBlock(world, targetBlockPos)) {
                    Vec3i nextPos = new Vec3i(targetBlockPos.offset(direction).getX(), targetBlockPos.offset(direction).getY(), targetBlockPos.offset(direction).getZ());
                    if(blockFlooderBlockEntity.getSourcePos().isWithinDistance(nextPos, MAX_DISTANCE)) {
                        if (shouldPropagateTo(world, targetBlockPos)) {
                            world.setBlockState(targetBlockPos, state, 3);
                            if (world.getBlockEntity(targetBlockPos) instanceof BlockFlooderBlockEntity neighboringBlockFlooderBlockEntity) {
                                neighboringBlockFlooderBlockEntity.setOwnerUUID(blockFlooderBlockEntity.getOwnerUUID());
                                neighboringBlockFlooderBlockEntity.setSourcePos(blockFlooderBlockEntity.getSourcePos());
                            }
                        }
                    }
                } else {
                    Block currentBlock = currentBlockState.getBlock();

                    if(currentBlockState.isSolidBlock(world, targetBlockPos)) {
                        if (neighboringBlockAmounts.contains(currentBlock)) {
                            neighboringBlockAmounts.put(currentBlock, neighboringBlockAmounts.get(currentBlock) + 1);
                        } else {
                            neighboringBlockAmounts.put(currentBlock, 1);
                        }
                    }
                }
            }

            if(neighboringBlockAmounts.size() > 0) {
                int max = 0;
                Block maxBlock = null;
                ItemStack maxBlockItemStack = ItemStack.EMPTY;

                for(Map.Entry<Block, Integer> entry : neighboringBlockAmounts.entrySet()) {
                    Block currentBlock = entry.getKey();
                    int currentOccurrences = entry.getValue();
                    Item blockItem = currentBlock.asItem();

                    if(blockItem != Items.AIR) {
                        if (currentOccurrences > max || (currentOccurrences == max && random.nextBoolean())) {
                            ItemStack currentItemStack = new ItemStack(blockItem);
                            if(owner.isCreative() || owner.getInventory().contains(currentItemStack) && currentBlock.canPlaceAt(currentBlock.getDefaultState(), world, pos)) {
                                maxBlock = currentBlock;
                                maxBlockItemStack = currentItemStack;
                            } else {
                                Optional<Tag> tag = Support.getFirstMatchingTag(currentBlock, exchangeBlockTags);
                                if (tag.isPresent()) {
                                    currentBlock = exchangeableBlocks.get(tag.get());
                                    blockItem = currentBlock.asItem();
                                    if (blockItem != Items.AIR) {
                                        currentItemStack = new ItemStack(blockItem);
                                        if (owner.isCreative() || owner.getInventory().contains(currentItemStack) && currentBlock.canPlaceAt(currentBlock.getDefaultState(), world, pos)) {
                                            maxBlock = currentBlock;
                                            maxBlockItemStack = currentItemStack;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                if(maxBlock != null) {
                    // and turn this to the leftover block state
                    world.setBlockState(pos, maxBlock.getDefaultState(), 3);
                    if(!owner.isCreative()) {
                        InventoryHelper.removeFromInventory(maxBlockItemStack, owner.getInventory().main);
                    }
                } else {
                    world.setBlockState(pos, DEFAULT_BLOCK_STATE, 3);
                }
            }
        } else {
            world.setBlockState(pos, DEFAULT_BLOCK_STATE, 3);
        }
    }

    private boolean shouldPropagateTo(ServerWorld world, BlockPos targetBlockPos) {
        if(isReplaceableBlock(world, targetBlockPos)) {
            int count = 0;
            for (Direction direction : Direction.values()) {
                for (int i = 1; i < MAX_DISTANCE / 2; i++) {
                    BlockPos offsetPos = targetBlockPos.offset(direction, i);
                    if (isValidCornerBlock(world, offsetPos)) {
                        count++;
                        break;
                    }
                }
            }
            return (count >= 4);
        }
        return false;
    }

    public static boolean isReplaceableBlock(World world, BlockPos blockPos) {
        BlockState state = world.getBlockState(blockPos);
        Block block = state.getBlock();
        return world.getBlockEntity(blockPos) == null && !(block instanceof BlockFlooderBlock) && (state.isAir() || block instanceof FluidBlock || state.getMaterial().isReplaceable() || block instanceof AbstractPlantBlock || block instanceof FlowerBlock);
    }

    public static boolean isValidCornerBlock(World world, BlockPos blockPos) {
        BlockState state = world.getBlockState(blockPos);
        Block block = state.getBlock();
        return state.isSolidBlock(world, blockPos) || block instanceof FluidBlock || block instanceof BlockFlooderBlock;
    }

}