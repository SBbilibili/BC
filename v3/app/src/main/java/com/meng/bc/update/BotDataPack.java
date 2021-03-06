package com.meng.bc.update;

import com.meng.bc.libs.*;
import java.io.*;
import java.util.*;

public class BotDataPack {

	public ArrayList<Byte> data=new ArrayList<>();
	public byte[] dataArray;
	public static final short headLength=10;
	public int dataPointer=0;

	public static final byte typeByte=0;
	public static final byte typeShort=1;
	public static final byte typeInt=2;
	public static final byte typeLong=3;
	public static final byte typeFloat=4;
	public static final byte typeDouble=5;
	public static final byte typeString=6;
	public static final byte typeBoolean=7;
	public static final byte typeFile=8;

	public static final int opTextNotify = 32;

	public static final int opGetApp = 53;
	public static final int opCrashLog = 54;

	public static final int sendToMaster = 56;
	public static final int getIdFromHash = 57;
	public static final int cookie = 58;

	public static BotDataPack encode(int opCode) {
		return new BotDataPack(opCode);
	}

	public static BotDataPack decode(byte[] bytes) {
		return new BotDataPack(bytes);
	}

	private BotDataPack(int opCode) {
		//length(4) version(2) opCode(4)
		writeByteDataIntoArray(Tools.BitConverterlittleEndian.getBytes(0));
		writeByteDataIntoArray(Tools.BitConverterlittleEndian.getBytes((short)1));
		writeByteDataIntoArray(Tools.BitConverterlittleEndian.getBytes(opCode));
	}   

	private BotDataPack(byte[] pack) {
		dataArray = pack;
		dataPointer = headLength;
	} 

	public byte[] getData() {
		byte[] retData=new byte[data.size()];
		for (int i=0;i < data.size();++i) {
			retData[i] = data.get(i);
		}
		byte[] len=Tools.BitConverterlittleEndian.getBytes(retData.length);
		retData[0] = len[0];
		retData[1] = len[1];
		retData[2] = len[2];
		retData[3] = len[3];
		dataArray = retData;
		return retData;
	}

	public int getLength() {
		return Tools.BitConverterlittleEndian.toInt(dataArray, 0);
	}  

	public short getVersion() {
		return Tools.BitConverterlittleEndian.toShort(dataArray, 4);
	}

	public int getOpCode() {
		return Tools.BitConverterlittleEndian.toShort(dataArray, 6);
	}

	private BotDataPack writeByteDataIntoArray(byte... bs) {
		for (byte b:bs) {
			data.add(b);
			++dataPointer;
		}
		return this;
	}

	public BotDataPack write(byte b) {
		writeByteDataIntoArray(typeByte);
		writeByteDataIntoArray(b);
		return this;
	}

	public BotDataPack write(short s) {
		writeByteDataIntoArray(typeShort);
		writeByteDataIntoArray(Tools.BitConverterlittleEndian.getBytes(s));
		return this;
	}

	public BotDataPack write(int i) {
		writeByteDataIntoArray(typeInt);
		writeByteDataIntoArray(Tools.BitConverterlittleEndian.getBytes(i));
		return this;
	}

	public BotDataPack write(long l) {
		writeByteDataIntoArray(typeLong);
		writeByteDataIntoArray(Tools.BitConverterlittleEndian.getBytes(l));
		return this;
	}

	public BotDataPack write(float f) {
		writeByteDataIntoArray(typeFloat);
		writeByteDataIntoArray(Tools.BitConverterlittleEndian.getBytes(f));
		return this;
	}

	public BotDataPack write(double d) {
		writeByteDataIntoArray(typeDouble);
		writeByteDataIntoArray(Tools.BitConverterlittleEndian.getBytes(d));
		return this;
	}

	public BotDataPack write(String s) {
		writeByteDataIntoArray(typeString);
		byte[] stringBytes = Tools.BitConverterlittleEndian.getBytes(s);
		write(stringBytes.length);
		writeByteDataIntoArray(stringBytes);
		return this;
	}

	public BotDataPack write(boolean b) {
		writeByteDataIntoArray(typeBoolean);
		writeByteDataIntoArray(b ?(byte)1: (byte)0);
		return this;
	}

	public BotDataPack write(File file) {
		try {
			FileInputStream fin=new FileInputStream(file);
			byte[] bs=new byte[(int)file.length()];
			fin.read(bs, 0, bs.length);
			writeByteDataIntoArray(typeFile);
			write((int)file.length());
			writeByteDataIntoArray(bs);
		} catch (Exception e) {
			throw new RuntimeException(e.toString());
		}
		return this;
	}

	public File readFile(File file) {
		if (dataArray[dataPointer++] == typeFile) {
			int fileLen=readInt();
			try {
				FileOutputStream fos=new FileOutputStream(file);
				fos.write(dataArray, dataPointer, fileLen);
			} catch (Exception e) {
				file.delete();
				file = null;
			}
			dataPointer += fileLen;
			return file;
		}
		throw new RuntimeException("not a file");
	}

	public byte readByte() {
		if (dataArray[dataPointer++] == typeByte) {
			return dataArray[dataPointer++];
		}
		throw new RuntimeException("not a byte number");
	}

	public short readShort() {
		if (dataArray[dataPointer++] == typeShort) {
			short s = Tools.BitConverterlittleEndian.toShort(dataArray, dataPointer);
			dataPointer += 2;
			return s;
		}
		throw new RuntimeException("not a short number");
	}

	public int readInt() {
		if (dataArray[dataPointer++] == typeInt) {
			int i= Tools.BitConverterlittleEndian.toInt(dataArray, dataPointer);
			dataPointer += 4;
			return i;
		}
		throw new RuntimeException("not a int number");
	}

	public long readLong() {
		if (dataArray[dataPointer++] == typeLong) {
			long l= Tools.BitConverterlittleEndian.toLong(dataArray, dataPointer);
			dataPointer += 8;
			return l;
		}
		throw new RuntimeException("not a long number");
	}

	public float readFloat() {
		if (dataArray[dataPointer++] == typeFloat) {
			float f = Tools.BitConverterlittleEndian.toFloat(dataArray, dataPointer);
			dataPointer += 4;
			return f;
		}
		throw new RuntimeException("not a float number");
	}

	public double readDouble() {
		if (dataArray[dataPointer++] == typeDouble) {
			double d = Tools.BitConverterlittleEndian.toDouble(dataArray, dataPointer);
			dataPointer += 8;
			return d;
		}
		throw new RuntimeException("not a double number");
	}

	public String readString() {
		try {
			if (dataArray[dataPointer++] == typeString) {
				int len = readInt();
				String s = Tools.BitConverterlittleEndian.toString(dataArray, dataPointer, len);
				dataPointer += len;
				return s;
			}
		} catch (ArrayIndexOutOfBoundsException e) {
			return null;
		}
		return null;
	}

	public boolean readBoolean() {
		if (dataArray[dataPointer++] == typeBoolean) {
			return dataArray[dataPointer++] == 1;
		}
		throw new RuntimeException("not a boolean value");
	}

	public boolean hasNext() {
		return dataPointer != dataArray.length;
	}
}


