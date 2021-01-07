package com.hzmc.dbmgr.util;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 版权所有：美创科技 创建者: gpchen 创建日期: 2020年4月8日 上午10:25:27
 */
public class UdpUtil {
	private static final Logger logger = LoggerFactory.getLogger(UdpUtil.class);

	private static final int LOCAL_PORT = 9000;
	private static final int TIMEOUT = 5000; // 设置接收数据的超时时间
	private static final int MAXNUM = 2; // 设置重发数据的最多次数
	private static final int BUFF_STR_MAX = 1024;// 缓存大小

	private DatagramPacket dp_send;// 定义用来发送数据的DatagramPacket实例
	private DatagramPacket dp_receive;// 定义用来接收数据的DatagramPacket实例
	private DatagramSocket local_ds;// 本地监听

	private String ip;
	private Integer port;
	
	private UdpUtil() {
	};

	private UdpUtil(String ip, Integer port) throws Exception {
		this.ip = ip;
		this.port = port;
		if (dp_receive == null) {
			initUdpPackg();
		}
	};

	public static synchronized UdpUtil getInstance(String ip, Integer port) throws Exception {
		return new UdpUtil(ip, port);
	}

	private void initUdpPackg() throws Exception {
		byte[] buf = new byte[BUFF_STR_MAX];
		this.dp_receive = new DatagramPacket(buf, BUFF_STR_MAX);// 定义用来接收数据的DatagramPacket实例
		this.local_ds = new DatagramSocket(LOCAL_PORT);
		this.local_ds.setSoTimeout(TIMEOUT);// 超时设置
	}

	public String sendUpdReq(String msg, boolean autoClose) throws Exception {
		String result = null;
		try {
			this.dp_send = new DatagramPacket(msg.getBytes(), msg.length(), InetAddress.getByName(this.ip), this.port);// 定义用来发送数据的DatagramPacket实例
		} catch (Exception e) {
			closeLocalDs();
			throw e;
		}
		int tries = 0; // 重发数据的次数
		boolean receivedResponse = false; // 是否接收到数据的标志位
		// 直到接收到数据，或者重发次数达到预定值，则退出循环
		while (!receivedResponse && tries < MAXNUM) {
			try {
				// 发送数据
				local_ds.send(dp_send);
				// 接收从服务端发送回来的数据
				local_ds.receive(dp_receive);
				// 如果接收到的数据不是来自目标地址，则抛出异常
				if (!dp_receive.getAddress().equals(InetAddress.getByName(this.ip))) {
					throw new IOException("Received packet from an umknown source");
				}
				// 如果接收到数据。则将receivedResponse标志位改为true，从而退出循环
				receivedResponse = true;
				//获取结果
				result = new String(dp_receive.getData(), 0, dp_receive.getLength());
				result = StringUtils.split(result, ';')[9];
			} catch (Exception e) {
				// 如果接收数据时阻塞超时，重发并减少一次重发的次数
				tries += 1;
				logger.error(e.getMessage(), e);
			}
		}
		if (autoClose) {
			closeLocalDs();
		}
		return result;
	}

	public void closeLocalDs() {
		if (local_ds != null) {
			local_ds.close();
		}
		local_ds = null;
	}

	public static void main(String[] args) throws Exception {
		String str_send = ((char) Integer.parseInt("04", 16)) + "MSSQLServer";
		String result = UdpUtil.getInstance("192.168.61.15", 1434).sendUpdReq(str_send, true);
		String[] result1 = StringUtils.split(result, ';');
		System.out.println(result1[9]);
	}



}
