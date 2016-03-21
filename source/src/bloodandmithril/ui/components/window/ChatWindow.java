package bloodandmithril.ui.components.window;

import java.util.ArrayDeque;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.google.common.collect.Lists;

import bloodandmithril.control.BloodAndMithrilClientInputProcessor;
import bloodandmithril.core.Copyright;
import bloodandmithril.core.Wiring;
import bloodandmithril.networking.ClientServerInterface;
import bloodandmithril.networking.requests.SendChatMessage.Message;
import bloodandmithril.ui.UserInterface;
import bloodandmithril.ui.components.Component;
import bloodandmithril.ui.components.ContextMenu;
import bloodandmithril.ui.components.panel.ScrollableListingPanel;
import bloodandmithril.ui.components.panel.TextInputFieldPanel;
import bloodandmithril.util.Fonts;
import bloodandmithril.util.Util;
import bloodandmithril.util.Util.Colors;

/**
 * {@link Window} used for chatting with other connected players
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
public class ChatWindow extends Window {

	public final ScrollableListingPanel<String, Object> participants;

	private final TextInputFieldPanel textInputPanel;

	public static Deque<Message> messagesToDisplay = new ArrayDeque<>(50);

	/**
	 * Constructor
	 */
	public ChatWindow(int length, int height, boolean active, int minLength, int minHeight) {
		super(length, height, "Chat", active, minLength, minHeight, true, true, true);

		participants = new ScrollableListingPanel<String, Object>(this, Comparator.<String>naturalOrder(), false, 35, null) {
			@Override
			protected String getExtraString(Entry<ListingMenuItem<String>, Object> item) {
				return "";
			}

			@Override
			protected int getExtraStringOffset() {
				return 0;
			}

			@Override
			protected void populateListings(List<HashMap<ListingMenuItem<String>, Object>> listings) {
			}

			@Override
			public boolean keyPressed(int keyCode) {
				return false;
			}
		};

		textInputPanel = new TextInputFieldPanel(this, "");

		ClientServerInterface.SendRequest.sendRequestConnectedPlayerNamesRequest();
	}


	@Override
	protected void internalWindowRender(SpriteBatch batch) {

		renderMessage(batch);
		renderSeparator();
		renderPlayerList(batch);
		renderTextInputPanel();
	}


	private void renderTextInputPanel() {
		textInputPanel.x = x + 7;
		textInputPanel.y = y - height + 5;
		textInputPanel.width = width - 180;

		textInputPanel.render();
	}


	private void renderMessage(SpriteBatch batch) {
		String string = "";

		Iterator<Message> iterator = Lists.newArrayList(messagesToDisplay).iterator();
		while (iterator.hasNext()) {
			Message next = iterator.next();
			String temp = "";
			temp = temp.concat(next.sender) + ": ";
			temp = temp.concat(next.message);
			temp = Util.fitToWindow(temp, width - 170, Integer.MAX_VALUE);
			string = string.concat(temp + "\n");
		}

		Fonts.defaultFont.setColor(
			Color.CYAN.r * (isActive() ? 1f : 0.4f),
			Color.CYAN.g * (isActive() ? 1f : 0.4f),
			Color.CYAN.b * (isActive() ? 1f : 0.4f),
			getAlpha()
		);

		int lines = StringUtils.countMatches(string, "\n");
		int maxLines = (height - 130)/26;

		while (lines > maxLines) {
			string = string.substring(StringUtils.indexOf(string, "\n") + 1);
			lines--;
		}

		Fonts.defaultFont.drawWrapped(batch, string, x + 10, y - 30, width - 170);

		batch.flush();
	}


	@Override
	public boolean scrolled(int amount) {
		return participants.scrolled(amount);
	}


	public static synchronized void addMessage(Message messageToAdd) {
		if (!messagesToDisplay.offerLast(messageToAdd)) {
			messagesToDisplay.removeFirst();
			addMessage(messageToAdd);
		}
	}


	private void renderPlayerList(SpriteBatch batch) {
		participants.x = x + width - 170;
		participants.y = y;
		participants.width = 170;
		participants.height = height;

		participants.render(batch);
	}


	private void renderSeparator() {
		Color color = isActive() ? Colors.modulateAlpha(borderColor, getAlpha()) : Colors.modulateAlpha(borderColor, 0.4f * getAlpha());
		UserInterface.shapeRenderer.begin(ShapeType.Filled);
		UserInterface.shapeRenderer.rect(
			x + width - 170,
			y - height,
			2,
			height - 21,
			Color.CLEAR,
			Color.CLEAR,
			color,
			color
		);
		UserInterface.shapeRenderer.end();
	}


	@Override
	protected void internalLeftClick(List<ContextMenu> copy, Deque<Component> windowsCopy) {
		participants.leftClick(copy, windowsCopy);
	}


	@Override
	protected void uponClose() {
	}


	@Override
	public boolean keyPressed(int keyCode) {
		if (super.keyPressed(keyCode)) {
			return true;
		}

		textInputPanel.keyPressed(keyCode);

		if (keyCode == Wiring.injector().getInstance(BloodAndMithrilClientInputProcessor.class).getKeyMappings().sendChatMessage.keyCode) {
			if (!StringUtils.isEmpty(textInputPanel.getInputText())) {
				ClientServerInterface.SendRequest.sendChatMessage(textInputPanel.getInputText());
				textInputPanel.clear();
			}
		}

		return true;
	}


	@Override
	public void leftClickReleased() {
		participants.leftClickReleased();
	}


	@Override
	public Object getUniqueIdentifier() {
		return getClass();
	}
}