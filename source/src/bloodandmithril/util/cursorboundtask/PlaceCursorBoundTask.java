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
import bloodandmithril.networking.ClientServerInterface;
import bloodandmithril.prop.Prop;
import bloodandmithril.ui.UserInterface;
import bloodandmithril.util.CursorBoundTask;
import bloodandmithril.world.Domain;
import bloodandmithril.world.topography.Topography.NoTileFoundException;

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
		public PlaceCursorBoundTask(Prop toPlace) {
			super(
				args -> {
					Vector2 coords;
					try {
						if (toPlace.grounded) {
							coords = Domain.getActiveWorld().getTopography().getLowestEmptyTileOrPlatformTileWorldCoords(getMouseWorldX(), getMouseWorldY(), true);
						} else {
							coords = new Vector2(getMouseWorldX(), getMouseWorldY());
						}
					} catch (NoTileFoundException e) {
						return;
					}

					boolean canBuild = toPlace.canPlaceAt(getMouseWorldX(), coords.y);

					if (canBuild) {
						if (ClientServerInterface.isServer()) {
							Domain.getWorld(toPlace.getWorldId()).props().addProp(toPlace);
						} else {
							ClientServerInterface.SendRequest.sendPlacePropRequest(getMouseWorldX(), coords.y, toPlace, Domain.getActiveWorld().getWorldId());
						}
						Domain.getActiveWorld().getPositionalIndexMap().get(toPlace.position.x, toPlace.position.y).getAllEntitiesForType(Prop.class).add(toPlace.id);
					}
				},
				true
			);
			this.toPlace = toPlace;
		}


		@Override
		public void renderUIGuide() {
			Vector2 coords;
			try {
				if (toPlace.grounded) {
					coords = Domain.getActiveWorld().getTopography().getLowestEmptyTileOrPlatformTileWorldCoords(getMouseWorldX(), getMouseWorldY(), true);
				} else {
					coords = new Vector2(getMouseWorldX(), getMouseWorldY());
				}
			} catch (NoTileFoundException e) {
				return;
			}

			float x = worldToScreenX(getMouseWorldX());
			float y = worldToScreenY(coords.y);

			boolean canBuild = toPlace.canPlaceAt(getMouseWorldX(), coords.y);

			toPlace.position.x = getMouseWorldX();
			toPlace.position.y = coords.y;

			gl.glEnable(GL_BLEND);
			gl.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
			UserInterface.shapeRenderer.begin(Rectangle);
			UserInterface.shapeRenderer.setColor(canBuild ? Color.GREEN : Color.RED);
			UserInterface.shapeRenderer.rect(x - toPlace.width/2, y, toPlace.width, toPlace.height);
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
				Vector2 coords = Domain.getActiveWorld().getTopography().getLowestEmptyTileOrPlatformTileWorldCoords(getMouseWorldX(), getMouseWorldY(), true);
				return toPlace.canPlaceAt(getMouseWorldX(), coords.y);
			} catch (NoTileFoundException e) {
				return false;
			}
		}
	}