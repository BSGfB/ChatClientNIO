package ru.objects;

import java.math.BigInteger;

public class RC4 {
	private int numlen = 128;
	private final byte[] S = new byte[numlen];
	private final byte[] Z = new byte[numlen];
	
	
	public RC4(final byte[] key) {		
		for(byte i = 0; i < numlen && i >= 0; ++i)
			S[i] = i;
		
		for(int i = 0, j = 0; i < numlen; i++) {			
			j = (j + S[i] + (key[i % key.length] < 0 ? key[i % key.length] * -1: key[i % key.length])) % numlen;			
			RC4.swap(S, i, j);
		}
			
		for(byte m = 0, i = 0, j = 0; m < numlen && m >= 0; ++m) {
			i = (byte) ((i + 1) % numlen);
			j = (byte) ((j + S[i]) % numlen);
			RC4.swap(S, i, j);
			Z[m] = S[(S[i] + S[j]) % numlen];
		}
	}
	
	private static void swap(byte[] array, int i, int j) {
		byte temp = array[i];
		array[i] = array[j];
		array[j] = temp;
	}
	
	public BigInteger DoIt(final BigInteger inArray) {		
		final byte[] array = inArray.toByteArray();
		final byte[] outArray = inArray.toByteArray();
		
		for(byte i = 0; i >= 0 & i < array.length; ++i) {						
			outArray[i] = (byte) (array[i] ^ Z[i % numlen]);
		}
		
		return new BigInteger(outArray);
	}  
	/*
	public byte[] DoIt(final byte[] inArray) {			
		final byte[] outArray = new byte[inArray.length];
		
		for(byte i = 0; i >= 0 & i < inArray.length; ++i) {						
			outArray[i] = (byte) (inArray[i] ^ Z[i % numlen]);
		}
		
		return outArray;
	}  
	*/
}

