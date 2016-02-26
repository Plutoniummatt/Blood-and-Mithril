package bloodandmithril.world.weather;

import java.util.List;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.google.common.collect.Lists;

import bloodandmithril.util.SerializableColor;

public final class Cloud {

	private List<CloudSegment> segments = Lists.newLinkedList();
	private Vector2 position;
	
	/**
	 * Constructor
	 */
	public Cloud(Vector2 position) {
		this.position = position;
	}
	
	
	/**
	 * Adds a {@link CloudSegment} to this {@link Cloud}
	 * 
	 * @return this, for chaining
	 */
	public Cloud segment(CloudSegment segment) {
		this.segments.add(segment);
		return this;
	}
	
	
	/**
	 * Renders the cloud
	 */
	public void render(ShapeRenderer renderer, Vector2 camLocation) {
		renderer.setColor(Color.WHITE);
		for (CloudSegment segment : segments) {
			renderer.circle(
				0f, 
				3000f, 
				20000f
			);
		}
	}
	
	
	/**
	 * A cloud segment making up this cloud
	 * 
	 * @author Matt
	 */
	public static class CloudSegment {
		/**
		 * Radius of this segment
		 */
		public float radius;
		
		/**
		 * Relative position of this segment to the position of the cloud cluster
		 */
		public Vector2 relativePosition;
		
		/**
		 * Relative distance from the cloud cluster, 0 meaning the depth of the cluster, -1 being infinity, 1 being on par with foreground
		 */
		public float distance;
		
		/**
		 * Color of the segment
		 */
		public SerializableColor color;
		
		/**
		 * Amount of time before this cloud disperses, in seconds
		 */
		public float lifeTime;
		
		/**
		 * Amount of time it takes this cloud to fade in/out
		 */
		public float decayTime;
		
		/**
		 * Constructor
		 */
		public CloudSegment(float radius, Vector2 relativePosition, float distance, Color color, float lifeTime, float decayTime)  {
			this.radius = radius;
			this.relativePosition = relativePosition;
			this.distance = distance;
			this.lifeTime = lifeTime;
			this.decayTime = decayTime;
			this.color = new SerializableColor(color);
		}
	}
}
