package space.bbkr.druidsropes.block;

import java.util.Map;

import javax.annotation.Nullable;

import com.google.common.collect.Maps;
import space.bbkr.druidsropes.DruidsRopes;
import space.bbkr.druidsropes.Knifeable;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.FluidDrainable;
import net.minecraft.block.FluidFillable;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Util;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;

public class RopeBlock extends Block implements Knifeable, FluidFillable, FluidDrainable {
	public static final BooleanProperty NORTH = BooleanProperty.of("north");
	public static final BooleanProperty EAST = BooleanProperty.of("east");
	public static final BooleanProperty SOUTH = BooleanProperty.of("south");
	public static final BooleanProperty WEST = BooleanProperty.of("west");
	public static final BooleanProperty UP = BooleanProperty.of("up");
	public static final BooleanProperty DOWN = BooleanProperty.of("down");
	public static final BooleanProperty KNOTTED = BooleanProperty.of("knotted");
	public static final BooleanProperty WATERLOGGED = Properties.WATERLOGGED;
	public static final Map<Direction, BooleanProperty> FACING_TO_PROPERTY_MAP = Util.make(Maps.newEnumMap(Direction.class), (map) -> {
		map.put(Direction.NORTH, EAST);
		map.put(Direction.EAST, EAST);
		map.put(Direction.SOUTH, SOUTH);
		map.put(Direction.WEST, WEST);
		map.put(Direction.UP, UP);
		map.put(Direction.DOWN, DOWN);
	});

	public RopeBlock(AbstractBlock.Settings properties) {
		super(properties);
		this.setDefaultState(this.getDefaultState()
				.with(NORTH, false)
				.with(EAST, false)
				.with(SOUTH, false)
				.with(WEST, false)
				.with(UP, false)
				.with(DOWN, false)
				.with(KNOTTED, false)
				.with(WATERLOGGED, false));
	}

	@Override
	public ActionResult onKnife(ItemUsageContext context) {
		BlockPos pos = context.getBlockPos();
		World world = context.getWorld();
		BlockState state = world.getBlockState(pos);
		Vec3d relative = context.getHitPos().subtract(pos.getX(), pos.getY(), pos.getZ());

		Direction side = getClickedConnection(relative);
		if (side != null) {
			BlockPos sidePos = pos.offset(side);
			 BlockState sideState = world.getBlockState(sidePos);
			if (!(world.getBlockState(pos.offset(side)).getBlock() instanceof RopeBlock)) {
				BlockState state1 = cycleProperty(state, side, context);
				world.setBlockState(pos, state1, 18);
				world.setBlockState(sidePos, sideState.getStateForNeighborUpdate(side.getOpposite(), sideState, world, sidePos, pos), 18);
				return ActionResult.SUCCESS;
			}
		}

		return ActionResult.PASS;
	}

	@Override
	public boolean canPathfindThrough(BlockState state, BlockView world, BlockPos pos, NavigationType type) {
		return false;
	}

	private BlockState cycleProperty(BlockState state, Direction sideUsed, ItemUsageContext context) {
		BooleanProperty property = FACING_TO_PROPERTY_MAP.get(sideUsed);
		if (!context.getPlayer().isSneaking()) {
			if (state.get(property)) {
				return calculateKnot(state.with(property, false));
			} else {
				if (context.getWorld().getBlockState(context.getBlockPos()).isSideSolidFullSquare(context.getWorld(), context.getBlockPos().offset(sideUsed), sideUsed)) {
					return calculateKnot(state.with(property, true));
				}
			}
		}
		else if (context.getPlayer().isSneaking()) {
			if (!state.get(KNOTTED))
				return state.with(KNOTTED, true);
			else if (!calculateKnot(state).get(KNOTTED)) {
				return state.with(KNOTTED, false);
			}
		}
		return calculateState(state, context.getWorld(), context.getBlockPos());
	}

	@Nullable
	private static Direction getClickedConnection(Vec3d relative) {
		if (relative.x < 0.25)
			return Direction.WEST;
		if (relative.x > 0.75)
			return Direction.EAST;
		if (relative.y < 0.25)
			return Direction.DOWN;
		if (relative.y > 0.75)
			return Direction.UP;
		if (relative.z < 0.25)
			return Direction.NORTH;
		if (relative.z > 0.75)
			return Direction.SOUTH;
		return null;
	}

	@Override
	public VoxelShape getOutlineShape(BlockState state, BlockView worldIn, BlockPos pos, ShapeContext context) {
		VoxelShape shape = Block.createCuboidShape(6.0f, 6.0f, 6.0f, 10.0f, 10.0f, 10.0f);
		if (state.get(NORTH)) {
			shape = VoxelShapes.union(shape, Block.createCuboidShape(6.0f, 6.0f, 0.0f, 10.0f, 10.0f, 6.0f));
		}
		if (state.get(SOUTH)) {
			shape = VoxelShapes.union(shape, Block.createCuboidShape(6.0f, 6.0f, 10.0f, 10.0f, 10.0f, 16.0f));
		}
		if (state.get(WEST)) {
			shape = VoxelShapes.union(shape, Block.createCuboidShape(0.0f, 6.0f, 6.0f, 6.0f, 10.0f, 10.0f));
		}
		if (state.get(EAST)) {
			shape = VoxelShapes.union(shape, Block.createCuboidShape(10.0f, 6.0f, 6.0f, 16.0f, 10.0f, 10.0f));
		}
		if (state.get(DOWN)) {
			shape = VoxelShapes.union(shape, Block.createCuboidShape(6.0f, 0.0f, 6.0f, 10.0f, 6.0f, 10.0f));
		}
		if (state.get(UP)) {
			shape = VoxelShapes.union(shape, Block.createCuboidShape(6.0f, 10.0f, 6.0f, 10.0f, 16.0f, 10.0f));
		}
		return shape;
	}

	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
		builder.add(NORTH, EAST, SOUTH, WEST, UP, DOWN, KNOTTED, WATERLOGGED);
	}

	@Override
	public BlockState getPlacementState(ItemPlacementContext context) {
		FluidState fluidstate = context.getWorld().getFluidState(context.getBlockPos());
		return calculateState(getDefaultState(), context.getWorld(), context.getBlockPos()).with(WATERLOGGED, fluidstate.getFluid() == Fluids.WATER);
	}

	private BlockState calculateKnot (BlockState currentState) {
		int count = 0;

		if (currentState.get(NORTH))
			count++;
		if (currentState.get(SOUTH))
			count++;
		if (currentState.get(EAST))
			count++;
		if (currentState.get(WEST))
			count++;
		if (currentState.get(UP))
			count++;
		if (currentState.get(DOWN))
			count++;

		boolean doKnot = count > 2 || count == 0;
		return currentState.with(KNOTTED, doKnot);
	}

	@Override
	public boolean isTranslucent(BlockState state, BlockView reader, BlockPos pos) {
		return false;
	}

	@Override
	public BlockState getStateForNeighborUpdate(BlockState state, Direction facing, BlockState facingState, WorldAccess world, BlockPos currentPos, BlockPos facingPos) {
		if (state.get(WATERLOGGED)) {
			world.getFluidTickScheduler().schedule(currentPos, Fluids.WATER, Fluids.WATER.getTickRate(world));
		}
		return calculateState(state, world, currentPos);
	}

	private BlockState calculateState(BlockState currentState, WorldAccess world, BlockPos pos) {

		boolean northType = false;
		BlockState northState = world.getBlockState(pos.offset(Direction.NORTH));
		if (northState.isSideSolidFullSquare(world, pos.offset(Direction.NORTH), Direction.NORTH.getOpposite())
				|| northState.getBlock() instanceof RopeBlock
				|| northState.getBlock() instanceof SmallBeamBlock) {
			northType = true;
		}

		boolean eastType = false;
		BlockState eastState = world.getBlockState(pos.offset(Direction.EAST));
		if (eastState.isSideSolidFullSquare(world, pos.offset(Direction.EAST), Direction.EAST.getOpposite())
				|| eastState.getBlock() instanceof RopeBlock
				|| eastState.getBlock() instanceof SmallBeamBlock) {
			eastType = true;
		}

		boolean southType = false;
		BlockState southState = world.getBlockState(pos.offset(Direction.SOUTH));
		if (southState.isSideSolidFullSquare(world, pos.offset(Direction.SOUTH), Direction.SOUTH.getOpposite())
				|| southState.getBlock() instanceof RopeBlock
				|| southState.getBlock() instanceof SmallBeamBlock) {
			southType = true;
		}

		boolean westType = false;
		BlockState westState = world.getBlockState(pos.offset(Direction.WEST));
		if (westState.isSideSolidFullSquare(world, pos.offset(Direction.WEST), Direction.WEST.getOpposite())
				|| westState.getBlock() instanceof RopeBlock
				|| westState.getBlock() instanceof SmallBeamBlock) {
			westType = true;
		}

		boolean upType = false;
		BlockState upState = world.getBlockState(pos.offset(Direction.UP));
		if (upState.isSideSolidFullSquare(world, pos.offset(Direction.UP), Direction.UP.getOpposite())
				|| upState.getBlock() instanceof RopeBlock
				|| upState.getBlock() instanceof SmallBeamBlock) {
			upType = true;
		}

		boolean downType = false;
		BlockState downState = world.getBlockState(pos.offset(Direction.DOWN));
		if (downState.isSideSolidFullSquare(world, pos.offset(Direction.DOWN), Direction.DOWN.getOpposite())
				|| downState.getBlock() instanceof RopeBlock
				|| downState.getBlock() instanceof RopeLanternBlock
				|| downState.getBlock() instanceof SmallBeamBlock
				|| (downState.getBlock() instanceof RopeableLanternBlock
						&& (downState.get(RopeableLanternBlock.HANGING)
								&& downState.get(RopeableLanternBlock.ROPED)))) {
			downType = true;
		}

		BlockState finalState = calculateKnot(currentState
				.with(NORTH, northType)
				.with(EAST, eastType)
				.with(SOUTH, southType)
				.with(WEST, westType)
				.with(UP, upType)
				.with(DOWN, downType));

		if (finalState == currentState) {
			return currentState;
		} else {
			return finalState;
		}
	}

	@Override
	public ActionResult onUse(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockHitResult hit) {
		ItemStack itemstack = player.getStackInHand(handIn);
		Item item = itemstack.getItem();

		if (item == Items.LANTERN && hit.getSide() == Direction.DOWN && worldIn.getBlockState(pos.down()).getMaterial().isReplaceable()) {
			if (!player.abilities.creativeMode) {
				itemstack.decrement(1);
			}
			worldIn.setBlockState(pos.down(), DruidsRopes.ROPE_LANTERN.getDefaultState());
			worldIn.playSound(pos.getX(), pos.getY(), pos.getZ(), SoundEvents.BLOCK_LANTERN_PLACE, SoundCategory.BLOCKS, 1.0F, 0.88F, true);
			return ActionResult.SUCCESS;
		} else {
			return ActionResult.FAIL;
		}
	}

	@Override
	public PistonBehavior getPistonBehavior(BlockState state) {
		return PistonBehavior.DESTROY;
	}

	@Override
	public Fluid tryDrainFluid(WorldAccess worldIn, BlockPos pos, BlockState state) {
		if (state.get(WATERLOGGED)) {
			worldIn.setBlockState(pos, state.with(WATERLOGGED, false), 3);
			return Fluids.WATER;
		} else {
			return Fluids.EMPTY;
		}
	}

	@Override
	@SuppressWarnings("deprecation")
	public FluidState getFluidState(BlockState state) {
		return state.get(WATERLOGGED) ? Fluids.WATER.getStill(false) : super.getFluidState(state);
	}

	@Override
	public boolean canFillWithFluid(BlockView worldIn, BlockPos pos, BlockState state, Fluid fluidIn) {
		return fluidIn == Fluids.WATER;
	}

	@Override
	public boolean tryFillWithFluid(WorldAccess worldIn, BlockPos pos, BlockState state, FluidState fluidStateIn) {
		if (fluidStateIn.getFluid() == Fluids.WATER) {
			if (!worldIn.isClient()) {
				worldIn.setBlockState(pos, state.with(WATERLOGGED, true), 3);
				worldIn.getFluidTickScheduler().schedule(pos, fluidStateIn.getFluid(), fluidStateIn.getFluid().getTickRate(worldIn));
			}
			return true;
		}
		return false;
	}

}