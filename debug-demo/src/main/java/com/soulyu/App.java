package com.soulyu;

import com.soulyu.bean.B;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class App {

	public static void main(String[] args) {
		ApplicationContext context = new ClassPathXmlApplicationContext("classpath:spring.xml");

		B bBean = (B)context.getBean("bBean");
		System.out.println(bBean.toString());
	}
}
