package bloodandmithril.control;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import bloodandmithril.core.Copyright;

import com.badlogic.gdx.Input;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * Maps keys to actions
 * Maps keyCodes to strings
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
public class Controls implements Serializable {
	private static final long serialVersionUID = -1052808676922281824L;

	public static final Map<Integer, String> keyMap = Maps.newHashMap();

	public MappedKey middleClick = new MappedKey("Middle click", 2);
	public MappedKey rightClick = new MappedKey("Right click", 1);
	public MappedKey leftClick = new MappedKey("Left click", 0);
	public MappedKey moveLeft = new MappedKey("Move left", Input.Keys.A);
	public MappedKey moveRight = new MappedKey("Move right", Input.Keys.D);
	public MappedKey walk = new MappedKey("Walk", Input.Keys.SHIFT_RIGHT);
	public MappedKey rightClickDragBox = new MappedKey("Bulk Loot", Input.Keys.X);
	public MappedKey contextMenuBypass = new MappedKey("Suppress Context Menus", Input.Keys.C);
	public MappedKey forceMove = new MappedKey("Force Move", Input.Keys.ALT_LEFT);
	public MappedKey jump = new MappedKey("Jump", Input.Keys.SPACE);
	public MappedKey toggleWalkRun = new MappedKey("Toggle Run/Walk", Input.Keys.W);
	public MappedKey addWayPoint = new MappedKey("Add Waypoint", Input.Keys.SHIFT_LEFT);
	public MappedKey selectIndividual = new MappedKey("Add to Selection", Input.Keys.SHIFT_LEFT);
	public MappedKey mineTile = new MappedKey("Mine/Dig", Input.Keys.Q);
	public MappedKey attack = new MappedKey("Attack Melee", Input.Keys.A);
	public MappedKey rangedAttack = new MappedKey("Attack Ranged", Input.Keys.CONTROL_LEFT);
	public MappedKey snapToGrid = new MappedKey("Placement Grid Alignment", Input.Keys.G);

	public static class MappedKey implements Serializable {
		private static final long serialVersionUID = -7721011148775681451L;
		public String description;
		public int keyCode;

		/**
		 * Constructor
		 */
		public MappedKey(String description, int keyCode) {
			this.description = description;
			this.keyCode = keyCode;
		}
	}

	public List<MappedKey> getFunctionalKeyMappings() {
		return Lists.newArrayList(
			rightClickDragBox,
			contextMenuBypass,
			forceMove,
			jump,
			toggleWalkRun,
			addWayPoint,
			selectIndividual,
			mineTile,
			attack,
			rangedAttack,
			snapToGrid
		);
	}

	public static void setup() {
		keyMap.put(Input.Keys.NUM_0, "0");
		keyMap.put(Input.Keys.NUM_1, "1");
		keyMap.put(Input.Keys.NUM_2, "2");
		keyMap.put(Input.Keys.NUM_3, "3");
		keyMap.put(Input.Keys.NUM_4, "4");
		keyMap.put(Input.Keys.NUM_5, "5");
		keyMap.put(Input.Keys.NUM_6, "6");
		keyMap.put(Input.Keys.NUM_7, "7");
		keyMap.put(Input.Keys.NUM_8, "8");
		keyMap.put(Input.Keys.NUM_9, "9");
		keyMap.put(Input.Keys.NUMPAD_0, "0");
		keyMap.put(Input.Keys.NUMPAD_1, "1");
		keyMap.put(Input.Keys.NUMPAD_2, "2");
		keyMap.put(Input.Keys.NUMPAD_3, "3");
		keyMap.put(Input.Keys.NUMPAD_4, "4");
		keyMap.put(Input.Keys.NUMPAD_5, "5");
		keyMap.put(Input.Keys.NUMPAD_6, "6");
		keyMap.put(Input.Keys.NUMPAD_7, "7");
		keyMap.put(Input.Keys.NUMPAD_8, "8");
		keyMap.put(Input.Keys.NUMPAD_9, "9");
		keyMap.put(Input.Keys.A, "a");
		keyMap.put(Input.Keys.B, "b");
		keyMap.put(Input.Keys.C, "c");
		keyMap.put(Input.Keys.D, "d");
		keyMap.put(Input.Keys.E, "e");
		keyMap.put(Input.Keys.F, "f");
		keyMap.put(Input.Keys.G, "g");
		keyMap.put(Input.Keys.H, "h");
		keyMap.put(Input.Keys.I, "i");
		keyMap.put(Input.Keys.J, "j");
		keyMap.put(Input.Keys.K, "k");
		keyMap.put(Input.Keys.L, "l");
		keyMap.put(Input.Keys.M, "m");
		keyMap.put(Input.Keys.N, "n");
		keyMap.put(Input.Keys.O, "o");
		keyMap.put(Input.Keys.P, "p");
		keyMap.put(Input.Keys.Q, "q");
		keyMap.put(Input.Keys.R, "r");
		keyMap.put(Input.Keys.S, "s");
		keyMap.put(Input.Keys.T, "t");
		keyMap.put(Input.Keys.U, "u");
		keyMap.put(Input.Keys.V, "v");
		keyMap.put(Input.Keys.W, "w");
		keyMap.put(Input.Keys.X, "x");
		keyMap.put(Input.Keys.Y, "y");
		keyMap.put(Input.Keys.Z, "z");
		keyMap.put(Input.Keys.COMMA, ",");
		keyMap.put(Input.Keys.PERIOD, ".");
		keyMap.put(Input.Keys.SPACE, " ");
	}
}