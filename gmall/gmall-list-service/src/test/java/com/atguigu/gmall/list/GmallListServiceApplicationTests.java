package com.atguigu.gmall.list;

import ch.qos.logback.core.net.SyslogOutputStream;
import com.atguigu.gmall.service.ListService;
import io.searchbox.client.JestClient;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;

import org.apache.lucene.search.Query;
import org.elasticsearch.action.get.MultiGetRequest;
import org.elasticsearch.index.mapper.SourceToParse;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GmallListServiceApplicationTests {

	//注入jest
	@Autowired
	JestClient jestClient;
	@Autowired
	ListService listService;



	@Test
	public void testES() {

		//定义查询语句为变量
		String query = "{\n" +
				"  \n" +
				"  \"query\":{\n" +
				"    \"match\": {\n" +
				"    \"acterList.name\":\"张涵予\"\n" +
				"    }\n" +
				"    \n" +
				"  }\n" +
				"  \n" +
				"  \n" +
				"}";

		Search s = new Search.Builder(query).addIndex("movie_yajun").addType("fist_type").build();
		SearchResult execute= null;


		try {
			execute = jestClient.execute(s);
		} catch (IOException e) {
			e.printStackTrace();
		}

		List<SearchResult.Hit<Map, Void>> hits = execute.getHits(Map.class);

		for (SearchResult.Hit<Map, Void> hit : hits) {
			Map source = hit.source;

			System.err.println(source.toString());
		}
		//关闭连接
		try {
			jestClient.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	@Test
	public void testES1(){
			//定义查询语句
				String  s1 ="{\n" +
						"\"query\": {\n" +
						"  \"fuzzy\": {\"name\": \"行动\"}\n" +
						"  \n" +
						"}\n" +
						"\n" +
						"}\n";
		//							要查询的语句				库名								表名
		Search s= new Search.Builder(s1).addIndex("movie_yajun").addType("fist_type").build();
		SearchResult execute = null;
		try {
			//执行时间
			 execute = jestClient.execute(s);
		} catch (IOException e) {
			e.printStackTrace();
		}

		List<SearchResult.Hit<Map, Void>> hits = execute.getHits(Map.class);

		for (SearchResult.Hit<Map, Void> hit : hits) {

				Map source = hit.source;

			System.err.println(source.toString());

		}
		try {
			jestClient.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}



	@Test
	public void testes2(){

		String s = "{\n" +
				"\"query\": {\n" +
				"  \"fuzzy\": {\"name\": \"行动\"}\n" +
				"  \n" +
				"}\n" +
				"\n" +
				"}\n";


		Search build = new Search.Builder(s).addIndex("movie_yajun").addType("fist_type").build();
		SearchResult execute = null;

		try {
			execute = jestClient.execute(build);
		} catch (IOException e) {
			e.printStackTrace();
		}

		List<SearchResult.Hit<Map, Void>> hits = execute.getHits(Map.class);
		for (SearchResult.Hit<Map, Void> hit : hits) {
			Map source = hit.source;

			System.err.println(source.toString());
		}

		try {
			jestClient.close();
		} catch (IOException e) {
			e.printStackTrace();
		}


	}


}
