/*
 * Copyright 2023 Yury Kharchenko
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

package com.mexa.opgl;

import org.lwjgl.BufferUtils;

public class IntBuffer extends Buffer implements com.nttdocomo.ui.ogl.IntBuffer {

	private IntBuffer(int size) {
		super(BufferUtils.createIntBuffer(size));
	}

	private IntBuffer(IntBuffer buffer) {
		this(buffer.length());
		java.nio.IntBuffer nio = (java.nio.IntBuffer) super.buffer;
		nio.put((java.nio.IntBuffer) buffer.getNioBuffer());
	}

	IntBuffer(ByteBuffer b) {
		super(((java.nio.ByteBuffer) b.getNioBuffer()).asShortBuffer());
	}

	public static IntBuffer allocateDirect(int size) {
		return new IntBuffer(size);
	}

	public static IntBuffer allocateDirect(IntBuffer buffer) {
		return new IntBuffer(buffer);
	}

	public int[] get(int paramInt, int[] paramArrayOfInt) {
		return get(paramInt, paramArrayOfInt, 0, paramArrayOfInt.length);
	}

	public int[] get(int srcIndex, int[] buf, int dstIndex, int length) {
		java.nio.IntBuffer nio = (java.nio.IntBuffer) super.buffer;
		nio.position(srcIndex);
		nio.get(buf, dstIndex, length);
		return buf;
	}

	public void put(int paramInt, int[] paramArrayOfInt) {
		put(paramInt, paramArrayOfInt, 0, paramArrayOfInt.length);
	}

	public void put(int dstIndex, int[] buf, int srcIndex, int length) {
		java.nio.IntBuffer nio = (java.nio.IntBuffer) super.buffer;
		nio.position(dstIndex);
		nio.put(buf, srcIndex, length);
	}
}