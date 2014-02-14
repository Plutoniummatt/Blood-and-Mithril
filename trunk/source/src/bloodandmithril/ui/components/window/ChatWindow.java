package bloodandmithril.ui.components.window;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;

import bloodandmithril.BloodAndMithrilClient;
import bloodandmithril.csi.ClientServerInterface;
import bloodandmithril.csi.requests.SendChatMessage.Message;
import bloodandmithril.ui.UserInterface;
import bloodandmithril.ui.components.Component;
import bloodandmithril.ui.components.ContextMenu;
import bloodandmithril.ui.components.panel.ScrollableListingPanel;
import bloodandmithril.ui.components.panel.TextInputFieldPanel;
import bloodandmithril.util.Fonts;
import bloodandmithril.util.Util;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.google.common.collect.Lists;

/**
 * {@link Window} used for chatting with other connected players
 *
 * @author Matt
 */
public class ChatWindow extends Window {

	public final ScrollableListingPanel<String> participants;

	private final TextInputFieldPanel textInputPanel;

	public static Deque<Message> messagesToDisplay = new ArrayDeque<>(50);

	/**
	 * Constructor
	 */
	public ChatWindow(int x, int y, int length, int height, boolean active, int minLength, int minHeight) {
		super(x, y, length, height, "Chat", active, minLength, minHeight, true);

		participants = new ScrollableListingPanel<String>(this) {
			@Override
			protected String getExtraString(Entry<ListingMenuItem<String>, Integer> item) {
				return "";
			}

			@Override
			protected int getExtraStringOffset() {
				return 0;
			}

			@Override
			protected void onSetup(List<HashMap<ListingMenuItem<String>, Integer>> listings) {
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
	protected void internalWindowRender() {

		renderMessage();
		renderSeparator();
		renderPlayerList();
		renderTextInputPanel();
	}


	private void renderTextInputPanel() {
		textInputPanel.x = x + 7;
		textInputPanel.y = y - height + 5;
		textInputPanel.width = width - 110;

		textInputPanel.render();
	}


	private void renderMessage() {
		String string = "";

		Iterator<Message> iterator = Lists.newArrayList(messagesToDisplay).iterator();
		while (iterator.hasNext()) {
			Message next = iterator.next();
			String temp = "";
			temp = temp.concat(next.sender) + ": ";
			temp = temp.concat(next.message);
			temp = Util.fitToWindow(temp, width - 100, Integer.MAX_VALUE);
			string = string.concat(temp + "\n");
		}

		Fonts.defaultFont.setColor(
			Color.CYAN.r * (active ? 1f : 0.4f),
			Color.CYAN.g * (active ? 1f : 0.4f),
			Color.CYAN.b * (active ? 1f : 0.4f),
			alpha
		);

		int lines = StringUtils.countMatches(string, "\n");
		int maxLines = (height - 130)/26;

		while (lines > maxLines) {
			string = string.substring(StringUtils.indexOf(string, "\n") + 1);
			lines--;
		}

		Fonts.defaultFont.drawWrapped(BloodAndMithrilClient.spriteBatch, string, x + 10, y - 30, width - 100);

		BloodAndMithrilClient.spriteBatch.flush();
	}


	public static synchronized void addMessage(Message messageToAdd) {
		if (!messagesToDisplay.offerLast(messageToAdd)) {
			messagesToDisplay.removeFirst();
			addMessage(messageToAdd);
		}
	}


	private void renderPlayerList() {
		participants.x = x + width - 100;
		participants.y = y;
		participants.width = 100;
		participants.height = height;

		participants.render();
	}


	private void renderSeparator() {
		Color color = active ? new Color(borderColor.r, borderColor.g, borderColor.b, alpha) : new Color(borderColor.r, borderColor.g, borderColor.b, borderColor.a * 0.4f * alpha);
		UserInterface.shapeRenderer.begin(ShapeType.FilledRectangle);
		UserInterface.shapeRenderer.filledRect(
			x + width - 100,
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
		textInputPanel.keyPressed(keyCode);

		if (keyCode == Input.Keys.ENTER) {
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
}