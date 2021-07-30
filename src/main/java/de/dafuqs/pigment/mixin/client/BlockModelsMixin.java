package de.dafuqs.pigment.mixin.client;

import de.dafuqs.pigment.misc.PigmentBlockCloaker;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.block.BlockModels;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedModelManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

@Mixin(BlockModels.class)
public class BlockModelsMixin {

    @Shadow
    @Final
    private Map<BlockState, BakedModel> models;

    @Shadow
    @Final
    private BakedModelManager modelManager;

    @Inject(at = @At("HEAD"), method = "getModel", cancellable = true)
    private void getModel(BlockState blockState, CallbackInfoReturnable<BakedModel> callbackInfoReturnable) {
        if(PigmentBlockCloaker.isCloaked(blockState)) {
            BlockState destinationBlockState = PigmentBlockCloaker.getTarget(blockState);
            BakedModel overriddenModel = this.models.getOrDefault(destinationBlockState, modelManager.getMissingModel());
            callbackInfoReturnable.setReturnValue(overriddenModel);
        }
    }

}
