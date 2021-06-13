package com.soulyu;

import com.soulyu.bean.ABean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class App {

	public static void main(String[] args) {
		ApplicationContext context = new ClassPathXmlApplicationContext("classpath:spring.xml");
		ABean service = (ABean)context.getBean("loginService");
		System.out.println(service.toString());
	}
}
