package space.bbkr.druidsropes.block;

import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.WorldAccess;

import space.bbkr.druidsropes.DruidsRopes;

public class SmallBeamBlock extends Block implements FluidFillable, FluidDrainable {

	public static final BooleanProperty X_AXIS = BooleanProperty.of("x_axis");
	public static final BooleanProperty Y_AXIS = BooleanProperty.of("y_axis");
	public static final BooleanProperty Z_AXIS = BooleanProperty.of("z_axis");
	public static final IntProperty CONNECTIONS = IntProperty.of("connections", 0, 3);
	public static final BooleanProperty WATERLOGGED = Properties.WATERLOGGED;
	public static final EnumProperty<Direction.Axis> DEFAULT_AXIS = Properties.AXIS;

	public SmallBeamBlock(AbstractBlock.Settings properties) {
		super(properties);
		this.setDefaultState(this.getDefaultState()
				.with(X_AXIS, false)
				.with(Y_AXIS, false)
				.with(Z_AXIS, false)
				.with(CONNECTIONS, 0)
				.with(WATERLOGGED, false)
				.with(DEFAULT_AXIS, Direction.Axis.Y));
	}

	@Override
	public VoxelShape getOutlineShape(BlockState state, BlockView worldIn, BlockPos pos, ShapeContext context) {
		VoxelShape voxelshape = VoxelShapes.empty();

		if (state.get(X_AXIS)) {
			voxelshape = VoxelShapes.union(voxelshape, Block.createCuboidShape(0.0D, 5.0D, 5.0D, 16.0D, 11.0D, 11.0D));
		}
		if (state.get(Y_AXIS)) {
			voxelshape = VoxelShapes.union(voxelshape, Block.createCuboidShape(5.0D, 0.0D, 5.0D, 11.0D, 16.0D, 11.0D));
		}
		if (state.get(Z_AXIS)) {
			voxelshape = VoxelShapes.union(voxelshape, Block.createCuboidShape(5.0D, 5.0D, 0.0D, 11.0D, 11.0D, 16.0D));
		}
		if (!state.get(X_AXIS) && !state.get(Y_AXIS) && !state.get(Z_AXIS)) {
			voxelshape = VoxelShapes.fullCube();
		}

		return voxelshape;
	}

	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
		builder.add(X_AXIS, Y_AXIS, Z_AXIS, CONNECTIONS, WATERLOGGED, DEFAULT_AXIS);
	}

	@Override
	public BlockRenderType getRenderType(BlockState state) {
		return BlockRenderType.MODEL;
	}

	@Override
	public BlockState getPlacementState(ItemPlacementContext context) {
		FluidState fluidstate = context.getWorld().getFluidState(context.getBlockPos());
		return calculateState(getDefaultState(), context.getWorld(), context.getBlockPos(), context.getSide().getAxis()).with(WATERLOGGED, fluidstate.getFluid() == Fluids.WATER);
	}

	@Override
	public boolean isTranslucent(BlockState state, BlockView reader, BlockPos pos) {
		return !state.get(WATERLOGGED);
	}

	private BlockState calculateState (BlockState currentState, WorldAccess world, BlockPos pos, Direction.Axis defaultAxis) {

		BlockState northState = world.getBlockState(pos.offset(Direction.NORTH));
		BlockState eastState = world.getBlockState(pos.offset(Direction.EAST));
		BlockState southState = world.getBlockState(pos.offset(Direction.SOUTH));
		BlockState westState = world.getBlockState(pos.offset(Direction.WEST));
		BlockState upState = world.getBlockState(pos.offset(Direction.UP));
		BlockState downState = world.getBlockState(pos.offset(Direction.DOWN));

		boolean xBool = defaultAxis == Direction.Axis.X
				|| (eastState.getBlock() == this && eastState.get(X_AXIS))
				|| (westState.getBlock() == this && westState.get(X_AXIS))
				|| (eastState.getBlock() == DruidsRopes.ROPE && eastState.get(RopeBlock.WEST))
				|| westState.getBlock() == DruidsRopes.ROPE && westState.get(RopeBlock.EAST);

		boolean yBool = defaultAxis == Direction.Axis.Y
				|| (upState.getBlock() == this && upState.get(Y_AXIS))
				|| (downState.getBlock() == this && downState.get(Y_AXIS))
				|| (upState.getBlock() == DruidsRopes.ROPE && upState.get(RopeBlock.DOWN))
				|| downState.getBlock() == DruidsRopes.ROPE && downState.get(RopeBlock.UP);

		boolean zBool = defaultAxis == Direction.Axis.Z
				|| (northState.getBlock() == this && northState.get(Z_AXIS))
				|| (southState.getBlock() == this && southState.get(Z_AXIS))
				|| (northState.getBlock() == DruidsRopes.ROPE && northState.get(RopeBlock.SOUTH))
				|| southState.getBlock() == DruidsRopes.ROPE && southState.get(RopeBlock.NORTH);

		int count = 0;
		if (xBool) {
			count++;
		}
		if (yBool) {
			count++;
		}
		if (zBool) {
			count++;
		}

		return currentState.with(X_AXIS, xBool).with(Y_AXIS, yBool).with(Z_AXIS, zBool).with(CONNECTIONS, count).with(DEFAULT_AXIS, defaultAxis);
	}

	@Override
	public boolean tryFillWithFluid(WorldAccess worldIn, BlockPos pos, BlockState state, FluidState fluidState) {
		if (!state.get(WATERLOGGED) && fluidState.getFluid() == Fluids.WATER) {
			if (!worldIn.isClient()) {
				worldIn.setBlockState(pos, state.with(WATERLOGGED, true), 3);
				worldIn.getFluidTickScheduler().schedule(pos, Fluids.WATER, Fluids.WATER.getTickRate(worldIn));
			}
			return true;
		} else {
			return false;
		}
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
	public FluidState getFluidState(BlockState state) {
		return state.get(WATERLOGGED) ? Fluids.WATER.getStill(false) : super.getFluidState(state);
	}

	@Override
	public boolean canFillWithFluid(BlockView worldIn, BlockPos pos, BlockState state, Fluid fluid) {
		return !state.get(WATERLOGGED) && fluid == Fluids.WATER;
	}

	@Override
	public BlockState getStateForNeighborUpdate(BlockState state, Direction facing, BlockState facingState, WorldAccess world, BlockPos currentPos, BlockPos facingPos) {

		if (state.get(WATERLOGGED)) {
			world.getFluidTickScheduler().schedule(currentPos, Fluids.WATER, Fluids.WATER.getTickRate(world));
		}

		return calculateState(state, world, currentPos, state.get(DEFAULT_AXIS));
	}

	@Override
	public boolean canPathfindThrough(BlockState state, BlockView worldIn, BlockPos pos, NavigationType type) {
		return false;
	}
}