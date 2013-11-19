package bloodandmithril.ui;

import java.util.Map;

import com.badlogic.gdx.Input;
import com.google.common.collect.Maps;

/**
 * Maps keys to actions
 * Maps keyCodes to strings
 *
 * @author Matt
 */
public class KeyMappings {

	public static final Map<Integer, String> keyMap = Maps.newHashMap();

	public static int middleClick = 2;
	public static int rightClick = 1;
	public static int leftClick = 0;
	public static int cameraDrag = Input.Keys.CONTROL_LEFT;
	public static int moveLeft = Input.Keys.A;
	public static int moveRight = Input.Keys.D;
	public static int walk = Input.Keys.SHIFT_RIGHT;
	public static int contextMenuBypass = Input.Keys.SHIFT_LEFT;
	public static int forceMove = Input.Keys.ALT_LEFT;

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
		keyMap.put(Input.Keys.J, "y");
		keyMap.put(Input.Keys.Z, "z");
		keyMap.put(Input.Keys.COMMA, ",");
		keyMap.put(Input.Keys.PERIOD, ".");
	}
}