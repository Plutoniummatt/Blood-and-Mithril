package bloodandmithril.ui.components.window;

import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.google.common.collect.Maps;
import com.google.inject.Inject;

import bloodandmithril.character.individuals.Individual;
import bloodandmithril.character.individuals.IndividualContextMenuService;
import bloodandmithril.core.Copyright;
import bloodandmithril.core.Wiring;
import bloodandmithril.graphics.Graphics;
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

	@Inject
	private IndividualContextMenuService individualContextMenuService;

	/**
	 * Constructor
	 */
	public UnitsWindow(final int factoinId) {
		super(500, 400, Domain.getFaction(factoinId).name + " - Members", true, 500, 400, true, true, true);
		factionId = factoinId;
		refresh();
	}


	/**
	 * Renders the separator
	 */
	private void renderSeparator(final Graphics graphics) {
		graphics.getSpriteBatch().setShader(Shaders.filter);
		shapeRenderer.begin(ShapeType.Filled);
		final Color color = isActive() ? Colors.modulateAlpha(borderColor, getAlpha()) : Colors.modulateAlpha(borderColor, 0.4f * getAlpha());
		shapeRenderer.rect(x + width - 130, y + 24 - height, 2, height - 45, Color.CLEAR, Color.CLEAR, color, color);
		shapeRenderer.end();
	}


	private void populateList(final Graphics graphics) {
		Domain.getIndividuals().forEach(individual -> {
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
							individual.isAlive() ? Color.YELLOW : Color.MAROON,
							individual.isAlive() ? Color.WHITE : Color.MAROON,
							individual.isAlive() ? Color.GREEN : Color.MAROON,
							UIRef.BR
						),
						() -> { return individualContextMenuService.getContextMenu(individual).addFirst(
							new ContextMenu.MenuItem(
								"Go to",
								() -> {
									graphics.getCam().position.x = individual.getState().position.x;
									graphics.getCam().position.y = individual.getState().position.y;
								},
								Color.ORANGE,
								Color.WHITE,
								Color.ORANGE,
								null
							)
						);}
					),
					""
				);
			}
		});
	}


	@Override
	protected void internalWindowRender(final Graphics graphics) {
		listing.x = x;
		listing.y = y;
		listing.width = width;
		listing.height = height;
		renderSeparator(graphics);
		listing.render(graphics);
	}


	@Override
	protected void internalLeftClick(final List<ContextMenu> copy, final Deque<Component> windowsCopy) {
		listing.leftClick(copy, windowsCopy);
	}


	@Override
	public boolean scrolled(final int amount) {
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
		populateList(Wiring.injector().getInstance(Graphics.class));
		this.listing = new ScrollableListingPanel<Individual, String>(
			this,
			(i1, i2) -> {
				return i1.getId().getSimpleName().compareTo(i2.getId().getSimpleName());
			},
			false,
			35,
			null
		) {
			@Override
			protected String getExtraString(final Entry<ScrollableListingPanel.ListingMenuItem<Individual>, String> item) {
				if (item.getKey().t.isAlive()) {
					return Util.truncate(item.getKey().t.getAI().getCurrentTask().getShortDescription(), 8);
				} else {
					return "Deceased";
				}
			}

			@Override
			protected int getExtraStringOffset() {
				return 120;
			}

			@Override
			protected void populateListings(final List<HashMap<ScrollableListingPanel.ListingMenuItem<Individual>, String>> listings) {
				listings.add(individuals);
			}

			@Override
			public boolean keyPressed(final int keyCode) {
				return false;
			}
		};
	}
}