package com.atguigu.gmall1129.manage;

import com.atguigu.gmall1129.manage.service.impl.CatalogCatcherServiceImpl;
import com.atguigu.gmall1129.manage.service.impl.ManageServiceImpl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GmallManageServiceApplicationTests {

	@Autowired
	CatalogCatcherServiceImpl catalogCatcherService;

	@Test
	public void contextLoads() {
		catalogCatcherService.catchCatalog();
	}

}
