package bloodandmithril.prop.building;

import bloodandmithril.BloodAndMithrilClient;
import bloodandmithril.prop.Prop;
import bloodandmithril.world.GameWorld.Depth;

/**
 * A Construction
 *
 * @author Matt
 */
public abstract class Construction extends Prop {

	/** Dimensions of this {@link Construction} */
	protected final int width, height;

	/** The current stage of construction */
	//TODO - This should not be defaulted to complete
	private ConstructionStage constructionStage = ConstructionStage.COMPLETE;

	/**
	 * Constructor
	 */
	protected Construction(float x, float y, int width, int height, boolean grounded) {
		super(x, y, grounded, Depth.MIDDLEGROUND);
		this.width = width;
		this.height = height;
	}


	@Override
	public boolean isMouseOver() {
		float mx = BloodAndMithrilClient.getMouseWorldX();
		float my = BloodAndMithrilClient.getMouseWorldY();

		return mx > position.x - width/2 && mx < position.x + width/2 && my > position.y && my < position.y + height;
	}


	@Override
	public void render() {
		switch (constructionStage) {
			case PLANNED:
				renderPlanned();
				break;
			case SCAFFOLDED:
				renderScaffolded();
				break;
			case PHASE1:
				renderPhase1();
				break;
			case PHASE2:
				renderPhase2();
				break;
			case PHASE3:
				renderPhase3();
				break;
			case COMPLETE:
				renderComplete();
				break;

			default:
				break;
		}
	}


	@Override
	public boolean leftClick() {
		if (!isMouseOver()) {
			return false;
		}
		return true;
	}


	@Override
	public boolean rightClick() {
		if (!isMouseOver()) {
			return false;
		}
		return true;
	}


	/**
	 * See {@link #constructionStage}
	 */
	public ConstructionStage getConstructionStage() {
		return constructionStage;
	}


	/**
	 * See {@link #constructionStage}
	 */
	public void setConstructionStage(ConstructionStage constructionStage) {
		this.constructionStage = constructionStage;
	}


	/** Rendering */
	protected abstract void renderPlanned();

	/** Rendering */
	protected abstract void renderScaffolded();

	/** Rendering */
	protected abstract void renderPhase1();

	/** Rendering */
	protected abstract void renderPhase2();

	/** Rendering */
	protected abstract void renderPhase3();

	/** Rendering */
	protected abstract void renderComplete();


	/**
	 * Stages of construction
	 *
	 * @author Matt
	 */
	public static enum ConstructionStage {
		PLANNED, SCAFFOLDED, PHASE1, PHASE2, PHASE3, COMPLETE
	}
}