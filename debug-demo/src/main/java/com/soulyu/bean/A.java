package com.soulyu.bean;

/**
 * @Author soul.yu
 * @Date 2021/6/16 1:58 下午
 * @Version 1.0
 */
public class A {

	private B b;

	public B getB() {
		return b;
	}

	public void setB(B b) {
		this.b = b;
	}

	@Override
	public String toString() {
		return "A create success";
	}
}
