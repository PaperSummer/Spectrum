package de.dafuqs.spectrum.blocks.jade_vines;

import de.dafuqs.spectrum.helpers.TimeHelper;
import de.dafuqs.spectrum.progression.advancement.FusionShrineCraftingCriterion;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.CropBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.LightType;
import net.minecraft.world.World;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Random;

public class JadeVinesBlock extends BlockWithEntity {
	
	public enum JadeVinesBlockPart implements StringIdentifiable {
		UPPER,
		CENTER,
		LOWER;
		
		@Contract(pure = true)
		public @NotNull String toString() {
			return this.asString();
		}
		
		@Contract(pure = true)
		public @NotNull String asString() {
			return this == UPPER ? "upper" : this == CENTER ? "center" : "lower";
		}
	}
	
	public enum JadeVinesGrowthStage {
		DEAD,
		LEAVES,
		PETALS,
		NECTAR;
		
		public static JadeVinesGrowthStage fromAge(int age) {
			if(age == 0) {
				return DEAD;
			} else if(age == Properties.AGE_7_MAX) {
				return NECTAR;
			} else if(age > 2) {
				return PETALS;
			} else {
				return LEAVES;
			}
		}
		
		public static boolean fullyGrown(int age) {
			return age == Properties.AGE_7_MAX;
		}
		
		public static boolean dead(int age) {
			return age == 0;
		}
		
	}
	
	protected static final VoxelShape SHAPE = Block.createCuboidShape(4.0D, 0.0D, 4.0D, 12.0D, 14.0D, 12.0D);
	
	public static final EnumProperty<JadeVinesBlockPart> PART = EnumProperty.of("part", JadeVinesBlockPart.class);
	public static final IntProperty AGE = Properties.AGE_7;
	
	public JadeVinesBlock(Settings settings) {
		super(settings);
		this.setDefaultState((this.stateManager.getDefaultState()).with(PART, JadeVinesBlockPart.UPPER).with(AGE, 1));
	}
	
	@Override
	public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
		super.randomTick(state, world, pos, random);
		
		int age = state.get(AGE);
		JadeVinesGrowthStage stage = JadeVinesGrowthStage.fromAge(age);
		if(stage != JadeVinesGrowthStage.DEAD) {
			// die in sunlight
			if(doesDie(world, pos)) {
				world.setBlockState(pos, state.with(AGE, 0));
				world.playSound(null, pos, SoundEvents.BLOCK_GROWING_PLANT_CROP, SoundCategory.BLOCKS, 0.5F, 0.9F + 0.2F * world.random.nextFloat() * 0.2F);
			} else if (!JadeVinesGrowthStage.fullyGrown(age) && canGrow(world, pos)) {
				BlockState newState = state.cycle(AGE);
				world.setBlockState(pos, newState);
				world.playSound(null, pos, SoundEvents.BLOCK_GROWING_PLANT_CROP, SoundCategory.BLOCKS, 0.5F, 0.9F + 0.2F * world.random.nextFloat() * 0.2F);
			}
		}
	}
	
	public static boolean doesDie(@NotNull World world, BlockPos blockPos) {
		return world.getLightLevel(LightType.SKY, blockPos) > 8 && TimeHelper.isBrightSunlight(world);
	}
	
	public static boolean canGrow(@NotNull World world, BlockPos blockPos) {
		BlockEntity blockEntity = world.getBlockEntity(blockPos);
		if (blockEntity instanceof JadeVinesBlockEntity jadeVinesBlockEntity) {
			if (world.getLightLevel(LightType.SKY, blockPos) > 8 && jadeVinesBlockEntity.isLaterNight(world)) {
				jadeVinesBlockEntity.setLastGrownTime(world.getTime());
				return true;
			}
		}
		return false;
	}
	
	@Override
	protected void appendProperties(StateManager.@NotNull Builder<Block, BlockState> builder) {
		builder.add(PART, AGE);
	}
	
	@Nullable
	@Override
	public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
		return new JadeVinesBlockEntity(pos, state);
	}
	
}