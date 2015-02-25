package bloodandmithril.util.cursorboundtask;

import static bloodandmithril.core.BloodAndMithrilClient.getMouseWorldX;
import static bloodandmithril.core.BloodAndMithrilClient.getMouseWorldY;
import static bloodandmithril.core.BloodAndMithrilClient.spriteBatch;
import static com.badlogic.gdx.Gdx.gl;
import static com.badlogic.gdx.graphics.GL10.GL_BLEND;
import static com.badlogic.gdx.graphics.GL10.GL_ONE_MINUS_SRC_ALPHA;
import static com.badlogic.gdx.graphics.GL10.GL_SRC_ALPHA;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.Copyright;
import bloodandmithril.core.ItemPackage;
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

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.google.common.collect.Maps;

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
	public ChooseStartingLocationCursorBoundTask(Set<Individual> startingIndividuals, ItemPackage startingItemPackage, int startingFactionId) {
		super(
			null, 
			true
		);
		
		setTask(args -> {
			Domain.getActiveWorld().props().addProp((Prop) startingItemPackage.getContainer());
			for (Individual individual : individuals.values()) {
				individual.setFactionId(startingFactionId);
				Domain.addIndividual(individual, Domain.getActiveWorld().getWorldId());
			}
		});
		
		this.startingItemPackage = startingItemPackage;
		
		int maxSpread = startingIndividuals.size() * 30;
		for (Individual individual : startingIndividuals) {
			this.individuals.put(
				Util.getRandom().nextInt(maxSpread) - maxSpread / 2, 
				individual
			);
		}
	}


	@Override
	public void renderUIGuide() {
		float mouseX = getMouseWorldX();
		float mouseY = getMouseWorldY();
		
		gl.glEnable(GL_BLEND);
		gl.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
		spriteBatch.begin();
		spriteBatch.setShader(Shaders.filter);
		Shaders.filter.setUniformMatrix("u_projTrans", UserInterface.UICameraTrackingCam.combined);
		for (Entry<Integer, Individual> entry : individuals.entrySet()) {
			Vector2 pos;
			try {
				pos = Domain.getActiveWorld().getTopography().getLowestEmptyTileOrPlatformTileWorldCoords(mouseX + entry.getKey(), mouseY, true);
				entry.getValue().getState().position.x = pos.x;
				entry.getValue().getState().position.y = pos.y;
				
				boolean canPlace = canPlaceIndividual(entry.getValue());
				
				Shaders.filter.setUniformf("color", canPlace ? Color.GREEN : Color.RED);
				spriteBatch.draw(UserInterface.currentArrow, pos.x - 5, pos.y);
				spriteBatch.flush();
			} catch (NoTileFoundException e) {
				pos = new Vector2();
			}
		}
		spriteBatch.end();
		gl.glDisable(GL_BLEND);
		
		Container container = startingItemPackage.getContainer();
		if (container instanceof Prop) {
			((Prop) container).position.x = mouseX;
			((Prop) container).position.y = mouseY;
			PlaceCursorBoundTask.renderGuide((Prop) container);
		}
	}


	private boolean canPlaceIndividual(Individual individual) {
		return Prop.canPlaceAt(
			individual.getState().position.x,
			individual.getState().position.y, 
			1, 
			individual.getHitBox().height, 
			new SerializableMappingFunction<Tile, Boolean>() {
				private static final long serialVersionUID = 1L;
				@Override
				public Boolean apply(Tile input) {
					return !(input instanceof EmptyTile);
				}
			},
			new SerializableMappingFunction<Tile, Boolean>() {
				private static final long serialVersionUID = 1L;
				@Override
				public Boolean apply(Tile input) {
					return true;
				}
			},
			true, 
			() -> {
				return true;
			}
		);
	}

	
	@Override
	public boolean executionConditionMet() {
		Container container = startingItemPackage.getContainer();
		
		if (container instanceof Prop) {
			for (Individual individual : individuals.values()) {
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
}