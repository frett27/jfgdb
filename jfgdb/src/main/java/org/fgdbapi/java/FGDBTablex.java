package org.fgdbapi.java;

import java.io.File;

class FGDBTablex extends FGDBBaseObject {

	public FGDBTablex(File file) throws Exception {
		super(file);

	}

	long n1024BlocksPresent;

	long number_of_rows;

	/**
	 * nb of bytes for offset
	 */
	int size_offset; // nb of bytes

	long array_start;

	byte[] pabyTablXBlockMap;

	@Override
	protected void readHeader() throws Exception {

		// 4 bytes: 0x03 0x00 0x00 0x00 - unknown role. Constant among the
		// files. Kind of signature ?
		read(4);

		// int32: n1024BlocksPresent = number of blocks of offsets for 1024
		// features that are effectively present in that file (ie sparse blocks
		// are not counted in that number).
		n1024BlocksPresent = read(4);

		// int32: number_of_rows : number of rows, included deleted rows
		number_of_rows = read(4);
		// int32: size_offset = number of bytes to encode each feature offset.
		// Must be 4 (.gdbtable up to 4GB), 5 (.gdbtable up to 1TB) or 6
		// (.gdbtable up to 256TB)
		size_offset = (int) read(4);

		//
		array_start = raf.getFilePointer();

		// read trailing block

		raf.seek(array_start + size_offset * n1024BlocksPresent * 1024);

		// int32: nBitmapInt32Words = number of int32 words for the bitmap
		// (rounded to the next multiple of 32)

		long nBitmapInt32Words = read(4);
		// if (nBitmapInt32Words % 32 > 0) {
		// nBitmapInt32Words = ((nBitmapInt32Words >> 5) + 1) << 5;
		// }

		// int32: n1024BlocksTotal = (number_of_rows + 1023) / 1024. In the case
		// where there's a bitmap, this is also nBitsForBlockMap = number of
		// bits in the block map.

		long n1024BlocksTotal = read(4);

		// int32: n1024BlocksPresentBis (must be == n1024BlocksPresent of the
		// header)

		long n1024BlocksPresentBis = read(4);

		// int32: nUsefulBitmapIn32Words = number of int32 words in the bitmap
		// where there's at least a non-zero bit. Said otherwise, all following
		// words until the end of the bitmap are 0. Doesn't seem to be used by
		// proprietary implementations.

		long nUsefulBitmapIn32Words = read(4);

		// if nBitmapInt32Words == 0 (no bitmap), then n1024BlocksTotal ==
		// n1024BlocksPresentBis ( == n1024BlocksPresent) and
		// nUsefulBitmapIn32Words = 0
		//

		if (nBitmapInt32Words == 0) {
			 assert n1024BlocksTotal == n1024BlocksPresentBis;
			 	assert n1024BlocksTotal == n1024BlocksPresent;
			 	assert nUsefulBitmapIn32Words == 0;
		} else
		if (nBitmapInt32Words != 0) {

			// Otherwise, following those 16 trailer bytes, there is a bit array
			// of
			// at least (n1024BlocksTotal + 7) / 8 bytes (in practice its size
			// is
			// rounded to the next muliple of 32 int32 words). Each bit in the
			// array
			// represents the presence of a block of offsets for 1024 features
			// (bit
			// = 1), or its absence (bit = 0). The total number of bits set to 1
			// must be equal to n1024Blocks
			//

			int l = ((int) n1024BlocksTotal + 7) / 8;
			pabyTablXBlockMap = new byte[l];
			for (int i = 0; i < l; i++) {
				pabyTablXBlockMap[i] = (byte) 0xFF;
			}

			int r = raf.read(pabyTablXBlockMap, 0, l);
			if (r != l) {
				throw new Exception("eof reached");
			}

		}

	}

	/**
	 * 
	 * @param irow
	 * @return
	 * @throws Exception
	 */
	public int getCorrectedRow(int irow) throws Exception {

		if (pabyTablXBlockMap == null)
			return irow;
		
		long nCountBlocksBefore = 0;
		int iBlock = irow / 1024;

		// Check if the block is not empty
		if ((pabyTablXBlockMap[iBlock / 8] & (1 << (iBlock % 8))) == 0) {
			return -1;
		}

		for (int i = 0; i < iBlock; i++) {
			boolean present = (pabyTablXBlockMap[i / 8] & (1 << (i % 8))) != 0;
			nCountBlocksBefore += (present ? 1 : 0);
		}

		return (int) (nCountBlocksBefore * 1024 + (irow % 1024));

	}

	public long getRecordOffset(int iCorrectedRow) throws Exception {
		raf.seek(array_start + iCorrectedRow * size_offset);
		long offset = read(size_offset);
		return offset;
	}

}
