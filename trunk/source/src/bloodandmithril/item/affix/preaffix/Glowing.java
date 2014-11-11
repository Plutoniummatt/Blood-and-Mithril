package bloodandmithril.item.affix.preaffix;

import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.Copyright;
import bloodandmithril.graphics.particles.Particle.MovementMode;
import bloodandmithril.graphics.particles.TracerParticle;
import bloodandmithril.item.affix.Affix;
import bloodandmithril.item.affix.PreAffix;
import bloodandmithril.item.items.Item;
import bloodandmithril.util.Countdown;
import bloodandmithril.util.Util;
import bloodandmithril.world.Domain;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;

@Copyright("Matthew Peck 2014")
public class Glowing extends PreAffix {
	private static final long serialVersionUID = 1496504772959342155L;
	private Color color;
	
	
	/**
	 * Constructor
	 */
	public Glowing(Color color) {
		this.color = color;
	}
	

	@Override
	protected String getPreAffixDescription() {
		return "Glowing";
	}

	
	@Override
	public void itemEffects(Item item) {
		Domain.getWorld(item.getWorldId()).getParticles().add(new TracerParticle(
			item.getPosition().cpy().add(new Vector2(Util.getRandom().nextFloat() * 10f, 0f).rotate(Util.getRandom().nextFloat() * 360f)), 
			new Vector2(Util.getRandom().nextFloat() * 30f, 0f).rotate(Util.getRandom().nextFloat() * 360f), 
			color, 
			1f, 
			Domain.getActiveWorld().getWorldId(),
			new Countdown(Util.getRandom().nextInt(1000)),
			Util.getRandom().nextFloat() * 15f,
			MovementMode.EMBER
		));
	}
	
	
	@Override
	public void itemEffects(Individual individual, Item item) {
		try {
			Domain.getWorld(individual.getWorldId()).getParticles().add(new TracerParticle(
				item.getPosition().cpy().add(new Vector2(Util.getRandom().nextFloat() * 10f, 0f).rotate(Util.getRandom().nextFloat() * 360f)), 
				new Vector2(Util.getRandom().nextFloat() * 30f, 0f).rotate(Util.getRandom().nextFloat() * 360f), 
				color, 
				1f, 
				Domain.getActiveWorld().getWorldId(),
				new Countdown(Util.getRandom().nextInt(1000)),
				Util.getRandom().nextFloat() * 15f,
				MovementMode.EMBER
			));		
		} catch (NullPointerException e) {}
	}

	
	@Override
	public boolean isSameAs(Affix other) {
		return other instanceof Glowing && ((Glowing)other).color.equals(color);
	}
}