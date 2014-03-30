package bloodandmithril.character.conditions;

import bloodandmithril.character.Individual;
import bloodandmithril.character.Individual.Condition;
import bloodandmithril.world.Domain;

/**
 * {@link Condition} representing Hunger
 *
 * @author Matt
 */
public class Hunger extends Condition {
	private static final long serialVersionUID = 6876432751800675014L;

	private final int affected;
	private final float oldHealthRegen;

	/**
	 * Constructor
	 */
	public Hunger(int affected) {
		this.affected = affected;
		this.oldHealthRegen = Domain.getIndividuals().get(affected).getState().healthRegen;
		Domain.getIndividuals().get(affected).changeHealthRegen(oldHealthRegen * 0.5f);
	}


	@Override
	public void affect(Individual affected, float delta) {
		if (affected.getState().hunger == 0f) {
			affected.damage(delta * 0.01f);
			if (affected.getState().healthRegen != 0f) {
				affected.changeHealthRegen(0f);
			}
		} else if (affected.getState().healthRegen == 0f) {
			affected.changeHealthRegen(oldHealthRegen);
		}
	}


	@Override
	public void infect(Individual infected, float delta) {
		// Not infectious
	}


	@Override
	public boolean isExpired() {
		return Domain.getIndividuals().get(affected).getState().hunger > 0.75f;
	}


	@Override
	public boolean isNegative() {
		return true;
	}


	@Override
	public String getHelpText() {
		return "Hunger is the physical sensation of desiring food.  Has a detrimental effect on natural wound healing, prolonged hunger will cause vital signs to weaken.";
	}


	@Override
	public String getName() {
		return getName(Domain.getIndividuals().get(affected).getState().hunger);
	}
		
	
	/**
	 * See {@link Condition#getName()}
	 */
	public static String getName(float hunger) {
		int h = Math.round(hunger * 10f);
		switch (h) {
			case 0: return "Starving";
			case 1: return "Ravenous";
			case 2: return "Very hungry";
			case 3: return "Very hungry";
			case 4: return "Hungry";
			case 5: return "Hungry";
			case 6: return "Hungry";
			case 7: return "Peckish";
			case 8: return "Peckish";
			case 9: return "Not hungry";
			case 10: return "Not hungry";
			default: throw new RuntimeException("Unexpected hunger level");
		}
	}


	@Override
	public void uponExpiry() {
		Domain.getIndividuals().get(affected).changeHealthRegen(oldHealthRegen);
	}


	@Override
	public void stack(Condition condition) {
	}
}