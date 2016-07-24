package bloodandmithril.prop.plant.tree;

import static bloodandmithril.control.InputUtilities.getMouseScreenX;
import static bloodandmithril.control.InputUtilities.getMouseScreenY;
import static bloodandmithril.graphics.Textures.GAME_WORLD_TEXTURE;
import static bloodandmithril.util.Util.getRandom;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static java.lang.Math.abs;

import java.util.HashMap;
import java.util.List;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;

import bloodandmithril.core.Copyright;
import bloodandmithril.core.Timers;
import bloodandmithril.core.UpdatedBy;
import bloodandmithril.core.Wiring;
import bloodandmithril.graphics.Graphics;
import bloodandmithril.graphics.Textures;
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
public class TestTree extends Tree {
	private static final long serialVersionUID = -439711190324228635L;
	
	private static List<Callable<TreeSegment>> segments = newArrayList();
	private static int TRUNK_OVERLAP = 10;
	
	static {
		segments.add(() -> new TreeSegment(0, 26, 51));
		segments.add(() -> new TreeSegment(1, 26, 51));
		segments.add(() -> new TreeSegment(2, 26, 51));
	}
	
	/** The curvature of this {@link Tree} */
	private float curvature = 0f;
	private float baseAngle = 0f;
	private float maxThinningFactor;
	
	public TestTree(float x, float y, float maxThinningFactor) {
		super(x, y, 0, 0, new SerializableMappingFunction<Tile, Boolean>() {
			private static final long serialVersionUID = 2159543403323901068L;
			@Override
			public Boolean apply(Tile input) {
				return true;
			}
		});
		this.maxThinningFactor = maxThinningFactor;
		
		this.stump = segments.get(getRandom().nextInt(segments.size())).call();
		this.stump.generateTree(
			() -> segments.get(getRandom().nextInt(segments.size())).call(), 
			trunkSegment -> {
				trunkSegment.getBranches().add(
					WrapperForThree.wrap(
						0.5f, 
						(Util.getRandom().nextFloat() - 0.5f) * 100f, 
						segments.get(getRandom().nextInt(segments.size())).call()
					)
				);
			}, 
			Util.getRandom().nextInt(10) + 5
		);
		
		this.width = 26;
		this.height = (51 - TRUNK_OVERLAP) * getHeight();
	}


	@Override
	public void setupTextures() {
		HashMap<Integer, TextureRegion> testTreeTextures = newHashMap();
		testTreeTextures.put(0, new TextureRegion(GAME_WORLD_TEXTURE, 1, 593, 26, 51));
		testTreeTextures.put(1, new TextureRegion(GAME_WORLD_TEXTURE, 28, 593, 26, 51));
		testTreeTextures.put(2, new TextureRegion(GAME_WORLD_TEXTURE, 55, 593, 26, 51));
		
		Textures.trunkTextures.put(TestTree.class, testTreeTextures);
	}


	@Override
	public void render(Graphics graphics) {
		Timers timers = Wiring.injector().getInstance(Timers.class);
		
		float windStrength = 0.5f;
		
		double windStrengthTerm = 1f + 3 * abs(windStrength);
		float swayMagnitude = 0.1f + windStrength * 1.2f;
		
		curvature = (float) Math.sin(
			position.x / (abs(windStrength * 800f) + 1f) + // How in sync the tree sway is 
			timers.renderUtilityTime * windStrengthTerm // Rate of sway
		) * swayMagnitude - windStrength * 4f;
		
		setupTextures();
		float angle = baseAngle;
		
		Vector2 renderPosition = this.position.cpy();				
		renderPosition.x = position.x - stump.width/2;
		renderPosition.y = position.y;
		
		stump.render(graphics, renderPosition, angle, 1f, maxThinningFactor/getHeight(), curvature, TRUNK_OVERLAP, TestTree.class);
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
							"What you want to know? its a fuckin tree",
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
		return Color.MAROON;
	}


	@Override
	public String getContextMenuItemLabel() {
		return "Fuckin tree mate, innit";
	}


	@Override
	public void preRender() {
	}
}
