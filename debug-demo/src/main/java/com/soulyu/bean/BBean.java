package com.soulyu.bean;

/**
 * @Author soul.yu
 * @Date 2021/6/16 1:58 下午
 * @Version 1.0
 */
public class BBean {

	private ABean aBean;

	public ABean getaBean() {
		return aBean;
	}

	public void setaBean(ABean aBean) {
		this.aBean = aBean;
	}

	@Override
	public String toString() {
		return "BBean{" +
				"aBean=" + aBean +
				'}';
	}
}
