package bloodandmithril.networking.requests;

import bloodandmithril.audio.SoundService;
import bloodandmithril.core.Copyright;
import bloodandmithril.networking.Response;

import com.badlogic.gdx.math.Vector2;

@Copyright("Matthew Peck 2014")
public class PlaySound implements Response {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6383519222619451664L;
	private final int client;
	private final int soundId;
	private final Vector2 location;

	public PlaySound(int client, int soundId, Vector2 location) {
		this.client = client;
		this.soundId = soundId;
		this.location = location;
	}


	public PlaySound(int soundId, Vector2 location) {
		this.client = -1;
		this.soundId = soundId;
		this.location = location;
	}


	@Override
	public void acknowledge() {
		SoundService.play(soundId, location, false, null);
	}


	@Override
	public int forClient() {
		return client;
	}


	@Override
	public void prepare() {
	}
}