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

package com.vodafone.v10.graphics.j3d;

import java.io.IOException;

public class ActionTable {
	final com.mascotcapsule.micro3d.v3.ActionTable impl;

	public ActionTable(byte[] b) {
		impl = new com.mascotcapsule.micro3d.v3.ActionTable(b);
	}

	public ActionTable(String name) throws IOException {
		impl = new com.mascotcapsule.micro3d.v3.ActionTable(name);
	}

	public final int getNumAction() {
		return impl.getNumActions();
	}

	public final int getNumFrame(int idx) {
		return impl.getNumFrames(idx);
	}
}