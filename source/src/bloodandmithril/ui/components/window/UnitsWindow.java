package bloodandmithril.ui.components.window;

import static bloodandmithril.core.BloodAndMithrilClient.HEIGHT;
import static bloodandmithril.core.BloodAndMithrilClient.WIDTH;

import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.BloodAndMithrilClient;
import bloodandmithril.core.Copyright;
import bloodandmithril.ui.Refreshable;
import bloodandmithril.ui.UserInterface.UIRef;
import bloodandmithril.ui.components.Button;
import bloodandmithril.ui.components.Component;
import bloodandmithril.ui.components.ContextMenu;
import bloodandmithril.ui.components.panel.ScrollableListingPanel;
import bloodandmithril.util.Fonts;
import bloodandmithril.util.Shaders;
import bloodandmithril.util.Util;
import bloodandmithril.util.Util.Colors;
import bloodandmithril.world.Domain;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.google.common.collect.Maps;

/**
 * Window to display all {@link Individual}s that belong to factions under control
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
public class UnitsWindow extends Window implements Refreshable {

	private ScrollableListingPanel<Individual, String> listing;
	private final HashMap<ScrollableListingPanel.ListingMenuItem<Individual>, String> individuals = Maps.newHashMap();
	private final int factionId;

	/**
	 * Constructor
	 */
	public UnitsWindow(int factoinId) {
		super(WIDTH/2 - 250, HEIGHT/2 + 200, 500, 400, Domain.getFactions().get(factoinId).name + " - Members", true, 500, 400, true, true, true);
		factionId = factoinId;
		refresh();
	}


	/**
	 * Renders the separator
	 */
	private void renderSeparator() {
		BloodAndMithrilClient.spriteBatch.setShader(Shaders.filter);
		shapeRenderer.begin(ShapeType.Filled);
		Color color = isActive() ? Colors.modulateAlpha(borderColor, getAlpha()) : Colors.modulateAlpha(borderColor, 0.4f * getAlpha());
		shapeRenderer.rect(x + width - 130, y + 24 - height, 2, height - 45, Color.CLEAR, Color.CLEAR, color, color);
		shapeRenderer.end();
	}


	private void populateList() {
		Domain.getIndividuals().values().forEach(individual -> {
			if (individual.getFactionId() == this.factionId) {
				this.individuals.put(
					new ScrollableListingPanel.ListingMenuItem<Individual>(
						individual,
						new Button(
							individual.getId().getSimpleName(),
							Fonts.defaultFont,
							0,
							0,
							individual.getId().getSimpleName().length() * 9,
							16,
							() -> {},
							Color.YELLOW,
							Color.WHITE,
							Color.GREEN,
							UIRef.BR
						),
						individual.getContextMenu().addFirst(
							new ContextMenu.MenuItem(
								"Go to",
								() -> {
									BloodAndMithrilClient.cam.position.x = individual.getState().position.x;
									BloodAndMithrilClient.cam.position.y = individual.getState().position.y;
								},
								Color.ORANGE,
								Color.WHITE,
								Color.ORANGE,
								null
							)
						)
					),
					""
				);
			}
		});
	}


	@Override
	protected void internalWindowRender() {
		listing.x = x;
		listing.y = y;
		listing.width = width;
		listing.height = height;
		renderSeparator();
		listing.render();
	}


	@Override
	protected void internalLeftClick(List<ContextMenu> copy, Deque<Component> windowsCopy) {
		listing.leftClick(copy, windowsCopy);
	}


	@Override
	public boolean scrolled(int amount) {
		return listing.scrolled(amount);
	}


	@Override
	protected void uponClose() {
	}


	@Override
	public Object getUniqueIdentifier() {
		return factionId;
	}


	@Override
	public void leftClickReleased() {
		listing.leftClickReleased();
	}


	@Override
	public void refresh() {
		individuals.clear();
		populateList();
		this.listing = new ScrollableListingPanel<Individual, String>(
			this,
			(i1, i2) -> {
				return i1.getId().getSimpleName().compareTo(i2.getId().getSimpleName());
			},
			false,
			35
		) {
			@Override
			protected String getExtraString(Entry<ScrollableListingPanel.ListingMenuItem<Individual>, String> item) {
				return Util.truncate(item.getKey().t.getAI().getCurrentTask().getDescription(), 8);
			}

			@Override
			protected int getExtraStringOffset() {
				return 120;
			}

			@Override
			protected void populateListings(List<HashMap<ScrollableListingPanel.ListingMenuItem<Individual>, String>> listings) {
				listings.add(individuals);
			}

			@Override
			public boolean keyPressed(int keyCode) {
				return false;
			}
		};
	}
}