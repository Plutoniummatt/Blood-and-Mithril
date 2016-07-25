package bloodandmithril.prop.plant.tree;

import java.util.List;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.google.common.collect.Lists;

import bloodandmithril.core.Copyright;
import bloodandmithril.graphics.Graphics;
import bloodandmithril.graphics.Textures;
import bloodandmithril.util.Callable;
import bloodandmithril.util.Operator;
import bloodandmithril.util.datastructure.WrapperForThree;

/**
 * @author Matt
 */
@Copyright("Matthew Peck 2016")
public class TreeSegment {
	
	/** The trunk */
	private TreeSegment trunk;
	
	/** Represents the position of the branch, the angle, and the branch itself */
	private final List<WrapperForThree<Float, Float, TreeSegment>> branches = Lists.newLinkedList();

	public final int textureId;
	public final int width;
	public final int height;
	public int segmentHeight;
	
	/**
	 * Constructor
	 */
	public TreeSegment(int textureId, int width, int height) {
		this.textureId = textureId;
		this.width = width;
		this.height = height;
	}

	
	public List<WrapperForThree<Float, Float, TreeSegment>> getBranches() {
		return branches;
	}


	public TreeSegment getTrunk() {
		return trunk;
	}
	
	
	/**
	 * @return the height of the trunk
	 */
	public int getTrunkHeight() {
		return 1 + (trunk == null ? 0 : trunk.getTrunkHeight());
	}
	
	
	/**
	 * @param trunkSegmentGenerator
	 * @param branchGenerator
	 * @param number of trunks to add
	 * 
	 * @return The top trunk segment
	 */
	public TreeSegment generateTree(Callable<TreeSegment> trunkSegmentGenerator, Operator<TreeSegment> branchGenerator, int number) {
		TreeSegment previous = this;
		TreeSegment current = this;
		branchGenerator.operate(this);
		this.segmentHeight = 1;
		
		for (int i = 0; i < number; i++) {
			current.trunk = trunkSegmentGenerator.call();
			current = current.trunk;
			current.segmentHeight = 1 + previous.segmentHeight;
			branchGenerator.operate(current);
			previous = current;
		}
		
		return current;
	}


	public void render(
		Graphics graphics, 
		Vector2 renderPosition, 
		float angle, 
		float thinningFactor, 
		float thinningFactorStep, 
		float curvature, 
		int overlap, 
		Class<? extends Tree> treeClass, 
		int treeWidth
	) {
		if (trunk != null) {
			trunk.render(
				graphics, 
				renderPosition.cpy().add(new Vector2(0, this.height - overlap).rotate(angle)),
				angle + curvature, 
				thinningFactor - thinningFactorStep, 
				thinningFactorStep, 
				curvature, 
				overlap, 
				treeClass,
				treeWidth
			);
		}
		
		for (WrapperForThree<Float, Float, TreeSegment> branch : branches) {
			branch.c.render(
				graphics, 
				renderPosition.cpy().add(new Vector2(0, branch.a * width - overlap).rotate(angle)), 
				angle + branch.b, 
				thinningFactor * 0.35f, 
				0f, 
				0f, 
				overlap, 
				treeClass,
				treeWidth
			);
		}
		
		TextureRegion textureRegion = Textures.trunkTextures.get(treeClass).get(textureId);
		graphics.getSpriteBatch().draw(
			textureRegion,
			renderPosition.x, 
			renderPosition.y,
			width/2,
			0f,
			textureRegion.getRegionWidth(),
			textureRegion.getRegionHeight(),
			thinningFactor,
			1f,
			angle
		);
	}
}