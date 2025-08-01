package emulator.graphics3D.lwjgl;

import emulator.Emulator;
import org.eclipse.swt.opengl.GLCanvas;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class GLCanvasUtil {
	private static Object platformCanvas;
	private static Method makeCurrentMethod;
	private static Method isCurrentMethod;
	private static Method swapBuffersMethod;

	public static Canvas initGLCanvas(Composite parent, int style, int type) throws Exception {
		Canvas c = null;
		if (type != 2) {
			try {
				Emulator.getEmulator().getLogStream().println("Initializing GLCanvas from swt");
				org.eclipse.swt.opengl.GLData gld = new org.eclipse.swt.opengl.GLData();
				gld.depthSize = Math.min(24, Emulator.getEmulator().getScreenDepth());
//				gld.doubleBuffer = true;

				int samples = 4;
				while (true) {
					try {
						gld.samples = samples;
						c = new org.eclipse.swt.opengl.GLCanvas(parent, style, gld);
						break;
					} catch (Exception e) {
						if ((samples >>= 1) == 0) {
							gld.samples = samples;
							c = new org.eclipse.swt.opengl.GLCanvas(parent, style, gld);
							break;
						}
					}
				}

			} catch (Exception e) {
				if (type == 1) throw e;
				e.printStackTrace();
			}
		}
		if (c == null) {
			Emulator.getEmulator().getLogStream().println("Initializing GLCanvas from lwjglx");
			org.lwjgl.opengl.swt.GLData gld = new org.lwjgl.opengl.swt.GLData();
			gld.depthSize = Math.min(24, Emulator.getEmulator().getScreenDepth());
			gld.doubleBuffer = true;

			int samples = 4;
			while (true) {
				try {
					gld.samples = samples;
					c = new org.lwjgl.opengl.swt.GLCanvas(parent, style, gld);
					break;
				} catch (Exception e) {
					if ((samples >>= 1) == 0) {
						gld.samples = samples;
						c = new org.lwjgl.opengl.swt.GLCanvas(parent, style, gld);
						break;
					}
				}
			}

			releaseContext(c);
		}

		return c;
	}

	public static void makeCurrent(Canvas canvas) throws Exception {
		if (canvas instanceof org.eclipse.swt.opengl.GLCanvas) {
			((org.eclipse.swt.opengl.GLCanvas) canvas).setCurrent();
		} else if (canvas instanceof org.lwjgl.opengl.swt.GLCanvas) {
			if (platformCanvas == null) {
				Field p = org.lwjgl.opengl.swt.GLCanvas.class.getDeclaredField("platformCanvas");
				p.setAccessible(true);
				platformCanvas = p.get(null);
			}

			if (makeCurrentMethod == null) {
				makeCurrentMethod = Class.forName("org.lwjgl.opengl.swt.PlatformGLCanvas")
						.getDeclaredMethod("makeCurrent", org.lwjgl.opengl.swt.GLCanvas.class, long.class);
				makeCurrentMethod.setAccessible(true);
			}
			makeCurrentMethod.invoke(platformCanvas, canvas, getContext(canvas));
		} else {
			throw new IllegalArgumentException();
		}
	}
	public static boolean isCurrent(Canvas canvas) {
		if (canvas instanceof org.eclipse.swt.opengl.GLCanvas) {
			return ((GLCanvas) canvas).isCurrent();
		}
		try {
			if (isCurrentMethod == null) {
				isCurrentMethod = Class.forName("org.lwjgl.opengl.swt.PlatformGLCanvas")
						.getDeclaredMethod("isCurrent", long.class);
				isCurrentMethod.setAccessible(true);
			}

			return (Boolean) makeCurrentMethod.invoke(platformCanvas, getContext(canvas));
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	private static long getContext(Canvas canvas) {
		try {
			Field ctx;
			if (canvas instanceof org.eclipse.swt.opengl.GLCanvas) {
				ctx = org.eclipse.swt.opengl.GLCanvas.class.getDeclaredField("context");
			} else {
				ctx = org.lwjgl.opengl.swt.GLCanvas.class.getDeclaredField("context");
			}
			ctx.setAccessible(true);

			long context;

			if (ctx.getType().equals(long.class)) {
				context = ctx.getLong(canvas);
			} else {
				context = ctx.getInt(canvas);
			}
			return context;
		} catch (Exception ignored) {}
		return 0;
	}

	public static void swapBuffers(Canvas canvas) {
		if (canvas instanceof org.eclipse.swt.opengl.GLCanvas) {
			((GLCanvas) canvas).swapBuffers();
			return;
		}
		try {
			if (swapBuffersMethod == null) {
				swapBuffersMethod = Class.forName("org.lwjgl.opengl.swt.PlatformGLCanvas")
						.getDeclaredMethod("swapBuffers", org.lwjgl.opengl.swt.GLCanvas.class);
				swapBuffersMethod.setAccessible(true);
			}
			if (platformCanvas == null) {
				Field p = org.lwjgl.opengl.swt.GLCanvas.class.getDeclaredField("platformCanvas");
				p.setAccessible(true);
				platformCanvas = p.get(null);
			}

			swapBuffersMethod.invoke(platformCanvas, canvas);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	public static void releaseContext(Canvas canvas) throws Exception {
		if (canvas instanceof org.eclipse.swt.opengl.GLCanvas) {
			Field ctx = org.eclipse.swt.opengl.GLCanvas.class.getDeclaredField("context");
			ctx.setAccessible(true);

			long context;
			if (ctx.getType().equals(long.class)) {
				context = ctx.getLong(canvas);
				ctx.set(canvas, (Long) 0L);
			} else {
				context = ctx.getInt(canvas);
				ctx.set(canvas, (Integer) 0);
			}

			((org.eclipse.swt.opengl.GLCanvas) canvas).setCurrent();

			if (ctx.getType().equals(long.class)) {
				ctx.set(canvas, (Long) context);
			} else {
				ctx.set(canvas, (Integer) (int) context);
			}
		} else if (canvas instanceof org.lwjgl.opengl.swt.GLCanvas) {
			if (platformCanvas == null) {
				Field p = org.lwjgl.opengl.swt.GLCanvas.class.getDeclaredField("platformCanvas");
				p.setAccessible(true);
				platformCanvas = p.get(null);
			}
			if (makeCurrentMethod == null) {
				makeCurrentMethod = Class.forName("org.lwjgl.opengl.swt.PlatformGLCanvas")
						.getDeclaredMethod("makeCurrent", org.lwjgl.opengl.swt.GLCanvas.class, long.class);
				makeCurrentMethod.setAccessible(true);
			}
			makeCurrentMethod.invoke(platformCanvas, canvas, 0);
		} else {
			throw new IllegalArgumentException();
		}
	}
}
