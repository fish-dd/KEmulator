package emulator.ui.swt;

import emulator.Devices;
import emulator.Emulator;
import emulator.Settings;
import emulator.graphics2D.IFont;
import emulator.graphics2D.IImage;
import emulator.graphics2D.awt.FontAWT;
import emulator.graphics2D.awt.ImageAWT;
import emulator.graphics2D.swt.FontSWT;
import emulator.graphics2D.swt.ImageSWT;
import emulator.graphics3D.IGraphics3D;
import emulator.ui.*;
import org.eclipse.swt.widgets.Display;

import javax.microedition.lcdui.Font;
import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Vector;

public final class SWTFrontend implements IEmulatorFrontend {
	private static Display display;
	private Vector plugins;
	private int screenDepth;
	private Methods methods;
	private MemoryView memoryView;
	private MediaView mediaView;
	private Watcher classWatcher;
	private Watcher profiler;
	private Property iproperty;
	private EmulatorScreen screen;
	private Log ilogstream;
	private KeyPad keyPad;
	private InfosBox infos;
	private M3GViewUI m3gView;
	private MessageConsole sms;
	public Properties midletProps;
	private static Hashtable<String, FontSWT> swtFontsCache = new Hashtable<String, FontSWT>();

	public SWTFrontend() {
		super();
		display = new Display();
		this.plugins = new Vector();
		this.screenDepth = SWTFrontend.display.getDepth();
		this.iproperty = new Property();
		this.ilogstream = new Log();
		this.sms = new MessageConsole();
		this.infos = new InfosBox();
		this.keyPad = new KeyPad();
		this.classWatcher = Watcher.createForStatics();
		this.profiler = Watcher.createForProfiler();
		this.methods = new Methods();
	}

	public void dispose() {
		if (!display.isDisposed()) {
			display.dispose();
		}
	}

	public static Display getDisplay() {
		return display;
	}

	public static void syncExec(final Runnable runnable) {
		if (!display.isDisposed()) {
			display.syncExec(runnable);
		}
	}

	public final int getScreenDepth() {
		return this.screenDepth;
	}

	public final Watcher getClassWatcher() {
		return this.classWatcher;
	}

	public final Watcher getProfiler() {
		return this.profiler;
	}

	public final MemoryView getMemory() {
		if (memoryView == null) {
			this.memoryView = new MemoryView();
		}
		return this.memoryView;
	}

	public final MediaView getMedia() {
		if (mediaView == null) {
			this.mediaView = new MediaView();
		}
		return this.mediaView;
	}

	public final Methods getMethods() {
		return this.methods;
	}

	public final InfosBox getInfos() {
		return this.infos;
	}

	public final KeyPad getKeyPad() {
		return this.keyPad;
	}

	public final M3GViewUI getM3GView() {
		if (m3gView == null) {
			this.m3gView = new M3GViewUI();
		}
		return this.m3gView;
	}

	public final void disposeSubWindows() {
		Settings.showLogFrame = this.ilogstream.isLogOpen();
		Settings.showInfoFrame = this.infos.isShown();
		this.keyPad.dipose();
		this.ilogstream.dispose();
		this.sms.dispose();
		this.infos.dispose();
		Settings.showMemViewFrame = memoryView != null && this.memoryView.isShown();
		this.methods.dispose();
		if (memoryView != null) this.memoryView.dispose();
		if (mediaView != null) mediaView.dispose();
		this.classWatcher.dispose();
		this.profiler.dispose();
		if (m3gView != null)
			this.m3gView.close();
		while (Watcher.activeWatchers.size() > 0) {
			((Watcher) Watcher.activeWatchers.get(0)).dispose();
		}
		for (Object o : this.plugins) {
			((IPlugin) o).close();
		}
	}

	public final void pushPlugin(final IPlugin plugin) {
		this.plugins.add(plugin);
	}

	public final ILogStream getLogStream() {
		return this.ilogstream;
	}

	public final IProperty getProperty() {
		return this.iproperty;
	}

	public final IScreen getScreen() {
		if (screen == null) {
			screen = new EmulatorScreen(Devices.getPropertyInt("SCREEN_WIDTH"), Devices.getPropertyInt("SCREEN_HEIGHT"));
		}
		return screen;
	}

	public final IMessage getMessage() {
		return this.sms;
	}

	private String getFontName(int face) {
		if (face == Font.FACE_MONOSPACE) {
			return iproperty.getMonospaceFontName();
		}
		return iproperty.getDefaultFontName();
	}

	public final IFont newFont(int face, final int size, final int style) {
		String name = getFontName(face);
		if (Settings.g2d == 0) {
			String s = name + "." + size + "." + style;
			if (swtFontsCache.containsKey(s)) return swtFontsCache.get(s);
			FontSWT f = new FontSWT(name, size, style & ~Font.STYLE_UNDERLINED);
			swtFontsCache.put(s, f);
			return f;
		}
		if (Settings.g2d == 1) {
			return new FontAWT(name, size, style, false);
		}
		return null;
	}

	public final IFont newCustomFont(int face, final int size, final int style, boolean height) {
		return newFont(getFontName(face), style, size, height);
	}

	public final IFont newFont(String name, int style, int pixelSize) {
		return newFont(name, style, pixelSize, false);
	}

	private IFont newFont(String name, int style, int size, boolean height) {
		if (Settings.g2d == 0) {
			String s = name + "." + size + "." + style + height;
			if (swtFontsCache.containsKey(s)) return swtFontsCache.get(s);
			FontSWT f = new FontSWT(name, size, style & ~Font.STYLE_UNDERLINED, height);
			swtFontsCache.put(s, f);
			return f;
		}
		if (Settings.g2d == 1) {
			return new FontAWT(name, size, style, height);
		}
		return null;
	}

	public final IFont loadFont(InputStream fontData, int size) throws IOException {
		if (Settings.g2d == 1) {
			return new FontAWT(fontData, size);
		}
		return null;
	}

	public final IImage newImage(final int n, final int n2, final boolean transparent) {
		if (Settings.g2d == 0) {
			return new ImageSWT(n, n2, transparent, -1);
		}
		if (Settings.g2d == 1) {
			return new ImageAWT(n, n2, transparent, -1);
		}
		return null;
	}

	public final IImage newImage(int n, int n2, boolean b, int n3) {
		return Settings.g2d == 0 ? new ImageSWT(n, n2, b, n3) : (Settings.g2d == 1 ? new ImageAWT(n, n2, b, n3) : null);
	}


	public final IImage newImage(final byte[] array) throws IOException {
		if (Settings.g2d == 0) {
			return new ImageSWT(array);
		}
		if (Settings.g2d == 1) {
			return new ImageAWT(array);
		}
		return null;
	}

	public final IGraphics3D getGraphics3D() {
		// TODO
		return Emulator.getPlatform().getGraphics3D();
	}

	public final void syncValues() {
		for (int i = 0; i < Watcher.activeWatchers.size(); ++i) {
			display.asyncExec(Watcher.activeWatchers.get(i));
		}
		if (this.methods.method438()) {
			display.asyncExec(this.methods);
		}
	}

	public final String getAppProperty(final String s) {
		final String property;
		if (midletProps != null && (property = this.midletProps.getProperty(s)) != null) {
			return property.trim();
		}
		return null;
	}

	public Properties getAppProperties() {
		return midletProps;
	}

	public void setAppProperties(final Properties p) {
		midletProps = p;
	}

	/**
	 * updateLanguage
	 */
	public void updateLanguage() {
		((EmulatorScreen) getScreen()).updateLanguage();
	}
}
