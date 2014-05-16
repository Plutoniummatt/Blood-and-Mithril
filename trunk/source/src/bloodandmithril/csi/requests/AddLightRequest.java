package bloodandmithril.csi.requests;

import com.badlogic.gdx.graphics.Color;

import bloodandmithril.csi.Request;
import bloodandmithril.csi.Response;
import bloodandmithril.csi.Response.Responses;
import bloodandmithril.graphics.Light;
import bloodandmithril.persistence.ParameterPersistenceService;
import bloodandmithril.world.Domain;

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
	private float intensity, spanBegin, spanEnd;

	/**
	 * Constructor
	 */
	public AddLightRequest(int size, float x, float y, Color color, float intensity, float spanBegin, float spanEnd) {
		this.size = size;
		this.x = x;
		this.y = y;
		this.color = color;
		this.intensity = intensity;
		this.spanBegin = spanBegin;
		this.spanEnd = spanEnd;
	}

	
	@Override
	public Responses respond() {
		int nextLightId = ParameterPersistenceService.getParameters().getNextLightId();
		Domain.getLights().put(nextLightId, new Light(size, x, y, color, intensity, spanBegin, spanEnd));
		Responses responses = new Responses(false);
		
		responses.add(new SyncLightResponse(nextLightId, size, x, y, color, intensity, spanBegin, spanEnd));
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
		private float x, y, spanBegin, spanEnd;
		private Color color;
		private float intensity;
		private int id;
		
		/**
		 * Constructor
		 */
		public SyncLightResponse(int id, int size, float x, float y, Color color, float intensity, float spanBegin, float spanEnd) {
			this.id = id;
			this.size = size;
			this.x = x;
			this.y = y;
			this.color = color;
			this.intensity = intensity;
			this.spanBegin = spanBegin;
			this.spanEnd = spanEnd;
		}

		
		@Override
		public void acknowledge() {
			Light light = new Light(size, x, y, color, intensity, spanBegin, spanEnd);
			if (Domain.getLights().get(id) == null) {
				Domain.getLights().put(id, light);
			} else {
				Domain.getLights().get(id).color = light.color;
				Domain.getLights().get(id).size = light.size;
				Domain.getLights().get(id).x = light.x;
				Domain.getLights().get(id).y = light.y;
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
			Domain.getLights().remove(lightId);
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