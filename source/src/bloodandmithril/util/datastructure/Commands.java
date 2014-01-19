package bloodandmithril.util.datastructure;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedList;

import com.google.common.collect.Lists;

/**
 * An implementation to hold a set of active commands that does not depend on {@link HashMap}s
 *
 * @author Matt
 */
public class Commands implements Serializable {
	private static final long serialVersionUID = 6218069030001141634L;
	
	/** We use a list here, the number of commands should be quite minimal */
	LinkedList<String> activeCommands = Lists.newLinkedList();
	
	public boolean isActive(String code) {
		return activeCommands.contains(code);
	}
	
	public void activate(String code) {
		if (isActive(code)) {
			return;
		}
		LinkedList<String> newCommands = Lists.newLinkedList(activeCommands);
		newCommands.add(code);
		activeCommands = newCommands;
	}
	
	public void deactivate(String code) {
		LinkedList<String> newCommands = Lists.newLinkedList(activeCommands);
		newCommands.remove(code);
		activeCommands = newCommands;
	}
	
	public void clear() {
		LinkedList<String> newCommands = Lists.newLinkedList(activeCommands);
		newCommands.clear();
		activeCommands = newCommands;
	}
}