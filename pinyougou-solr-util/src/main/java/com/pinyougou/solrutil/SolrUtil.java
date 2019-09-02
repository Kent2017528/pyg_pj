package com.pinyougou.solrutil;

import com.alibaba.fastjson.JSON;
import com.pinyougou.mapper.TbItemMapper;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojo.TbItemExample;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.Query;
import org.springframework.data.solr.core.query.SimpleQuery;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

import javax.print.attribute.standard.Destination;
import java.util.List;
import java.util.Map;

@Component
public class SolrUtil {
    @Autowired
    private SolrTemplate solrTemplate;
    @Autowired
    private TbItemMapper itemMapper;


    public void importItem(){
        TbItemExample example=new TbItemExample();
        TbItemExample.Criteria criteria = example.createCriteria();
        criteria.andStatusEqualTo("1");
        List<TbItem> tbItems = itemMapper.selectByExample(example);

        for (TbItem tbItem:tbItems){
            System.out.println(tbItem.getBrand());
            Map map = JSON.parseObject(tbItem.getSpec(), Map.class);
            tbItem.setSpecMap(map);
        }
        solrTemplate.saveBeans(tbItems);
        solrTemplate.commit();
        System.out.println("end");
    }
    public void delete(){
        Query query=new SimpleQuery("*:*");
        solrTemplate.delete(query);
        solrTemplate.commit();
    }

    public static void main(String[] args) {
        ApplicationContext context=new ClassPathXmlApplicationContext
                ("classpath*:spring/application*.xml");
        SolrUtil solrUtil =(SolrUtil) context.getBean("solrUtil");
        solrUtil.delete();
    }
}
