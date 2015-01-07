package bloodandmithril.prop.plant.tree;

import bloodandmithril.core.Copyright;
import bloodandmithril.prop.Prop;
import bloodandmithril.prop.plant.PlantProp;
import bloodandmithril.prop.plant.tree.trunksegments.TestTrunkSegment;
import bloodandmithril.ui.components.ContextMenu;
import bloodandmithril.world.Domain.Depth;

/**
 * A tree
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2015")
public class Tree extends PlantProp {
	private static final long serialVersionUID = 626410919431996100L;

	private TrunkSegment stump;

	public Tree(float x, float y, int width, int height) {
		super(x, y, width, height, Depth.BACKGROUND, null);
		stump = new TestTrunkSegment(true);
	}


	@Override
	public void render() {
		stump.renderBranches(position);
		stump.renderTrunk(position);
//		stump.renderLeaves();
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
		return "Tree";
	}


	@Override
	public void preRender() {
	}
}