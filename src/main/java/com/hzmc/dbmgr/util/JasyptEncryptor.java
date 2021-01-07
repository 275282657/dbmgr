package com.hzmc.dbmgr.util;


import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;


public class JasyptEncryptor {

	public static String encoder(String value) {
		StandardPBEStringEncryptor encryptor = new StandardPBEStringEncryptor();
		encryptor.setPassword("capaa");
		String returnValue = encryptor.encrypt(value);
		return returnValue;
	}

	public static String decoder(String value) {
		StandardPBEStringEncryptor encryptor = new StandardPBEStringEncryptor();
		encryptor.setPassword("capaa");
		String returnValue = encryptor.decrypt(value);
		return returnValue;
	}
}
