package bloodandmithril.graphics.renderers;

import static bloodandmithril.core.BloodAndMithrilClient.getGraphics;

import java.util.List;

import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.Copyright;
import bloodandmithril.graphics.Renderer;
import bloodandmithril.graphics.WorldRenderer;
import bloodandmithril.item.items.Item;
import bloodandmithril.item.items.equipment.Equipable;
import bloodandmithril.item.items.equipment.armor.Armor;
import bloodandmithril.item.items.equipment.offhand.OffhandEquipment;
import bloodandmithril.item.items.equipment.weapon.OneHandedMeleeWeapon;
import bloodandmithril.item.items.equipment.weapon.TwoHandedMeleeWeapon;
import bloodandmithril.item.items.equipment.weapon.Weapon;
import bloodandmithril.util.AnimationHelper.AnimationSwitcher;
import bloodandmithril.util.Shaders;
import bloodandmithril.util.SpacialConfiguration;
import bloodandmithril.util.datastructure.WrapperForTwo;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Vector2;

/**
 * Handles the rendering of {@link Individual}s
 *
 * @author mattp
 */
@Copyright("Matthew Peck 2015")
public class IndividualRenderer<T extends Individual> implements Renderer<T> {

	@Override
	public void internalRender(T individual) {
		int animationIndex = 0;

		// Draw the body, position is centre bottom of the frame
		List<WrapperForTwo<AnimationSwitcher, ShaderProgram>> currentAnimations = individual.getCurrentAnimation();
		if (currentAnimations == null) {
			return;
		}

		getGraphics().getSpriteBatch().begin();
		for (WrapperForTwo<AnimationSwitcher, ShaderProgram> animation : currentAnimations) {

			// Render equipped items
			renderCustomizations(individual, animationIndex);
			renderEquipment(individual, animationIndex);
			getGraphics().getSpriteBatch().flush();

			getGraphics().getSpriteBatch().setShader(animation.b);
			animation.b.setUniformMatrix("u_projTrans", getGraphics().getCam().combined);

			TextureRegion keyFrame = animation.a.getAnimation(individual).getKeyFrame(individual.getAnimationTimer(), true);
			getGraphics().getSpriteBatch().draw(
				keyFrame.getTexture(),
				individual.getState().position.x - keyFrame.getRegionWidth()/2,
				individual.getState().position.y,
				keyFrame.getRegionWidth(),
				keyFrame.getRegionHeight(),
				keyFrame.getRegionX(),
				keyFrame.getRegionY(),
				keyFrame.getRegionWidth(),
				keyFrame.getRegionHeight(),
				individual.getCurrentAction().left(),
				false
			);

			animationIndex++;
		}
		Gdx.gl.glDisable(GL20.GL_BLEND);

		getGraphics().getSpriteBatch().end();
		getGraphics().getSpriteBatch().flush();
	}


	private void renderEquipment(T individual, int animationIndex) {
		getGraphics().getSpriteBatch().flush();
		WorldRenderer.individualTexture.setFilter(TextureFilter.Linear, TextureFilter.Linear);
		for (Item equipped : individual.getEquipped().keySet()) {
			if (((Equipable)equipped).getRenderingIndex(individual) != animationIndex) {
				continue;
			}

			Equipable toRender = (Equipable) equipped;
			getGraphics().getSpriteBatch().setShader(Shaders.pass);
			if (equipped instanceof Weapon) {
				@SuppressWarnings({ "rawtypes", "unchecked" })
				WrapperForTwo<Animation, Vector2> attackAnimationEffects = ((Weapon) equipped).getAttackAnimationEffects(individual);

				if (equipped instanceof OneHandedMeleeWeapon) {
					SpacialConfiguration config = individual.getOneHandedWeaponSpatialConfigration();
					if (config != null) {
						Shaders.pass.setUniformMatrix("u_projTrans", getGraphics().getCam().combined);
						Vector2 pos = config.position.add(individual.getState().position);
						toRender.render(pos, config.orientation, config.flipX);
						if (config.flipX) {
							equipped.setPosition(pos.cpy().add(new Vector2(toRender.getTextureRegion().getRegionWidth()/2, 0).rotate(config.flipX ? config.orientation + 180f : -config.orientation)));
						} else {
							equipped.setPosition(pos.cpy().add(new Vector2(toRender.getTextureRegion().getRegionWidth()/2, 0).rotate(config.flipX ? -config.orientation : config.orientation)));
						}
					}
				} else if (equipped instanceof TwoHandedMeleeWeapon) {
					SpacialConfiguration config = individual.getTwoHandedWeaponSpatialConfigration();
					if (config != null) {
						Shaders.pass.setUniformMatrix("u_projTrans", getGraphics().getCam().combined);
						Vector2 pos = config.position.add(individual.getState().position);
						toRender.render(pos, config.orientation, config.flipX);
						if (config.flipX) {
							equipped.setPosition(pos.cpy().add(new Vector2(toRender.getTextureRegion().getRegionWidth()/2, 0).rotate(config.flipX ? config.orientation + 180f : -config.orientation)));
						} else {
							equipped.setPosition(pos.cpy().add(new Vector2(toRender.getTextureRegion().getRegionWidth()/2, 0).rotate(config.flipX ? -config.orientation : config.orientation)));
						}
					}
				}

				if (attackAnimationEffects != null) {
					TextureRegion keyFrame = attackAnimationEffects.a.getKeyFrame(individual.getAnimationTimer());
					getGraphics().getSpriteBatch().draw(
						keyFrame.getTexture(),
						individual.getState().position.x - keyFrame.getRegionWidth()/2 + (individual.getCurrentAction().left() ? - attackAnimationEffects.b.x : attackAnimationEffects.b.x),
						individual.getState().position.y + attackAnimationEffects.b.y,
						keyFrame.getRegionWidth(),
						keyFrame.getRegionHeight(),
						keyFrame.getRegionX(),
						keyFrame.getRegionY(),
						keyFrame.getRegionWidth(),
						keyFrame.getRegionHeight(),
						individual.getCurrentAction().left(),
						false
					);
				}
			} else if (equipped instanceof Armor) {

			} else if (equipped instanceof OffhandEquipment) {
				SpacialConfiguration config = individual.getOffHandSpatialConfigration();
				Shaders.pass.setUniformMatrix("u_projTrans", getGraphics().getCam().combined);
				Vector2 pos = config.position.add(individual.getState().position);
				((OffhandEquipment) equipped).render(pos, config.orientation, config.flipX);

				if (config.flipX) {
					equipped.setPosition(pos.cpy().add(new Vector2(toRender.getTextureRegion().getRegionWidth()/2, 0).rotate(config.flipX ? config.orientation + 180f : -config.orientation)));
				} else {
					equipped.setPosition(pos.cpy().add(new Vector2(toRender.getTextureRegion().getRegionWidth()/2, 0).rotate(config.flipX ? -config.orientation : config.orientation)));
				}
				toRender.particleEffects(pos, config.orientation, config.flipX);
			}
		}
		getGraphics().getSpriteBatch().flush();
		WorldRenderer.individualTexture.setFilter(TextureFilter.Nearest, TextureFilter.Nearest);
	}


	protected void renderCustomizations(T individual, int animationIndex) {}
}