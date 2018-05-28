package com.atguigu.gmall.list.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.atguigu.gmall.bean.SkuInfoEs;
import com.atguigu.gmall.bean.SkuInfoEsParam;
import com.atguigu.gmall.bean.SkuInfoEsResult;
import com.atguigu.gmall.service.ListService;
import com.atguigu.gmall.utils.RedisUtil;
import io.searchbox.client.JestClient;
import io.searchbox.core.*;
import io.searchbox.core.search.aggregation.MetricAggregation;
import io.searchbox.core.search.aggregation.TermsAggregation;

import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.TermsBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


@Service
public class ListServiceImpl  implements  ListService  {

    @Autowired
    JestClient jestClient;

    @Autowired
    RedisUtil redisUtil;





    //保存skuInfoEs
    public void saveSkuInfoEs(SkuInfoEs skuInfoEs){

        Index index = new Index.Builder(skuInfoEs).index("gmall").type("SkuInfo").id(skuInfoEs.getId()).build();
        DocumentResult execute = null;
        try {
             execute = jestClient.execute(index);
        } catch (IOException e) {e.printStackTrace();

        }
    }



    //提取获取sql字符串方法(根据查询条件组合query串)
    private String makeQueryStringForSearch(SkuInfoEsParam skuInfoParam){
        //创建工具对象(将sql封装为一个对象)
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();


        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
        

        //获取第一层(过滤)
        if (skuInfoParam.getCatalog3Id()!=null) {
            TermQueryBuilder termQueryBuilder = new TermQueryBuilder("catalog3Id", skuInfoParam.getCatalog3Id());
            boolQueryBuilder.filter(termQueryBuilder);
        }
        if (skuInfoParam.getValueIdList()!=null&&skuInfoParam.getValueIdList().size()>0) {

            List<String> valueidListForSearch   =   skuInfoParam.getValueIdList();
            for (String valueIdForSearch : valueidListForSearch) {

                TermQueryBuilder termQueryBuilderValueId = new TermQueryBuilder("skuAttrValueListEs.valueId", valueIdForSearch);
                boolQueryBuilder.filter(termQueryBuilderValueId);
            }
        }
        //获取第二层（匹配）
        if (skuInfoParam.getKeyword()!=null&&skuInfoParam.getKeyword().length()>0){

            MatchQueryBuilder matchQueryBuilder = new MatchQueryBuilder("skuName", skuInfoParam.getKeyword());
            boolQueryBuilder.must(matchQueryBuilder);
            //三获取高亮显示属性
            HighlightBuilder highlightBuilder = new HighlightBuilder();
            highlightBuilder.field("skuName");
            highlightBuilder.preTags("<span style = 'color:red'>");
            highlightBuilder.postTags("</span>");

            searchSourceBuilder.highlight(highlightBuilder);
        }
        
        
        searchSourceBuilder.query(boolQueryBuilder);
     
       

      
        //四设置分页
        searchSourceBuilder.size(skuInfoParam.getPageSize());
        
        int fromRow = (skuInfoParam.getPageNo()-1)*skuInfoParam.getPageSize();
        
        searchSourceBuilder.from(fromRow);
        //五排序                                            //倒序
        searchSourceBuilder.sort("hotScore", SortOrder.DESC);

        //六获取聚合
        TermsBuilder termsBuilder = AggregationBuilders.terms("groupby_valueId").field("skuAttrValueListEs.valueId");

        searchSourceBuilder.aggregation(termsBuilder);

        //生成字符串
        String query = searchSourceBuilder.toString();

        System.out.println(query);

        return query;
    }
    
    //
    private SkuInfoEsResult makeResoultForSearch(SearchResult serchResult,SkuInfoEsParam skuInfoEsParam){

        SkuInfoEsResult skuInfoEsResult = new SkuInfoEsResult();
        
        //获取查询结果(商品列表)
        List<SearchResult.Hit<SkuInfoEs, Void>> hits = serchResult.getHits(SkuInfoEs.class);
        List<SkuInfoEs> skuInfoEslist = new ArrayList<>(hits.size());//避免扩容一次给足资源空间
        for (SearchResult.Hit<SkuInfoEs, Void> hit : hits) {
            SkuInfoEs skuInfoEs = hit.source;

            if (hit.highlight!=null){
                List<String> skuNmaeHlList = hit.highlight.get("skuName");
                //替换高粱显示的名称
                String skuNameHl = skuNmaeHlList.get(0);
                skuInfoEs.setSkuName(skuNameHl);
            }


                skuInfoEslist.add(skuInfoEs);



        }
        System.out.println(skuInfoEslist.toString());
        skuInfoEsResult.setSkuInfoEsList(skuInfoEslist);


        //获取聚合数据（平台属性值列表）
        MetricAggregation aggregations = serchResult.getAggregations();


        TermsAggregation groupby_valueId = aggregations.getTermsAggregation("groupby_valueId");

        List<TermsAggregation.Entry> buckets = groupby_valueId.getBuckets();

        List<String> attrValueList = new ArrayList<>(buckets.size());//避免扩容一次给足资源空间
        for (TermsAggregation.Entry bucket : buckets) {
            String key = bucket.getKey();

            attrValueList.add(key);
        }
        System.out.println(attrValueList.toString());
        skuInfoEsResult.setValueIdList(attrValueList);
        
        Long total = serchResult.getTotal();
        skuInfoEsResult.setTotal(Integer.parseInt(total+""));
        long totalPage = (total + skuInfoEsParam.getPageSize() - 1) / skuInfoEsParam.getPageSize();
        skuInfoEsResult.setTotalPage(Integer.parseInt(totalPage+""));

        return skuInfoEsResult;

    }

    //利用buider工具拼接sql字符串
  
    public SkuInfoEsResult searchSkuInfoList(SkuInfoEsParam skuInfoParam){
        //调用提取方法获取查询对象（Stirng）
        String query = makeQueryStringForSearch(skuInfoParam);

        Search search = new Search.Builder(query).addIndex("gmall").addType("SkuInfo").build();

        SearchResult serchResult = null;
        try {
           serchResult = jestClient.execute(search);
        } catch (IOException e) {
            e.printStackTrace();
        }

        SkuInfoEsResult skuInfoEsResult = makeResoultForSearch(serchResult,skuInfoParam);
        
        return  skuInfoEsResult;


    }
    //热度评分计数器
    public void countHotScore(String skuId){
        final Jedis jedis = redisUtil.getJedis();
        //确定key值
        String key = "hotScoreSet";
        //设定计数器（每次点击的效果）
        Double curCount = jedis.zincrby(key, 1, skuId);
        //设定跟新年次数(枚加100次更新一次数据)
        if (curCount%100==0){
            //调用下面的评分点击方法
            updateHotScore(skuId,curCount.longValue());

        }
    }

    //热度评分
    public void updateHotScore(String id,Long hotScore){
        String query = "{\n"+
                "\"doc\":{\n"+
                "\"hostScore\":"+hotScore+"\n"+
                "}\n"+
                "}";

        Update update = new Update.Builder(query).index("gmall").type("skuInfo").id(id).build();


        try {
            jestClient.execute(update);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
