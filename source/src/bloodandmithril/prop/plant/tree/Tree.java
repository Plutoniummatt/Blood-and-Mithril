package bloodandmithril.prop.plant.tree;

import java.util.Collection;

import bloodandmithril.prop.Prop;
import bloodandmithril.prop.plant.PlantProp;
import bloodandmithril.ui.components.ContextMenu;
import bloodandmithril.util.SerializableMappingFunction;
import bloodandmithril.world.topography.tile.Tile;

import com.badlogic.gdx.math.Vector2;

public class Tree extends PlantProp {
	private static final long serialVersionUID = 626410919431996100L;

	private TrunkSegment stump;

	protected Tree(float x, float y, int width, int height, SerializableMappingFunction<Tile, Boolean> canPlaceOnTopOf) {
		super(x, y, width, height, canPlaceOnTopOf);
	}


	@Override
	public void render() {
		stump.renderBranches();
		stump.renderTrunk();
		stump.renderLeaves();
	}


	@Override
	public void synchronizeProp(Prop other) {
	}


	@Override
	public ContextMenu getContextMenu() {
		return null;
	}


	@Override
	public void update(float delta) {
	}


	@Override
	public boolean canBeUsedAsFireSource() {
		return false;
	}


	@Override
	public String getContextMenuItemLabel() {
		return null;
	}


	@Override
	public void preRender() {
	}


	public static abstract class TrunkSegment {
		private final int width, height;

		/**
		 * The vectors specifying the position of the origin and the connected trunk relative to the bottom left corner of the texture
		 */
		private Vector2 origin, connectedTrunk;
		private TrunkSegment connected;

		/**
		 * Branch point positions are relative to the origin
		 */
		private Collection<BranchPoint> branchPoints;

		/**
		 * Pivot points are vectors
		 */
		Collection<Vector2> pivotPoints;

		public TrunkSegment(int width, int height) {
			this.width = width;
			this.height = height;
		}


		public void renderBranches() {
			// Then branches for all trunk segments
			if (connected != null) {
				connected.renderBranches();
			}

			for (BranchPoint branchPoint : branchPoints) {
				branchPoint.branch.renderBranch();
			}
		}


		public void renderTrunk() {
			// TODO render this trunk

			if (connected != null) {
				connected.renderTrunk();
			}
		}


		public void renderLeaves() {
			if (connected != null) {
				connected.renderLeaves();
			}

			for (BranchPoint branchPoint : branchPoints) {
				branchPoint.branch.renderLeaves();
			}
		}
	}


	public static class BranchPoint {
		public Vector2 position, direction;
		public Branch branch;
	}


	public static class Branch {
		private final int width, height;

		/**
		 * The vectors specifying the position of the origin relative to the bottom left corner of the texture
		 */
		private Vector2 origin;

		/**
		 * Branch point positions are relative to the origin
		 */
		private Collection<BranchPoint> branchPoints;

		/**
		 */
		private Collection<Leaves> leaves;

		public Branch(int width, int height) {
			this.width = width;
			this.height = height;
		}

		public void renderBranch() {
			for (BranchPoint branchPoint : branchPoints) {
				branchPoint.branch.renderBranch();
			}

			// TODO Render this branch
		}


		public void renderLeaves() {
			for (BranchPoint branchPoint : branchPoints) {
				branchPoint.branch.renderLeaves();
			}

			// Render leaves on this
			for (Leaves leaf : leaves) {
				leaf.render();
			}
		}
	}


	public static class Leaves {
		public Vector2 position, direction;

		public void render() {
		}
	}
}