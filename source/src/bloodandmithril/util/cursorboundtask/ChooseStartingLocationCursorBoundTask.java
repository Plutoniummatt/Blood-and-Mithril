package bloodandmithril.util.cursorboundtask;

import static bloodandmithril.control.InputUtilities.getMouseWorldX;
import static bloodandmithril.control.InputUtilities.getMouseWorldY;
import static com.badlogic.gdx.Gdx.gl;
import static com.badlogic.gdx.graphics.GL20.GL_BLEND;
import static com.badlogic.gdx.graphics.GL20.GL_ONE_MINUS_SRC_ALPHA;
import static com.badlogic.gdx.graphics.GL20.GL_SRC_ALPHA;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.google.common.collect.Maps;

import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.Copyright;
import bloodandmithril.core.GameClientStateTracker;
import bloodandmithril.core.ItemPackage;
import bloodandmithril.core.Wiring;
import bloodandmithril.graphics.Graphics;
import bloodandmithril.item.items.container.Container;
import bloodandmithril.prop.Prop;
import bloodandmithril.ui.UserInterface;
import bloodandmithril.util.CursorBoundTask;
import bloodandmithril.util.SerializableMappingFunction;
import bloodandmithril.util.Shaders;
import bloodandmithril.util.Util;
import bloodandmithril.world.Domain;
import bloodandmithril.world.topography.Topography.NoTileFoundException;
import bloodandmithril.world.topography.tile.Tile;
import bloodandmithril.world.topography.tile.Tile.EmptyTile;

/**
 * {@link CursorBoundTask} to choose the starting location for a new game
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2015")
public class ChooseStartingLocationCursorBoundTask extends CursorBoundTask {

	private final Map<Integer, Individual> individuals = Maps.newHashMap();
	private final ItemPackage startingItemPackage;

	/**
	 * Constructor
	 */
	public ChooseStartingLocationCursorBoundTask(final Set<Individual> startingIndividuals, final ItemPackage startingItemPackage, final int startingFactionId, final int worldId) {
		super(
			null,
			true
		);

		((Prop) startingItemPackage.getContainer()).setWorldId(worldId);
		setTask(args -> {
			Domain.getWorld(worldId).props().addProp((Prop) startingItemPackage.getContainer());
			for (final Individual individual : individuals.values()) {
				individual.setFactionId(startingFactionId);
				Domain.addIndividual(individual, Wiring.injector().getInstance(GameClientStateTracker.class).getActiveWorld().getWorldId());
			}
		});

		this.startingItemPackage = startingItemPackage;

		final int maxSpread = startingIndividuals.size() * 30;
		for (final Individual individual : startingIndividuals) {
			this.individuals.put(
				Util.getRandom().nextInt(maxSpread) - maxSpread / 2,
				individual
			);
			individual.setWorldId(worldId);
		}
	}


	@Override
	public void renderUIGuide(final Graphics graphics) {
		final float mouseX = getMouseWorldX();
		final float mouseY = getMouseWorldY();

		gl.glEnable(GL_BLEND);
		gl.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
		graphics.getSpriteBatch().begin();
		graphics.getSpriteBatch().setShader(Shaders.filter);
		Shaders.filter.setUniformMatrix("u_projTrans", graphics.getUi().getUITrackingCamera().combined);
		for (final Entry<Integer, Individual> entry : individuals.entrySet()) {
			Vector2 pos;
			try {
				pos = Wiring.injector().getInstance(GameClientStateTracker.class).getActiveWorld().getTopography().getLowestEmptyTileOrPlatformTileWorldCoords(mouseX + entry.getKey(), mouseY, true);
				entry.getValue().getState().position.x = pos.x;
				entry.getValue().getState().position.y = pos.y;

				final boolean canPlace = canPlaceIndividual(entry.getValue());

				Shaders.filter.setUniformf("color", canPlace ? Color.GREEN : Color.RED);
				graphics.getSpriteBatch().draw(UserInterface.currentArrow, pos.x - 5, pos.y);
				graphics.getSpriteBatch().flush();
			} catch (final NoTileFoundException e) {
				pos = new Vector2();
			}
		}
		graphics.getSpriteBatch().end();
		gl.glDisable(GL_BLEND);

		final Container container = startingItemPackage.getContainer();
		if (container instanceof Prop) {
			((Prop) container).position.x = mouseX;
			((Prop) container).position.y = mouseY;
			PlaceCursorBoundTask.renderGuide((Prop) container);
		}
	}


	private boolean canPlaceIndividual(final Individual individual) {
		return Prop.canPlaceAt(
			individual.getState().position.x,
			individual.getState().position.y,
			1,
			individual.getHitBox().height,
			new SerializableMappingFunction<Tile, Boolean>() {
				private static final long serialVersionUID = 1L;
				@Override
				public Boolean apply(final Tile input) {
					return !(input instanceof EmptyTile);
				}
			},
			new SerializableMappingFunction<Tile, Boolean>() {
				private static final long serialVersionUID = 1L;
				@Override
				public Boolean apply(final Tile input) {
					return true;
				}
			},
			true,
			() -> {
				return true;
			},
			Domain.getWorld(individual.getWorldId())
		);
	}


	@Override
	public boolean executionConditionMet() {
		final Container container = startingItemPackage.getContainer();

		if (container instanceof Prop) {
			for (final Individual individual : individuals.values()) {
				if  (!canPlaceIndividual(individual)) {
					return false;
				}
			}
			return ((Prop) container).canPlaceAtCurrentPosition();
		}

		return false;
	}


	@Override
	public String getShortDescription() {
		return "Choose starting location";
	}


	@Override
	public boolean canCancel() {
		return false;
	}


	@Override
	public CursorBoundTask getImmediateTask() {
		return null;
	}


	@Override
	public void keyPressed(final int keyCode) {
	}
}