/*
 *  Copyright 2020 Yury Kharchenko
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.motorola.graphics.j3d;

import ru.woesss.j2me.micro3d.TextureImpl;

import java.io.IOException;

public class Texture {
	public final TextureImpl impl;
	final boolean isForModel;

	public Texture(byte[] b, boolean isForModel) {
		this.isForModel = isForModel;
		impl = new TextureImpl(b);
	}

	public Texture(String name, boolean isForModel) throws IOException {
		this.isForModel = isForModel;
		impl = new TextureImpl(name);
	}
}