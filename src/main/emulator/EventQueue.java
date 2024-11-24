package emulator;

import javax.microedition.lcdui.*;

import com.j_phone.amuse.ACanvas;
import com.vodafone.v10.graphics.sprite.SpriteCanvas;
import emulator.ui.IScreen;
import net.rim.device.api.system.Application;
import emulator.graphics2D.*;

import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

public final class EventQueue implements Runnable {
	public static final int EVENT_PAINT = 1;
	public static final int EVENT_CALL = 2;
	public static final int EVENT_SCREEN = 4;
	public static final int EVENT_START = 10;
	public static final int EVENT_EXIT = 11;
	public static final int EVENT_SHOW = 15;
	public static final int EVENT_PAUSE = 16;
	public static final int EVENT_RESUME = 17;
	public static final int EVENT_INPUT = 18;
	public static final int EVENT_COMMAND = 19;
	public static final int EVENT_ITEM_STATE = 20;

	private int[] events;
	private int count;
	boolean running;
	private final Vector eventArguments = new Vector();
	private final Thread eventThread;
	private boolean paused;
	private final InputThread input = new InputThread();
	private final Thread inputThread;
	private final Object lock = new Object();
	private final Object repaintLock = new Object();
	private boolean alive;
	private int[][] inputs;
	private int inputsCount;
	private final Object eventLock = new Object();
	private boolean repaintPending;
	private int repaintX, repaintY, repaintW, repaintH;

	private Timer screenTimer;
	private TimerTask screenTimerTask;

	public EventQueue() {
		events = new int[128];
		inputs = new int[128][];
		paused = false;
		running = true;
		eventThread = new Thread(this, "KEmulator-EventQueue");
		eventThread.setPriority(3);
		eventThread.start();
		inputThread = new Thread(input, "KEmulator-InputQueue");
		inputThread.setPriority(3);
		inputThread.start();
		Thread crashThread = new Thread() {
			public void run() {
				long time = System.currentTimeMillis();
				try {
					while (running) {
						if (alive || paused || Settings.steps >= 0) {
							time = System.currentTimeMillis();
							alive = false;
						} else if ((System.currentTimeMillis() - time) > 5000) {
							time = System.currentTimeMillis();
							Emulator.getEmulator().getLogStream().println("Event thread is not responding! Is it dead locked?");
						}
						sleep(100);
					}
				} catch (InterruptedException ignored) {}
			}
		};
		crashThread.start();
	}

	public void stop() {
		running = false;
	}

	public void keyPress(int n) {
		if (n == 10000) return;
		if (Settings.synchronizeKeyEvents) {
			queueInput(new int[] {0, n, 0, 1});
		} else input.queue(0, n, 0, true);
	}

	public void keyRelease(int n) {
		if (n == 10000) return;
		if (Settings.synchronizeKeyEvents) {
			queueInput(new int[] {1, n, 0, 1});
		} else input.queue(1, n, 0, true);
	}

	public void keyRepeat(int n) {
		if (n == 10000) return;
		if (Settings.synchronizeKeyEvents) {
			queueInput(new int[] {2, n, 0, 1});
		} else input.queue(2, n, 0, true);
	}

	public void mouseDown(int x, int y) {
		if (Settings.synchronizeKeyEvents) {
			queueInput(new int[] {0, x, y, 0});
		} else try {
			Displayable d = getCurrent();
			if (d == null) return;
			if (d instanceof Canvas) {
				((Canvas) d).invokePointerPressed(x, y);
				return;
			}
			((Screen) d)._invokePointerPressed(x, y);
		} catch (Throwable ignored) {}
	}

	public void mouseUp(int x, int y) {
		if (Settings.synchronizeKeyEvents) {
			queueInput(new int[] {1, x, y, 0});
		} else try {
			Displayable d = getCurrent();
			if (d == null) return;
			if (d instanceof Canvas) {
				((Canvas) d).invokePointerReleased(x, y);
				return;
			}
			((Screen) d)._invokePointerReleased(x, y);
		} catch (Throwable ignored) {}
	}

	public void mouseDrag(int x, int y) {
		if (Settings.synchronizeKeyEvents) {
			queueInput(new int[] {2, x, y, 0});
		} else try {
			Displayable d = getCurrent();
			if (d == null) return;
			if (d instanceof Canvas) {
				((Canvas) d).invokePointerDragged(x, y);
				return;
			}
			((Screen) d)._invokePointerDragged(x, y);
		} catch (Throwable ignored) {}
	}

	public void sizeChanged(int x, int y) {
		queue(Integer.MIN_VALUE | (x & 0xFFF) | (y & 0xFFF) << 12);
	}

	private void queueInput(int[] o) {
		synchronized (this) {
			inputs[inputsCount++] = o;
			if (inputsCount >= inputs.length) {
				System.arraycopy(inputs, 0, inputs = new int[inputs.length * 2][], 0, inputsCount);
			}
		}
		queue(EVENT_INPUT);
	}

	public synchronized void queue(int n) {
		if (n == EVENT_PAINT) {
			if (repaintPending)
				return;
			repaintPending = true;
		}
		if (n == EVENT_SHOW || n == EVENT_RESUME) {
			paused = false;
		}
		events[count++] = n;
		if (count >= events.length) {
			System.arraycopy(events, 0, events = new int[events.length * 2], 0, count);
		}
		synchronized (eventLock) {
			eventLock.notify();
		}
	}

	private synchronized int nextEvent() {
		if (count == 0) return 0;
		int n = events[0];
		System.arraycopy(events, 1, events, 0, events.length - 1);
		count--;
		return n;
	}

	private Object nextArgument() {
		Object a = null;
		synchronized (eventArguments) {
			if (!eventArguments.isEmpty()) {
				a = eventArguments.remove(0);
			}
		}
		return a;
	}

	public void queueRepaint() {
		repaintX = repaintY = repaintW = repaintH = -1;
		queue(EVENT_PAINT);
	}

	public void queueRepaint(int x, int y, int w, int h) {
		int x1 = x,
			y1 = y,
			x2 = x + w,
			y2 = y + h;
		Displayable d = getCurrent();
		int sw = d.getWidth(), sh = d.getHeight();
		if (repaintX != -1) {
			x1 = Math.min(repaintX, x1);
			y1 = Math.min(repaintY, y1);
			x2 = Math.max(repaintX + repaintW, x2);
			y2 = Math.max(repaintY + repaintH, y2);
		}
		if (x1 < 0) x1 = 0;
		if (y1 < 0) y1 = 0;
		if (x2 > sw) x2 = sw;
		if (y2 > sh) y2 = sh;
		repaintX = x1;
		repaintY = y1;
		repaintW = x2 - x1;
		repaintH = y2 - y1;
		queue(EVENT_PAINT);
	}

	public void itemStateChanged(Item item) {
		eventArguments.add(item);
		queue(EVENT_ITEM_STATE);
	}

	public void commandAction(Command command, Item item) {
		eventArguments.add(command);
		eventArguments.add(item);
		queue(EVENT_COMMAND);
	}

	public void commandAction(Command command, Displayable d) {
		eventArguments.add(command);
		eventArguments.add(d);
		queue(EVENT_COMMAND);
	}

	public void gameGraphicsFlush() {
		synchronized (lock) {
			IScreen scr = Emulator.getEmulator().getScreen();
			final IImage screenImage = scr.getScreenImg();
			final IImage backBufferImage2 = scr.getBackBufferImage();
			final IImage xRayScreenImage2 = scr.getXRayScreenImage();
			(Settings.xrayView ? xRayScreenImage2 : backBufferImage2).cloneImage(screenImage);
			scr.repaint();
		}
	}

	public void gameGraphicsFlush(int x, int y, int w, int h) {
		synchronized (lock) {
			IScreen scr = Emulator.getEmulator().getScreen();
			final IImage screenImage = scr.getScreenImg();
			final IImage backBufferImage2 = scr.getBackBufferImage();
			final IImage xRayScreenImage2 = scr.getXRayScreenImage();
			(Settings.xrayView ? xRayScreenImage2 : backBufferImage2).cloneImage(screenImage, x, y, w, h);
			scr.repaint();
		}
	}

	public void serviceRepaints() {
		if (Settings.ignoreServiceRepaints) return;
		synchronized (lock) {
			if (!repaintPending) return;
			repaintPending = false;
			int x = repaintX, y = repaintY, w = repaintW, h = repaintH;
			repaintX = repaintY = repaintW = repaintH = -1;
			internalRepaint(x, y, w, h);
		}
		Displayable._fpsLimiter(true);
	}

	public void run() {
		int event = 0;
		try {
			while (running) {
				alive = true;
				if (Emulator.getMIDlet() == null || paused) {
					Thread.sleep(5);
					continue;
				}
				try {
					switch (event = nextEvent()) {
						case EVENT_PAINT: {
							synchronized (lock) {
								if (!repaintPending) break;
								repaintPending = false;
								int x = repaintX, y = repaintY, w = repaintW, h = repaintH;
								repaintX = repaintY = repaintW = repaintH = -1;
								internalRepaint(x, y, w, h);
							}
							Displayable._fpsLimiter(true);
							break;
						}
						case EVENT_CALL: {
							processSerialEvent();
							break;
						}
						case EVENT_SCREEN: {
							Displayable d = getCurrent();
							if (!(d instanceof Screen)) break;
							IScreen scr = Emulator.getEmulator().getScreen();
							final IImage backBufferImage3 = scr.getBackBufferImage();
							final IImage xRayScreenImage3 = scr.getXRayScreenImage();
							((Screen) d)._invokePaint(new Graphics(backBufferImage3, xRayScreenImage3));
							try {
								(Settings.xrayView ? xRayScreenImage3 : backBufferImage3)
										.cloneImage(scr.getScreenImg());
							} catch (Exception ignored) {
							}
							scr.repaint();
							int interval = ((Screen) d)._repaintInterval();
							if (interval > 0) {
								synchronized (lock) {
									if (screenTimer == null) {
										screenTimer = new Timer();
									}
									if (screenTimerTask != null) {
										try {
											screenTimerTask.cancel();
										} catch (Exception ignored) {
										}
										screenTimerTask = null;
									}
									screenTimerTask = new ScreenTimerTask();
									screenTimer.schedule(screenTimerTask, interval);
								}
							}
							break;
						}
						case EVENT_START: {
							if (Emulator.getMIDlet() == null) break;
							new Thread(new InvokeStartAppRunnable(true)).start();
							break;
						}
						case EVENT_EXIT: {
							this.stop();
							if (Emulator.getMIDlet() == null) break;
							new Thread(new InvokeDestroyAppRunnable(this)).start();
							break;
						}
						case EVENT_SHOW: {
							Displayable d = getCurrent();
							if (!(getCurrent() instanceof Canvas)) break;
							((Canvas) d)._invokeShowNotify();
							break;
						}
						case EVENT_PAUSE: {
							Displayable d = getCurrent();
							if (!(d instanceof Canvas)) break;
							((Canvas) d)._invokeHideNotify();
							this.paused = true;
							if (Settings.startAppOnResume) {
								try {
									Emulator.getMIDlet().invokePauseApp();
								} catch (Exception e) {
									e.printStackTrace();
								}
							}
							break;
						}
						case EVENT_RESUME: {
							Displayable d = getCurrent();
							if (!(d instanceof Canvas)) break;
							if (Settings.startAppOnResume) {
								try {
									new Thread(new InvokeStartAppRunnable(false)).start();
								} catch (Exception e) {
									e.printStackTrace();
								}
							}
							((Canvas) d)._invokeShowNotify();
							break;
						}
						case EVENT_INPUT: {
							if (inputsCount <= 0) break;
							int[] e;
							synchronized (this) {
								e = inputs[0];
								System.arraycopy(inputs, 1, inputs, 0, inputs.length - 1);
								inputsCount--;
							}
							if (e == null) break;
							synchronized (lock) {
								processInputEvent(e);
							}
							break;
						}
						case EVENT_ITEM_STATE: {
							Item item = (Item) nextArgument();
							Screen screen = item._getParent();
							if (!(screen instanceof Form) || screen != getCurrent()) break;
							((Form) screen)._itemStateChanged(item);
							break;
						}
						case EVENT_COMMAND: {
							Command cmd = (Command) nextArgument();
							Object target = nextArgument();
							if (target instanceof Item) {
								((Item) target)._callCommandAction(cmd);
							} else {
								((Displayable) target)._callCommandAction(cmd);
							}
							break;
						}
						case 0: {
							Displayable._fpsLimiter(false);
							synchronized (eventLock) {
								eventLock.wait(1000);
							}
							break;
						}
						default: {
							if ((event & Integer.MIN_VALUE) == 0) break;
							Displayable d = getCurrent();
							if (d == null) break;
							d._invokeSizeChanged(
									event & 0xFFF,
									(event >> 12) & 0xFFF);
							break;
						}
					}
					if (Settings.processSerialCallsOutOfQueue)
						processSerialEvent();
					Thread.sleep(1);
				} catch (Throwable e) {
					System.err.println("Exception in Event Thread!");
					System.err.println("Event: " + event);
					e.printStackTrace();
				}
			}
		} catch (InterruptedException ignored) {}
	}

	private void processSerialEvent() {
		Runnable r = null;
		synchronized (eventArguments) {
			if (!eventArguments.isEmpty()) {
				r = ((Runnable) eventArguments.remove(0));
			}
		}
		if (r == null) return;
		synchronized (lock) {
			r.run();
		}
	}

	public void callSerially(Runnable run) {
		synchronized (eventArguments) {
			eventArguments.add(run);
		}
		if (!Settings.processSerialCallsOutOfQueue)
        	queue(EVENT_CALL);
	}

	private void internalRepaint(int x, int y, int w, int h) {
		repaintPending = false;
		try {
			Canvas canvas = Emulator.getCanvas();
			if (canvas == null
					|| Emulator.getCurrentDisplay().getCurrent() != Emulator.getCanvas()) {
				return;
			}
			if (Settings.xrayView) Displayable._resetXRayGraphics();
			IScreen scr = Emulator.getEmulator().getScreen();
			final IImage backBufferImage = scr.getBackBufferImage();
			final IImage xRayScreenImage = scr.getXRayScreenImage();
			Displayable._checkForSteps(lock);
			try {
				if (x == -1) { // full repaint
					canvas._invokePaint(backBufferImage, xRayScreenImage);
				} else {
					canvas._invokePaint(backBufferImage, xRayScreenImage, x, y, w, h);
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			if (canvas instanceof SpriteCanvas && ((SpriteCanvas) canvas)._skipCopy) {
				((SpriteCanvas) canvas)._skipCopy = false;
			} else if (canvas instanceof ACanvas && ((ACanvas) canvas)._skipCopy) {
				((ACanvas) canvas)._skipCopy = false;
			} else {
				(Settings.xrayView ? xRayScreenImage : backBufferImage)
						.cloneImage(scr.getScreenImg());
			}
			scr.repaint();
		} catch (Exception e) {
			System.err.println("Exception in repaint!");
			e.printStackTrace();
		}
		try {
			synchronized (repaintLock) {
				repaintLock.notifyAll();
			}
		} catch (Exception ignored) {}
	}

	void processInputEvent(int[] o) {
		if (o == null) return;
		Displayable d = getCurrent();
		if (d == null) return;
		boolean canv = d instanceof Canvas;
		if (o[3] > 0) {
			// keyboard
			int n = o[1];
			switch (o[0]) {
				case 0: {
					Application.internalKeyPress(n);
					if (d.handleSoftKeyAction(n, true)) return;
					if (canv) ((Canvas) d)._invokeKeyPressed(n);
					else ((Screen) d)._invokeKeyPressed(n);
					break;
				}
				case 1: {
					Application.internalKeyRelease(n);
					if (d.handleSoftKeyAction(n, false)) return;
					if (canv) ((Canvas) d)._invokeKeyReleased(n);
					else ((Screen) d)._invokeKeyReleased(n);
					break;
				}
				case 2:
					if (canv) ((Canvas) d)._invokeKeyRepeated(n);
					else ((Screen) d)._invokeKeyRepeated(n);
					break;
			}
		} else {
			// mouse
			int x = o[1];
			int y = o[2];
			switch (o[0]) {
				case 0:
					if (canv) ((Canvas) d).invokePointerPressed(x, y);
					else ((Screen) d)._invokePointerPressed(x, y);
					break;
				case 1:
					if (canv) ((Canvas) d).invokePointerReleased(x, y);
					else ((Screen) d)._invokePointerReleased(x, y);
					break;
				case 2:
					if (canv) ((Canvas) d).invokePointerDragged(x, y);
					else ((Screen) d)._invokePointerDragged(x, y);
					break;
			}
		}
	}

	private Displayable getCurrent() {
		return Emulator.getCurrentDisplay().getCurrent();
	}

	class InputThread implements Runnable {
		private Object[] elements;
		private final Object readLock = new Object();
		private int count;
		private boolean added;

		private InputThread() {
			elements = new Object[16];
		}

		public void queue(int state, int arg1, int arg2, boolean key) {
			append(new int[]{state, arg1, arg2, key ? 1 : 0});
		}

		private void append(Object o) {
			synchronized (this) {
				if (count + 1 >= elements.length) {
					System.arraycopy(elements, 0, elements = new Object[elements.length * 2], 0, count);
				}
			}
			elements[count++] = o;
			added = true;
			synchronized (readLock) {
				readLock.notify();
			}
		}

		public void run() {
			while (running) {
				try {
					if (!added)
						synchronized (readLock) {
							readLock.wait();
						}
					added = false;
					while (Emulator.getMIDlet() == null || paused) {
						Thread.sleep(5);
					}
					while (count > 0) {
						try {
							int[] o = (int[]) elements[0];
							if (Settings.synchronizeKeyEvents) {
								synchronized (lock) {
									processInputEvent(o);
								}
							} else processInputEvent(o);
						} catch (Throwable e) {
							System.err.println("Exception in Input Thread!");
							e.printStackTrace();
						}
						synchronized (this) {
							System.arraycopy(elements, 1, elements, 0, elements.length - 1);
							elements[--count] = null;
						}
					}
				} catch (Throwable e) {
					System.err.println("Exception in Input Thread!");
					e.printStackTrace();
				}
			}
		}
	}

	private class ScreenTimerTask extends TimerTask {
		public void run() {
			queue(EVENT_SCREEN);
		}
	};
}
