package com.pinyougou.sellergoods.service.impl;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.pinyougou.mapper.TbSpecificationOptionMapper;
import com.pinyougou.pojo.*;
import com.pinyougou.pojogroup.Specification;
import org.springframework.beans.factory.annotation.Autowired;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.mapper.TbSpecificationMapper;
import com.pinyougou.pojo.TbSpecificationExample.Criteria;
import com.pinyougou.sellergoods.service.SpecificationService;

import entity.PageResult;
import org.springframework.transaction.annotation.Transactional;

/**
 * 服务实现层
 * @author Administrator
 *
 */
@Service
@Transactional
public class SpecificationServiceImpl implements SpecificationService {

	@Autowired
	private TbSpecificationMapper specificationMapper;
	@Autowired
	private TbSpecificationOptionMapper specificationOptionMapper;
	
	/**
	 * 查询全部
	 */
	@Override
	public List<TbSpecification> findAll() {
		return specificationMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);		
		Page<TbSpecification> page=   (Page<TbSpecification>) specificationMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 增加
	 */
	@Override
	public void add(Specification specification) {
		TbSpecification tbSpecification = specification.getSpecification();
		 specificationMapper.insert(tbSpecification);


		//获取tbSpecification在数据库的id
		String specName = tbSpecification.getSpecName();
		TbSpecificationExample example=new TbSpecificationExample();
		Criteria criteria = example.createCriteria();
		criteria.andSpecNameEqualTo(specName);
		List<TbSpecification> newSpecs = specificationMapper.selectByExample(example);
		TbSpecification newSpec = newSpecs.get(0);
		Long id = newSpec.getId();
		//*****************************




		List<TbSpecificationOption> specificationOptionList =
				specification.getSpecificationOptionList();
		for (TbSpecificationOption specificationOption:specificationOptionList){
			specificationOption.setSpecId(id);
			specificationOptionMapper.insert(specificationOption);
		}


	}

	
	/**
	 * 修改
	 */
	@Override
	public void update(Specification specification){
		TbSpecification tbSpecification = specification.getSpecification();
		specificationMapper.updateByPrimaryKey(tbSpecification);


		//获取tbSpecification在数据库的id
		String specName = tbSpecification.getSpecName();
		TbSpecificationExample example=new TbSpecificationExample();
		Criteria criteria = example.createCriteria();
		criteria.andSpecNameEqualTo(specName);
		List<TbSpecification> newSpecs = specificationMapper.selectByExample(example);
		TbSpecification newSpec = newSpecs.get(0);
		Long id = newSpec.getId();
		//*****************************

		TbSpecificationOptionExample optionExample=new TbSpecificationOptionExample();
		optionExample.createCriteria().andSpecIdEqualTo(id);
		specificationOptionMapper.deleteByExample(optionExample);


		List<TbSpecificationOption> specificationOptionList =
				specification.getSpecificationOptionList();
		for (TbSpecificationOption specificationOption:specificationOptionList){
			specificationOption.setSpecId(id);
			specificationOptionMapper.insert(specificationOption);
		}


	}	
	
	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public Specification findOne(Long id){
		Specification specification=new Specification();
		TbSpecification tbSpecification = specificationMapper.selectByPrimaryKey(id);
		specification.setSpecification(tbSpecification);

		TbSpecificationOptionExample example =new TbSpecificationOptionExample();
		TbSpecificationOptionExample.Criteria criteria = example.createCriteria();
		criteria.andSpecIdEqualTo(id);
		List<TbSpecificationOption> list =
				specificationOptionMapper.selectByExample(example);

		specification.setSpecificationOptionList(list);

		return specification;
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		for(Long id:ids){
			specificationMapper.deleteByPrimaryKey(id);


			//同时删除tb_SpecificationOption表对应的数据
			TbSpecificationOptionExample optionExample=new TbSpecificationOptionExample();
			optionExample.createCriteria().andSpecIdEqualTo(id);
			specificationOptionMapper.deleteByExample(optionExample);
		}		
	}
	
	
		@Override
	public PageResult findPage(TbSpecification specification, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		
		TbSpecificationExample example=new TbSpecificationExample();
		Criteria criteria = example.createCriteria();
		
		if(specification!=null){			
						if(specification.getSpecName()!=null && specification.getSpecName().length()>0){
				criteria.andSpecNameLike("%"+specification.getSpecName()+"%");
			}
	
		}
		
		Page<TbSpecification> page= (Page<TbSpecification>)specificationMapper.selectByExample(example);		
		return new PageResult(page.getTotal(), page.getResult());
	}

	@Override
	public List<Map> selectOptionList() {

		List<Map> list= new ArrayList<Map>();
		Map<String,String> map=null;
		List<TbSpecification> specifications = specificationMapper.selectByExample(null);
		for (TbSpecification specification:specifications){
			map=new HashMap();
			map.put("id", specification.getId().toString());
			map.put("text", specification.getSpecName());
			list.add(map);
		}

		return list;
	}
}
