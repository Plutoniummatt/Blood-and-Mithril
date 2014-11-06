package bloodandmithril.ui.components.window;

import static bloodandmithril.core.BloodAndMithrilClient.getMouseScreenX;
import static bloodandmithril.core.BloodAndMithrilClient.getMouseScreenY;
import static bloodandmithril.core.BloodAndMithrilClient.getMouseWorldX;
import static bloodandmithril.core.BloodAndMithrilClient.getMouseWorldY;
import static bloodandmithril.core.BloodAndMithrilClient.worldToScreenX;
import static bloodandmithril.core.BloodAndMithrilClient.worldToScreenY;
import static com.badlogic.gdx.Gdx.gl;
import static com.badlogic.gdx.graphics.GL10.GL_BLEND;
import static com.badlogic.gdx.graphics.GL10.GL_ONE_MINUS_SRC_ALPHA;
import static com.badlogic.gdx.graphics.GL10.GL_SRC_ALPHA;
import static com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType.Rectangle;

import java.util.Comparator;
import java.util.Map;
import java.util.Map.Entry;

import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.BloodAndMithrilClient;
import bloodandmithril.core.Copyright;
import bloodandmithril.networking.ClientServerInterface;
import bloodandmithril.prop.Prop;
import bloodandmithril.prop.construction.Construction;
import bloodandmithril.ui.UserInterface;
import bloodandmithril.ui.components.ContextMenu;
import bloodandmithril.util.CursorBoundTask;
import bloodandmithril.world.Domain;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.google.common.base.Function;
import com.google.common.collect.Maps;

/**
 * Listing window to display a lsit of {@link Construction}s to be built by some {@link Individual}
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
public class BuildWindow extends ScrollableListingWindow<Construction, String> {

	private Individual builder;

	/**
	 * Constructor
	 */
	public BuildWindow(
			int x,
			int y,
			Individual builder,
			Function<Construction, String> displayFunction,
			Comparator<Construction> sortingOrder) {
		super(
			x,
			y,
			300,
			200,
			"Build",
			true,
			300,
			200,
			true,
			true,
			buildMap(builder),
			construction -> construction.getTitle(),
			sortingOrder
		);

		this.builder = builder;
	}


	private static Map<Construction, String> buildMap(Individual builder) {
		Map<Construction, String> listing = Maps.newHashMap();

		for (Construction construction : builder.getConstructables()) {
			float time = construction.constructionRate == 0f ? 0f : 1f/construction.constructionRate;
			listing.put(construction, String.format("%.1f", time) + "s");
		}
		return listing;
	}


	@Override
	public void refresh() {
		buildListing(
			buildMap(builder),
			(a1, a2) -> {
				return a1.getTitle().compareTo(a2.getTitle());
			}
		);
	}


	@Override
	protected ContextMenu buttonContextMenu(Entry<Construction, String> tEntry) {
		return new ContextMenu(
			getMouseScreenX(),
			getMouseScreenY(),
			true,
			new ContextMenu.MenuItem(
				"Show info",
				() -> {
					UserInterface.addLayeredComponentUnique(
						tEntry.getKey().getInfoWindow()
					);
				},
				Color.WHITE,
				Color.GREEN,
				Color.GRAY,
				null
			),
			new ContextMenu.MenuItem(
				"Construct",
				() -> {
					BloodAndMithrilClient.setCursorBoundTask(
						new BuildCursorBoundTask(tEntry.getKey())
					);
					this.setClosing(true);
				},
				Color.WHITE,
				Color.GREEN,
				Color.GRAY,
				null
			)
		);
	}


	@Override
	public Object getUniqueIdentifier() {
		return builder.getId().getId() + "BuildMenu";
	}


	public class BuildCursorBoundTask extends CursorBoundTask {

		private Construction toConstruct;

		/**
		 * Constructor
		 */
		public BuildCursorBoundTask(Construction toConstruct) {
			super(
				args -> {
					Vector2 coords = Domain.getActiveWorld().getTopography().getLowestEmptyTileOrPlatformTileWorldCoords(getMouseWorldX(), getMouseWorldY(), true);
					boolean canBuild = toConstruct.canBuildAt(getMouseWorldX(), coords.y);

					if (canBuild) {
						if (ClientServerInterface.isServer()) {
							Domain.addProp(toConstruct, Domain.getActiveWorld().getWorldId());
						} else {
							ClientServerInterface.SendRequest.sendPlaceConstructionRequest(getMouseWorldX(), coords.y, toConstruct, Domain.getActiveWorld().getWorldId());
						}
						Domain.getActiveWorld().getPositionalIndexMap().get(toConstruct.position.x, toConstruct.position.y).getAllEntitiesForType(Prop.class).add(toConstruct.id);
					}
				},
				true
			);
			this.toConstruct = toConstruct;
		}


		@Override
		public void renderUIGuide() {
			Vector2 coords = Domain.getActiveWorld().getTopography().getLowestEmptyTileOrPlatformTileWorldCoords(getMouseWorldX(), getMouseWorldY(), true);

			float x = worldToScreenX(getMouseWorldX());
			float y = worldToScreenY(coords.y);

			boolean canBuild = toConstruct.canBuildAt(getMouseWorldX(), coords.y);

			toConstruct.position.x = getMouseWorldX();
			toConstruct.position.y = coords.y;

			gl.glEnable(GL_BLEND);
			gl.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
			shapeRenderer.begin(Rectangle);
			shapeRenderer.setColor(canBuild ? Color.GREEN : Color.RED);
			shapeRenderer.rect(x - toConstruct.width/2, y, toConstruct.width, toConstruct.height);
			shapeRenderer.end();
			gl.glDisable(GL_BLEND);
		}


		@Override
		public String getShortDescription() {
			return "Construct " + toConstruct.getTitle();
		}


		@Override
		public boolean executionConditionMet() {
			Vector2 coords = Domain.getActiveWorld().getTopography().getLowestEmptyTileOrPlatformTileWorldCoords(getMouseWorldX(), getMouseWorldY(), true);
			return toConstruct.canBuildAt(getMouseWorldX(), coords.y);
		}
	}
}