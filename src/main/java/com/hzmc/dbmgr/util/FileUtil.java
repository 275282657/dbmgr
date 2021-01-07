package com.hzmc.dbmgr.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.util.Enumeration;
import java.util.List;
import java.util.Scanner;
import java.util.zip.ZipException;
import java.util.zip.ZipOutputStream;

import org.apache.log4j.Logger;

/**
 * 文件读取
 * 
 * @author xxq
 * 
 */
public class FileUtil {
    private static final Logger log4j = Logger.getLogger(FileUtil.class);
    static byte md5salt[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'H', 'Z', 'M', 'C' };
	/**
	 * 文件读取
	 * @param file 要读取的文件gbk
	 */
	public static String read(String file){
	    String content = "";
	    String encoding = "gbk";
        try {
            File filedd=new File(file);
            if(filedd.isFile() && filedd.exists()){      //判断文件是否存在
                InputStreamReader read = new InputStreamReader(
                new FileInputStream(filedd),encoding);    //考虑到编码格式
                BufferedReader bufferedReader = new BufferedReader(read);
                String lineTxt = null;
                while((lineTxt = bufferedReader.readLine()) != null){
                     content += lineTxt;
                     content += "\n";
                }
                read.close();
            }else{
                log4j.error("找不到指定的文件");
            }
        } catch (Exception e) {
            log4j.error("读取文件内容出错");
            e.printStackTrace();
        }
	    return content;
	}
	
	/**
	 * 文件读取
	 * @param file 要读取的文件
	 * @param encoding 指定编码
	 * @return
	 */
	public static String read(String file,String encoding){
	    String content = "";
        try {
            File filedd=new File(file);
            if(filedd.isFile() && filedd.exists()){      //判断文件是否存在
                InputStreamReader read = new InputStreamReader(
                new FileInputStream(filedd),encoding);    //考虑到编码格式
                BufferedReader bufferedReader = new BufferedReader(read);
                String lineTxt = null;
                while((lineTxt = bufferedReader.readLine()) != null){
                     content += lineTxt;
                }
                read.close();
            }else{
                log4j.error("找不到指定的文件");
            }
        } catch (Exception e) {
            log4j.error("读取文件内容出错");
            e.printStackTrace();
        }
	    return content;
	}
	
	/**
	 * 
	 * @param strList  写入内容，没文件会自动创建
	 * @param fileLocation 路径
	 * @param coding 编码
	 */
	public static Boolean write(String file, String encoding, String content) {
		try {
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), encoding));
			bw.write(content);
			bw.flush();
			bw.close();
			return true;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			log4j.error("写入文件" + file + "失败，错误IO");
			e.printStackTrace();
			return false;
		} catch (Exception e) {
			log4j.error("写入文件" + file + "失败，未知错误");
			e.printStackTrace();
			return false;
		}
	}
	
	
	/**
	 * 删除指定目录下的文件
	 * 
	 * @param file
	 */
	public void deleteFile(File file) {
		File[] files = file.listFiles();
		for (int i = 0; i < files.length; i++) {
			files[i].delete();
		}
	}

	/**
	 * 删除指定目录下的文件所有文件
	 * 
	 * @param file
	 */
	public static boolean deleteDir(File dir) {
		if (dir.isDirectory()) {
			String[] children = dir.list();
			for (int i = 0; i < children.length; i++) {
				boolean success = deleteDir(new File(dir, children[i]));
				if (!success) {
					return false;
				}
			}
		}

		return dir.delete();
	}

	/**
	 * 读取文件到byte[]
	 * 
	 * @param path
	 * @return
	 * @throws Exception
	 */
	public static byte[] readfilebyte(String path) throws Exception {
		InputStream in = new FileInputStream(path);
		byte[] data = toByteArray(in);
		in.close();
		return data;
	}
	 
	/**
	 * 写byte到文件
	 * @param bytes
	 * @param path
	 * @throws Exception 
	 */
	public static void write(byte[] bytes, String path) throws Exception {
		OutputStream out = new FileOutputStream(path);
		out.write(bytes);
		out.close();
	}

	private static byte[] toByteArray(InputStream in) throws IOException {

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024 * 4];
		int n = 0;
		while ((n = in.read(buffer)) != -1) {
			out.write(buffer, 0, n);
		}
		return out.toByteArray();
	}
	
	// byte[]加md5
	public static byte[] addMD5(byte[] bt) {
		try {
			byte[] btasalt = new byte[bt.length + md5salt.length];// bt加盐
			for (int i = 0; i < btasalt.length; i++) {
				if (i < bt.length) {
					btasalt[i] = bt[i];
				} else {
					for (int j = 0; j < md5salt.length; j++) {
						btasalt[i] = md5salt[j];
						i++;
					}
				}
			}
			getPtintString(btasalt);// 打印bt加盐

			byte[] btasaltMD5 = MD5(btasalt);// 计算bt加盐的md5
			System.out.println(md5to16th(btasaltMD5));// bt加盐的md5转16进制输出

			if (md5salt.length != btasaltMD5.length) {
				System.out.println("length not equal");
				return null;
			}
			int j = 0;
			for (int i = bt.length; i < btasalt.length; i++) {
				btasalt[i] = btasaltMD5[j];
				j++;
			}// 用bt加盐的md5替换原盐

			return btasalt;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	// 指定文件校验内容MD5写在最后
	public static boolean fileCheakMD5(String path) {
		try {
			byte[] btasalt = readfilebyte(path);
			byte[] temp = new byte[md5salt.length];// 用于存放加密串
			int j = 0;
			for (int i = btasalt.length - md5salt.length; i < btasalt.length; i++) {
				temp[j] = btasalt[i];
				btasalt[i] = md5salt[j];
				j++;
			}// 用盐替换加密串,加密串存储到temp

			byte[] btasaltMD5 = MD5(btasalt); // 计算用盐替换后的MD5
			if (temp.length != btasaltMD5.length) {
				System.out.println("length not equal");
				return false;
			}

			for (int i = 0; i < md5salt.length; i++) {// 比对MD5
				if (btasaltMD5[i] != temp[i]) {
					System.out.println("MD5 CHECK ERROR");
					return false;
				}
			}

			System.out.println("MD5 CHECK OK");

			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}	
	
	// 指定文件校验内容MD5写在最后
	public static boolean cheakMD5(byte[] btasalt) {
		try {
			byte[] temp = new byte[md5salt.length];// 用于存放加密串
			int j = 0;
			for (int i = btasalt.length - md5salt.length; i < btasalt.length; i++) {
				temp[j] = btasalt[i];
				btasalt[i] = md5salt[j];
				j++;
			}// 用盐替换加密串,加密串存储到temp

			byte[] btasaltMD5 = MD5(btasalt); // 计算用盐替换后的MD5
			if (temp.length != btasaltMD5.length) {
				System.out.println("length not equal");
				return false;
			}

			for (int i = 0; i < md5salt.length; i++) {// 比对MD5
				if (btasaltMD5[i] != temp[i]) {
					System.out.println("MD5 CHECK ERROR");
					return false;
				}
			}

			System.out.println("MD5 CHECK OK");

			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	
	public static byte[] MD5(byte[] buffer) {
		// 用于加密的字符
//		char md5String[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };
//		System.out.println(new String(md5String));
		try {
			// 使用平台的默认字符集将此 String 编码为 byte序列，并将结果存储到一个新的 byte数组中
			byte[] btInput = buffer;
			// 获得指定摘要算法的 MessageDigest对象，此处为MD5
			// MessageDigest类为应用程序提供信息摘要算法的功能，如 MD5 或 SHA 算法。
			// 信息摘要是安全的单向哈希函数，它接收任意大小的数据，并输出固定长度的哈希值。
			MessageDigest mdInst = MessageDigest.getInstance("MD5");
			
			// MD5 Message Digest from SUN, <initialized>
			// MessageDigest对象通过使用 update方法处理数据， 使用指定的byte数组更新摘要
			mdInst.update(btInput);
			
			// MD5 Message Digest from SUN, <in progress>
			// 摘要更新之后，通过调用digest（）执行哈希计算，获得密文
			byte[] md = mdInst.digest();
			
			return md;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	// 把密文转换成十六进制的字符串形式
	public static String md5to16th(byte[] md) {
		// 用于加密的字符
		char md5String[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };
		int j = md.length;
		
		char str[] = new char[j * 2];
		int k = 0;
		for (int i = 0; i < j; i++) { // i = 0
			byte byte0 = md[i]; // 95
			str[k++] = md5String[byte0 >>> 4 & 0xf]; // 5
			str[k++] = md5String[byte0 & 0xf]; // F
		}
		// 返回经过加密后的字符串
		return new String(str);
	}
	
	public static byte[] remoteByte(byte[] bytes){
		int count = bytes.length - md5salt.length;
		byte[] bt =new byte[count];
		for(int i =0;i<count ;i++){
			bt[i] = bytes[i];
		}
		return bt;
	}

	public static String getPtintString(byte[] bt) {
		Charset charset = Charset.defaultCharset();
		ByteBuffer buf = ByteBuffer.wrap(bt);
		CharBuffer cBuf = charset.decode(buf);
  
		System.out.println(cBuf.toString());
		return cBuf.toString();
	}

	
}
