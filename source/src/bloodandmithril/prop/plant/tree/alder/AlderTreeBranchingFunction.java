package bloodandmithril.prop.plant.tree.alder;

import static bloodandmithril.util.Util.getRandom;
import static bloodandmithril.util.Util.randomOneOf;
import static bloodandmithril.util.datastructure.WrapperForFour.wrap;

import bloodandmithril.core.Copyright;
import bloodandmithril.prop.plant.tree.TreeSegment;
import bloodandmithril.util.Operator;
import bloodandmithril.util.Util;

/**
 * @author Matt
 */
@Copyright("Matthew Peck 2016")
public class AlderTreeBranchingFunction implements Operator<TreeSegment> {
	private static final long serialVersionUID = 5454225709811161271L;
	private int branchLength;
	
	/**
	 * Constructor
	 */
	public AlderTreeBranchingFunction(int branchLength) {
		this.branchLength = branchLength;
	}


	@Override
	public void operate(TreeSegment branch) {
		if (this.branchLength > 5) {
			return;
		}
		
		TreeSegment extension = AlderTree.segments.get(getRandom().nextInt(AlderTree.segments.size())).call();
		extension.segmentHeight = branchLength;
		
		if (branch.segmentHeight > 3) {
			branch.setLeaves(AlderTree.leaves.get(Util.getRandom().nextInt(AlderTree.leaves.size())).call());
		}
		
		branch.addBranch(wrap(
			1f,
			(getRandom().nextFloat() - 0.5f) * 50,
			extension,
			new AlderTreeBranchingFunction(branchLength + 1)
		));
		
		TreeSegment newBranch = AlderTree.segments.get(getRandom().nextInt(AlderTree.segments.size())).call();
		newBranch.segmentHeight = branchLength;
		
		branch.addBranch(wrap(
			0.7f, 
			randomOneOf(getRandom().nextFloat() * 20 + 40, -getRandom().nextFloat() * 20 - 40), 
			newBranch, 
			new AlderTreeBranchingFunction(branchLength + 1)
		));
	}
}