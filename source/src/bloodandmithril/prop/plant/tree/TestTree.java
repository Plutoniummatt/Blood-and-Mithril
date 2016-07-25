package bloodandmithril.prop.plant.tree;

import static bloodandmithril.control.InputUtilities.getMouseScreenX;
import static bloodandmithril.control.InputUtilities.getMouseScreenY;
import static bloodandmithril.util.Util.getRandom;
import static bloodandmithril.util.Util.randomOneOf;
import static com.google.common.collect.Lists.newArrayList;

import java.util.List;

import com.badlogic.gdx.graphics.Color;

import bloodandmithril.core.Copyright;
import bloodandmithril.core.UpdatedBy;
import bloodandmithril.core.Wiring;
import bloodandmithril.graphics.RenderPropWith;
import bloodandmithril.prop.Prop;
import bloodandmithril.ui.UserInterface;
import bloodandmithril.ui.components.ContextMenu;
import bloodandmithril.ui.components.ContextMenu.MenuItem;
import bloodandmithril.ui.components.window.MessageWindow;
import bloodandmithril.util.Callable;
import bloodandmithril.util.SerializableMappingFunction;
import bloodandmithril.util.Util;
import bloodandmithril.util.datastructure.WrapperForThree;
import bloodandmithril.world.topography.tile.Tile;

/**
 * Test {@link Tree}
 * 
 * @author Matt
 */
@Copyright("Matthew Peck 2016")
@UpdatedBy()
@RenderPropWith(TestTreeRenderer.class)
public class TestTree extends Tree {
	private static final long serialVersionUID = -439711190324228635L;
	
	private static List<Callable<TreeSegment>> segments = newArrayList();
	private static List<Callable<TreeSegment>> stumps = newArrayList();
	
	static {
		segments.add(() -> new TreeSegment(0, 44, 51));
		segments.add(() -> new TreeSegment(1, 44, 51));
		segments.add(() -> new TreeSegment(2, 44, 51));
		
		stumps.add(() -> new TreeSegment(3, 44, 51));
		stumps.add(() -> new TreeSegment(4, 44, 51));
	}
	
	/** The curvature of this {@link Tree} */
	float curvature = 0f;
	final float baseAngle = 0f;
	final float maxThinningFactor;
	
	public TestTree(float x, float y, float maxThinningFactor) {
		super(x, y, 0, 0, new SerializableMappingFunction<Tile, Boolean>() {
			private static final long serialVersionUID = 2159543403323901068L;
			@Override
			public Boolean apply(Tile input) {
				return true;
			}
		});
		this.maxThinningFactor = maxThinningFactor;
		
		this.stump = stumps.get(getRandom().nextInt(stumps.size())).call();
		
		int treeHeight = Util.getRandom().nextInt(10) + 5;
		this.stump.generateTree(
			() -> segments.get(getRandom().nextInt(segments.size())).call(), 
			trunkSegment -> {
				
				// Only add branches to the top half of the tree
				if (trunkSegment.segmentHeight > treeHeight / 2) {
					trunkSegment.getBranches().add(
						WrapperForThree.wrap(
							0.1f, 
							randomOneOf(getRandom().nextFloat() * 20 + 40, -getRandom().nextFloat() * 20 - 40), 
							segments.get(getRandom().nextInt(segments.size())).call()
						)
					);
				}
			}, 
			treeHeight
		);
		
		this.width = 26;
		this.height = (51 - getTrunkOverlap()) * getHeight();
	}


	@Override
	public void synchronizeProp(Prop other) {
	}


	@Override
	public ContextMenu getContextMenu() {
		final ContextMenu menu = new ContextMenu(getMouseScreenX(), getMouseScreenY(), true);

		menu.addMenuItem(
			new MenuItem(
				"Show info",
				() -> {
					Wiring.injector().getInstance(UserInterface.class).addLayeredComponent(
						new MessageWindow(
							"A skeleton of its former self",
							Color.ORANGE,
							500,
							250,
							getTitle(),
							true,
							300,
							150
						)
					);
				},
				Color.WHITE,
				Color.GREEN,
				Color.GRAY,
				null
			)
		);
		
		menu.addMenuItem(
			new MenuItem(
				"Chop",
				() -> {
				},
				Color.WHITE,
				Color.GREEN,
				Color.GRAY,
				null
			)
		);

		return menu;
	}


	@Override
	public boolean canBeUsedAsFireSource() {
		return false;
	}


	@Override
	public Color getContextMenuColor() {
		return Color.WHITE;
	}


	@Override
	public String getContextMenuItemLabel() {
		return "Dead tree";
	}


	@Override
	public void preRender() {
	}


	@Override
	public int getTrunkOverlap() {
		return 10;
	}
}
