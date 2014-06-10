package bloodandmithril.persistence;

import java.io.Serializable;

/**
 * Config class
 *
 * @author Matt
 */
public class Config implements Serializable {
	private static final long serialVersionUID = -1737356527689517262L;

	private int resX = 800, resY = 600;
	private boolean fullScreen = false, vSync = true;

	/**
	 * @return Screen width
	 */
	public int getResX() {
		return resX;
	}


	public void setResX(int resX) {
		this.resX = resX;
	}


	/**
	 * @return Screen height
	 */
	public int getResY() {
		return resY;
	}


	public void setResY(int resY) {
		this.resY = resY;
	}


	/**
	 * @return Whether to run the game in full screen mode
	 */
	public boolean isFullScreen() {
		return fullScreen;
	}


	public void setFullScreen(boolean fullScreen) {
		this.fullScreen = fullScreen;
	}


	/**
	 * @return whether vSync is enabled
	 */
	public boolean isvSync() {
		return vSync;
	}


	public void setvSync(boolean vSync) {
		this.vSync = vSync;
	}
}