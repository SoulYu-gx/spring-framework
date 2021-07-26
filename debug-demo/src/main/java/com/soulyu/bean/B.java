package com.soulyu.bean;

/**
 * @Author soul.yu
 * @Date 2021/6/16 1:58 下午
 * @Version 1.0
 */
public class B {

	private A a;

	public A getA() {
		return a;
	}

	public void setA(A a) {
		this.a = a;
	}

	@Override
	public String toString() {
		return "B create success";
	}
}
