package bloodandmithril.util.cursorboundtask;

import static bloodandmithril.core.BloodAndMithrilClient.getMouseWorldX;
import static bloodandmithril.core.BloodAndMithrilClient.getMouseWorldY;
import static bloodandmithril.core.BloodAndMithrilClient.worldToScreenX;
import static bloodandmithril.core.BloodAndMithrilClient.worldToScreenY;
import static com.badlogic.gdx.Gdx.gl;
import static com.badlogic.gdx.graphics.GL10.GL_BLEND;
import static com.badlogic.gdx.graphics.GL10.GL_ONE_MINUS_SRC_ALPHA;
import static com.badlogic.gdx.graphics.GL10.GL_SRC_ALPHA;
import static com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType.Rectangle;
import bloodandmithril.character.ai.task.PlaceProp;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.item.items.PropItem;
import bloodandmithril.networking.ClientServerInterface;
import bloodandmithril.prop.Prop;
import bloodandmithril.ui.KeyMappings;
import bloodandmithril.ui.UserInterface;
import bloodandmithril.util.CursorBoundTask;
import bloodandmithril.world.Domain;
import bloodandmithril.world.World;
import bloodandmithril.world.topography.Topography;
import bloodandmithril.world.topography.Topography.NoTileFoundException;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;

/**
 * @author Matt
 */
public class PlaceCursorBoundTask extends CursorBoundTask {

		private Prop toPlace;

		/**
		 * Constructor
		 */
		public PlaceCursorBoundTask(Prop toPlace, Individual indi, PropItem propItem) {
			super(
				args -> {
					Vector2 coords;
					try {
						if (toPlace.grounded) {
							coords = new Vector2(
								getMouseWorldX(),
								Domain.getActiveWorld().getTopography().getLowestEmptyTileOrPlatformTileWorldCoords(getMouseWorldX(), getMouseWorldY(), true).y
							);
						} else {
							coords = new Vector2(getMouseWorldX(), getMouseWorldY());
						}

						if (Gdx.input.isKeyPressed(KeyMappings.snapToGrid)) {
							coords.x = Topography.convertToWorldTileCoord(coords.x) * Topography.TILE_SIZE;
							coords.y = Topography.convertToWorldTileCoord(coords.y) * Topography.TILE_SIZE;
						}
					} catch (NoTileFoundException e) {
						return;
					}

					boolean canBuild = toPlace.canPlaceAt(coords);

					if (canBuild) {
						if (ClientServerInterface.isServer()) {
							if (indi == null) {
								World world = Domain.getWorld(toPlace.getWorldId());
								world.props().addProp(toPlace);
							} else {
								indi.getAI().setCurrentTask(new PlaceProp(indi, coords, propItem));
							}
						} else {
							ClientServerInterface.SendRequest.sendPlacePropRequest(indi, propItem, coords.x, coords.y, toPlace, Domain.getActiveWorld().getWorldId());
						}
					}
				},
				true
			);
			this.toPlace = toPlace;
		}


		@Override
		public void renderUIGuide() {
			renderGuide(toPlace);
		}


		public static void renderGuide(Prop propToPlace) {
			Vector2 coords;
			Gdx.gl20.glLineWidth(2f);
			try {
				if (propToPlace.grounded) {
					coords = new Vector2(
						getMouseWorldX(),
						Domain.getActiveWorld().getTopography().getLowestEmptyTileOrPlatformTileWorldCoords(getMouseWorldX(), getMouseWorldY(), true).y
					);
				} else {
					coords = new Vector2(getMouseWorldX(), getMouseWorldY());
				}
				if (Gdx.input.isKeyPressed(KeyMappings.snapToGrid)) {
					coords.x = Topography.convertToWorldTileCoord(coords.x) * Topography.TILE_SIZE;
					coords.y = Topography.convertToWorldTileCoord(coords.y) * Topography.TILE_SIZE;
				}
			} catch (NoTileFoundException e) {
				return;
			}

			float x = worldToScreenX(coords.x);
			float y = worldToScreenY(coords.y);

			boolean canBuild = propToPlace.canPlaceAt(coords);

			propToPlace.position.x = coords.x;
			propToPlace.position.y = coords.y;

			gl.glEnable(GL_BLEND);
			gl.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
			UserInterface.shapeRenderer.begin(Rectangle);
			UserInterface.shapeRenderer.setColor(canBuild ? Color.GREEN : Color.RED);
			UserInterface.shapeRenderer.rect(x - propToPlace.width/2, y, propToPlace.width, propToPlace.height);
			UserInterface.shapeRenderer.end();
			gl.glDisable(GL_BLEND);
		}


		@Override
		public String getShortDescription() {
			return "Place " + toPlace.getTitle();
		}


		@Override
		public boolean executionConditionMet() {
			try {
				Vector2 coords = new Vector2(
					getMouseWorldX(),
					Domain.getActiveWorld().getTopography().getLowestEmptyTileOrPlatformTileWorldCoords(getMouseWorldX(), getMouseWorldY(), true).y
				);

				if (Gdx.input.isKeyPressed(KeyMappings.snapToGrid)) {
					coords.x = Topography.convertToWorldTileCoord(coords.x) * Topography.TILE_SIZE;
					coords.y = Topography.convertToWorldTileCoord(coords.y) * Topography.TILE_SIZE;
				}
				return toPlace.canPlaceAt(coords);
			} catch (NoTileFoundException e) {
				return false;
			}
		}
	}