package bloodandmithril.control.keydown;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import bloodandmithril.control.Controls;
import bloodandmithril.control.KeyPressedHandler;
import bloodandmithril.core.Copyright;
import bloodandmithril.core.Threading;

/**
 * Controls game speed
 *
 * @author Matt
 */
@Singleton
@Copyright("Matthew Peck 2016")
public class GameSpeedControlKeyPressedHandler implements KeyPressedHandler {

	@Inject
	private Controls controls;

	@Inject
	private Threading threading;

	@Override
	public boolean handle(final int keycode) {
		if (keycode == controls.speedUp.keyCode) {
			if (threading.getUpdateRate() < 16f) {
				threading.setUpdateRate(Math.round(threading.getUpdateRate()) + 1);
			}
		}
		if (keycode == controls.slowDown.keyCode) {
			if (threading.getUpdateRate() > 1f) {
				threading.setUpdateRate(Math.round(threading.getUpdateRate()) - 1);
			}
		}

		return false;
	}
}