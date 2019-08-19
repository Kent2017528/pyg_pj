package com.pinyougou.sellergoods.service;

import com.pinyougou.pojo.TbBrand;
import entity.PageResult;

import java.util.List;

public interface BrandService {

    public List<TbBrand> findAll();
    public PageResult findBrandByPage(int pageNum, int pageSize);
    public int add(TbBrand tbBrand);

    public TbBrand findOne(long id);
    public int update(TbBrand tbBrand);
    public void delete(long[] ids);
    public PageResult search(TbBrand tbBrand,int pageNum, int pageSize);
}
