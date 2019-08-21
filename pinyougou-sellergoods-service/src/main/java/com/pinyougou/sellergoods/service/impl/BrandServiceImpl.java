package com.pinyougou.sellergoods.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.mapper.TbBrandMapper;
import com.pinyougou.pojo.TbBrand;
import com.pinyougou.pojo.TbBrandExample;
import com.pinyougou.sellergoods.service.BrandService;
import entity.PageResult;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class BrandServiceImpl implements BrandService {
    @Autowired
    private TbBrandMapper tbBrandMapper;

    @Override
    public List<TbBrand> findAll() {

        return tbBrandMapper.selectByExample(null);
    }

    @Override
    public PageResult findBrandByPage(int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
       Page<TbBrand> page =(Page<TbBrand>) tbBrandMapper.selectByExample(null);

        return new PageResult(page.getTotal(),page.getResult());
    }

    @Override
    public int add(TbBrand tbBrand) {

        return tbBrandMapper.insert(tbBrand);
    }

    @Override
    public TbBrand findOne(long id) {
        return tbBrandMapper.selectByPrimaryKey(id);
    }

    @Override
    public int update(TbBrand tbBrand) {

        return tbBrandMapper.updateByPrimaryKey(tbBrand);
    }

    @Override
    public void delete(long[] ids) {
        for(long id:ids){
            tbBrandMapper.deleteByPrimaryKey(id);
        }
    }

    @Override
    public PageResult search(TbBrand tbBrand, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);

        TbBrandExample example=new TbBrandExample();
        TbBrandExample.Criteria criteria = example.createCriteria();
        if (null!=tbBrand){
            if (tbBrand.getName()!=null){
                criteria.andNameLike("%"+tbBrand.getName()+"%");
            }
            if (tbBrand.getFirstChar()!=null){
                criteria.andFirstCharLike("%"+tbBrand.getFirstChar()+"%");
            }
        }


        Page<TbBrand> page =(Page<TbBrand>) tbBrandMapper.selectByExample(example);

        return new PageResult(page.getTotal(),page.getResult());
    }

    @Override
    public List<Map> selectOptionList() {
        List<Map> list= new ArrayList<Map>();
        Map<String,String> map=null;
        List<TbBrand> tbBrands = tbBrandMapper.selectByExample(null);
        for (TbBrand tbBrand:tbBrands){
            map=new HashMap();
            map.put("id", tbBrand.getId().toString());
            map.put("text", tbBrand.getName());
            list.add(map);
        }

        return list;
    }
}
