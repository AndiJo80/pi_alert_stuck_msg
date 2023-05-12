/**
 * Project: LocalDevelopment~pi.alertstuckmsg.ejb~cust.sap.com
 * File: Hex.java
 * Type: Hex
 * Author: Andreas Job
 * Creation Date: Apr 9, 2016
 */
package com.sap.pi.alertstuckmsg.util;


/**
 * @author d047487
 *
 */
public class Hex
{

	private static final char[] aHexAlphabet = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };

	private static final byte[] aHexConversion = { 	-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, // 16
													-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, // 32
													-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, // 48
													 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, -1, -1, -1, -1, -1, -1, // 64 - '0', '1', '2', '3', '4', '5', '6', '7', '8', '9'
													-1, 10, 11, 12, 13, 14, 15, -1, -1, -1, -1, -1, -1, -1, -1, -1, // 80 - 'A', 'B', 'C', 'D', 'E', 'F'
													-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, // 96
													-1, 10, 11, 12, 13, 14, 15, -1, -1, -1, -1, -1, -1, -1, -1, -1, // 112
													-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1 // 128
												};

	private static final int BLOCK_SIZE = 512;

	/**
	 * Encode's a byte[] array into a hexadecimal String. The 8 Bit of a Byte are encoded into two Hex values. 0x0F =
	 * 0000 1111 Bit 0xF0 = 1111 0000 Bit
	 *
	 * @param aByte
	 *            - byte[] array
	 *
	 * @return String - hexadecimal String
	 */
	public static synchronized String encode(byte[] aByte)
	{

		if (aByte == null)
		{
			return null;
		}

		// 8 Bit -> 2 * 8 Bit
		char[] buffer = new char[aByte.length * 2];
		int pos = 0;

		for (int i = 0; i < aByte.length; i++)
		{
			// 0x0F = 0000 1111 Bit; encode the first 4 Bit into a Hex value.
			buffer[pos++] = Hex.aHexAlphabet[(aByte[i] & 0xF0) >>> 4];
			// 0xF0 = 1111 0000 Bit; encode the second 4 Bit into a Hex value.
			buffer[pos++] = Hex.aHexAlphabet[(aByte[i] & 0x0F)];
		}

		return new String(buffer);
	}

	/**
	 * Encode's a byte[] array into a hexadecimal String. The 8 Bit of a Byte are encoded into two Hex values. 0x0F =
	 * 0000 1111 Bit 0xF0 = 1111 0000 Bit Two Byte's are separated by a colon. The result looks like: F4:43:AA:FE
	 *
	 * @param aByte
	 *            - byte[] array
	 *
	 * @return String - hexadecimal String
	 */
	public static synchronized String encodeColon(byte[] aByte)
	{

		if (aByte == null)
		{
			return null;
		}

		// 8 Bit -> 2 * 8 Bit + ( colon )
		char[] buffer = new char[(aByte.length * 2) + (aByte.length - 1)];
		int pos = 0;

		for (int i = 0; i < aByte.length; i++)
		{
			if (i != 0)
			{
				// 0x3A = colon (:)
				buffer[pos++] = 0x3A;
			}
			// 0x0F = 0000 1111 Bit; encode the first 4 Bit into a Hex value.
			buffer[pos++] = Hex.aHexAlphabet[(aByte[i] & 0xF0) >>> 4];
			// 0xF0 = 1111 0000 Bit; encode the second 4 Bit into a Hex value.
			buffer[pos++] = Hex.aHexAlphabet[(aByte[i] & 0x0F)];
		}

		return new String(buffer);
	}

	/**
	 * Encode's a hexadecimal String into an byte[] array. Two Hex values are encoded into 8 Bit of a Byte. 0x0F = 0000
	 * 1111 Bit 0xF0 = 1111 0000 Bit
	 *
	 * @param hexadecimal
	 *            - String
	 *
	 * @return byte[] array
	 * @throws NumberFormatException
	 *             is thrown if the String contains characters other than 0-9, a-f or A-F.
	 **/
	public static byte[] decode(String hexadecimal)
	{

		if (hexadecimal == null)
		{
			return null;
		}
		if (((hexadecimal.length()) % 2) == 1)
		{
			throw new NumberFormatException("Odd number of hexadecimal characters! Length: " + Integer.toString(hexadecimal.length()));
		}

		int size = (hexadecimal.length()) / 2;
		byte[] aByte = new byte[size];

		// Set buffer size
		int bufferSize = 0;
		if (size > Hex.BLOCK_SIZE)
		{
			bufferSize = 2 * Hex.BLOCK_SIZE;
		}
		else
		{
			bufferSize = 2 * size;
		}

		char[] buffer = new char[bufferSize];
		int pos = 0;

		for (int i = 0; i < size; i = (i + Hex.BLOCK_SIZE))
		{
			int end = Hex.BLOCK_SIZE + i;
			if (end > size)
			{
				end = size;
			}
			// Fill buffer
			hexadecimal.getChars(pos, pos + 2 * (end - i), buffer, 0);
			int _pos = 0;
			for (int k = i; k < end; k++)
			{
				// High 4 Bit of the Byte
				char cTemp = buffer[_pos++];
				byte hByte = Hex.aHexConversion[cTemp];
				if (hByte == -1)
				{
					throw new NumberFormatException("Found NO hexadecimal character: " + cTemp);
				}
				// Low 4 Bit of the Byte
				cTemp = buffer[_pos++];
				byte lByte = Hex.aHexConversion[cTemp];
				if (lByte == -1)
				{
					throw new NumberFormatException("Found NO hexadecimal character: " + cTemp);
				}

				// 0x0F = 0000 1111 Bit; 0xF0 = 1111 0000 Bit.
				aByte[k] = (byte) ((byte) (hByte << 4) + lByte);
			}

			pos = pos + 2 * Hex.BLOCK_SIZE;
		}

		return aByte;
	}
}
// end class Hex