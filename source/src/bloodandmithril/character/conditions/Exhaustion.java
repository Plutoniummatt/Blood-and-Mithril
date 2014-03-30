package bloodandmithril.character.conditions;

import bloodandmithril.character.Individual;
import bloodandmithril.character.Individual.Condition;
import bloodandmithril.world.Domain;

public class Exhaustion extends Condition {
	private static final long serialVersionUID = 1486454742433474423L;
	
	private final int affected;

	/**
	 * Constructor
	 */
	public Exhaustion(int affected) {
		this.affected = affected;
	}
	

	@Override
	public void affect(Individual affected, float delta) {
		affected.decreaseHunger((1 - affected.getState().stamina) * 0.00001f);
	}

	
	@Override
	public void infect(Individual infected, float delta) {
		// Not infectious
	}
	
	
	@Override
	public boolean isExpired() {
		return Domain.getIndividuals().get(affected).getState().stamina > 0.75f;
	}

	
	@Override
	public void uponExpiry() {
	}

	
	@Override
	public void stack(Condition condition) {
	}

	
	@Override
	public boolean isNegative() {
		return true;
	}

	
	@Override
	public String getHelpText() {
		return "Fuckin tired mate";
	}

	
	@Override
	public String getName() {
		return getName(Domain.getIndividuals().get(affected).getState().stamina);
	}
	
	
	/**
	 * See {@link Condition#getName()}
	 */
	public static String getName(float stamina) {
		int h = Math.round(stamina * 10f);
		switch (h) {
			case 0: return "Exhausted";
			case 1: return "Very tired";
			case 2: return "Very tired";
			case 3: return "Tired";
			case 4: return "Tired";
			case 5: return "Slightly tired";
			case 6: return "Slightly tired";
			case 7: return "Rapid breathing";
			case 8: return "Rapid breathing";
			case 9: return "Energetic";
			case 10: return "Energetic";
			default: throw new RuntimeException("Unexpected thirst level");
		}
	}
}