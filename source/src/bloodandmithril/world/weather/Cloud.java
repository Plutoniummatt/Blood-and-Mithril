package bloodandmithril.world.weather;

import static java.lang.Math.abs;

import java.io.Serializable;
import java.util.List;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.google.common.collect.Lists;

import bloodandmithril.graphics.WorldRenderer;
import bloodandmithril.graphics.background.Layer;
import bloodandmithril.networking.ClientServerInterface;
import bloodandmithril.util.SerializableColor;
import bloodandmithril.util.Shaders;
import bloodandmithril.util.Util;
import bloodandmithril.world.Domain;

public final class Cloud implements Serializable {
	private static final long serialVersionUID = 7809733656698481649L;
	private List<CloudSegment> segments = Lists.newLinkedList();
	private Vector2 position;
	private static TextureRegion circle;
	
	static {
		if (ClientServerInterface.isClient()) {
			circle = new TextureRegion(WorldRenderer.gameWorldTexture, 102, 422, 100, 100);
		}
	}
	
	/**
	 * Constructor
	 */
	public Cloud(Vector2 position) {
		this.position = position;
	}
	
	
	/**
	 * Constructor
	 */
	public Cloud(Vector2 position, int smallSegments, float minRadius, int largeSegments, float maxRadius, float spreadX, float spreadZ) {
		this.position = position;
		
		for (int i = 0; i < smallSegments; i++) {
			float radius = minRadius + Util.getRandom().nextFloat() * (maxRadius - minRadius) / 2;
			float x = (Util.getRandom().nextFloat() - 0.5f) * spreadX;
			float y = (Util.getRandom().nextFloat() - 0.5f) * spreadX / 20;
			
			segment(
				new CloudSegment(
					radius * (1f - (abs(x) / abs(position.x - spreadX/2))), 
					new Vector2(
						x,
						y + radius * (1f - (abs(x) / abs(position.x - spreadX/2)))
					), 
					(Util.getRandom().nextFloat() - 0.5f) * spreadZ, 
					Color.WHITE, 
					99999f, 
					60f, 
					0.2f
				)
			);
		}
		
		for (int i = 0; i < largeSegments; i++) {
			float radius = minRadius + Util.getRandom().nextFloat() * (maxRadius - minRadius);
			float x = (Util.getRandom().nextFloat() - 0.5f) * spreadX;
			float y = (Util.getRandom().nextFloat() - 0.5f) * spreadX / 5;
			
			segment(
				new CloudSegment(
					radius * (1f - (abs(x) / abs(position.x - spreadX/2))), 
					new Vector2(
						x,
						y + radius * (1f - (abs(x) / abs(position.x - spreadX/2)))
					), 
					(Util.getRandom().nextFloat() - 0.5f) * spreadZ, 
					Color.WHITE, 
					99999f, 
					60f, 
					0.2f
				)
			);
		}
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
	 * 
	 * Renders the cloud
	 */
	public void render(SpriteBatch spriteBatch, Vector3 camLocation) {
		for (CloudSegment segment : segments) {
			Shaders.transparentPass.setUniformf("feather", segment.feather);
			Shaders.transparentPass.setUniformf("topLeft", circle.getU(), circle.getV());
			Shaders.transparentPass.setUniformf("bottomRight", circle.getU2(), circle.getV2());
			
			Color daylightColor = WeatherRenderer.getDaylightColor(Domain.getActiveWorld());
			Shaders.transparentPass.setUniformf("override", daylightColor.r, daylightColor.g, daylightColor.b, 0.04f);
			
			spriteBatch.draw(
				circle, 
				-camLocation.x * 0.01f + position.x - segment.radius + segment.relativePosition.x + (camLocation.x * 0.01f - position.x) * segment.distance * 2f, 
				-camLocation.y * 0.01f + Layer.getScreenHorizonY() + position.y - segment.radius + segment.relativePosition.y + (camLocation.y * 0.01f - position.y) * segment.distance * 2f,
				segment.radius * 2,
				segment.radius * 2
			);
		}
	}
	
	
	/**
	 * Updates this {@link Cloud} instance
	 */
	public void update(float updateTick) {
		position.x = position.x + 10f * updateTick;
	}
	
	
	public Vector2 getPosition() {
		return position;
	}
	
	
	/**
	 * A cloud segment making up this cloud
	 * 
	 * @author Matt
	 */
	public static class CloudSegment implements Serializable {
		private static final long serialVersionUID = 9139096153986022105L;

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
		 * Amount of feathering on the edges
		 */
		public float feather;
		
		/**
		 * Constructor
		 */
		public CloudSegment(float radius, Vector2 relativePosition, float distance, Color color, float lifeTime, float decayTime, float feather)  {
			this.radius = radius;
			this.relativePosition = relativePosition;
			this.distance = distance;
			this.lifeTime = lifeTime;
			this.decayTime = decayTime;
			this.feather = feather;
			this.color = new SerializableColor(color);
		}
	}
}
