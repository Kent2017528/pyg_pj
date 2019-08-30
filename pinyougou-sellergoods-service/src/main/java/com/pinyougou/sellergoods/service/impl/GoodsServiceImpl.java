package com.pinyougou.sellergoods.service.impl;
import java.util.*;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.pinyougou.mapper.*;
import com.pinyougou.pojo.*;
import com.pinyougou.pojogroup.Goods;
import org.springframework.beans.factory.annotation.Autowired;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.pojo.TbGoodsExample.Criteria;
import com.pinyougou.sellergoods.service.GoodsService;

import entity.PageResult;
import org.springframework.transaction.annotation.Transactional;

/**
 * 服务实现层
 * @author Administrator
 *
 */
@Service
@Transactional
public class GoodsServiceImpl implements GoodsService {

	@Autowired
	private TbGoodsMapper goodsMapper;
	@Autowired
	private TbGoodsDescMapper tbGoodsDescMapper;
	@Autowired
	private TbItemMapper itemMapper;
	@Autowired
	private TbBrandMapper brandMapper;
	@Autowired
	private TbItemCatMapper itemCatMapper;
	@Autowired
	private TbSellerMapper sellerMapper;

	/**
	 * 查询全部
	 */
	@Override
	public List<TbGoods> findAll() {
		return goodsMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);		
		Page<TbGoods> page=   (Page<TbGoods>) goodsMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 增加
	 */
	@Override
	public void add(Goods goods) {
		TbGoods tbGoods = goods.getGoods();


		tbGoods.setAuditStatus("0");
		goodsMapper.insert(tbGoods);
		Long id = tbGoods.getId();


		TbGoodsDesc tbGoodsDesc = goods.getGoodsDesc();
		tbGoodsDesc.setGoodsId(id);
		tbGoodsDescMapper.insert(tbGoodsDesc);
		List<TbItem> itemList = goods.getItemList();
		saveItemList( goods ,tbGoods, tbGoodsDesc,itemList);




	}
	private void saveItemList(Goods goods,TbGoods tbGoods,TbGoodsDesc tbGoodsDesc,List<TbItem> itemList ){
		//查出品牌名称
		Long brandId = tbGoods.getBrandId();
		TbBrand tbBrand = brandMapper.selectByPrimaryKey(brandId);
		String brandName = tbBrand.getName();
		StringBuffer title=null;
		if ("1".equals(tbGoods.getIsEnableSpec())) {

			for (TbItem item : itemList) {
				//查出spec规格信息
				JSONObject jsonObject = JSON.parseObject(item.getSpec());
				Map<String, String> map = JSONObject.toJavaObject(jsonObject, Map.class);
				Set<String> strings = map.keySet();
				title = new StringBuffer(brandName);
				for (String s1 : strings) {
					System.out.println(map.get(s1));
					title.append(" " + map.get(s1));
				}
				//插入标题
				item.setTitle(title.toString());
				setItemValue(item, tbGoods, tbGoodsDesc);
				itemMapper.insert(item);
			}
		}else {
			TbItem item=new TbItem();
			item.setTitle(goods.getGoods().getGoodsName());//商品KPU+规格描述串作为SKU名称
			item.setPrice( goods.getGoods().getPrice() );//价格
			item.setStatus("1");//状态
			item.setIsDefault("1");//是否默认
			item.setNum(99999);//库存数量
			item.setSpec("{}");
			setItemValue( item ,tbGoods ,tbGoodsDesc );
			itemMapper.insert(item);
		}
	}

	private void setItemValue(TbItem item,TbGoods tbGoods,TbGoodsDesc tbGoodsDesc ){
		Long brandId = tbGoods.getBrandId();
		TbBrand tbBrand = brandMapper.selectByPrimaryKey(brandId);
		String brandName = tbBrand.getName();
		item.setGoodsId(tbGoods.getId());

		//插入图片地址
		String itemImages = tbGoodsDesc.getItemImages();
		List<Map> imageList = JSON.parseArray(itemImages, Map.class);
		if(imageList.size()>0) {
			item.setImage(imageList.get(0).get("url").toString());
		}
		//插入第3级分类的id和名称
		item.setCategoryid(tbGoods.getCategory3Id());
		TbItemCat tbItemCat = itemCatMapper.selectByPrimaryKey(tbGoods.getCategory3Id());
		item.setCategory(tbItemCat.getName());
		//插入品牌名称
		item.setBrand(brandName);

		//查出店铺名称
		TbSeller seller = sellerMapper.selectByPrimaryKey(tbGoods.getSellerId());
		//插入sellerId
		item.setSellerId(tbGoods.getSellerId());
		//插入店铺名称
		item.setSeller(seller.getNickName());

		//插入创建时间和修改时间
		item.setCreateTime(new Date());
		item.setUpdateTime(new Date());



	}
	
	/**
	 * 修改
	 */
	@Override
	public void update(Goods goods){
		TbGoods tbGoods = goods.getGoods();
		TbGoodsDesc goodsDesc = goods.getGoodsDesc();
		goodsMapper.updateByPrimaryKey(tbGoods);
		tbGoodsDescMapper.updateByPrimaryKey(goodsDesc);
		goods.getGoods().setAuditStatus("0");
		//删除
		TbItemExample example=new TbItemExample();
		example.createCriteria().andGoodsIdEqualTo(tbGoods.getId());
		itemMapper.deleteByExample(example);

		//插入
		List<TbItem> itemList = goods.getItemList();
		saveItemList( goods ,tbGoods, goodsDesc,itemList);

	}	
	
	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public Goods findOne(Long id){
		Goods goods=new Goods();
		TbGoods tbGoods = goodsMapper.selectByPrimaryKey(id);
		TbGoodsDesc tbGoodsDesc = tbGoodsDescMapper.selectByPrimaryKey(id);
		goods.setGoods(tbGoods);
		goods.setGoodsDesc(tbGoodsDesc);

		TbItemExample example=new TbItemExample();
		example.createCriteria().andGoodsIdEqualTo(id);
		List<TbItem> items = itemMapper.selectByExample(example);
		goods.setItemList(items);

		return goods;
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		for (Long id : ids) {
			TbGoods tbGoods = goodsMapper.selectByPrimaryKey(id);
			tbGoods.setIsDelete("1");
			goodsMapper.updateByPrimaryKey(tbGoods);

		}
	}
	
		@Override
	  public PageResult findPage(TbGoods goods, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		
		TbGoodsExample example=new TbGoodsExample();
		Criteria criteria = example.createCriteria();
		criteria.andIsDeleteIsNull();
		if(goods!=null){			
						if(goods.getSellerId()!=null && goods.getSellerId().length()>0){
				//criteria.andSellerIdLike("%"+goods.getSellerId()+"%");
							criteria.andSellerIdEqualTo(goods.getSellerId());
			}
			if(goods.getGoodsName()!=null && goods.getGoodsName().length()>0){
				criteria.andGoodsNameLike("%"+goods.getGoodsName()+"%");
			}
			if(goods.getAuditStatus()!=null && goods.getAuditStatus().length()>0){
				criteria.andAuditStatusLike("%"+goods.getAuditStatus()+"%");
			}
			if(goods.getIsMarketable()!=null && goods.getIsMarketable().length()>0){
				criteria.andIsMarketableLike("%"+goods.getIsMarketable()+"%");
			}
			if(goods.getCaption()!=null && goods.getCaption().length()>0){
				criteria.andCaptionLike("%"+goods.getCaption()+"%");
			}
			if(goods.getSmallPic()!=null && goods.getSmallPic().length()>0){
				criteria.andSmallPicLike("%"+goods.getSmallPic()+"%");
			}
			if(goods.getIsEnableSpec()!=null && goods.getIsEnableSpec().length()>0){
				criteria.andIsEnableSpecLike("%"+goods.getIsEnableSpec()+"%");
			}
			if(goods.getIsDelete()!=null && goods.getIsDelete().length()>0){
				criteria.andIsDeleteLike("%"+goods.getIsDelete()+"%");
			}
	
		}
		
		Page<TbGoods> page= (Page<TbGoods>)goodsMapper.selectByExample(example);		
		return new PageResult(page.getTotal(), page.getResult());
	}

	@Override
	public void updateAuditStatus(Long[] ids, String status) {

		for (Long id:ids){
			TbGoods tbGoods = goodsMapper.selectByPrimaryKey(id);
			tbGoods.setAuditStatus(status);
			goodsMapper.updateByPrimaryKey(tbGoods);
		}
	}

	/**
	 * 通过SPU的id数组和审核状态查到SKU列表
	 * @param ids SPU的id数组
	 * @param status 审核状态
	 * @return SKU列表
	 */

	public List<TbItem> findItemListByGoodsIdandStatus(Long[]ids,String status){

		TbItemExample example=new TbItemExample();
		TbItemExample.Criteria criteria = example.createCriteria();
		criteria.andStatusEqualTo(status);
		criteria.andGoodsIdIn(Arrays.asList(ids));

		List<TbItem> tbItems = itemMapper.selectByExample(example);
		return tbItems;

	}
}
