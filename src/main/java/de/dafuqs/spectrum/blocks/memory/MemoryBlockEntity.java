package de.dafuqs.spectrum.blocks.memory;

import de.dafuqs.spectrum.interfaces.PlayerOwned;
import de.dafuqs.spectrum.progression.SpectrumAdvancementCriteria;
import de.dafuqs.spectrum.registries.SpectrumBlockEntityRegistry;
import de.dafuqs.spectrum.registries.SpectrumBlockTags;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.UUID;

public class MemoryBlockEntity extends BlockEntity implements PlayerOwned {
	
	protected ItemStack creatureSpawnItemStack = ItemStack.EMPTY;
	protected int ticksToHatch = -1; // zero or negative values: never hatch
	
	protected UUID ownerUUID;
	
	public MemoryBlockEntity(BlockPos pos, BlockState state) {
		super(SpectrumBlockEntityRegistry.MEMORY, pos, state);
	}
	
	public void setData(PlayerEntity playerEntity, @NotNull ItemStack creatureSpawnItemStack) {
		setOwner(playerEntity);
		
		if(creatureSpawnItemStack.getItem() instanceof MemoryItem) {
			this.creatureSpawnItemStack = creatureSpawnItemStack;
		}
	}
	
	@Override
	public void readNbt(NbtCompound nbt) {
		super.readNbt(nbt);
		
		if(nbt.contains("MemoryItem", NbtElement.COMPOUND_TYPE)) {
			NbtCompound creatureSpawnCompound = nbt.getCompound("MemoryItem");
			this.creatureSpawnItemStack = ItemStack.fromNbt(creatureSpawnCompound);
		}
	}
	
	@Override
	protected void writeNbt(NbtCompound nbt) {
		super.writeNbt(nbt);
		if(this.creatureSpawnItemStack != null) {
			NbtCompound creatureSpawnCompound = new NbtCompound();
			creatureSpawnItemStack.writeNbt(creatureSpawnCompound);
			nbt.put("MemoryItem", creatureSpawnCompound);
		}
	}
	
	public void advanceHatching(ServerWorld world, BlockPos blockPos) {
		if(this.ticksToHatch > 0) {
			this.ticksToHatch -= getHatchAdvanceSteps(world, blockPos);
			if(this.ticksToHatch <= 0) {
				this.hatch(world, blockPos);
			}
		}
	}
	
	public void hatch(ServerWorld world, BlockPos blockPos) {
		Optional<Entity> hatchedEntity = hatchEntity(world, blockPos, this.creatureSpawnItemStack);
		if(hatchedEntity.isPresent()) {
			triggerHatchingAdvancementCriterion(hatchedEntity.get());
		}
	}
	
	protected void triggerHatchingAdvancementCriterion(Entity hatchedEntity) {
		PlayerEntity owner = PlayerOwned.getPlayerEntityIfOnline(world, this.ownerUUID);
		if(owner instanceof ServerPlayerEntity serverPlayerEntity) {
			SpectrumAdvancementCriteria.MEMORY_MANIFESTING.trigger(serverPlayerEntity, hatchedEntity);
		}
	}
	
	protected Optional<Entity> hatchEntity(ServerWorld world, BlockPos blockPos, ItemStack itemStack) {
		if(!(this.creatureSpawnItemStack.getItem() instanceof MemoryItem)) {
			Optional<EntityType<?>> entityType = MemoryItem.getEntityType(creatureSpawnItemStack.getNbt());
			if(entityType.isPresent()) {
				// alignPosition: center the mob in the center of the blockPos
				Entity entity = entityType.get().spawnFromItemStack(world, creatureSpawnItemStack, null, blockPos, SpawnReason.SPAWN_EGG, true, false);
				if(entity != null) {
					if (entity instanceof MobEntity mobEntity) {
						mobEntity.setBaby(true);
						if (itemStack.hasCustomName()) {
							mobEntity.setCustomName(itemStack.getName());
						}
					}
					
					world.spawnEntityAndPassengers(entity);
					return Optional.of(entity);
				}
			}
		}
		return Optional.empty();
	}
	
	public static int getHatchAdvanceSteps(@NotNull World world, @NotNull BlockPos blockPos) {
		BlockState belowBlockState = world.getBlockState(blockPos.down());
		if(belowBlockState.isIn(SpectrumBlockTags.MEMORY_NEVER_MANIFESTERS)) {
			return 0;
		} else if(belowBlockState.isIn(SpectrumBlockTags.MEMORY_VERY_FAST_MANIFESTERS)) {
			return 8;
		} else if(belowBlockState.isIn(SpectrumBlockTags.MEMORY_FAST_MANIFESTERS)) {
			return 3;
		} else {
			return 1;
		}
	}
	
	@Override
	public UUID getOwnerUUID() {
		return this.ownerUUID;
	}
	
	@Override
	public void setOwner(@NotNull PlayerEntity playerEntity) {
		this.ownerUUID = playerEntity.getUuid();
	}
	
}