package bloodandmithril.prop.plant.tree.testtree;

import static bloodandmithril.util.Util.getRandom;
import static bloodandmithril.util.Util.randomOneOf;
import static bloodandmithril.util.datastructure.WrapperForFour.wrap;

import bloodandmithril.core.Copyright;
import bloodandmithril.prop.plant.tree.TreeSegment;
import bloodandmithril.util.Operator;

/**
 * @author Matt
 */
@Copyright("Matthew Peck 2016")
public class TestTreeBranchingFunction implements Operator<TreeSegment> {
	private static final long serialVersionUID = 5454225709811161271L;
	private int branchLength;
	
	/**
	 * Constructor
	 */
	public TestTreeBranchingFunction(int branchLength) {
		this.branchLength = branchLength;
	}


	@Override
	public void operate(TreeSegment branch) {
		if (this.branchLength > 5) {
			return;
		}
		
		TreeSegment extension = TestTree.segments.get(getRandom().nextInt(TestTree.segments.size())).call();
		extension.segmentHeight = branchLength;
		
		branch.addBranch(wrap(
			1f,
			(getRandom().nextFloat() - 0.5f) * 50,
			extension,
			new TestTreeBranchingFunction(branchLength + 1)
		));
		
		TreeSegment newBranch = TestTree.segments.get(getRandom().nextInt(TestTree.segments.size())).call();
		newBranch.segmentHeight = branchLength;
		
		branch.addBranch(wrap(
			0.7f, 
			randomOneOf(getRandom().nextFloat() * 20 + 40, -getRandom().nextFloat() * 20 - 40), 
			newBranch, 
			new TestTreeBranchingFunction(branchLength + 1)
		));
	}
}