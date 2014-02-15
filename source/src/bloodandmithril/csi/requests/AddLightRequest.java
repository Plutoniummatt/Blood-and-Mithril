package bloodandmithril.csi.requests;

import java.util.LinkedList;

import com.badlogic.gdx.graphics.Color;

import bloodandmithril.csi.Request;
import bloodandmithril.csi.Response;
import bloodandmithril.csi.Response.Responses;
import bloodandmithril.persistence.ParameterPersistenceService;
import bloodandmithril.world.GameWorld;
import bloodandmithril.world.GameWorld.Light;

/**
 * {@link Request} to synchronize a {@link Light}
 *
 * @author Matt
 */
public class AddLightRequest implements Request {
	
	private int size;
	private float x;
	private float y;
	private Color color;
	private float intensity;

	/**
	 * Constructor
	 */
	public AddLightRequest(int size, float x, float y, Color color, float intensity) {
		this.size = size;
		this.x = x;
		this.y = y;
		this.color = color;
		this.intensity = intensity;
	}

	
	@Override
	public Responses respond() {
		int nextLightId = ParameterPersistenceService.getParameters().getNextLightId();
		GameWorld.lights.put(nextLightId, new Light(size, x, y, color, intensity));
		Responses responses = new Responses(false, new LinkedList<Response>());
		
		responses.responses.add(new SyncLightResponse(nextLightId, size, x, y, color, intensity));
		return responses;
	}
	

	@Override
	public boolean tcp() {
		return true;
	}

	
	@Override
	public boolean notifyOthers() {
		return true;
	}

	
	public static class SyncLightResponse implements Response {

		private int size;
		private float x;
		private float y;
		private Color color;
		private float intensity;
		private int id;
		
		/**
		 * Constructor
		 */
		public SyncLightResponse(int id, int size, float x, float y, Color color, float intensity) {
			this.id = id;
			this.size = size;
			this.x = x;
			this.y = y;
			this.color = color;
			this.intensity = intensity;
		}

		
		@Override
		public void acknowledge() {
			Light light = new Light(size, x, y, color, intensity);
			if (GameWorld.lights.replace(id, light) == null) {
				GameWorld.lights.put(id, light);
			}
		}
		

		@Override
		public int forClient() {
			return -1;
		}


		@Override
		public void prepare() {
		}
	}
	
	
	public static class RemoveLightNotification implements Response {
		
		private int lightId;

		/**
		 * Constructor
		 */
		public RemoveLightNotification(int lightId) {
			this.lightId = lightId;
		}
		

		@Override
		public void acknowledge() {
			GameWorld.lights.remove(lightId);
		}

		
		@Override
		public int forClient() {
			return -1;
		}

		
		@Override
		public void prepare() {
		}
	}
}