/*
 * Copyright 2022-2023 Yury Kharchenko
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ru.woesss.j2me.micro3d;

import emulator.Settings;
import emulator.custom.ResourceManager;

import javax.microedition.lcdui.Image;
import java.io.IOException;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.GL_CLAMP_TO_EDGE;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;

public final class TextureImpl {
	static int sLastId;

	public final TextureData image;
	private final boolean isMutable;

	int mTexId = -1;

	public TextureImpl() {
		image = new TextureData(256, 256);
		isMutable = true;
	}

	public TextureImpl(byte[] b) {
		if (b == null) {
			throw new NullPointerException();
		}
		try {
			image = Loader.loadBmpData(b, 0, b.length);
		} catch (IOException e) {
			System.err.println("Error loading data");
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		isMutable = false;
	}

	public TextureImpl(byte[] b, int offset, int length) throws IOException {
		if (b == null) {
			throw new NullPointerException();
		}
		if (offset < 0 || offset + length > b.length) {
			throw new ArrayIndexOutOfBoundsException();
		}
		try {
			image = Loader.loadBmpData(b, offset, length);
		} catch (Exception e) {
			System.err.println("Error loading data");
			e.printStackTrace();
			throw e;
		}
		isMutable = false;
	}

	public TextureImpl(String name) throws IOException {
		if (name == null) {
			throw new NullPointerException();
		}
		byte[] b = ResourceManager.getBytes(name);
		if (b == null) {
			throw new IOException();
		}
		try {
			image = Loader.loadBmpData(b, 0, b.length);
		} catch (IOException e) {
			System.err.println("Error loading data from [" + name + "]");
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		isMutable = false;
	}

	public TextureImpl(Image image, int x, int y, int width, int height) {
		if (image == null) {
			throw new NullPointerException();
		} else if (x < 0 || y < 0 || width <= 0 || height <= 0 ||
				x + width > image.getWidth() || y + height > image.getHeight()) {
			throw new IllegalArgumentException();
		}
		isMutable = false;
		this.image = new TextureData(width, height);
		int len = width * height;
		int[] pixels = new int[len];
		image.getRGB(pixels, 0, width, x, y, width, height);
		for (int i = 0; i < len; i++) {
			int pixel = pixels[i];
			pixels[i] = pixel << 8 | pixel >>> 24;
		}
		this.image.getRaster().asIntBuffer().put(pixels);
	}

	public void dispose() {
	}

	public boolean isMutable() {
		return isMutable;
	}

	int getId() {
		if (!glIsTexture(mTexId)) {
			generateId();
		} else if (!isMutable) {
			return mTexId;
		}
		loadToGL();
		return mTexId;
	}

	public int getWidth() {
		return image.width;
	}

	public int getHeight() {
		return image.height;
	}

	private void generateId() {
		final IntBuffer textureIds = BufferUtils.createIntBuffer(1);
		synchronized (TextureImpl.class) {
			while (textureIds.get(0) <= sLastId) {
				textureIds.rewind();
				glGenTextures(/*1, */textureIds);
			}
			sLastId = textureIds.get(0);
			mTexId = textureIds.get(0);
		}
		Render.checkGlError("glGenTextures");
	}

	private void loadToGL() {
		glActiveTexture(GL_TEXTURE0);
		glBindTexture(GL_TEXTURE_2D, mTexId);

		boolean filter = Settings.mascotTextureFilter;
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, filter ? GL_LINEAR : GL_NEAREST);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, filter ? GL_LINEAR : GL_NEAREST);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
		glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, image.width, image.height, 0, GL_RGBA, GL_UNSIGNED_BYTE, image.getRaster());

		glBindTexture(GL_TEXTURE_2D, 0);
	}

	@Override
	protected void finalize() throws Throwable {
		try {
			dispose();
		} finally {
			super.finalize();
		}
	}
}