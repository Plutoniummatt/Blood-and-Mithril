package bloodandmithril.item.misc;

import static com.google.common.collect.Iterables.tryFind;

import java.util.Collections;
import java.util.List;

import bloodandmithril.item.Item;

/**
 * A skeleton {@link Key} with 7 teeth, 10 unique heights per tooth.
 *
 * @author Matt
 */
public abstract class SkeletonKey extends Key {
	private static final long serialVersionUID = -5499080091518772541L;
	
	private final List<Integer> teeth;
	
	/**
	 * Constructor
	 */
	protected SkeletonKey(List<Integer> teeth) {
		super(0.01f, 0);
		
		if (teeth.size() != 7) {
			throw new RuntimeException("Must have 7 teeth");
		}

		if (tryFind(teeth, tooth -> {
			return tooth > 10 || tooth < 0;
		}).isPresent()) {
			throw new RuntimeException("Each tooth must have a height between 0 and 10 inclusive");
		}
		
		this.teeth = Collections.unmodifiableList(teeth);
	}


	@Override
	public boolean sameAs(Item other) {
		if (!(other instanceof SkeletonKey)) {
			return false;
		}

		for (int i = 6; i >= 0; i--) {
			if (teeth.get(i) != ((SkeletonKey)other).teeth.get(i)) {
				return false;
			}
		}
		
		return true;
	}

	
	public boolean match(List<Integer> toMatch) {
		for (int i = 6; i >= 0; i--) {
			if (teeth.get(i) != toMatch.get(i)) {
				return false;
			}
		}
		
		return true;
	}
}