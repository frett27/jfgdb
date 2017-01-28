package org.fgdbapi.java;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class FGDBBaseObject {

	protected RandomAccessFile raf;

	public FGDBBaseObject(File file) throws Exception {
		this.raf = new RandomAccessFile(file, "r");
		readHeader();
	}

	
	protected double readDouble() throws Exception {
		long l = read(8);
		return Double.longBitsToDouble(l);
	}
	
	protected long read(int length) throws Exception {

		byte[] data = new byte[8];

		if (raf.read(data, 0, length) != length) {
			throw new Exception("eof encountered");
		}

		return ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN).getLong();

	}

	protected String readStringWithSizeHeader() throws Exception {
		int size = (int) read(1);
		byte[] data = new byte[size * 2];
		if (raf.read(data, 0, size * 2) != size * 2) {
			throw new Exception("eof encountered");
		}
		return new String(data, "UTF-16le");
	}

	protected String readWideStringWithSizeHeader() throws Exception {
		int size = (int) read(2);
		byte[] data = new byte[size];
		if (raf.read(data, 0, size) != size) {
			throw new Exception("eof encountered");
		}
		return new String(data, "UTF-16le");
	}

	protected void readHeader() throws Exception {

	}

}
