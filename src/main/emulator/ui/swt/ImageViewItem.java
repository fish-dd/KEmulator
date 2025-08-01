package emulator.ui.swt;

import emulator.debug.MemoryViewImage;
import emulator.debug.MemoryViewImageType;
import org.eclipse.swt.graphics.Rectangle;

import javax.microedition.lcdui.Image;
import javax.microedition.m3g.Image2D;

/**
 * {@link Image} wrapper with some metadata for Images View.
 */
public class ImageViewItem {
	public final Image drawable;
	public final Object source;
	public final MemoryViewImageType type;
	public final boolean released;
	public final String type2;
	/**
	 * Rect where image was drawn. May be null if image was not drawn.
	 */
	public Rectangle drawnRect;

	public ImageViewItem(Image image, boolean released) {
		this.drawable = image;
		this.released = released;
		if (image instanceof MemoryViewImage) {
			source = ((MemoryViewImage) image).source;
			type = ((MemoryViewImage) image).type;
			if (source instanceof Image2D) {
				int t = ((Image2D) source).getFormat();
				type2 = m3gTypes[t - Image2D.ALPHA];
			} else {
				type2 = ""; //TODO i'm not familiar with micro3d
			}
		} else {
			source = image;
			type = MemoryViewImageType.LCDUI;
			type2 = image.isMutable() ? "Mutable" : "Immutable";
		}
	}

	public String getCaption() {
		return type.toString();
	}

	private final static String[] m3gTypes = new String[]{"A", "L", "LA", "RGB", "RGBA"};
}
