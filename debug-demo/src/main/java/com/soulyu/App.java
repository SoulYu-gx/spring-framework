package com.soulyu;

import com.soulyu.bean.ABean;
import com.soulyu.bean.BBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class  App {

	public static void main(String[] args) {
		ApplicationContext context = new ClassPathXmlApplicationContext("classpath:spring.xml");
		// ABean aBean = (ABean)context.getBean("aBean");
		// System.out.println(aBean.toString());

		BBean bBean = (BBean)context.getBean("bBean");
		System.out.println(bBean.toString());
	}
}
