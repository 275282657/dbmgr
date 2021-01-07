package com.hzmc.dbmgr.dbenum;
/**
 * 版权所有：美创科技
 * 创建者: gpchen
 * 创建日期: 2019年10月17日 上午10:57:36
 */
public enum IpTypeEnum {

	ALONE_IP(0, "单个ip"),

	BLURRY_IP(1, "通配符ip"),

	SEGMENTATION_IP(2, "地址段ip"),;

	private Integer ipMode;

	private String value;

	private IpTypeEnum(Integer ipMode, String value) {
		this.ipMode = ipMode;
		this.value = value;
	}

	public Integer getIpMode() {
		return ipMode;
	}

	public String getValue() {
		return value;
	}

}
