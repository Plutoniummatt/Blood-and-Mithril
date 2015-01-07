package bloodandmithril.prop.plant.tree;

import java.util.Collection;

import com.badlogic.gdx.math.Vector2;
import com.google.common.collect.Lists;

public abstract class TrunkSegment {
	protected final int width, height;

	/**
	 * The vectors specifying the position of the origin and the connected trunk relative to the bottom left corner of the texture
	 */
	protected Vector2 origin, connectedTrunk;
	
	/**
	 * The angle at which the connected trunk is connected at
	 */
	protected Vector2 direction;
	
	/**
	 * The connected segment
	 */
	protected TrunkSegment connected;

	/**
	 * Branch point positions are relative to the origin
	 */
	protected Collection<BranchPoint> branchPoints = Lists.newLinkedList();

	/**
	 * Pivot points are vectors
	 */
	Collection<Vector2> pivotPoints;

	public TrunkSegment(int width, int height) {
		this.width = width;
		this.height = height;
	}


	public void renderBranches(Vector2 position) {
		// Then branches for all trunk segments
		if (connected != null) {
			Vector2 difference = connectedTrunk.cpy().sub(origin).rotate(direction.angle() - 90f);
			connected.renderBranches(position.cpy().add(difference));
		}

		for (BranchPoint branchPoint : branchPoints) {
			Vector2 difference = branchPoint.position.cpy().sub(origin).rotate(direction.angle() - 90f);
			branchPoint.branch.renderBranches(position.cpy().add(difference));
		}
	}
	
	
	protected abstract void renderSegment(Vector2 position);


	public void renderTrunk(Vector2 position) {
		renderSegment(position);

		if (connected != null) {
			Vector2 difference = connectedTrunk.cpy().sub(origin).rotate(direction.angle() - 90f);
			connected.renderTrunk(position.cpy().add(difference));
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
