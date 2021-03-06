package bloodandmithril.control;

import static bloodandmithril.util.datastructure.WrapperForTwo.wrap;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.badlogic.gdx.Input;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.inject.Singleton;

import bloodandmithril.core.Copyright;
import bloodandmithril.util.datastructure.WrapperForTwo;

/**
 * Maps keys to actions
 * Maps keyCodes to strings
 *
 * @author Matt
 */
@Singleton
@Copyright("Matthew Peck 2014")
public class Controls implements Serializable {
	private static final long serialVersionUID = -1052808676922281824L;

	/** The maximum spread of individuals when going to location */
	public static final float INDIVIDUAL_SPREAD = 600f;

	/** The tolerance for double clicking */
	public static final long DOUBLE_CLICK_TIME = 250L;

	/** For camera dragging */
	public static int camDragX, camDragY, oldCamX, oldCamY;

	public static final Map<Integer, WrapperForTwo<String, String>> keyMap = Maps.newHashMap();
	public static final Map<Integer, String> keyName = Maps.newHashMap();
	public static final Set<Integer> disallowedKeys = Sets.newHashSet();

	public MappedKey middleClick = new MappedKey("Middle click", 2, "", false);
	public MappedKey rightClick = new MappedKey("Right click", 1, "", false);
	public MappedKey leftClick = new MappedKey("Left click", 0, "", false);
	public MappedKey sendChatMessage = new MappedKey("Send message", Input.Keys.ENTER, "", false);
	public MappedKey deleteCharacter = new MappedKey("Delete character", Input.Keys.BACKSPACE, "", false);

	public MappedKey selectIndividual = new MappedKey("Add to Selection", Input.Keys.SHIFT_LEFT, "Hold and left click to add an individual to currently selected individuals.", false);
	public MappedKey addWayPoint = new MappedKey("Add Waypoint", Input.Keys.SHIFT_LEFT, "Hold and right click to add a waypoint.", false);
	public MappedKey bulkCraft = new MappedKey("Bulk Craft", Input.Keys.SHIFT_LEFT, "Hold to craft multiple items at a crafting station.", false);
	public MappedKey bulkDiscard = new MappedKey("Bulk Discard", Input.Keys.SHIFT_LEFT, "Hold to discard multiple items from inventory.", false);
	public MappedKey bulkTrade = new MappedKey("Bulk Trade/Transfer", Input.Keys.SHIFT_LEFT, "Hold to trade/transfer multiple items.", false);
	public MappedKey continuousThrowing = new MappedKey("Bulk Throw", Input.Keys.SHIFT_LEFT, "Hold to continuously throw items.", false);

	public MappedKey disableEnableAI = new MappedKey("Disable/Enable AI", Input.Keys.S, "Toggle to enable/disable AI of controllable individuals.", true);
	public MappedKey moveCamUp = new MappedKey("Move Camera Up", Input.Keys.UP, "Camera movement.", true);
	public MappedKey moveCamDown = new MappedKey("Move Camera Down", Input.Keys.DOWN, "Camera movement.", true);
	public MappedKey moveCamLeft = new MappedKey("Move Camera Left", Input.Keys.LEFT, "Camera movement.", true);
	public MappedKey moveCamRight = new MappedKey("Move Camera Right", Input.Keys.RIGHT, "Camera movement.", true);
	public MappedKey rightClickDragBox = new MappedKey("Bulk Loot", Input.Keys.X, "Hold this button and use the right mouse button to drag a box around items to loot.", true);
	public MappedKey forceMove = new MappedKey("Force Move", Input.Keys.F, "Force move will instruct an individual to move to a location despite any fall damage.", true);
	public MappedKey jump = new MappedKey("Jump", Input.Keys.SPACE, "Hold and right click with selected individual(s) to jump.", true);
	public MappedKey toggleWalkRun = new MappedKey("Toggle Run/Walk", Input.Keys.W, "Toggles walk/run modes.", true);
	public MappedKey mineTile = new MappedKey("Mine/Dig", Input.Keys.Q, "Hold and right click to mine a tile.", true);
	public MappedKey attack = new MappedKey("Attack Melee", Input.Keys.A, "Hold and right click to attack another individual. (Melee)", true);
	public MappedKey rangedAttack = new MappedKey("Attack Ranged", Input.Keys.CONTROL_LEFT, "Hold and right click to perform ranged attack with a ranged weapon.", true);
	public MappedKey snapToGrid = new MappedKey("Placement Grid Alignment", Input.Keys.G, "Hold when attempting to place items in the world to snap to tile grid.", true);
	public MappedKey openInventory = new MappedKey("Open Inventory", Input.Keys.I, "Press to open inventory of currently selected individual (Only works when one is selected).", true);
	public MappedKey openAIRoutines = new MappedKey("Open AI Routines", Input.Keys.R, "Press to open AI routines of currently selected individual (Only works when one is selected).", true);
	public MappedKey openBuildWindow = new MappedKey("Open Build Window", Input.Keys.B, "Press to open build window of currently selected individual (Only works when one is selected).", true);
	public MappedKey speedUp = new MappedKey("Increase game speed", Input.Keys.PLUS, "Press to increase game speed.", true);
	public MappedKey slowDown = new MappedKey("Decrease game speed", Input.Keys.MINUS, "Press to decrease game speed.", true);

	public Map<Integer, MappedKey> getFunctionalKeyMappings() {
		final Map<Integer, MappedKey> map = Maps.newHashMap();

		final List<MappedKey> keys = Lists.newArrayList(
			rightClickDragBox,
			forceMove,
			jump,
			toggleWalkRun,
			mineTile,
			attack,
			rangedAttack,
			snapToGrid,
			openInventory,
			openAIRoutines,
			openBuildWindow,
			moveCamUp,
			moveCamDown,
			moveCamLeft,
			moveCamRight,
			disableEnableAI
		);

		for (final MappedKey key : keys) {
			map.put(key.keyCode, key);
		}

		return map;
	}

	public List<MappedKey> getUnmappableKeys() {
		return Lists.newArrayList(
			selectIndividual,
			addWayPoint,
			bulkCraft,
			bulkDiscard,
			bulkTrade,
			continuousThrowing
		);
	}

	public static class MappedKey implements Serializable {
		private static final long serialVersionUID = -7721011148775681451L;
		public final String description;
		public final String showInfo;
		public final boolean canChange;

		public int keyCode;

		/**
		 * Constructor
		 */
		public MappedKey(final String description, final int keyCode, final String showInfo, final boolean canChange) {
			this.description = description;
			this.keyCode = keyCode;
			this.showInfo = showInfo;
			this.canChange = canChange;
		}
	}

	public static void setup() {
		disallowedKeys.add(Input.Keys.SHIFT_LEFT);
		disallowedKeys.add(Input.Keys.ESCAPE);
		disallowedKeys.add(Input.Keys.ENTER);

		keyName.put(Input.Keys.NUM_0, "0");
		keyName.put(Input.Keys.NUM_1, "1");
		keyName.put(Input.Keys.NUM_2, "2");
		keyName.put(Input.Keys.NUM_3, "3");
		keyName.put(Input.Keys.NUM_4, "4");
		keyName.put(Input.Keys.NUM_5, "5");
		keyName.put(Input.Keys.NUM_6, "6");
		keyName.put(Input.Keys.NUM_7, "7");
		keyName.put(Input.Keys.NUM_8, "8");
		keyName.put(Input.Keys.NUM_9, "9");
		keyName.put(Input.Keys.NUMPAD_0, "Numpad 0");
		keyName.put(Input.Keys.NUMPAD_1, "Numpad 1");
		keyName.put(Input.Keys.NUMPAD_2, "Numpad 2");
		keyName.put(Input.Keys.NUMPAD_3, "Numpad 3");
		keyName.put(Input.Keys.NUMPAD_4, "Numpad 4");
		keyName.put(Input.Keys.NUMPAD_5, "Numpad 5");
		keyName.put(Input.Keys.NUMPAD_6, "Numpad 6");
		keyName.put(Input.Keys.NUMPAD_7, "Numpad 7");
		keyName.put(Input.Keys.NUMPAD_8, "Numpad 8");
		keyName.put(Input.Keys.NUMPAD_9, "Numpad 9");
		keyName.put(Input.Keys.A, "A");
		keyName.put(Input.Keys.B, "B");
		keyName.put(Input.Keys.C, "C");
		keyName.put(Input.Keys.D, "D");
		keyName.put(Input.Keys.E, "E");
		keyName.put(Input.Keys.F, "F");
		keyName.put(Input.Keys.G, "G");
		keyName.put(Input.Keys.H, "H");
		keyName.put(Input.Keys.I, "I");
		keyName.put(Input.Keys.J, "J");
		keyName.put(Input.Keys.K, "K");
		keyName.put(Input.Keys.L, "L");
		keyName.put(Input.Keys.M, "M");
		keyName.put(Input.Keys.N, "N");
		keyName.put(Input.Keys.O, "O");
		keyName.put(Input.Keys.P, "P");
		keyName.put(Input.Keys.Q, "Q");
		keyName.put(Input.Keys.R, "R");
		keyName.put(Input.Keys.S, "S");
		keyName.put(Input.Keys.T, "T");
		keyName.put(Input.Keys.U, "U");
		keyName.put(Input.Keys.V, "V");
		keyName.put(Input.Keys.W, "W");
		keyName.put(Input.Keys.X, "X");
		keyName.put(Input.Keys.Y, "Y");
		keyName.put(Input.Keys.Z, "Z");
		keyName.put(Input.Keys.COMMA, ",");
		keyName.put(Input.Keys.PERIOD, ".");
		keyName.put(Input.Keys.SPACE, "SPACE");
		keyName.put(Input.Keys.CONTROL_LEFT, "L CTRL");
		keyName.put(Input.Keys.SHIFT_LEFT, "L SHIFT");
		keyName.put(Input.Keys.TAB, "TAB");
		keyName.put(Input.Keys.ALT_LEFT, "L ALT");
		keyName.put(Input.Keys.ALT_RIGHT, "R ALT");
		keyName.put(Input.Keys.CONTROL_RIGHT, "R CTRL");
		keyName.put(Input.Keys.SHIFT_RIGHT, "R SHIFT");
		keyName.put(Input.Keys.ENTER, "ENTER");
		keyName.put(Input.Keys.F1, "F1");
		keyName.put(Input.Keys.F2, "F2");
		keyName.put(Input.Keys.F3, "F3");
		keyName.put(Input.Keys.F4, "F4");
		keyName.put(Input.Keys.F5, "F5");
		keyName.put(Input.Keys.F6, "F6");
		keyName.put(Input.Keys.F7, "F7");
		keyName.put(Input.Keys.F8, "F8");
		keyName.put(Input.Keys.F9, "F9");
		keyName.put(Input.Keys.F10, "F10");
		keyName.put(Input.Keys.F11, "F11");
		keyName.put(Input.Keys.F12, "F12");
		keyName.put(Input.Keys.EQUALS, "=");
		keyName.put(Input.Keys.MINUS, "-");
		keyName.put(Input.Keys.BACKSPACE, "BKSPCE");
		keyName.put(Input.Keys.LEFT_BRACKET, "[");
		keyName.put(Input.Keys.RIGHT_BRACKET, "]");
		keyName.put(Input.Keys.SEMICOLON, ";");
		keyName.put(Input.Keys.APOSTROPHE, "'");
		keyName.put(Input.Keys.SLASH, "/");
		keyName.put(Input.Keys.BACKSLASH, "\"");
		keyName.put(Input.Keys.UP, "UP");
		keyName.put(Input.Keys.DOWN, "DOWN");
		keyName.put(Input.Keys.LEFT, "LEFT");
		keyName.put(Input.Keys.RIGHT, "RIGHT");

		keyMap.put(Input.Keys.NUM_0, wrap("0", "0"));
		keyMap.put(Input.Keys.NUM_1, wrap("1", "1"));
		keyMap.put(Input.Keys.NUM_2, wrap("2", "2"));
		keyMap.put(Input.Keys.NUM_3, wrap("3", "3"));
		keyMap.put(Input.Keys.NUM_4, wrap("4", "4"));
		keyMap.put(Input.Keys.NUM_5, wrap("5", "5"));
		keyMap.put(Input.Keys.NUM_6, wrap("6", "6"));
		keyMap.put(Input.Keys.NUM_7, wrap("7", "7"));
		keyMap.put(Input.Keys.NUM_8, wrap("8", "8"));
		keyMap.put(Input.Keys.NUM_9, wrap("9", "9"));
		keyMap.put(Input.Keys.NUMPAD_0, wrap("0", "0"));
		keyMap.put(Input.Keys.NUMPAD_1, wrap("1", "1"));
		keyMap.put(Input.Keys.NUMPAD_2, wrap("2", "2"));
		keyMap.put(Input.Keys.NUMPAD_3, wrap("3", "3"));
		keyMap.put(Input.Keys.NUMPAD_4, wrap("4", "4"));
		keyMap.put(Input.Keys.NUMPAD_5, wrap("5", "5"));
		keyMap.put(Input.Keys.NUMPAD_6, wrap("6", "6"));
		keyMap.put(Input.Keys.NUMPAD_7, wrap("7", "7"));
		keyMap.put(Input.Keys.NUMPAD_8, wrap("8", "8"));
		keyMap.put(Input.Keys.NUMPAD_9, wrap("9", "9"));
		keyMap.put(Input.Keys.A, wrap("a", "A"));
		keyMap.put(Input.Keys.B, wrap("b", "B"));
		keyMap.put(Input.Keys.C, wrap("c", "C"));
		keyMap.put(Input.Keys.D, wrap("d", "D"));
		keyMap.put(Input.Keys.E, wrap("e", "E"));
		keyMap.put(Input.Keys.F, wrap("f", "F"));
		keyMap.put(Input.Keys.G, wrap("g", "G"));
		keyMap.put(Input.Keys.H, wrap("h", "H"));
		keyMap.put(Input.Keys.I, wrap("i", "I"));
		keyMap.put(Input.Keys.J, wrap("j", "J"));
		keyMap.put(Input.Keys.K, wrap("k", "K"));
		keyMap.put(Input.Keys.L, wrap("l", "L"));
		keyMap.put(Input.Keys.M, wrap("m", "M"));
		keyMap.put(Input.Keys.N, wrap("n", "N"));
		keyMap.put(Input.Keys.O, wrap("o", "O"));
		keyMap.put(Input.Keys.P, wrap("p", "P"));
		keyMap.put(Input.Keys.Q, wrap("q", "Q"));
		keyMap.put(Input.Keys.R, wrap("r", "R"));
		keyMap.put(Input.Keys.S, wrap("s", "S"));
		keyMap.put(Input.Keys.T, wrap("t", "T"));
		keyMap.put(Input.Keys.U, wrap("u", "U"));
		keyMap.put(Input.Keys.V, wrap("v", "V"));
		keyMap.put(Input.Keys.W, wrap("w", "W"));
		keyMap.put(Input.Keys.X, wrap("x", "X"));
		keyMap.put(Input.Keys.Y, wrap("y", "Y"));
		keyMap.put(Input.Keys.Z, wrap("z", "Z"));
		keyMap.put(Input.Keys.COMMA, wrap(",", ","));
		keyMap.put(Input.Keys.PERIOD, wrap(".", "."));
		keyMap.put(Input.Keys.SPACE, wrap(" ", " "));
		keyMap.put(Input.Keys.SEMICOLON, wrap(";", ":"));
	}
}