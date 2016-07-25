package bloodandmithril.prop.plant.tree;

import java.util.List;

import com.google.common.collect.Lists;

import bloodandmithril.core.Copyright;
import bloodandmithril.util.Callable;
import bloodandmithril.util.Operator;
import bloodandmithril.util.datastructure.WrapperForThree;

/**
 * @author Matt
 */
@Copyright("Matthew Peck 2016")
public class TreeSegment {
	
	/** The trunk */
	TreeSegment trunk;
	
	/** Represents the position of the branch, the angle, and the branch itself */
	final List<WrapperForThree<Float, Float, TreeSegment>> branches = Lists.newLinkedList();

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
}