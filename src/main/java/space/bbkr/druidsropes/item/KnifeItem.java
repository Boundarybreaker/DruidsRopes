package space.bbkr.druidsropes.item;

import java.util.List;

import javax.annotation.Nullable;

import space.bbkr.druidsropes.Knifeable;

import net.minecraft.block.BlockState;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.state.property.Properties;
import net.minecraft.state.property.Property;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

public class KnifeItem extends Item {

	public KnifeItem(Item.Settings properties) {
		super(properties);
	}

	@Override
	public ActionResult useOnBlock(ItemUsageContext context) {
		PlayerEntity player = context.getPlayer();
		if (player == null) return ActionResult.PASS;

		World world = context.getWorld();
		BlockPos pos = context.getBlockPos();
		BlockState state = world.getBlockState(pos);

		if (state.getBlock() instanceof Knifeable) {
			ActionResult result = ((Knifeable) state.getBlock()).onKnife(context);
			if (result != ActionResult.PASS) {
				return result;
			}
		}

		if (player.isSneaking() && state.contains(Properties.HORIZONTAL_FACING)) {
			BlockState state1 = cycleProperty(state, Properties.HORIZONTAL_FACING);
			world.setBlockState(pos, state1, 18);
			return ActionResult.SUCCESS;
		}

		return super.useOnBlock(context);
	}

	private static <T extends Comparable<T>> BlockState cycleProperty(BlockState state, Property<T> propertyIn) {
		return state.with(propertyIn, getAdjacentValue(propertyIn.getValues(), state.get(propertyIn)));
	}

	private static <T> T getAdjacentValue(Iterable<T> iterable, @Nullable T value) {
		return Util.next(iterable, value);
	}

	@Environment(EnvType.CLIENT)
	@Override
	public void appendTooltip(ItemStack stack, @Nullable World worldIn, List<Text> tooltip, TooltipContext flagIn) {
		if (worldIn == null) return;
		if (!Screen.hasShiftDown()) {
			tooltip.add(new TranslatableText("item.druidsropes.hold_shift").formatted(Formatting.GRAY, Formatting.ITALIC));
		} else {
			tooltip.add(new TranslatableText("item.druidsropes.knife.description1").formatted(Formatting.GRAY, Formatting.ITALIC));
		}
	}
}
