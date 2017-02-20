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

	protected float readFloat() throws Exception {
		long l = read(4);
		float f = Float.intBitsToFloat((int)l);
		return f;
	}
	
	
	protected long read(int length) throws Exception {

		byte[] data = new byte[8];
		assert length <=8;
	
		if (raf.read(data, 0, length) != length) {
			throw new Exception("eof encountered");
		}

		return ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN).getLong();

	}

	protected String readStringWithSizeHeader() throws Exception {
		int size = (int)read(1);
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
	
	protected String readVarUIntString() throws Exception {
		
		long length = readVarUintGeom();
		
		byte[] content = new byte[(int)length];
		if ((raf.read(content,0,(int)length)) != length) {
			throw new Exception("eof reach");
		}
		
		
		return new String(content, "UTF-8");
		
	}
//
//	protected long readVarUint() throws Exception {
//
//		long current = 0;
//
//		int read;
//		do {
//			current = current << 7;
//			if ((read = raf.read()) == -1) {
//				throw new Exception("eof reach");
//			}
//			current = current + (read & 0x7F);
//
//		} while ((read & 0x80) != 0);
//
//		return current;
//	}


	protected long readVarUintGeom() throws Exception {

		long current = 0;
		long shift = -1;
		int read;
		do {
			shift++;
			if ((read = raf.read()) == -1) {
				throw new Exception("eof reach");
			}
			current = current + ((read & 0x7F) << (shift * 7));

		} while ((read & 0x80) != 0);

		return current;
	}

	protected void readHeader() throws Exception {

	}

}
