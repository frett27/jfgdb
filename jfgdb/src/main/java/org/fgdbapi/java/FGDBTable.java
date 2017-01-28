package org.fgdbapi.java;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;

import org.fgdbapi.thindriver.swig.SpatialReference;
import org.fgdbapi.thindriver.xml.EsriFieldType;
import org.fgdbapi.thindriver.xml.Field;
import org.fgdbapi.thindriver.xml.GeographicCoordinateSystem;
import org.fgdbapi.thindriver.xml.GeometryDef;
import org.fgdbapi.thindriver.xml.ProjectedCoordinateSystem;

public class FGDBTable extends FGDBBaseObject {

	public FGDBTable(File file) throws Exception {
		super(file);
	}

	int rowNumber;

	long fieldsOffset;

	long offsetRecords;

	int numberOfNullableFields;

	public int getRowNumber() {
		return rowNumber;
	}
	
	@Override
	protected void readHeader() throws Exception {

		// Header (40 bytes)

		// 4 bytes: 0x03 0x00 0x00 0x00 - unknown role. Constant among the
		// files. Kind of signature ?
		read(4);

		// int32: number of (valid) rows
		rowNumber = (int)read(4);

		// 4 bytes: varying values - unknown role (TBC : this value does have
		// something to do with row size. A value larger than the size of the
		// largest row seems to be ok)
		read(4);

		// 4 bytes: 0x05 0x00 0x00 0x00 - unknown role. Constant among the files
		read(4);

		// 4 bytes: varying values - unknown role. Seems to be 0x00 0x00 0x00
		// 0x00 for FGDB 10 files, but not for earlier versions
		read(4);

		// 4 bytes: 0x00 0x00 0x00 0x00 - unknown role. Constant among the files
		read(4);

		// int32: file size in bytes
		long filesize = read(4);

		// 4 bytes: 0x00 0x00 0x00 0x00 - unknown role. Constant among the files
		long zeros = read(4);
		assert zeros == 0;

		// int32: offset in bytes at which the field description section begins
		// (often 40 in FGDB 10)
		fieldsOffset = read(4);

		// 4 bytes: 0x00 0x00 0x00 0x00 - unknown role. Constant among the files
		zeros = read(4);
		assert zeros == 0;

		readFields();

		readOffsetRecords();
	}

	ArrayList<Field> fields;

	private void readFields() throws Exception {
		fields = new ArrayList<>();
		raf.seek(fieldsOffset);

		// Fixed part
		//
		// int32: size of header in bytes (this field excluded)
		long headerSize = read(4);
		// int32: version of the file ? Seems to be 3 for FGDB 9.X files and 4
		// for FGDB 10.X files
		long version = read(4);
		// ubyte: layer geometry type. 0 = none, 1 = point, 2 = multipoint, 3=
		// (multi)polyline, 4 = (multi)polygon, 9=multipatch
		// 3 bytes: 0x03 0x00 0x00 - unknown role

		long geometryType = read(1);

		read(3);

		// int16: number of fields (including geometry field and implicit
		// OBJECTID field)
		long fieldNumber = read(2);

		// read fields

		for (int i = 0; i < fieldNumber; i++) {

			// ubyte: number of UTF-16 characters (not bytes) of the name of the
			// field
			// utf16: name of the field
			String name = readStringWithSizeHeader();
			System.out.println(name);
			// ubyte: number of UTF-16 characters (not bytes) of the alias of
			// the field. Might be 0
			// utf16: alias of the field (ommitted if previous field is 0)

			String alias = readStringWithSizeHeader();

			// ubyte: field type ( 0 = int16, 1 = int32, 2 = float32, 3 =
			// float64, 4 = string, 5 = datetime, 6 = objectid, 7 = geometry, 8
			// = binary, 9=raster, 10/11 = UUID, 12 = XML )
			int fieldType = (int) read(1);

			Field f = new Field();
			f.setName(name);
			f.setAliasName(alias);

			int length;
			boolean nullable = true;
			String wkt;
			int flag;

			switch (fieldType) {

			case 1:
				// 0 = int16, 1 = int32
				f.setType(EsriFieldType.ESRI_FIELD_TYPE_INTEGER);
				f.setLength((int) read(1));
				flag = (int) read(1);
				int ldf = (int) read(1);

				if (ldf > 0) {
					byte[] buffer = new byte[ldf];
					if (raf.read(buffer, 0, buffer.length) != buffer.length) {
						throw new Exception("eof reach");
					}
				}
				f.setIsNullable((flag & 1) != 0);
				fields.add(f);

				break;

			// For field type = 4 (string),
			//
			// int32: maximum length of string
			// ubyte: flag
			// varuint: ldf = length of default value in byte if (flag&4) != 0
			// followed by ldf bytes with the default value numeric

			case 4:
				f.setType(EsriFieldType.ESRI_FIELD_TYPE_STRING);
				length = (int) read(4);
				f.setLength(length);
				//f.setIsNullable(value);

				flag = (int) read(1);
				if ((flag & 0x4) != 0) {
					String defaultValue = readStringWithSizeHeader();
					// System.out.println(defaultValue);
				}

				if ( ((flag & 0x01) != 0 )) {
					f.setIsNullable(true);
				}
				
				
				fields.add(f);
				break;

			// For field type = 6 (objectid),
			// ubyte: unknown role = 4
			// ubyte: unknown role = 2

			case 6:
				f.setType(EsriFieldType.ESRI_FIELD_TYPE_OID);
				read(2);
				fields.add(f);
				break;

			case 7:
				f.setType(EsriFieldType.ESRI_FIELD_TYPE_GEOMETRY);

				GeometryDef gdef = new GeometryDef();

				f.setGeometryDef(gdef);

				// ubyte: unknown role = 0
				read(1);

				// ubyte: flag = 6 or 7. If lsb is 1, the field can be null.
				nullable = (read(1) & 1) != 0;

				f.setIsNullable(nullable);

				// int16: length (in bytes) of the WKT string describing the
				// SRS.
				// string: WKT string describing the SRS Or
				// {B286C06B-0879-11D2-AACA-00C04FA33C20} for no SRS .

				wkt = readWideStringWithSizeHeader();
				org.fgdbapi.thindriver.xml.SpatialReference r = null;
				if (wkt.startsWith("GEOGCS")) {
					GeographicCoordinateSystem gcs = new GeographicCoordinateSystem();
					gcs.setWKT(wkt);
					r = gcs;
				} else if (wkt.startsWith("PROJ")) {
					ProjectedCoordinateSystem projcs = new ProjectedCoordinateSystem();
					projcs.setWKT(wkt);
					r = projcs;
				}

				gdef.setSpatialReference(r);

				// ubyte: flags. Value is generally 1 (has_z = has_m = false,
				// generally for system tablea00000004.gdbtable ),
				// 5 (has_z = true, has_m = false)
				// or 7 (has_z = has_m = true)

				flag = (int) read(1);

				switch (flag) {
				case 1:
					gdef.setHasZ(false);
					gdef.setHasM(false);
					break;
				case 5:
					gdef.setHasZ(true);
					gdef.setHasM(false);
					break;
				case 7:
					gdef.setHasZ(true);
					gdef.setHasM(true);
					break;
				default:
					throw new Exception("unsupported value for field flag " + flag);
				}

				// float64: xorigin
				r.setXOrigin(readDouble());
				// float64: yorigin
				r.setYOrigin(readDouble());

				// float64: xyscale
				r.setXYScale(readDouble());

				if (gdef.isHasM()) {
					// float64: morigin (present only if has_m = True)
					r.setMOrigin(readDouble());

					// float64: mscale (present only if has_m = True)
					r.setMScale(readDouble());
				}

				if (gdef.isHasZ()) {
					// float64: zorigin (present only if has_z = True)
					r.setZOrigin(readDouble());

					// float64: zscale (present only if has_z = True)
					r.setZScale(readDouble());

				}
				// float64: xytolerance

				r.setXYTolerance(readDouble());
				if (gdef.isHasM()) {
					// float64: mtolerance (present only if has_m = True)
					r.setMTolerance(readDouble());
				}
				if (gdef.isHasZ()) {
					// float64: ztolerance (present only if has_z = True)
					r.setZTolerance(readDouble());

				}

				// float64: xmin of layer extent (might be NaN)
				read(8);
				// float64: ymin of layer extent (might be NaN)
				read(8);
				// float64: xmax of layer extent (might be NaN)
				read(8);
				// float64: ymax of layer extent (might be NaN)
				read(8);

				fields.add(f);

				while (true) {
					long pos = raf.getFilePointer();
					byte[] buffer = new byte[5];
					if (raf.read(buffer, 0, 5) != 5)
						throw new Exception("eof reach");

					if (buffer[0] == 0 && buffer[2] == 0 && buffer[3] == 0 && buffer[4] == 0
							&& (buffer[1] == 1 || buffer[1] == 2 || buffer[1] == 3)) {
						for (int j = 0; j < buffer[1]; j++) {
							readDouble();
						}
						break;
					} else {
						raf.seek(pos);
						readDouble();
					}
				}

				break;
			default:
				throw new Exception("unsupported field type :" + fieldType);
			}

		}

		// once fields are read,
		// count the number of nullable records

		for (Field f : fields) {
			if (f.isIsNullable())
				numberOfNullableFields += 1;
		}

	}

	private void readOffsetRecords() throws Exception {

		// The rows section does not necessarily immediately follow the last
		// field description. It starts generally a few bytes after, but not in
		// a predictable way. Note : for FGDB layers created by the ESRI FGDB
		// SDK API, there are 4 bytes between the end of the field description
		// section and the beginning of the rows section : 0xDE 0xAD 0xBE 0xEF
		// (!)

		raf.seek(40); // after header size

		while (true) {

			byte[] buffer = new byte[4];
			long current = raf.getFilePointer();

			if (raf.read(buffer, 0, 4) != 4) {
				throw new Exception("eof reach");
			}
			// System.out.println(Arrays.asList(buffer));
			if (buffer[0] == (byte) 0xDE /* -22 */ && buffer[1] == (byte) 0xAD /* -53 */
					&& buffer[2] == (byte) 0xBE /* 42 */
					&& buffer[3] == (byte) 0xEF /* -11 */ ) {
				offsetRecords = raf.getFilePointer();
				break;
			} else {
				raf.seek(current);
				read(1);
			}

		}

		System.out.println("record section found");

	}

	/**
	 * 
	 * @param offset
	 * @throws Exception
	 */
	public Object[] readRow(long offset, int objectid) throws Exception {

		raf.seek(offset);

		long length = read(4);
		int nb = (int) Math.ceil(numberOfNullableFields * 1.0 / 8);
		byte[] nullRecords = new byte[nb + (8 - nb)];

		if (raf.read(nullRecords, 0, nb) != nb) {
			throw new Exception("eof reach");
		}

		long nulls = ByteBuffer.wrap(nullRecords).order(ByteOrder.LITTLE_ENDIAN).getLong();

		Object[] result = new Object[fields.size()];

		int index = 0;
		int nullableIndex = -1;
		for (Field f : fields) {

			if (f.isIsNullable()) {
				nullableIndex ++;
			}
			
			// null ?
			if (f.getType() != EsriFieldType.ESRI_FIELD_TYPE_OID) {

				if ((nulls & (1 << nullableIndex)) != 0) {
					// field is null, don't read value
					index++;
					continue;
				}
			}

			switch (f.getType()) {

			case ESRI_FIELD_TYPE_OID:
				result[index] = objectid;
				break;

			case ESRI_FIELD_TYPE_GEOMETRY:
				int geomlength = (int) readVarUintGeom();
				byte[] geom = new byte[geomlength];
				if (raf.read(geom, 0, geomlength) != geomlength) {
					throw new Exception("eof reach");
				}

				result[index] = geom;
				break;

			case ESRI_FIELD_TYPE_STRING:
			case ESRI_FIELD_TYPE_XML:

				result[index] = readVarUIntString();
				break;

			case ESRI_FIELD_TYPE_INTEGER:

				long l = read(f.getLength());
				result[index] = l; // TODO proper type, not only long

				break;

			default:
				throw new Exception("unsupported field type");
			}

			index++;

		}

		return result;
	}

}
