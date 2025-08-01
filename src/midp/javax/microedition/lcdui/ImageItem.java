package javax.microedition.lcdui;

import emulator.lcdui.LCDUIUtils;
import emulator.lcdui.TextUtils;

public class ImageItem extends Item {
	public static final int LAYOUT_DEFAULT = 0;
	public static final int LAYOUT_LEFT = 1;
	public static final int LAYOUT_RIGHT = 2;
	public static final int LAYOUT_CENTER = 3;
	public static final int LAYOUT_NEWLINE_BEFORE = 256;
	public static final int LAYOUT_NEWLINE_AFTER = 512;
	private Image image;
	private String altText;
	private int appearanceMode;
	private int width;

	public ImageItem(final String s, final Image image, final int n, final String s2) {
		this(s, image, n, s2, 0);
	}

	public ImageItem(final String s, final Image anImage427, final int anInt178, final String aString25, final int anInt179) {
		super(s);
		if (anInt179 < 0 || anInt179 > 2) {
			throw new IllegalArgumentException();
		}
		this.image = anImage427;
		layout = anInt178;
		this.altText = aString25;
		this.appearanceMode = anInt179;
	}

	public Image getImage() {
		return this.image;
	}

	public void setImage(final Image anImage427) {
		this.image = anImage427;
		layoutForm();
	}

	public String getAltText() {
		return this.altText;
	}

	public void setAltText(final String aString25) {
		this.altText = aString25;
	}

	public int getLayout() {
		return super.getLayout();
	}

	public void setLayout(final int layout) {
		super.setLayout(layout);
	}

	public int getAppearanceMode() {
		return this.appearanceMode;
	}

	void paint(final Graphics g, int x, int y, int w, int h) {
		super.paint(g, x, y, w, h);
		int yo = y;
		if (labelArr != null && labelArr.length > 0) {
			g.setFont(labelFont);
			g.setColor(focused ? LCDUIUtils.highlightedForegroundColor : labelColor);
			for (String s : labelArr) {
				g.drawString(s, x + (w - labelFont.stringWidth(s)) / 2, yo + 2, 0);
				yo += labelFont.getHeight() + 4;
			}
		}
		if (appearanceMode == BUTTON) {
			g.setColor(LCDUIUtils.borderColor);
			int lx = x + w - 1;
			int ly = y + h - 3;
			g.drawLine(x + 1, ly, lx, ly);
			g.drawLine(lx, ly, lx, y + 1);
			g.setColor(focused ? LCDUIUtils.highlightedForegroundColor : LCDUIUtils.foregroundColor);
			g.drawRect(x, y, w, h - 2);
		}
		if (image != null) {
			int iw = Math.min(w, image.getWidth());
			// clip image
			g.drawRegion(image, 0, 0,
					iw, Math.min(h-yo+y, image.getHeight()), 0,
					x + (w - iw) / 2, yo, 0);
		}
	}

	void layout(Row row) {
		super.layout(row);
		int n = 0;
		int w = row.getAvailableWidth(screen.bounds[W]);
		int[] tw = new int[1];
		if (hasLabel()) {
			int min = labelFont.stringWidth("...") + 2;
			if (w < min) w = min;
			labelArr = TextUtils.textArr(label, labelFont, w, w, tw);
			n = (labelFont.getHeight() + 4) * labelArr.length;
		} else {
			labelArr = null;
		}
		int iw = 0;
		if (image != null) {
			iw = image.getWidth();
			n += image.getHeight() + 4;
		}
		width = Math.max(iw + 2, tw[0] + 5);
		bounds[H] = n;
	}

	public int getPreferredWidth() {
		if (super.isSizeLocked() && preferredWidth != -1) return preferredWidth;
		return width + 2;
	}

	public int getMinimumWidth() {
		int w = getPreferredWidth();
		if (w < 4) {
			w = image != null ? image.getWidth() + 4 : 4;
			if (hasLabel()) {
				int m = labelFont.stringWidth("...") + 2;
				if (w < m) w = m;
			}
		}
		return w;
	}

	public int getMinimumHeight() {
		return bounds[H];
	}

	boolean isFocusable() {
		return (commands.size() > 0 || appearanceMode == BUTTON) && (image != null || hasLabel());
	}
}
