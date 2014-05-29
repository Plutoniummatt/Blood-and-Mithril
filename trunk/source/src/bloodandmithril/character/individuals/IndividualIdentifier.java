package bloodandmithril.character.individuals;

import static bloodandmithril.persistence.ParameterPersistenceService.getParameters;
import static bloodandmithril.world.WorldState.getCurrentEpoch;

import java.io.Serializable;

import bloodandmithril.world.Epoch;

/**
 * Encapsulates the set of properties that uniquely identifies an {@link Individual}
 *
 * @author Matt
 */
public class IndividualIdentifier implements Serializable {
	private static final long serialVersionUID = 468971814825676707L;

	private final String firstName, lastName;
	private String nickName;
	private final Epoch birthday;
	private final int id;

	/**
	 * Constructor
	 */
	public IndividualIdentifier(String firstName, String lastName, Epoch birthday) {
		this.firstName = firstName;
		this.lastName = lastName;
		this.birthday = birthday;
		this.id = getParameters().getNextIndividualId();
	}


	/** Gets the simple first name + last name representation of this {@link IndividualIdentifier} */
	public String getSimpleName() {
		if (getLastName().equals("")) {
			return getFirstName();
		}
		return getFirstName() + " " + getLastName();
	}


	public int getId() {
		return id;
	}


	public String getNickName() {
		return nickName;
	}


	public void setNickName(String nickName) {
		this.nickName = nickName;
	}


	public String getFirstName() {
		return firstName;
	}


	public String getLastName() {
		return lastName;
	}


	/** How old this {@link Individual} is */
	public int getAge() {
		int age = getCurrentEpoch().year - birthday.year;

		if (age == 0) {
			return 0;
		}

		if (getCurrentEpoch().monthOfYear < birthday.monthOfYear) {
			age--;
		} else if (getCurrentEpoch().monthOfYear == birthday.monthOfYear && getCurrentEpoch().dayOfMonth < birthday.dayOfMonth) {
			age--;
		}

		return age;
	}
}