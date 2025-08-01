package emulator.lcdui;

import javax.microedition.lcdui.Font;
import java.util.ArrayList;

public final class TextUtils {
	public TextUtils() {
		super();
	}

	private static String[] split(String s, char c) {
		char[] arr = s.toCharArray();
		int i = 0;
		ArrayList list = null;

		for (int j = 0; j < arr.length; ++j) {
			if (arr[j] == c) {
				if (list == null) {
					list = new ArrayList();
				}

				list.add(new String(arr, i, j - i));
				i = j + 1;
			}
		}

		if (list == null) {
			return new String[]{s};
		} else {
			if (i < arr.length) {
				list.add(new String(arr, i, arr.length - i));
			}

			return (String[]) list.toArray(new String[list.size()]);
		}
	}

	public static String[] textArr(String s, Font font, int availableWidth, int maxWidth, int[] w) {
		w[0] = 0;
		if (s == null || maxWidth < font.charWidth(' ') + 8) return new String[0];
		if (availableWidth > 0 && maxWidth > 0) {
			boolean var4 = s.indexOf(10) != -1;
			if (font.stringWidth(s) <= availableWidth) {
				setWidth(w, font.stringWidth(s));
				return var4 ? split(s, '\n') : new String[]{s};
			} else {
				ArrayList list = new ArrayList();
				if (!var4) {
					splitToWidth(s, font, availableWidth, maxWidth, list, w);
				} else {
					char[] var7 = s.toCharArray();
					int var8 = 0;

					for (int var9 = 0; var9 < var7.length; ++var9) {
						if (var7[var9] == 10 || var9 == var7.length - 1) {
							String var11 = var9 == var7.length - 1 ? new String(var7, var8, var9 + 1 - var8) : new String(var7, var8, var9 - var8);
							if (setWidth(w, font.stringWidth(var11)) <= availableWidth) {
								list.add(var11);
							} else {
								splitToWidth(var11, font, availableWidth, maxWidth, list, w);
							}

							var8 = var9 + 1;
							availableWidth = maxWidth;
						}
					}
				}

				return (String[]) list.toArray(new String[list.size()]);
			}
		} else {
			return new String[]{s};
		}
	}

	private static int setWidth(int[] w, int stringWidth) {
		if (stringWidth > w[0]) w[0] = stringWidth;
		return stringWidth;
	}

	public static String[] textArr(String s, Font font, int x1, int x2) {
		if (s == null || x2 < font.charWidth(' ') + 8) return new String[0];
		if (x1 > 0 && x2 > 0) {
			boolean var4 = s.indexOf('\n') != -1;
			if (font.stringWidth(s) <= x1) {
				return var4 ? split(s, '\n') : new String[]{s};
			} else {
				ArrayList list = new ArrayList();
				if (!var4) {
					splitToWidth(s, font, x1, x2, list, null);
				} else {
					char[] var7 = s.toCharArray();
					int var8 = 0;

					for (int var9 = 0; var9 < var7.length; ++var9) {
						if (var7[var9] == 10 || var9 == var7.length - 1) {
							String var11 = var9 == var7.length - 1 ? new String(var7, var8, var9 + 1 - var8) : new String(var7, var8, var9 - var8);
							if (font.stringWidth(var11) <= x1) {
								list.add(var11);
							} else {
								splitToWidth(var11, font, x1, x2, list, null);
							}

							var8 = var9 + 1;
							x1 = x2;
						}
					}
				}

				return (String[]) list.toArray(new String[list.size()]);
			}
		} else {
			return new String[]{s};
		}
	}

	private static void splitToWidth(String s, Font font, int x1, int x2, ArrayList list, int[] tw) {
		char[] arr = s.toCharArray();
		int k = 0;
		int i = 0;
		int w = 0;

		while (true) {
			while (i < arr.length) {
				if ((w += font.charWidth(arr[i])) > x1) {
					int j = i;

					while (arr[j] != ' ') {
						--j;
						if (j < k) {
							j = i;
							break;
						}
					}
					String t = new String(arr, k, j - k);
					if (tw != null) {
						int rw = font.stringWidth(t);
						if (rw > tw[0]) tw[0] = rw;
					}
					list.add(t);
					k = arr[j] != ' ' && arr[j] != '\n' ? j : j + 1;
					w = 0;
					i = k;
					x1 = x2;
				} else {
					++i;
				}
			}

			list.add(new String(arr, k, i - k));
			return;
		}
	}

}
