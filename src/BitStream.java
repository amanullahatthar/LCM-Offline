//import java.util.HashMap;
//import java.util.Map;
//
//class BitStream {
//	final int MAX_CAPACITY = 512;
//
//	char[] buffer = new char[MAX_CAPACITY];
//
//	char[] encodingTable = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz-_".toCharArray();
//	Map<Character, Integer> decodingTable = new HashMap<Character, Integer>();
//	boolean decodingTableInit = false;
//
//	int iter = 0;
//	int bitCount = 0;
//
//	public BitStream() {
//		if (decodingTableInit)
//			return;
//		for (int i = 0; i < 64; i++) {
//			decodingTable.put(encodingTable[i], i);
//		}
//		decodingTableInit = true;
//	}
//
//	void incBitCount() {
//		bitCount++;
//		if (bitCount >= 6) {
//			bitCount = 0;
//			iter++;
//			if (iter >= MAX_CAPACITY) {
//				System.out.println("BitStream buffer is full");
//				System.exit(0);
//			}
//		}
//	}
//
//	int readBit() {
//		int bit = buffer[iter] & (1 << (5 - bitCount));
//		incBitCount();
//		return bit > 0 ? 1 : 0;
//	}
//
//	void writeBit(boolean value) {
//		buffer[iter] <<= 1;
//		if (value) {
//			buffer[iter] |= 1;
//		}
//		incBitCount();
//	}
//
//	void encode() {
//		while (bitCount != 0)
//			writeBit(false);
//		for (int i = 0; i < iter; i++) {
//			buffer[i] = encodingTable[buffer[i]];
//		}
//	}
//
//	void decode(int count) {
//		for (int i = 0; i < count; i++) {
//			int value = decodingTable.get(buffer[i]);
//			buffer[i] = (char) value;
//		}
//	}
//
//	void print() {
//		System.err.print("Bit stream: ");
//		System.err.println(buffer);
//	}
//
//	void initRead(String str) {
//		buffer = str.toCharArray();
//		decode(str.length());
//		iter = bitCount = 0;
//	}
//
//	int readInt(int bits) {
//		int negative = readBit();
//		int result = 0;
//		for (int i = 0; i < bits; i++) {
//			result <<= 1;
//			int bit = readBit();
//			if (bit > 0)
//				result |= 1;
//		}
//		return negative > 0 ? -result : result;
//	}
//
//	void writeInt(int value, int bits) {
//		writeBit(value < 0);
//		value = Math.abs(value);
//		int mask = 1 << (bits - 1);
//		for (int i = 0; i < bits; i++) {
//			writeBit((value & mask) > 0 ? true : false);
//			mask >>= 1;
//		}
//	}
//}
//
//class Test {
//	public static void main(String[] args) {
//
//		BitStream bs = new BitStream();
////		bs.writeBit(true);
////		bs.writeBit(false);
////		bs.writeBit(false);
////		bs.writeBit(true);
////		bs.writeBit(false);
////		bs.writeBit(true);
////		bs.writeBit(true);
////		bs.encode();
////		bs.print();
////		bs.decode(2);
//
////		bs.initRead("y0");
////		for (int i = 0; i < 7; i++) {
////			System.out.println(bs.readBit());
////		}
//		bs.writeInt(120, 7);
//		bs.encode();
//		bs.print();
//		bs.initRead("y0");
//		System.out.println(bs.readInt(7));
//	}
//}