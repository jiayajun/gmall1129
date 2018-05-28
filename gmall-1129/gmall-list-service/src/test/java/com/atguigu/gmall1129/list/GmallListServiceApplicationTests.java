package com.atguigu.gmall1129.list;

import com.atguigu.gmall1129.service.ListService;
import io.searchbox.action.Action;
import io.searchbox.client.JestClient;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GmallListServiceApplicationTests {

	@Autowired
	JestClient jestClient;

	@Autowired
	ListService listService;

	@Test
	public void testEs() {
		String query="{\n" +
				"  \"query\": {\n" +
				"    \"match\": {\n" +
				"      \"actorList.name\": \"张涵予\"\n" +
				"    }\n" +
				"  }\n" +
				"}";


		Search search = new Search.Builder(query).addIndex("movie_chn").addType("movie_type_chn").build();
		SearchResult searchResult=null;
		try {
			  searchResult = jestClient.execute(search);
		} catch (IOException e) {
			e.printStackTrace();
		}

		List<SearchResult.Hit<HashMap, Void>> hits = searchResult.getHits(HashMap.class);
		for (SearchResult.Hit<HashMap, Void> hit : hits) {
			HashMap sourceMap = hit.source;
			System.err.println("sourceMap = " + sourceMap);
		}

		try {
			jestClient.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}




}
