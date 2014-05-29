package bloodandmithril.character.individuals;

import java.io.Serializable;

import com.badlogic.gdx.math.Vector2;

/**
 * Data used for {@link Kinematics} processing
 *
 * @author Matt
 */
public class IndividualKineticsProcessingData implements Serializable {
	private static final long serialVersionUID = 9001152449205822919L;

	/** Coordinates of the tile to jump off (ignored by ground detection) */
	public Vector2 jumpOff = null;

	/** Used for platform jump-off processing */
	public boolean jumpedOff = false;

	/** True if this {@link Individual} is currently stepping up */
	public boolean steppingUp;

	/** Part of the step-up processing */
	public int steps = 0;
}