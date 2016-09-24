package ru.objects;

import java.math.BigInteger;
import java.security.SecureRandom;

/**
 * Created by Sergei on 9/11/2016.
 */

/**
 * one - просто 1, для операций внутри.
 * random - крутой генератор для криптографических задач.
 *
 */

public class RSA {
    private final static BigInteger one  = new BigInteger("1");
    private final static SecureRandom random = new SecureRandom();
 
    private BigInteger privateKey;
    private BigInteger publicKey;
    private BigInteger modulus;

    public RSA() {
    	random.setSeed(System.currentTimeMillis());
    }

    /**
     * Алгоритм генерации ключей:
     * Генерируем p and q - два простых числа.
     * Вычислить функцию Эйлера. phi = (p - 1) * (q - 1);
     * Вычислить модуль (произведение) modulus = p * q
     * Вычислить секретный ключ = e^-1 mod(p)
     * @param N - количество бит под N/2 под ключ p и q.
     */
    public void init(int N) {
        BigInteger p = BigInteger.probablePrime(N / 2, random);
        BigInteger q = BigInteger.probablePrime(N / 2, random);
        BigInteger phi = (p.subtract(one)).multiply(q.subtract(one));
        modulus = p.multiply(q);
        publicKey  = new BigInteger("65537"); 
        privateKey = publicKey.modInverse(phi);
    }
   
    public void setPrivateKey(BigInteger privateKey) {
    	this.privateKey = privateKey;
	}

    public void setPublicKey(BigInteger publicKey) {
    	this.publicKey = publicKey;
	}

    public void setModulus(BigInteger modulus) {
    	this.modulus = modulus;
	}

    public BigInteger getPrivateKey() {
    	return privateKey;
	}

    public BigInteger getPublicKey() {
    	return publicKey;
	}

    public BigInteger getModulus() {
    	return modulus;
	}

    public BigInteger encrypt(BigInteger message) {
        return message.modPow(publicKey, modulus);
    }

    public BigInteger decrypt(BigInteger encrypted) {
        return encrypted.modPow(privateKey, modulus);
    }

    public String toString() {
        String s = "";
        s += "public  = " + publicKey  + "\n";
        s += "private = " + privateKey + "\n";
        s += "modulus = " + modulus;
         
        return s;
    }
}
