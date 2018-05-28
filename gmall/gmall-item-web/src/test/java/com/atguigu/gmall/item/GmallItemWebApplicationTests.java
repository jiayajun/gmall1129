
package com.atguigu.gmall.item;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GmallItemWebApplicationTests {

	@Test
	public void contextLoads() {

//		String a = "ab";
//		String b = "cd";
//		String c = a+b;


Thread t = new Thread(){

	public void run (){
		System.out.println("pong");

	}
};
t.run();
		System.out.println("ping");






	}

}
