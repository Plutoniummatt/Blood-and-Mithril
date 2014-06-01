package bloodandmithril.character.individuals.characters;

import static bloodandmithril.character.individuals.Individual.Action.RUN_RIGHT;
import static bloodandmithril.character.individuals.Individual.Action.STAND_RIGHT;
import static bloodandmithril.character.individuals.Individual.Action.WALK_RIGHT;
import static bloodandmithril.core.BloodAndMithrilClient.spriteBatch;
import static java.util.Collections.singletonList;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import bloodandmithril.character.ai.implementations.ElfAI;
import bloodandmithril.character.conditions.Exhaustion;
import bloodandmithril.character.conditions.Hunger;
import bloodandmithril.character.conditions.Thirst;
import bloodandmithril.character.individuals.GroundedIndividual;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.character.individuals.IndividualIdentifier;
import bloodandmithril.character.individuals.IndividualState;
import bloodandmithril.core.BloodAndMithrilClient;
import bloodandmithril.csi.ClientServerInterface;
import bloodandmithril.item.items.Item;
import bloodandmithril.item.items.equipment.Equipable;
import bloodandmithril.item.items.equipment.weapon.OneHandedWeapon;
import bloodandmithril.ui.KeyMappings;
import bloodandmithril.ui.components.ContextMenu.MenuItem;
import bloodandmithril.util.AnimationHelper;
import bloodandmithril.util.SerializableColor;
import bloodandmithril.util.Shaders;
import bloodandmithril.util.SpacialConfiguration;
import bloodandmithril.util.datastructure.Box;
import bloodandmithril.world.Domain;
import bloodandmithril.world.World;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * Exceptional at:
 *
 * - Archery
 * - Walking/Running on water
 * - Infinite stamina
 * - Longer natural lifespan
 * - Better vision
 * - Natural wound healing
 * - Better at magic
 *
 * Disadvantages:
 *
 * - Low hitpoints
 * - Vegetarians
 * - Technophobes
 * - Can't carry much
 *
 * @author Matt
 */
public class Elf extends GroundedIndividual {
	private static final long serialVersionUID = -5566954059579973505L;

	/** True if female */
	private boolean female;

	/** Hair/eye colors */
	private SerializableColor hairColor, eyeColor;

	/** Biography of this Elf */
	private String biography = "";

	/** Elf-specific animation map */
	private static Map<Action, List<Animation>> animationMap = Maps.newHashMap();

	static {
		animationMap.put(STAND_RIGHT, singletonList(AnimationHelper.animation(Domain.individualTexture, 0, 0, 64, 112, 1, 1f)));
		animationMap.put(WALK_RIGHT, singletonList(AnimationHelper.animation(Domain.individualTexture, 0, 112, 64, 112, 10, 0.11f)));
		animationMap.put(RUN_RIGHT, singletonList(AnimationHelper.animation(Domain.individualTexture, 0, 224, 64, 112, 8, 0.11f)));
	}

	/**
	 * Constructor
	 */
	public Elf(
			IndividualIdentifier id,
			IndividualState state,
			int factionId,
			boolean female,
			float capacity,
			World world) {
		super(id, state, factionId, capacity, 10, 40, 95, 30, new Box(new Vector2(state.position.x, state.position.y), 120, 120), world.getWorldId(), 2);
		this.female = female;

		this.ai = new ElfAI(this);
	}


	/**
	 * Constructor
	 */
	private Elf(
			IndividualIdentifier id,
			IndividualState state,
			int factionId,
			boolean female,
			float capacity,
			int worldId) {
		super(id, state, factionId, capacity, 10, 40, 95, 30, new Box(new Vector2(state.position.x, state.position.y), 120, 120), worldId, 2);
		this.female = female;

		this.ai = new ElfAI(this);
	}


	@Override
	protected void internalRender() {
		// Draw the body, position is centre bottom of the frame
		Collection<Animation> currentAnimations = getCurrentAnimation();
		if (currentAnimations == null) {
			return;
		}

		spriteBatch.begin();
		spriteBatch.setShader(Shaders.pass);
		Shaders.pass.setUniformMatrix("u_projTrans", BloodAndMithrilClient.cam.combined);
		for (Animation animation : currentAnimations) {
			TextureRegion keyFrame = animation.getKeyFrame(animationTimer, true);
			spriteBatch.draw(
				keyFrame,
				getState().position.x - keyFrame.getRegionWidth()/2,
				getState().position.y
			);
		}
		spriteBatch.end();

		// Render equipped items
		spriteBatch.begin();
		Shaders.elfHighLight.setUniformi("hair", 0);
		Shaders.elfDayLight.setUniformi("hair", 0);
		Shaders.elfDayLight.setUniformMatrix("u_projTrans", BloodAndMithrilClient.cam.combined);
		Shaders.elfHighLight.setUniformMatrix("u_projTrans", BloodAndMithrilClient.cam.combined);
		for (Item equipped : getEquipped().keySet()) {
			Equipable toRender = (Equipable) equipped;
			if (equipped instanceof OneHandedWeapon) {
				SpacialConfiguration config = getOneHandedWeaponSpatialConfigration();
				if (config != null) {
					toRender.render(config.position.add(getState().position), config.orientation, config.flipX);
				}
			}
		}

		spriteBatch.end();
		spriteBatch.flush();
	}



	@Override
	protected void internalUpdate(float delta) {
		super.internalUpdate(delta);

		if (ClientServerInterface.isServer()) {
			updateVitals(delta);
		}
	}


	private void updateVitals(float delta) {
		heal(delta * getState().healthRegen);

		decreaseHunger(0.000001f);
		decreaseThirst(0.000003f);

		if (!isWalking()) {
			if (isCommandActive(KeyMappings.moveLeft) || isCommandActive(KeyMappings.moveRight)) {
				decreaseStamina(0.0005f);
			} else {
				increaseStamina(delta * getState().staminaRegen);
			}
		} else {
			if (isCommandActive(KeyMappings.moveLeft) || isCommandActive(KeyMappings.moveRight)) {
				increaseStamina(delta * getState().staminaRegen / 2);
			} else {
				increaseStamina(0.001f);
			}
		}

		if (getState().hunger < 0.75f) {
			addCondition(new Hunger(getId().getId()));
		}

		if (getState().thirst < 0.75f) {
			addCondition(new Thirst(getId().getId()));
		}

		if (getState().stamina < 0.75f) {
			addCondition(new Exhaustion(getId().getId()));
		}
	}


	@Override
	public Color getToolTipTextColor() {
		return Color.GREEN;
	}


	@Override
	public List<MenuItem> internalGetContextMenuItems() {
		return Lists.newArrayList();
	}


	@Override
	public String getDescription() {
		return biography;
	}


	@Override
	public void updateDescription(String updated) {
		biography = updated;
	}


	@Override
	protected SpacialConfiguration getOneHandedWeaponSpatialConfigration() {
		return null;
	}


	@Override
	protected void internalCopyFrom(Individual other) {
		if (!(other instanceof Elf)) {
			throw new RuntimeException("Cannot cast " + other.getClass().getSimpleName() + " to Elf.");
		}

		this.hairColor = ((Elf) other).hairColor;
		this.eyeColor = ((Elf) other).eyeColor;
		this.female = ((Elf) other).female;
		this.biography = ((Elf) other).biography;
	}


	@Override
	public Individual copy() {
		Elf elf = new Elf(getId(), getState(), factionId, female, getMaxCapacity(), getWorldId());
		elf.copyFrom(this);
		return elf;
	}


	@Override
	protected Map<Action, List<Animation>> getAnimationMap() {
		return animationMap;
	}


	@Override
	public float getWalkSpeed() {
		return 55f;
	}


	@Override
	public float getRunSpeed() {
		return 120f;
	}
}