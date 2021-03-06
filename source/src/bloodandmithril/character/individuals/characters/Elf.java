package bloodandmithril.character.individuals.characters;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Vector2;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import bloodandmithril.audio.SoundService;
import bloodandmithril.character.ai.implementations.ElfAI;
import bloodandmithril.character.ai.perception.Listener;
import bloodandmithril.character.ai.perception.Observer;
import bloodandmithril.character.ai.perception.SoundStimulus;
import bloodandmithril.character.ai.perception.Visible;
import bloodandmithril.character.individuals.Action;
import bloodandmithril.character.individuals.Humanoid;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.character.individuals.IndividualIdentifier;
import bloodandmithril.character.individuals.IndividualState;
import bloodandmithril.core.Copyright;
import bloodandmithril.core.Description;
import bloodandmithril.core.Name;
import bloodandmithril.graphics.RenderIndividualWith;
import bloodandmithril.graphics.particles.ParticleService;
import bloodandmithril.graphics.renderers.ElfRenderer;
import bloodandmithril.networking.ClientServerInterface;
import bloodandmithril.prop.construction.Construction;
import bloodandmithril.prop.construction.craftingstation.BlacksmithWorkshop;
import bloodandmithril.prop.construction.craftingstation.Campfire;
import bloodandmithril.prop.construction.craftingstation.Furnace;
import bloodandmithril.prop.construction.craftingstation.WorkBench;
import bloodandmithril.util.AnimationHelper.AnimationSwitcher;
import bloodandmithril.util.SerializableColor;
import bloodandmithril.util.Util;
import bloodandmithril.util.datastructure.Box;
import bloodandmithril.util.datastructure.WrapperForTwo;
import bloodandmithril.world.World;

/**
 * Exceptional at:
 *
 * - Archery
 * - Physical activities do not deplete stamina
 * - Longer natural life span
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
@Name(name = "Elves")
@Description(description = "Elves are children of nature, they are nimble creatures with a good grip on magic and excel at archery.")
@Copyright("Matthew Peck 2014")
@RenderIndividualWith(ElfRenderer.class)
public class Elf extends Humanoid implements Observer, Listener {
	private static final long serialVersionUID = -5566954059579973505L;

	/** True if female */
	private boolean female;

	/** Hair/eye colors */
	private SerializableColor hairColor, eyeColor, skinColor;

	/** Biography of this Elf */
	private String biography = "";

	/** Hair style of this elf */
	private int hairStyle;

	/** Death animation timer */
	private float deathAlpha = 0f;

	/**
	 * Constructor
	 */
	public Elf(
			final IndividualIdentifier id,
			final IndividualState state,
			final int factionId,
			final boolean female,
			final World world,
			final Color hairColor,
			final Color eyeColor,
			final Color skinColor) {
		super(
			id,
			state,
			factionId,
			Behaviour.NEUTRAL,
			50f,
			250,
			10,
			40,
			95,
			30,
			new Box(
				new Vector2(
					state.position.x,
					state.position.y
				),
				120,
				120
			),
			world == null ? 0 : world.getWorldId(),
			2
		);

		this.setHairStyle(Util.getRandom().nextInt(3) + 1);
		this.female = female;
		this.setAi(new ElfAI(this));
		this.setHairColor(new SerializableColor(hairColor));
		this.setEyeColor(new SerializableColor(eyeColor));
		this.setSkinColor(new SerializableColor(skinColor));
	}


	/**
	 * Constructor
	 */
	private Elf(
			final IndividualIdentifier id,
			final IndividualState state,
			final int factionId,
			final boolean female,
			final float capacity,
			final int worldId,
			final Color hairColor,
			final Color eyeColor,
			final Color skinColor) {
		super(id, state, factionId, Behaviour.NEUTRAL, capacity, 100, 10, 40, 95, 30, new Box(new Vector2(state.position.x, state.position.y), 120, 120), worldId, 2);

		this.setHairStyle(Util.getRandom().nextInt(3) + 1);
		this.female = female;
		this.setAi(new ElfAI(this));
		this.setHairColor(new SerializableColor(hairColor));
		this.setEyeColor(new SerializableColor(eyeColor));
		this.setSkinColor(new SerializableColor(skinColor));
	}


	@Override
	public Color getToolTipTextColor() {
		return Color.GREEN;
	}


	@Override
	public String getDescription() {
		return biography;
	}


	@Override
	public void updateDescription(final String updated) {
		biography = updated;
	}


	@Override
	protected void internalCopyFrom(final Individual other) {
		if (!(other instanceof Elf)) {
			throw new RuntimeException("Cannot cast " + other.getClass().getSimpleName() + " to Elf.");
		}

		this.setHairColor(((Elf) other).getHairColor());
		this.setEyeColor(((Elf) other).getEyeColor());
		this.setSkinColor(((Elf) other).getSkinColor());
		this.female = ((Elf) other).female;
		this.biography = ((Elf) other).biography;
	}


	@Override
	public Individual copy() {
		final Elf elf = new Elf(
			getId(),
			getState(),
			getFactionId(),
			female,
			getMaxCapacity(),
			getWorldId(),
			new Color(getHairColor().r, getHairColor().g, getHairColor().b, getHairColor().a),
			new Color(getEyeColor().r, getEyeColor().g, getEyeColor().b, getEyeColor().a),
			new Color(getSkinColor().r, getSkinColor().g, getSkinColor().b, getSkinColor().a)
		);
		elf.copyFrom(this);
		return elf;
	}


	@Override
	protected Map<Action, List<WrapperForTwo<AnimationSwitcher, ShaderProgram>>> getAnimationMap() {
		return ElfRenderer.animationMap;
	}


	@Override
	public float getWalkSpeed() {
		return 112.5f;
	}


	@Override
	public float getRunSpeed() {
		return 225f;
	}


	@Override
	public Box getDefaultAttackingHitBox() {
		return new Box(
			new Vector2(
				getHitBox().position.x + (getCurrentAction().left() ? - getHitBox().width * (1f/3f) : getHitBox().width * (1f/3f)),
				getHitBox().position.y
			),
			getHitBox().width * 2 / 3,
			getHitBox().height
		);
	}


	@Override
	public float getUnarmedMinDamage() {
		return 0.5f;
	}


	@Override
	public float getUnarmedMaxDamage() {
		return 1.0f;
	}


	@Override
	public float getDefaultAttackPeriod() {
		return 0.8f;
	}


	@Override
	protected float hungerDrain() {
		return 0.000001f;
	}


	@Override
	protected float thirstDrain() {
		return 0.000003f;
	}


	@Override
	protected float staminaDrain() {
		return 0.0004f;
	}


	@Override
	public void moan() {
		if (Util.roll(0.85f)) {
			return;
		}

		if (ClientServerInterface.isClient()) {
			SoundService.play(
				SoundService.femaleHit,
				getState().position,
				true,
				this
			);
		}
	}


	@Override
	public Set<Construction> getConstructables() {
		return Sets.newHashSet(
			new Furnace(0, 0),
			new WorkBench(0, 0),
			new BlacksmithWorkshop(0, 0),
			new Campfire(0, 0)
		);
	}


	@Override
	public Vector2 getEmissionPosition() {
		return getState().position.cpy().add(getCurrentAction().left() ? 3 : 7, getHeight() / 2);
	}


	@Override
	public Vector2 getObservationPosition() {
		return getState().position.cpy().add(0f, getHeight() - 15);
	}


	@Override
	public Vector2 getObservationDirection() {
		return getCurrentAction().left() ? new Vector2(-1f, 0f) : new Vector2(1f, 0f);
	}


	@Override
	public float getFieldOfView() {
		return 150f;
	}


	@Override
	public float getViewDistance() {
		return 2000f;
	}


	@Override
	public Collection<Vector2> getVisibleLocations() {
		final LinkedList<Vector2> locations = Lists.newLinkedList();
		for (int i = 10; i < getHeight() - 10 ; i += 10) {
			locations.add(getState().position.cpy().add(0f, i));
		}
		return locations;
	}


	@Override
	public void listen(final SoundStimulus stimulus) {
	}


	@Override
	public boolean isVisible() {
		return true;
	}


	@Override
	public void sayStuck() {
		speak("Looks like I'm stuck...", 1000);
	}


	@Override
	public boolean reactIfVisible(final SoundStimulus stimulus) {
		return false;
	}


	@Override
	public void internalKill() {
		setDeathAlpha(0f);
	}


	@Override
	public void setAnimationTimer(final float animationTimer) {
		if (isAlive()) {
			super.setAnimationTimer(animationTimer);
		} else {
			if (getDeathAlpha() > 2.4f && getCurrentAction() != Action.DEAD) {
				setCurrentAction(Action.DEAD);
				ParticleService.fireworks(getState().position.cpy().add(0, getHeight()/3), getEyeColor().getColor(), getHairColor().getColor());
			}

			if (getDeathAlpha() >= 2.4f) {
				setDeathAlpha(2.4f);
			} else {
				setDeathAlpha(getDeathAlpha() + 0.04f);
			}
		}
	}


	public SerializableColor getHairColor() {
		return hairColor;
	}


	public void setHairColor(final SerializableColor hairColor) {
		this.hairColor = hairColor;
	}


	public SerializableColor getEyeColor() {
		return eyeColor;
	}


	public void setEyeColor(final SerializableColor eyeColor) {
		this.eyeColor = eyeColor;
	}


	public SerializableColor getSkinColor() {
		return skinColor;
	}


	public void setSkinColor(final SerializableColor skinColor) {
		this.skinColor = skinColor;
	}


	public float getDeathAlpha() {
		return deathAlpha;
	}


	public void setDeathAlpha(final float deathAlpha) {
		this.deathAlpha = deathAlpha;
	}


	public int getHairStyle() {
		return hairStyle;
	}


	public void setHairStyle(final int hairStyle) {
		this.hairStyle = hairStyle;
	}


	@Override
	public boolean sameAs(final Visible other) {
		if (other instanceof Hare) {
			return ((Elf) other).getId().getId() == getId().getId();
		}
		return false;
	}


	@Override
	public void playAffirmativeSound() {
		if (ClientServerInterface.isClient()) {
			SoundService.play(Util.randomOneOf(SoundService.femaleOk, SoundService.femaleGoing), getState().position, false, this);
		}
	}
}