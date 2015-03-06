package bloodandmithril.generation.component.interfaces;

import bloodandmithril.core.BloodAndMithrilClient;
import bloodandmithril.core.Copyright;
import bloodandmithril.generation.component.Component;
import bloodandmithril.generation.component.Component.ComponentCreationCustomization;
import bloodandmithril.generation.component.components.Corridor;
import bloodandmithril.generation.component.components.Corridor.CorridorCreationCustomization;
import bloodandmithril.generation.component.components.Room;
import bloodandmithril.generation.component.components.Room.RoomCreationCustomization;
import bloodandmithril.generation.component.components.Stairs;
import bloodandmithril.generation.component.components.Stairs.StairsCreationCustomization;
import bloodandmithril.ui.UserInterface;
import bloodandmithril.util.Logger;
import bloodandmithril.util.Logger.LogLevel;
import bloodandmithril.util.datastructure.Boundaries;
import bloodandmithril.world.topography.Topography;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;

/**
 * A simple rectangular interface, edges are inclusive
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
public class RectangularInterface extends Interface {
	private static final long serialVersionUID = -2025423443070618814L;

	/** The boundaries that defines this {@link RectangularInterface} */
	public final Boundaries boundaries;

	/**
	 * Constructor
	 */
	public RectangularInterface(Boundaries boundaries) {
		this.boundaries = boundaries;
	}


	@Override
	public <T extends Component> Component createComponent(Class<T> type, ComponentCreationCustomization<T> custom, int structureKey) {
		if (type.equals(Corridor.class)) {
			return createCorridor(custom, structureKey);
		}

		if (type.equals(Room.class)) {
			return createRoom(custom, structureKey);
		}

		if (type.equals(Stairs.class)) {
			return createStairs(custom, structureKey);
		}

		return null;
	}


	/**
	 * Create {@link Stairs} from this {@link RectangularInterface}
	 */
	@SuppressWarnings("rawtypes")
	private Component createStairs(ComponentCreationCustomization custom, int structureKey) {
		StairsCreationCustomization customization = (StairsCreationCustomization) custom;

		int slopeConstant;
		if (customization.stemRight == customization.slopeGradient > 0) {
			slopeConstant = boundaries.bottom - (int)customization.slopeGradient * (customization.slopeGradient > 0 ? boundaries.right : boundaries.left) - (customization.stemRight == customization.slopeGradient > 0 ? 0 : 1) - 1;
		} else {
			slopeConstant = boundaries.bottom - (int)customization.slopeGradient * (customization.slopeGradient > 0 ? boundaries.right : boundaries.left) - (customization.stemRight == customization.slopeGradient > 0 ? 0 : 1);
		}

		int top;
		int bottom;
		int left;
		int right;

		// If stemming from vertical interface
		if (boundaries.left == boundaries.right) {
			// Opposite interface is vertical
			if (customization.isOppositeInterfaceVertical) {
				// If gradient is positive in the stemming direction
				if (customization.stemRight == customization.slopeGradient > 0) {
					Logger.generationDebug("Stemming stairs with positive gradient in direction of stem from vertical interface into vertical interface", LogLevel.DEBUG);
					top = boundaries.bottom + Math.abs(Math.round(customization.length * customization.slopeGradient)) + customization.corridorHeight + customization.borderThickness - 1;
					bottom = boundaries.bottom - customization.borderThickness - 1;
					left = customization.stemRight ? boundaries.left : boundaries.right - customization.length;
					right = customization.stemRight ? boundaries.left + customization.length : boundaries.right;
				} else {
					Logger.generationDebug("Stemming stairs with negative gradient in direction of stem from vertical interface into vertical interface", LogLevel.DEBUG);
					top = boundaries.top + customization.borderThickness;
					bottom = boundaries.top - Math.abs(Math.round(customization.length * customization.slopeGradient)) - customization.corridorHeight - customization.borderThickness;
					left = customization.stemRight ? boundaries.left : boundaries.right - customization.length;
					right = customization.stemRight ? boundaries.left + customization.length : boundaries.right;
				}

			// Opposite interface is horizontal
			} else {
				// If gradient is positive in the stemming direction
				if (customization.stemRight == customization.slopeGradient > 0) {
					Logger.generationDebug("Stemming stairs with positive gradient in direction of stem from vertical interface into horizontal interface", LogLevel.DEBUG);
					top = boundaries.bottom + Math.abs(Math.round(customization.length * customization.slopeGradient));
					bottom = boundaries.bottom - 1 - customization.borderThickness;
					left = customization.stemRight ? boundaries.left : boundaries.right - customization.length - customization.borderThickness - 1;
					right = customization.stemRight ? boundaries.left + customization.length + customization.borderThickness + 1 : boundaries.right;
				} else {
					Logger.generationDebug("Stemming stairs with negative gradient in direction of stem from vertical interface into horizontal interface", LogLevel.DEBUG);
					top = boundaries.top + customization.borderThickness;
					bottom = boundaries.top - Math.abs(Math.round(customization.length * customization.slopeGradient));
					left = customization.stemRight ? boundaries.left : boundaries.right - customization.length - customization.borderThickness;
					right = customization.stemRight ? boundaries.left + customization.length + customization.borderThickness : boundaries.right;
				}
			}
		// else we're stemming from horizontal interface
		} else {
			// Opposite interface is vertical
			if (customization.isOppositeInterfaceVertical) {
				// If gradient is positive in the stemming direction
				if (customization.stemRight == customization.slopeGradient > 0) {
					Logger.generationDebug("Stemming stairs with positive gradient in direction of stem from horizontal interface into vertical interface", LogLevel.DEBUG);
					top = boundaries.bottom + Math.abs(Math.round(customization.length * customization.slopeGradient)) + customization.corridorHeight + customization.borderThickness * 2;
					bottom = boundaries.bottom;
					left = customization.stemRight ? boundaries.left - customization.borderThickness : boundaries.right - customization.length - customization.corridorHeight - customization.borderThickness;
					right = customization.stemRight ? boundaries.left + customization.length + customization.corridorHeight + customization.borderThickness : boundaries.right + customization.borderThickness - 1;
				} else {
					Logger.generationDebug("Stemming stairs with negative gradient in direction of stem from horizontal interface into vertical interface", LogLevel.DEBUG);
					top = boundaries.top;
					bottom = boundaries.top - Math.abs(Math.round(customization.length * customization.slopeGradient)) - customization.corridorHeight - customization.borderThickness * 2 - 1;
					left = customization.stemRight ? boundaries.left - customization.borderThickness - 1 : boundaries.right - customization.length - customization.corridorHeight - customization.borderThickness;
					right = customization.stemRight ? boundaries.left + customization.length + customization.corridorHeight + customization.borderThickness : boundaries.right + customization.borderThickness + 1;
				}

			// Opposite interface is horizontal
			} else {
				// If gradient is positive in the stemming direction
				if (customization.stemRight == customization.slopeGradient > 0) {
					Logger.generationDebug("Stemming stairs with positive gradient in direction of stem from horizontal interface into horizontal interface", LogLevel.DEBUG);
					top = boundaries.bottom + Math.abs(Math.round(customization.length * customization.slopeGradient));
					bottom = boundaries.bottom;
					left = customization.stemRight ? boundaries.left - customization.borderThickness : boundaries.right - customization.length - customization.corridorHeight - customization.borderThickness;
					right = customization.stemRight ? boundaries.left + customization.length + customization.corridorHeight + customization.borderThickness : boundaries.right + customization.borderThickness;
				} else {
					Logger.generationDebug("Stemming stairs with negative gradient in direction of stem from horizontal interface into horizontal interface", LogLevel.DEBUG);
					top = boundaries.top;
					bottom = boundaries.top - Math.abs(Math.round(customization.length * customization.slopeGradient));
					left = customization.stemRight ? boundaries.left - customization.borderThickness - 1 : boundaries.right - customization.length - customization.corridorHeight - customization.borderThickness + 1;
					right = customization.stemRight ? boundaries.left + customization.length + customization.corridorHeight + customization.borderThickness - 1 : boundaries.right + customization.borderThickness + 1;
				}
			}
		}

		return new Stairs(
			customization.slopeGradient,
			slopeConstant,
			customization.corridorHeight,
			customization.borderThickness,
			new Boundaries(
				top,
				bottom,
				left,
				right
			),
			customization.tileType,
			customization.stairType,
			structureKey
		);
	}


	/**
	 * Create a {@link Room} from this {@link RectangularInterface}
	 */
	@SuppressWarnings("rawtypes")
	private Component createRoom(ComponentCreationCustomization custom, int structureKey) {
		RoomCreationCustomization customization = (RoomCreationCustomization) custom;

		int wallThickness = customization.wallThickness - 1;
		return new Room(
			new Boundaries(
				boundaries.top + customization.height + wallThickness,
				boundaries.bottom - 1 - wallThickness,
				(customization.stemRight ? boundaries.left - 1: boundaries.left - customization.height - 1) - wallThickness,
				(customization.stemRight ? boundaries.right + customization.height + 1: boundaries.right + 1) + wallThickness
			),
			new Boundaries(
				boundaries.top + customization.height - 1,
				boundaries.bottom,
				customization.stemRight ? boundaries.left : boundaries.left - customization.height,
				customization.stemRight ? boundaries.right + customization.height : boundaries.right
			),
			structureKey
		);
	}


	/**
	 * Create a {@link Corridor} from this {@link RectangularInterface}
	 */
	@SuppressWarnings("rawtypes")
	private Component createCorridor(ComponentCreationCustomization custom, int structureKey) {
		CorridorCreationCustomization customization = (CorridorCreationCustomization) custom;

		return new Corridor(
			new Boundaries(
				boundaries.top,
				boundaries.bottom,
				customization.stemRight ? boundaries.right : boundaries.left - customization.length,
				customization.stemRight ? boundaries.right + customization.length : boundaries.left
			),
			customization.ceilingThickness,
			customization.floorThickness,
			customization.tileType,
			structureKey
		);
	}


	@Override
	public Interface createConnectedInterface(InterfaceCustomization customization) {
		if (!(customization instanceof RectangularInterfaceCustomization)) {
			throw new RuntimeException("Can not customize " + this.getClass().getSimpleName() + " with " + customization.getClass().getSimpleName());
		} else {

			RectangularInterfaceCustomization custom = (RectangularInterfaceCustomization) customization;

			int topAttempt = boundaries.bottom + custom.verticalOffset + custom.verticalConstraint;
			int top = topAttempt > boundaries.top ? boundaries.top : topAttempt;

			int bottom = boundaries.bottom + custom.verticalOffset;

			int left = boundaries.left + custom.horizontalOffset;

			int rightAttempt = boundaries.left + custom.horizontalOffset + custom.horizontalConstraint;
			int right = rightAttempt > boundaries.right ? boundaries.right : rightAttempt;

			return new RectangularInterface(
				new Boundaries(
					top,
					bottom,
					left,
					right
				)
			);
		}
	}


	/**
	 * @return The width of this {@link RectangularInterface}
	 */
	public int getWidth() {
		return boundaries.right - boundaries.left + 1;
	}


	/**
	 * @return The height of this {@link RectangularInterface}
	 */
	public int getHeight() {
		return boundaries.top - boundaries.bottom + 1;
	}


	@Override
	public void render(Color color) {
		UserInterface.shapeRenderer.begin(ShapeType.Line);
		UserInterface.shapeRenderer.setColor(color);

		UserInterface.shapeRenderer.rect(
			BloodAndMithrilClient.worldToScreenX(boundaries.left * Topography.TILE_SIZE),
			BloodAndMithrilClient.worldToScreenY(boundaries.bottom * Topography.TILE_SIZE),
			(boundaries.right - boundaries.left + 1) * Topography.TILE_SIZE,
			(boundaries.top - boundaries.bottom + 1) * Topography.TILE_SIZE
		);

		UserInterface.shapeRenderer.end();
	}


	/**
	 * {@link InterfaceCustomization} specific to {@link RectangularInterface}
	 *
	 * @author Matt
	 */
	public static class RectangularInterfaceCustomization extends InterfaceCustomization {

		/** Customization values */
		public int verticalConstraint, horizontalConstraint, verticalOffset, horizontalOffset;

		/**
		 * Constructor
		 */
		public RectangularInterfaceCustomization(int verticalConstraint, int horizontalConstraint, int verticalOffset, int horizontalOffset) {
			this.verticalConstraint = verticalConstraint;
			this.horizontalConstraint = horizontalConstraint;
			this.verticalOffset = verticalOffset;
			this.horizontalOffset = horizontalOffset;
		}
	}
}