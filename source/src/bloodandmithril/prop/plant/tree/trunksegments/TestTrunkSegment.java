package bloodandmithril.prop.plant.tree.trunksegments;

import bloodandmithril.prop.plant.tree.Branch;
import bloodandmithril.prop.plant.tree.BranchPoint;
import bloodandmithril.prop.plant.tree.TrunkSegment;
import bloodandmithril.util.Util;
import bloodandmithril.world.Domain;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Vector2;

public class TestTrunkSegment extends TrunkSegment {

	public TestTrunkSegment(boolean grow) {
		super(0, 40);
		origin = new Vector2(0, 0);
		connectedTrunk = new Vector2(0, 40);
		direction = new Vector2(0, 1).rotate((Util.getRandom().nextFloat() - 0.5f) * 50f);
		if (grow) {
			connected = new TestTrunkSegment(Util.roll(0.8f));
		}
		
		branchPoints.add(
			new BranchPoint(
				new Vector2(0, 20), 
				new TestBranch()
			)
		);
	}


	@Override
	protected void renderSegment(Vector2 position) {
		Domain.shapeRenderer.begin(ShapeType.Line);
		Domain.shapeRenderer.setColor(new Color(0.2f, 0.1f, 0f, 1f));
		Gdx.gl20.glLineWidth(5f);
		Vector2 end = position.cpy().add(direction.cpy().mul(40));
		Domain.shapeRenderer.line(position.x - origin.x, position.y - origin.y, end.x, end.y);
		Domain.shapeRenderer.end();
	}
	
	
	public static class TestBranch extends Branch {
		public TestBranch() {
			super(0, (int) (Util.getRandom().nextFloat() * 50));
			direction = new Vector2(0, 1).rotate((Util.getRandom().nextFloat() - 0.5f) * 360);
			origin = new Vector2(0, 0);
			
			if (Util.roll(0.8f)) {
				branchPoints.add(
					new BranchPoint(
						new Vector2(0, Util.getRandom().nextFloat() * height), 
						new TestBranch()
					)
				);	
			}
		}


		@Override
		protected void renderBranch(Vector2 position) {
			Domain.shapeRenderer.begin(ShapeType.Line);
			Gdx.gl20.glLineWidth(2f);
			Domain.shapeRenderer.setColor(new Color(0.2f, 0.1f, 0f, 1f));
			Vector2 end = position.cpy().add(direction.cpy().mul(height));
			Domain.shapeRenderer.line(position.x - origin.x, position.y - origin.y, end.x, end.y);
			Domain.shapeRenderer.end();			
		}
	}
}