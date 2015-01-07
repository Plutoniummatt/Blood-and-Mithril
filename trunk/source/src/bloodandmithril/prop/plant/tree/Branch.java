package bloodandmithril.prop.plant.tree;

import java.util.Collection;

import com.badlogic.gdx.math.Vector2;
import com.google.common.collect.Lists;

public abstract class Branch {
	protected final int width, height;

	/**
	 * The vectors specifying the position of the origin relative to the bottom left corner of the texture
	 */
	protected Vector2 origin;
	
	/**
	 * The angle at which the connected branch is connected at
	 */
	protected Vector2 direction;

	/**
	 * Branch point positions are relative to the origin
	 */
	protected Collection<BranchPoint> branchPoints = Lists.newLinkedList();

	/**
	 */
	private Collection<Leaves> leaves;

	public Branch(int width, int height) {
		this.width = width;
		this.height = height;
	}

	public void renderBranches(Vector2 position) {
		for (BranchPoint branchPoint : branchPoints) {
			Vector2 difference = branchPoint.position.cpy().sub(origin).rotate(direction.angle() - 90f);
			branchPoint.branch.renderBranches(position.cpy().add(difference));
		}

		renderBranch(position);
	}
	
	protected abstract void renderBranch(Vector2 position);

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