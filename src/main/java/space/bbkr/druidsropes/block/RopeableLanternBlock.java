package space.bbkr.druidsropes.block;

import javax.annotation.Nullable;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.Waterloggable;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;

public class RopeableLanternBlock extends Block implements Waterloggable {

	public static final BooleanProperty HANGING = Properties.HANGING;
	public static final BooleanProperty ROPED = BooleanProperty.of("roped");
	public static final BooleanProperty WATERLOGGED = Properties.WATERLOGGED;

	public RopeableLanternBlock(AbstractBlock.Settings settings) {
		super(settings);
		this.setDefaultState(this.getStateManager().getDefaultState().with(HANGING, false).with(ROPED, false).with(WATERLOGGED, false));
	}

	@Override
	@Nullable
	public BlockState getPlacementState(ItemPlacementContext context) {
		FluidState fluidstate = context.getWorld().getFluidState(context.getBlockPos());
		for(Direction direction : context.getPlacementDirections()) {
			if (direction.getAxis() == Direction.Axis.Y) {
				boolean flag = direction == Direction.UP;
				BlockState blockstate = this.getDefaultState().with(HANGING, flag).with(ROPED, flag && context.getWorld().getBlockState(context.getBlockPos().offset(Direction.UP)).getBlock() instanceof RopeBlock);
				if (blockstate.canPlaceAt(context.getWorld(), context.getBlockPos())) {
					return blockstate.with(WATERLOGGED, fluidstate.getFluid() == Fluids.WATER);
				}
			}
		}
		return null;
	}

	@Override
	public VoxelShape getOutlineShape(BlockState state, BlockView worldIn, BlockPos pos, ShapeContext context) {
		VoxelShape lantern_grounded = VoxelShapes.union(VoxelShapes.union(Block.createCuboidShape(4.0f, 0.0f, 4.0f, 12.0f, 2.0f, 12.0f), Block.createCuboidShape(3.0f, 2.0f, 3.0f, 13.0f, 8.0f, 13.0f)), Block.createCuboidShape(6.0f, 7.0f, 6.0f, 12.0f, 9.0f, 12.0f));
		VoxelShape lantern_hanging = VoxelShapes.union(VoxelShapes.union(Block.createCuboidShape(4.0f, 1.0f, 4.0f, 12.0f, 3.0f, 12.0f), Block.createCuboidShape(3.0f, 3.0f, 3.0f, 13.0f, 9.0f, 13.0f)), Block.createCuboidShape(5.0f, 9.0f, 5.0f, 11.0f, 11.0f, 11.0f));

		if (state.get(HANGING)) {
			if (state.get(ROPED)) {
				return VoxelShapes.union(lantern_hanging, VoxelShapes.union(Block.createCuboidShape(5.0f, 11.0f, 5.0f, 11.0f, 14.0f, 11.0f), Block.createCuboidShape(6.0f, 14.0f, 6.0f, 10.0f, 16.0f, 10.0f)));
			}
			return VoxelShapes.union(lantern_hanging, Block.createCuboidShape(5.0f, 11.0f, 5.0f, 11.0f, 16.0f, 11.0f));
		}
		return VoxelShapes.union(lantern_grounded, Block.createCuboidShape(5.0f, 10.0f, 5.0f, 11.0f, 13.0f, 11.0f));
	}

	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
		builder.add(HANGING, ROPED, WATERLOGGED);
	}


	@Override
	public boolean canPlaceAt(BlockState state, WorldView worldIn, BlockPos pos) {
		Direction direction = getHangingDirection(state).getOpposite();
		return Block.sideCoversSmallSquare(worldIn, pos.offset(direction), direction.getOpposite()) || (state.get(ROPED) && worldIn.getBlockState(pos.offset(Direction.UP)).getBlock() instanceof RopeBlock);
	}

	protected static Direction getHangingDirection(BlockState state) {
		return state.get(HANGING) ? Direction.DOWN : Direction.UP;
	}

	@Override
	public PistonBehavior getPistonBehavior(BlockState state) {
		return PistonBehavior.DESTROY;
	}

	@Override
	public BlockState getStateForNeighborUpdate(BlockState stateIn, Direction facing, BlockState facingState, WorldAccess worldIn, BlockPos currentPos, BlockPos facingPos) {
		if (stateIn.get(WATERLOGGED)) {
			worldIn.getFluidTickScheduler().schedule(currentPos, Fluids.WATER, Fluids.WATER.getTickRate(worldIn));
		}
		return getHangingDirection(stateIn).getOpposite() == facing && !stateIn.canPlaceAt(worldIn, currentPos) ? Blocks.AIR.getDefaultState() : super.getStateForNeighborUpdate(stateIn, facing, facingState, worldIn, currentPos, facingPos);
	}

	@Override
	@SuppressWarnings("deprecation")
	public FluidState getFluidState(BlockState state) {
		return state.get(WATERLOGGED) ? Fluids.WATER.getStill(false) : super.getFluidState(state);
	}

	@Override
	public boolean tryFillWithFluid(WorldAccess worldIn, BlockPos pos, BlockState state, FluidState fluidState) {
		if (!state.get(WATERLOGGED) && fluidState.getFluid() == Fluids.WATER) {
			if (!worldIn.isClient()) {
				worldIn.setBlockState(pos, state.with(WATERLOGGED, Boolean.valueOf(true)), 3);
				worldIn.getFluidTickScheduler().schedule(pos, Fluids.WATER, Fluids.WATER.getTickRate(worldIn));
			}
			return true;
		} else {
			return false;
		}
	}

	@Override
	public boolean canPathfindThrough(BlockState state, BlockView worldIn, BlockPos pos, NavigationType type) {
		return false;
	}
}
