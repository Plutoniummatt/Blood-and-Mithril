package core;

import org.junit.Test;

import bloodandmithril.util.datastructure.BinaryTree;
import bloodandmithril.world.topography.tile.Tile;
import bloodandmithril.world.topography.tile.Tile.Orientation;

public class TestBinaryTree {

	@Test
	public void test() {
		
		int[][] edgeOrientationArray = new int[][] {
			{1,2},
			{1,2},
			{1,2},
			{1,2},
			{1,2}
		};
		
		System.out.println(edgeOrientationArray[4][1]);
	}
	
	
	@Test
	public void test2() {
		BinaryTree<Orientation> tree = new BinaryTree<Tile.Orientation>(4,
			Orientation.SINGLE, Orientation.PETRUDING_LEFT, Orientation.PETRUDING_RIGHT, Orientation.HORIZONTAL,
			Orientation.PETRUDING_TOP, Orientation.TOP_LEFT, Orientation.TOP_RIGHT, Orientation.TOP_MIDDLE,
			Orientation.PETRUDING_BOTTOM, Orientation.BOTTOM_LEFT, Orientation.BOTTOM_RIGHT, Orientation.BOTTOM_MIDDLE,
			Orientation.VERTICAL, Orientation.LEFT, Orientation.RIGHT, Orientation.MIDDLE
		);
		
		tree.printAll();
	}
}