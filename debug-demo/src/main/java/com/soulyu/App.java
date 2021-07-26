package com.soulyu;

import com.soulyu.bean.A;
import com.soulyu.bean.B;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @Author soul.yu
 * @Date 2021/6/16 1:58 下午
 * @Version 1.0
 */
public class App {

	public static void main(String[] args) {
		ApplicationContext context = new ClassPathXmlApplicationContext("classpath:spring.xml");

		System.out.println(context.getBean("b", B.class).getA().toString());
		System.out.println(context.getBean("a", A.class).getB().toString());
	}
}
