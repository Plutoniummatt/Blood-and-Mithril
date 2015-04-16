package bloodandmithril.character.individuals;

import static bloodandmithril.persistence.ParameterPersistenceService.getParameters;

import java.io.Serializable;

import bloodandmithril.core.Copyright;
import bloodandmithril.world.Domain;
import bloodandmithril.world.Epoch;

/**
 * Encapsulates the set of properties that uniquely identifies an {@link Individual}
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
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
		Epoch epoch = Domain.getWorld(Domain.getIndividual(getId()).getWorldId()).getEpoch();
		int age = epoch.year - getBirthday().year;

		if (age == 0) {
			return 0;
		}

		if (epoch.monthOfYear < getBirthday().monthOfYear) {
			age--;
		} else if (epoch.monthOfYear == getBirthday().monthOfYear && epoch.dayOfMonth < getBirthday().dayOfMonth) {
			age--;
		}

		return age;
	}


	public Epoch getBirthday() {
		return birthday;
	}
}