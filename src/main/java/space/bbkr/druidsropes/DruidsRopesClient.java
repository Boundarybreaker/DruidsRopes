package space.bbkr.druidsropes;

import net.minecraft.client.render.RenderLayer;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;

public class DruidsRopesClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		BlockRenderLayerMap.INSTANCE.putBlocks(RenderLayer.getCutout(),
				DruidsRopes.ROPE,
				DruidsRopes.ROPE_LANTERN,
				DruidsRopes.CERAMIC_LANTERN);
	}
}
