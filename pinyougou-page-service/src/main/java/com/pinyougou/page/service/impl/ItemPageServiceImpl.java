package com.pinyougou.page.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.mapper.TbGoodsDescMapper;
import com.pinyougou.mapper.TbGoodsMapper;
import com.pinyougou.page.service.ItemPageService;
import com.pinyougou.pojo.TbGoods;
import com.pinyougou.pojo.TbGoodsDesc;
import com.pinyougou.pojogroup.Goods;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfig;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

@Service
public class ItemPageServiceImpl implements ItemPageService {

    @Autowired
    private FreeMarkerConfig freeMarkerConfig;
    @Value("${pagedir}")
    private String pageDir;
    @Autowired
    private TbGoodsMapper goodsMapper;
    @Autowired
    private TbGoodsDescMapper goodsDescMapper;

    @Override
    public boolean getItemHtml(Long goodId) {
        Writer out=null;
        try {
            Configuration configuration = freeMarkerConfig.getConfiguration();
            Template template=configuration.getTemplate("item.ftl");

            Map dataModel=new  HashMap();
            TbGoods goods = goodsMapper.selectByPrimaryKey(goodId);
            TbGoodsDesc goodsDesc = goodsDescMapper.selectByPrimaryKey(goodId);
            dataModel.put("goods",goods);
            dataModel.put("goodsDesc",goodsDesc);

            out=new FileWriter(pageDir+goodId+"html");
            template.process(dataModel, out);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }finally {
            try {
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
}
