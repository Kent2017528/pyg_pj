package com.pinyougou.page.service.impl;

import com.pinyougou.mapper.TbGoodsDescMapper;
import com.pinyougou.mapper.TbGoodsMapper;
import com.pinyougou.mapper.TbItemCatMapper;
import com.pinyougou.mapper.TbItemMapper;
import com.pinyougou.page.service.ItemPageService;
import com.pinyougou.pojo.TbGoods;
import com.pinyougou.pojo.TbGoodsDesc;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojo.TbItemExample;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfig;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
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
    @Autowired
    private TbItemCatMapper itemCatMapper;
    @Autowired
    private TbItemMapper itemMapper;

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

            String itemCat1 = itemCatMapper.selectByPrimaryKey(goods.getCategory1Id()).getName();
            String itemCat2 = itemCatMapper.selectByPrimaryKey(goods.getCategory2Id()).getName();
            String itemCat3 = itemCatMapper.selectByPrimaryKey(goods.getCategory3Id()).getName();
            dataModel.put("itemCat1",itemCat1);
            dataModel.put("itemCat2",itemCat2);
            dataModel.put("itemCat3",itemCat3);

            TbItemExample itemExample=new TbItemExample();
            TbItemExample.Criteria criteria = itemExample.createCriteria();
            criteria.andGoodsIdEqualTo(goodId);
            criteria.andStatusEqualTo("1");
            itemExample.setOrderByClause("is_default desc");
            List<TbItem> itemList = itemMapper.selectByExample(itemExample);
            dataModel.put("itemList",itemList);

            out=new FileWriter(pageDir+goodId+".html");
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

    @Override
    public boolean deleteItemHtml(Long[] goodsIds) {
        try {
            for(Long goodsId:goodsIds){
                new File(pageDir+goodsId+".html").delete();
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

    }
}
