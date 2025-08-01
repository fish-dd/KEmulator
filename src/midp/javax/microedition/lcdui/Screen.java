package javax.microedition.lcdui;

import emulator.KeyMapping;
import emulator.lcdui.LCDUIUtils;
import emulator.lcdui.TextUtils;

import java.util.Vector;

public abstract class Screen extends Displayable {
	static final Font font = Font.getDefaultFont();
	static final int fontHeight = font.getHeight();
	static final int fontHeight4 = fontHeight + 4;
	final Vector items;
	//	private long lastPressTime;
	int scroll;

	Screen() {
		this("");
	}

	Screen(final String s) {
		super();
		super.title = ((s == null) ? "" : s);
		this.items = new Vector();
	}

	public void _invokeKeyPressed(final int n) {
		if (_isSWT()) return;
//		final long currentTimeMillis;
//		if ((currentTimeMillis = System.currentTimeMillis()) - this.lastPressTime < 100L) {
//			return;
//		}
//		this.lastPressTime = currentTimeMillis;
		if (n == KeyMapping.getArrowKeyFromDevice(Canvas.UP)) {
			_keyScroll(Canvas.UP, false);
			return;
		}
		if (n == KeyMapping.getArrowKeyFromDevice(Canvas.DOWN)) {
			_keyScroll(Canvas.DOWN, false);
			return;
		}
		if (n == KeyMapping.getArrowKeyFromDevice(Canvas.LEFT)) {
			_keyScroll(Canvas.LEFT, false);
			return;
		}
		if (n == KeyMapping.getArrowKeyFromDevice(Canvas.RIGHT)) {
			_keyScroll(Canvas.RIGHT, false);
			return;
		}
		if (focusedItem != null && focusedItem instanceof CustomItem) {
			((CustomItem) focusedItem).keyPressed(n);
			return;
		}
		if (focusedItem != null && n == KeyMapping.getArrowKeyFromDevice(Canvas.FIRE)) {
			focusedItem._itemApplyCommand();
			return;
		}
	}

	public void _invokeKeyRepeated(final int n) {
		if (_isSWT()) return;
		if (n == KeyMapping.getArrowKeyFromDevice(Canvas.UP)) {
			_keyScroll(Canvas.UP, true);
			return;
		}
		if (n == KeyMapping.getArrowKeyFromDevice(Canvas.DOWN)) {
			_keyScroll(Canvas.DOWN, true);
			return;
		}
		if (n == KeyMapping.getArrowKeyFromDevice(Canvas.LEFT)) {
			_keyScroll(Canvas.LEFT, true);
			return;
		}
		if (n == KeyMapping.getArrowKeyFromDevice(Canvas.RIGHT)) {
			_keyScroll(Canvas.RIGHT, true);
			return;
		}
		if (focusedItem != null && focusedItem instanceof CustomItem) {
			((CustomItem) focusedItem).keyRepeated(n);
			return;
		}
	}

	protected void _keyScroll(int key, boolean repeat) {
	}

	public void _invokeKeyReleased(final int n) {
		if (n == KeyMapping.getArrowKeyFromDevice(Canvas.UP)
				|| n == KeyMapping.getArrowKeyFromDevice(Canvas.DOWN)
				|| n == KeyMapping.getArrowKeyFromDevice(Canvas.LEFT)
				|| n == KeyMapping.getArrowKeyFromDevice(Canvas.RIGHT)) {
			return;
		}
		if (focusedItem != null && focusedItem instanceof CustomItem) {
			((CustomItem) focusedItem).keyReleased(n);
			return;
		}
	}

	public boolean _invokePointerPressed(final int x, final int y) {
		return false;
	}

	public void _invokePointerReleased(final int n, final int n2) {
	}

	public void _invokePointerDragged(final int n, final int n2) {
	}

	protected abstract void _paint(final Graphics p0);

	public void _invokePaint(final Graphics graphics) {
		if (_isSWT()) return;
		Displayable._resetXRayGraphics();
		final int color = graphics.getColor();
		final int strokeStyle = graphics.getStrokeStyle();
		final Font font = graphics.getFont();
		graphics.setFont(Screen.font);
		graphics.setStrokeStyle(0);
		LCDUIUtils.drawDisplayableBackground(graphics, 0, 0, super.w, super.h, false);
		this._drawTitleBar(graphics);
		this._paint(graphics);
		this._drawScrollBar(graphics);
		this._paintTicker(graphics);
		this._paintSoftMenu(graphics);
		graphics.setColor(color);
		graphics.setFont(font);
		graphics.setStrokeStyle(strokeStyle);
	}

	protected void _drawTitleBar(final Graphics graphics) {
		if (_isSWT()) return;
		String title = super.title == null ? "" : super.title.trim();
		final int n;
		final String value = String.valueOf(n = ((focusedItem != null) ? (this.items.indexOf(focusedItem) + 1) : this.items.size()));
		final int n2 = (Screen.fontHeight4 >> 1) - 1;
		final int stringWidth2 = Screen.font.stringWidth(value);
		int w = super.w - stringWidth2 - 16 - Screen.font.stringWidth("...");
		if (w > 16) {
			String[] s = TextUtils.textArr(title, Screen.font, w, w);
			title = s.length == 0 ? "" : (s.length != 1 ? s[0] + "..." : s[0]);
		}
		final int stringWidth = Screen.font.stringWidth(title);
		final int n3 = (super.w - stringWidth >> 1) + 2;
		final int n4 = super.w - stringWidth2 - 2;
		graphics.setColor(8617456);
		graphics.fillRect(2, n2, (super.w - stringWidth >> 1) - 2, 2);
		graphics.fillRect(n3 + stringWidth + 2, n2, n4 - n3 - stringWidth - 4, 2);
		graphics.setColor(LCDUIUtils.foregroundColor);
		graphics.setFont(Screen.font);
		graphics.drawString(title, n3, 1, 0);
		graphics.drawString(value, n4, 1, 0);
	}

	protected void sizeChanged(final int w, final int h) {
	}

	void _invokeSizeChanged(int w, int h, boolean b) {
		super._invokeSizeChanged(w, h, b);
	}

	protected void _drawScrollBar(final Graphics graphics) {
		LCDUIUtils.drawScrollbar(graphics, bounds[W] + 1, Screen.fontHeight4 - 1, 2, bounds[H] - 2, this.items.size(), (focusedItem != null) ? this.items.indexOf(focusedItem) : -1);
	}

	public int _repaintInterval() {
		return -1;
	}

	public boolean _isSWT() {
		return false;
	}

	public void _swtShown() {
	}

	public void _swtHidden() {
	}

	public void _swtUpdateSizes() {
	}

	public Object _getSwtContent() {
		return null;
	}
}