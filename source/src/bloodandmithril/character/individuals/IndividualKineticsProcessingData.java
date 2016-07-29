package bloodandmithril.character.individuals;

import java.io.Serializable;

import bloodandmithril.core.Copyright;

/**
 * Data used for {@link IndividualKinematicsUpdater} processing
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
public class IndividualKineticsProcessingData implements Serializable {
	private static final long serialVersionUID = 9001152449205822919L;

	/** Determines fall damage */
	public float distanceFallen = 0f;
	
	/** The coordinates most recent tile stood on */
	public Integer mostRecentTileX, mostRecentTileY;
	
	/** The tile coordinates of the non-empty tile directly below */
	public Integer tileDirectlyBelowX, tileDirectlyBelowY;
}