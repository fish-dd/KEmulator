/*
 *  Copyright 2018 Nikita Shakarun
 *  Copyright 2021 Yury Kharchenko
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

package com.siemens.mp.io;

import emulator.Emulator;

import java.io.*;
import java.nio.channels.FileChannel;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class File {
	public static final int INSIDE_STORAGE_PATH = 1;
	public static final int OUTSIDE_STORAGE_PATH = 0;
	public static final String STORAGE_DRIVE = "a:";

	private static final Map<Integer, RandomAccessFile> OPENED_FILES = new HashMap<Integer, RandomAccessFile>();
	private static int lastDescriptor;

	public static String buildPath(String fileName) {
		// FIXME: 12.07.2021
		return fileName;
	}

	public static int checkFileName(String fileName) {
		return fileName.indexOf(':') == -1 ? INSIDE_STORAGE_PATH : OUTSIDE_STORAGE_PATH;
	}

	public int close(int fileDescriptor) throws IOException {
		RandomAccessFile file = OPENED_FILES.get(fileDescriptor);
		if (file != null) {
			OPENED_FILES.remove(fileDescriptor);
			file.close();
			return fileDescriptor;
		}
		return -1;
	}

	public static int copy(String sourcePath, String destPath) throws IOException {
		java.io.File source = getFile(sourcePath);
		java.io.File dest = getFile(destPath);
		FileInputStream fis = new FileInputStream(source);
		try {
			FileChannel sourceChannel = fis.getChannel();
			try {
				FileOutputStream fos = new FileOutputStream(dest);
				try {
					FileChannel destChannel = fos.getChannel();
					try {
						destChannel.transferFrom(sourceChannel, 0, sourceChannel.size());
					} finally {
						destChannel.close();
					}
				} finally {
					fos.close();
				}
			} finally {
				sourceChannel.close();
			}
		} finally {
			fis.close();
		}
		return 1;
	}

	public static int debugWrite(String fileName, String infoString) throws IOException {
		FileWriter writer = new FileWriter(getFile(fileName), true);
		try {
			writer.write(infoString);
		} finally {
			writer.close();
		}
		return 1;
	}

	public static int delete(String fileName) {
		return getFile(fileName).delete() ? 1 : -1;
	}

	public static int exists(String fileName) throws IOException {
		return getFile(fileName).exists() ? 1 : -1;
	}

	public static boolean getIsHidden(String fileName) throws IOException {
		return getFile(fileName).isHidden();
	}

	public static long getLastModified(String fileName) throws IOException {
		return getFile(fileName).lastModified();
	}

	public static boolean isDirectory(String fileName) throws IOException {
		return getFile(fileName).isDirectory();
	}

	public int length(int fileDescriptor) throws IOException {
		RandomAccessFile file = OPENED_FILES.get(fileDescriptor);
		if (file == null) {
			return -1;
		}
		return (int) file.length();
	}

	public static String[] list(String pathName) throws IOException {
		return list(pathName, false);
	}

	public static String[] list(String pathName, boolean includeHidden) throws IOException {
		FilenameFilter filter = includeHidden ? null : (dir, name) -> name.charAt(0) != '.';
		String[] files = getFile(pathName).list(filter);
		if (files == null) {
			return new String[0];
		}
		Arrays.sort(files);
		return files;
	}

	public int open(String fileName) throws IOException {
		java.io.File file = getFile(fileName);
		RandomAccessFile raf = new RandomAccessFile(file, "rw");
		int fd = ++lastDescriptor;
		OPENED_FILES.put(fd, raf);
		return fd;
	}

	public int read(int fileDescriptor, byte[] buf, int offset, int numBytes) throws IOException {
		RandomAccessFile file = OPENED_FILES.get(fileDescriptor);
		if (file == null) {
			return -1;
		}
		return file.read(buf, offset, numBytes);
	}

	public static int rename(String source, String dest) {
		return getFile(source).renameTo(getFile(dest)) ? 1 : -1;
	}

	public int seek(int fileDescriptor, int seekpos) throws IOException {
		RandomAccessFile file = OPENED_FILES.get(fileDescriptor);
		if (file == null) {
			return -1;
		}
		file.seek(seekpos);
		return (int) file.getFilePointer();
	}

	public static int spaceAvailable() throws IOException {
		return (int) new java.io.File(Emulator.getUserPath()).getFreeSpace();
	}

	public static void truncate(int fileDescriptor, int size) throws IOException {
		RandomAccessFile file = OPENED_FILES.get(fileDescriptor);
		if (file == null) {
			return;
		}
		file.setLength(size);
	}

	public int write(int fileDescriptor, byte[] buf, int offset, int numBytes) throws IOException {
		RandomAccessFile file = OPENED_FILES.get(fileDescriptor);
		if (file == null) {
			return -1;
		}
		file.write(buf, offset, numBytes);
		return numBytes;
	}

	private static java.io.File getFile(String fileName) {
		String r = Emulator.getUserPath() + "/file/root/";
		try {
			new java.io.File(r).mkdirs();
		} catch (Exception ignored) {}
		int colon = fileName.indexOf(':');
		if (colon == -1) {
			return new java.io.File(r, fileName);
		} else {
			fileName = fileName.substring(colon + 2);
			return new java.io.File(r, fileName);
		}
	}
}