package bloodandmithril.prop.plant.tree;

import com.badlogic.gdx.math.Vector2;

public class BranchPoint {
	/**
	 * Branch point position is relative to the origin.
	 */
	public Vector2 position;
	public Branch branch;
	
	public BranchPoint(Vector2 position, Branch branch) {
		this.position = position;
		this.branch = branch;
	}
}