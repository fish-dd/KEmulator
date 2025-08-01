package emulator.ui.swt;

import emulator.Emulator;
import emulator.graphics2D.swt.ImageSWT;
import emulator.ui.effect.a;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Timer;
import java.util.TimerTask;

public final class About implements MouseListener, MouseMoveListener {
	private Shell aShell806;
	private CLabel aCLabel805;
	private Link aLink807;
	private Link aLink816;
	private Link aLink820;
	private StyledText aStyledText808;
	private Button aButton809;
	private Canvas aCanvas810;
	private a ana811;
	private ImageSWT ad812;
	private ImageSWT ad817;
	private Timer aTimer813;
	GC aGC814;
	int[] anIntArray815;
	int[] anIntArray818;
	private Button aButton819;

	public About() {
		super();
		this.aShell806 = null;
		this.aCLabel805 = null;
		this.aLink807 = null;
		this.aLink816 = null;
		this.aLink820 = null;
		this.aStyledText808 = null;
		this.aButton809 = null;
		this.aCanvas810 = null;
		this.aButton819 = null;
	}

	private void method462(final Shell shell) {
		final GridData layoutData;
		(layoutData = new GridData()).horizontalIndent = 2;
		layoutData.horizontalSpan = 2;
		layoutData.horizontalAlignment = 4;
		layoutData.verticalAlignment = 2;
		layoutData.grabExcessHorizontalSpace = true;
		final GridData layoutData2;
		(layoutData2 = new GridData()).heightHint = 20;
		layoutData2.widthHint = 90;
		final GridData gridData;
		(gridData = new GridData()).horizontalAlignment = 4;
		gridData.grabExcessHorizontalSpace = true;
		gridData.verticalAlignment = 2;
		final GridData layoutData3;
		(layoutData3 = new GridData()).horizontalAlignment = 3;
		layoutData3.grabExcessHorizontalSpace = false;
		layoutData3.verticalAlignment = 2;
		final GridData layoutData4;
		(layoutData4 = new GridData()).horizontalAlignment = 4;
		layoutData4.grabExcessHorizontalSpace = true;
		layoutData4.grabExcessVerticalSpace = true;
		layoutData4.horizontalSpan = 2;
		layoutData4.verticalAlignment = 4;
		final GridData layoutData5;
		(layoutData5 = new GridData()).horizontalAlignment = 4;
		layoutData5.grabExcessHorizontalSpace = true;
		layoutData5.grabExcessVerticalSpace = false;
		layoutData5.verticalAlignment = 4;
		layoutData5.verticalSpan = 3;
		final GridData gridData2;
		(gridData2 = new GridData()).horizontalIndent = 5;
		gridData2.horizontalAlignment = 4;
		gridData2.grabExcessHorizontalSpace = false;
		gridData2.grabExcessVerticalSpace = false;
		gridData2.verticalAlignment = 4;
		final GridLayout layout;
		(layout = new GridLayout()).numColumns = 2;
		layout.horizontalSpacing = 0;
		(this.aShell806 = new Shell(shell, 67680)).setText(emulator.UILocale.get("ABOUT_FRAME_TITLE", "About & Help"));
		this.aShell806.setImage(new Image(Display.getCurrent(), this.getClass().getResourceAsStream("/res/icon")));
		this.aShell806.setLayout(layout);
		this.aShell806.setSize(new Point(360, 400));
		(this.aCLabel805 = new CLabel(this.aShell806, 0)).setLayoutData(layoutData5);
		aCLabel805.setFont(EmulatorScreen.f);
		this.aCLabel805.setText(Emulator.getAboutString());
		this.method463();
		(this.aLink816 = new Link(this.aShell806, 0)).setText("<a>" + "nnmod web page" + "</a>");
		this.aLink816.setLayoutData(gridData2);
		//((Control)this.aLink816).setEnabled(false);
		this.aLink816.addSelectionListener(new Class158(this));
		(this.aLink820 = new Link(this.aShell806, 0)).setText("<a>" + "Chat, news (en)" + "</a>");
		this.aLink820.setLayoutData(gridData2);
		//   ((Control)this.aLink820).setEnabled(false);
		this.aLink820.addSelectionListener(new Class157(this));
		(this.aLink807 = new Link(this.aShell806, 0)).setText("Mod by shinovon\n3D engine contributions by rmn20\nSoftBank MEXA, MascotCapsule impl by woesss\n" + emulator.UILocale.get("ABOUT_AUTHOR", "Author") + ": <a>Wu.Liang</a>  (c) 2006,2008");
		this.aLink807.setLayoutData(layoutData);
		this.aLink807.addSelectionListener(new Class156(this));
		(this.aStyledText808 = new StyledText(this.aShell806, 2562)).setLayoutData(layoutData4);
		this.aStyledText808.setFocus();
		this.aStyledText808.setEditable(false);
		this.aStyledText808.setIndent(5);
		aStyledText808.setFont(EmulatorScreen.f);
		(this.aButton819 = new Button(this.aShell806, 8388608)).setText(emulator.UILocale.get("ABOUT_ONLINE_MANUAL", "Online Manual"));
		this.aButton819.setLayoutData(layoutData2);
		this.aButton819.setEnabled(false);
		this.aButton819.addSelectionListener(new Class162(this));
		(this.aButton809 = new Button(this.aShell806, 8388616)).setText(emulator.UILocale.get("DIALOG_OK", "OK"));
		this.aButton809.setLayoutData(layoutData3);
		layoutData3.heightHint = 20;
		layoutData3.widthHint = 70;
		this.aButton809.addSelectionListener(new Class163(this));
		this.method453();
	}

	private void method453() {
		try {
			final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(this.getClass().getResourceAsStream("/res/help")));
			String line;
			while ((line = bufferedReader.readLine()) != null) {
				final int charCount = this.aStyledText808.getCharCount();
				if (line.startsWith("$")) {
					this.aStyledText808.append(line.substring(1) + "\n");
					this.aStyledText808.setStyleRange(new StyleRange(charCount, line.length(), Display.getCurrent().getSystemColor(9), null));
				} else {
					this.aStyledText808.append(line + "\n");
				}
			}
			bufferedReader.close();
		} catch (Exception ignored) {
		}
	}

	public final void method454(final Shell shell) {
		this.method462(shell);
		final Display display = shell.getDisplay();
		this.aShell806.setLocation(shell.getLocation().x + (shell.getSize().x - this.aShell806.getSize().x >> 1), shell.getLocation().y + (shell.getSize().y - this.aShell806.getSize().y >> 1));
		this.aShell806.open();
		while (!this.aShell806.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}

	private void method463() {
		final GridData layoutData;
		(layoutData = new GridData()).horizontalAlignment = 4;
		layoutData.verticalAlignment = 4;
		layoutData.heightHint = 146;
		layoutData.widthHint = 156;
		(this.aCanvas810 = new Canvas(this.aShell806, 537133056)).setLayoutData(layoutData);
		this.aCanvas810.addMouseListener(this);
		this.aCanvas810.addMouseMoveListener(this);
		this.aGC814 = new GC(this.aCanvas810);
		this.method455(Emulator.class.getResourceAsStream("/res/sign"));
	}

	private void method455(final InputStream inputStream) {
		try {
			this.ad812 = new ImageSWT(inputStream);
			this.ad817 = new ImageSWT(this.ad812.getWidth(), this.ad812.getHeight(), false, 6393563);
			this.anIntArray815 = this.ad812.getData();
			this.anIntArray818 = this.ad817.getData();
			(this.ana811 = new a()).method135(this.ad812.getWidth(), this.ad812.getHeight());
			this.ana811.method137(this.ad812.getWidth() >> 1, this.ad812.getHeight() >> 1, 10, 500, this.ana811.anInt324);
			(this.aTimer813 = new Timer()).schedule(new WaterTask(this), 0L, 30L);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public final void mouseDoubleClick(final MouseEvent mouseEvent) {
	}

	public final void mouseDown(final MouseEvent mouseEvent) {
		this.ana811.method137(mouseEvent.x, mouseEvent.y, 5, 500, this.ana811.anInt324);
	}

	public final void mouseUp(final MouseEvent mouseEvent) {
	}

	public final void mouseMove(final MouseEvent mouseEvent) {
		this.ana811.method137(mouseEvent.x, mouseEvent.y, 5, 50, this.ana811.anInt324);
	}

	static Shell method456(final About class54) {
		return class54.aShell806;
	}

	static a method457(final About class54) {
		return class54.ana811;
	}

	static Canvas method458(final About class54) {
		return class54.aCanvas810;
	}

	static Timer method459(final About class54) {
		return class54.aTimer813;
	}

	static a method460(final About class54, final a ana811) {
		return class54.ana811 = ana811;
	}

	static ImageSWT method461(final About class54) {
		return class54.ad817;
	}

	final static class WaterTask extends TimerTask {
		private final About aClass54_775;

		private WaterTask(final About aClass54_775) {
			super();
			this.aClass54_775 = aClass54_775;
		}

		public final void run() {
			About.method457(this.aClass54_775).method136(this.aClass54_775.anIntArray815, this.aClass54_775.anIntArray818);
			//TODO DEOBFUSCATE ALL THIS MESS
			SWTFrontend.getDisplay().syncExec(new Water(this, aClass54_775.ana811));
		}

		WaterTask(final About class54, final Class158 class55) {
			this(class54);
		}

		static About method433(final WaterTask waterTask) {
			return waterTask.aClass54_775;
		}
	}

	public void finalize() {
		aShell806.getDisplay().asyncExec(() -> {
			try {
				if (!aGC814.isDisposed()) aGC814.dispose();
			} catch (Exception ignored) {
			}
		});
	}
}
