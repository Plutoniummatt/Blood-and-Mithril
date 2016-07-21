package bloodandmithril.util.cursorboundtask;

import static bloodandmithril.control.InputUtilities.getMouseWorldX;
import static bloodandmithril.control.InputUtilities.getMouseWorldY;
import static bloodandmithril.control.InputUtilities.isKeyPressed;
import static bloodandmithril.control.InputUtilities.worldToScreenX;
import static bloodandmithril.control.InputUtilities.worldToScreenY;
import static com.badlogic.gdx.Gdx.gl;
import static com.badlogic.gdx.graphics.GL20.GL_BLEND;
import static com.badlogic.gdx.graphics.GL20.GL_ONE_MINUS_SRC_ALPHA;
import static com.badlogic.gdx.graphics.GL20.GL_SRC_ALPHA;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Vector2;
import com.google.inject.Inject;

import bloodandmithril.character.ai.task.PlaceProp;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.control.Controls;
import bloodandmithril.core.GameClientStateTracker;
import bloodandmithril.core.Wiring;
import bloodandmithril.graphics.Graphics;
import bloodandmithril.item.items.PropItem;
import bloodandmithril.networking.ClientServerInterface;
import bloodandmithril.prop.Prop;
import bloodandmithril.ui.UserInterface;
import bloodandmithril.util.CursorBoundTask;
import bloodandmithril.world.Domain;
import bloodandmithril.world.World;
import bloodandmithril.world.topography.Topography;
import bloodandmithril.world.topography.Topography.NoTileFoundException;

/**
 * @author Matt
 */
public class PlaceCursorBoundTask extends CursorBoundTask {

		private Prop toPlace;

		@Inject private GameClientStateTracker gameClientStateTracker;

		/**
		 * Constructor
		 */
		public PlaceCursorBoundTask(final Prop toPlace, final Individual indi, final PropItem propItem) {
			super(
				args -> {
					Vector2 coords;
					try {
						if (toPlace.grounded) {
							coords = new Vector2(
								getMouseWorldX(),
								Wiring.injector().getInstance(GameClientStateTracker.class).getActiveWorld().getTopography().getLowestEmptyTileOrPlatformTileWorldCoords(getMouseWorldX(), getMouseWorldY(), true).y
							);
						} else {
							coords = new Vector2(getMouseWorldX(), getMouseWorldY());
						}

						if (isKeyPressed(Wiring.injector().getInstance(Controls.class).snapToGrid.keyCode)) {
							coords.x = Topography.convertToWorldTileCoord(coords.x) * Topography.TILE_SIZE;
							coords.y = Topography.convertToWorldTileCoord(coords.y) * Topography.TILE_SIZE;
						}
					} catch (final NoTileFoundException e) {
						return;
					}

					final boolean canBuild = toPlace.canPlaceAt(coords);

					if (canBuild) {
						if (ClientServerInterface.isServer()) {
							if (indi == null) {
								final World world = Domain.getWorld(toPlace.getWorldId());
								world.props().addProp(toPlace);
							} else {
								indi.getAI().setCurrentTask(new PlaceProp(indi, coords, propItem));
							}
						} else {
							ClientServerInterface.SendRequest.sendPlacePropRequest(indi, propItem, coords.x, coords.y, toPlace, Wiring.injector().getInstance(GameClientStateTracker.class).getActiveWorld().getWorldId());
						}
					}
				},
				true
			);
			this.toPlace = toPlace;
		}


		@Override
		public void renderUIGuide(final Graphics graphics) {
			renderGuide(toPlace);
		}


		public static void renderGuide(final Prop propToPlace) {
			Vector2 coords;
			Gdx.gl20.glLineWidth(2f);
			try {
				if (propToPlace.grounded) {
					coords = new Vector2(
						getMouseWorldX(),
						Wiring.injector().getInstance(GameClientStateTracker.class).getActiveWorld().getTopography().getLowestEmptyTileOrPlatformTileWorldCoords(getMouseWorldX(), getMouseWorldY(), true).y
					);
				} else {
					coords = new Vector2(getMouseWorldX(), getMouseWorldY());
				}
				if (isKeyPressed(Wiring.injector().getInstance(Controls.class).snapToGrid.keyCode)) {
					coords.x = Topography.convertToWorldTileCoord(coords.x) * Topography.TILE_SIZE;
					coords.y = Topography.convertToWorldTileCoord(coords.y) * Topography.TILE_SIZE;
				}
			} catch (final NoTileFoundException e) {
				return;
			}

			final float x = worldToScreenX(coords.x);
			final float y = worldToScreenY(coords.y);

			final boolean canBuild = propToPlace.canPlaceAt(coords);

			propToPlace.position.x = coords.x;
			propToPlace.position.y = coords.y;

			final UserInterface userInterface = Wiring.injector().getInstance(UserInterface.class);

			gl.glEnable(GL_BLEND);
			gl.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
			userInterface.getShapeRenderer().begin(ShapeType.Line);
			userInterface.getShapeRenderer().setColor(canBuild ? Color.GREEN : Color.RED);
			userInterface.getShapeRenderer().rect(x - propToPlace.width/2, y, propToPlace.width, propToPlace.height);
			userInterface.getShapeRenderer().end();
			gl.glDisable(GL_BLEND);
		}


		@Override
		public String getShortDescription() {
			return "Place " + toPlace.getTitle();
		}


		@Override
		public boolean executionConditionMet() {
			try {
				Vector2 coords;
				if (toPlace.grounded) {
					coords = new Vector2(
						getMouseWorldX(),
						gameClientStateTracker.getActiveWorld().getTopography().getLowestEmptyTileOrPlatformTileWorldCoords(getMouseWorldX(), getMouseWorldY(), true).y
					);
				} else {
					coords = new Vector2(getMouseWorldX(), getMouseWorldY());
				}
				if (isKeyPressed(Wiring.injector().getInstance(Controls.class).snapToGrid.keyCode)) {
					coords.x = Topography.convertToWorldTileCoord(coords.x) * Topography.TILE_SIZE;
					coords.y = Topography.convertToWorldTileCoord(coords.y) * Topography.TILE_SIZE;
				}

				if (isKeyPressed(Wiring.injector().getInstance(Controls.class).snapToGrid.keyCode)) {
					coords.x = Topography.convertToWorldTileCoord(coords.x) * Topography.TILE_SIZE;
					coords.y = Topography.convertToWorldTileCoord(coords.y) * Topography.TILE_SIZE;
				}
				return toPlace.canPlaceAt(coords);
			} catch (final NoTileFoundException e) {
				return false;
			}
		}


		@Override
		public boolean canCancel() {
			return true;
		}


		@Override
		public CursorBoundTask getImmediateTask() {
			return null;
		}


		@Override
		public void keyPressed(final int keyCode) {
		}
	}