package de.dafuqs.spectrum.items.trinkets;

import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import de.dafuqs.spectrum.SpectrumCommon;
import dev.emi.trinkets.api.*;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class AttackRingItem extends SpectrumTrinketItem {
	
	private final Identifier UNLOCK_IDENTIFIER = new Identifier(SpectrumCommon.MOD_ID, "midgame/spectrum_midgame");
	
	public static final UUID ATTACK_RING_DAMAGE_UUID = UUID.fromString("15d1fb68-6440-404a-aa31-7bf3310d3f52");

	public AttackRingItem(Settings settings) {
		super(settings);
	}
	
	@Override
	public void tick(ItemStack stack, SlotReference slot, LivingEntity entity) {
		super.tick(stack, slot, entity);
		
	}
	
	@Override
	public void onUnequip(ItemStack stack, SlotReference slot, LivingEntity entity) {
		super.onUnequip(stack, slot, entity);
		if(entity.getAttributes().hasModifierForAttribute(EntityAttributes.GENERIC_ATTACK_DAMAGE, AttackRingItem.ATTACK_RING_DAMAGE_UUID)) {
			Multimap<EntityAttribute, EntityAttributeModifier> map = Multimaps.newMultimap(Maps.newLinkedHashMap(), ArrayList::new);
			EntityAttributeModifier modifier = new EntityAttributeModifier(AttackRingItem.ATTACK_RING_DAMAGE_UUID, "spectrum:attack_ring", AttackRingItem.getAttackModifierForEntity(entity), EntityAttributeModifier.Operation.MULTIPLY_TOTAL);
			map.put(EntityAttributes.GENERIC_ATTACK_DAMAGE, modifier);
			entity.getAttributes().removeModifiers(map);
		}
	}
	
	@Override
	public boolean canEquip(ItemStack stack, SlotReference slot, LivingEntity entity) {
		// Only a single trinket of that type may be equipped at once
		if(SpectrumTrinketItem.hasEquipped(entity, this)) {
			return false;
		}
		
		return super.canEquip(stack, slot, entity);
	}
	
	@Override
	protected Identifier getUnlockIdentifier() {
		return UNLOCK_IDENTIFIER;
	}
	
	@Environment(EnvType.CLIENT)
	@Override
	public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
		super.appendTooltip(stack, world, tooltip, context);
		tooltip.add(new TranslatableText("item.spectrum.attack_ring.tooltip.damage", Math.round(getAttackModifierForEntity(MinecraftClient.getInstance().player) * 100)));
	}
	
	public static double getAttackModifierForEntity(LivingEntity entity) {
		if(entity == null) {
			return 0;
		} else {
			double mod = entity.getMaxHealth() / (entity.getHealth() * entity.getHealth() + 1); // starting with 1 % damage at 14 health up to 300 % damage at 1/20 health
			return Math.max(0, 1 + Math.log10(mod));
		}
	}
	
}