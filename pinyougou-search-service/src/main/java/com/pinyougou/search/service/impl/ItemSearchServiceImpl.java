package com.pinyougou.search.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.search.service.ItemSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.*;
import org.springframework.data.solr.core.query.result.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
@Service(timeout = 5000)
public class ItemSearchServiceImpl implements ItemSearchService {

    @Autowired
    private SolrTemplate solrTemplate;
    @Autowired
    private RedisTemplate redisTemplate;


    @Override
    public Map<String, Object> search(Map searchMap) {
        Map<String, Object> map=new HashMap<String, Object>();
        String keywords =(String) searchMap.get("keywords");
        searchMap.put("keywords", keywords.replace(" ", ""));

        map.putAll(searchList(searchMap));
        List<String> categoryList = searchCategoryList(searchMap);
        map.put("categoryList",categoryList);


        String category =(String) searchMap.get("category");
        if (!category.equals("")){
            map.putAll(searchBrandListAndSpecList(category));

        }else {
            if (categoryList.size()>0) {
                map.putAll(searchBrandListAndSpecList(categoryList.get(0)));
            }
        }

        return map;
    }


    private Map searchList(Map searchMap){
        Map<String, Object> map=new HashMap<String, Object>();
        HighlightQuery query=new SimpleHighlightQuery();
        Criteria criteria=new Criteria("item_keywords");
        criteria.is(searchMap.get("keywords"));
        HighlightOptions options=new HighlightOptions();
        options.addField("item_title");
        options.setSimplePrefix("<em style='color:red'>");
        options.setSimplePostfix("</em>");
        //1.1关键字查询
        query.setHighlightOptions(options);
        query.addCriteria(criteria);
        //1.2分类过滤
        if (!"".equals(searchMap.get("category"))) {

            Criteria filterCriteria = new Criteria("item_category")
                    .is(searchMap.get("category"));
            FilterQuery filterQuery = new SimpleFilterQuery(filterCriteria);
            query.addFilterQuery(filterQuery);
        }
        //1.3品牌过滤
        if(!"".equals(searchMap.get("brand"))) {
            Criteria filterCriteria=new Criteria("item_brand")
                    .is(searchMap.get("brand"));
            FilterQuery filterQuery=new SimpleFilterQuery(filterCriteria);
            query.addFilterQuery(filterQuery);
        }
        //1.4规格过滤
        if(searchMap.get("spec")!=null) {
            Map<String,String> specMap= (Map) searchMap.get("spec");

            for (String key:specMap.keySet()){

                Criteria filterCriteria=new Criteria("item_spec_"+key)
                        .is(specMap.get(key));
                FilterQuery filterQuery=new SimpleFilterQuery(filterCriteria);
                query.addFilterQuery(filterQuery);
            }
        }
        //1.5价格过滤
      if (!"".equals(searchMap.get("price"))){
          String priceStr =(String) searchMap.get("price");
          String[] prices = priceStr.split("-");
          if (!prices[0].equals("0")){
              Criteria filterCriteria=new Criteria("item_price")
                      .greaterThanEqual(prices[0]);
              FilterQuery filterQuery=new SimpleFilterQuery(filterCriteria);
              query.addFilterQuery(filterQuery);
          }
          if (!prices[1].equals("*")){
              Criteria filterCriteria=new Criteria("item_price")
                      .lessThanEqual(prices[1]);
              FilterQuery filterQuery=new SimpleFilterQuery(filterCriteria);
              query.addFilterQuery(filterQuery);
          }

      }
        //1.6 分页查询
        Integer pageNo =(Integer) searchMap.get("pageNo");
        if (pageNo==null){
            pageNo=1;
        }
        Integer pageSize =(Integer) searchMap.get("pageSize");
        if (pageSize==null){
            pageSize=20;
        }
        //1.7 排序
        String sortValue =(String) searchMap.get("sort");
        String sortField =(String)searchMap.get("sortField");

        if (sortValue!=null&&!"".equals(sortValue)) {
            if (sortValue.equals("ASC")) {
                Sort sort = new Sort(Sort.Direction.ASC,"item_"+sortField);
                query.addSort(sort);
            }
            if (sortValue.equals("DESC")) {
                Sort sort = new Sort(Sort.Direction.DESC,"item_"+sortField);
                query.addSort(sort);
            }

        }


        query.setOffset((pageNo-1)*pageSize);
        query.setRows(pageSize);
        //高亮显示处理
        HighlightPage<TbItem> page = solrTemplate.queryForHighlightPage(query, TbItem.class);
        List<HighlightEntry<TbItem>> highlightEntryList = page.getHighlighted();
        for (HighlightEntry<TbItem> h: highlightEntryList){
            TbItem entity = h.getEntity();
            if (h.getHighlights().size()>0&&
                    h.getHighlights().get(0).getSnipplets().size()>0){
                entity.setTitle(h.getHighlights().get(0).getSnipplets().get(0));
            }

        }

        map.put("rows", page.getContent());
        map.put("totalPages", page.getTotalPages()); //总页数
        map.put("total", page.getTotalElements()); //总记录数
        return map;
    }

    private List<String> searchCategoryList(Map searchMap){
        List list=new ArrayList();

        Query query=new SimpleQuery("*:*");
        Criteria criteria=new Criteria("item_keywords")
                .is(searchMap.get("keywords"));
        query.addCriteria(criteria);

        GroupOptions options=new GroupOptions();
        options.addGroupByField("item_category");
        query.setGroupOptions(options);

        GroupPage<TbItem> page = solrTemplate.queryForGroupPage(query, TbItem.class);
        GroupResult<TbItem> groupResult  = page.getGroupResult("item_category");
        Page<GroupEntry<TbItem>> entries = groupResult.getGroupEntries();
        List<GroupEntry<TbItem>> contentList = entries.getContent();

        for (GroupEntry<TbItem> content:contentList){
            list.add(content.getGroupValue());
        }


        return list;
    }

    private Map searchBrandListAndSpecList(String category){
        Map map=new HashMap();

        Long typeId  =(Long) redisTemplate.boundHashOps("itemCat").get(category);

        if (typeId!=null){
            List brandList = (List)redisTemplate.boundHashOps("brandList").get(typeId);
            map.put("brandList",brandList);
            System.out.println("brandList:"+brandList.size());
            List specList = (List)redisTemplate.boundHashOps("specList").get(typeId);
            map.put("specList",specList);
            System.out.println("specList:"+specList.size());
        }


        return map;
    }

    @Override
    public void importList(List list) {
        solrTemplate.saveBeans(list);
        solrTemplate.commit();
    }
}
