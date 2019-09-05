package com.pinyougou.cart.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.cart.service.CartService;
import com.pinyougou.mapper.TbItemMapper;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojo.TbOrderItem;
import com.pinyougou.pojogroup.Cart;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private TbItemMapper tbItemMapper;
    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public List<Cart> addGoodsToCartList(List<Cart> cartList, Long itemId, Integer num) {



        //1.通过itemId查到sku对象
        TbItem item = tbItemMapper.selectByPrimaryKey(itemId);
        if(item==null){
            throw new RuntimeException("商品不存在");
        }
        if(!item.getStatus().equals("1")){
            throw new RuntimeException("商品状态无效");
        }
        //2.通过sku对象查到商家的id
        String sellerId = item.getSellerId();
        //3.通过商家id查询购物车列表中的这个商家购物车对象
        Cart cart = searchCartBySellerId(cartList, sellerId);

        if (cart==null){ //4.原来的购物车列表不存在这个商家

            //4.1创建一个购物车对象
            cart=new Cart();

            cart.setSellerId(sellerId);
            cart.setSellerName(item.getSeller());

            TbOrderItem orderItem=createOrderItem(item,num);
            List<TbOrderItem> orderItemList=new ArrayList<TbOrderItem>();
            orderItemList.add(orderItem);

            cart.setOrderItemList(orderItemList);
            //4.2把这个商家购物车对象添加到购物车列表中
            cartList.add(cart);
        }else { //5.原来的购物车列表存在这个商家
            List<TbOrderItem> orderItemList = cart.getOrderItemList();
            TbOrderItem orderItem=searchOrderItemByItemId(orderItemList,itemId);
            if (orderItem==null){
                //5.1如果这个商品在购物车对象的商品明细列表不存在，就新增一个商品明细对象
                 orderItem=createOrderItem(item,num);
                 orderItemList.add(orderItem);
            }else {
                //5.2如果这个商品在购物车对象的商品明细列表存在，就增加这个商品的数量和金额
                orderItem.setNum(orderItem.getNum()+num);
                orderItem.setTotalFee
                        (new BigDecimal(orderItem.getPrice().doubleValue()*orderItem.getNum()));

                if (orderItem.getNum()<=0){
                    orderItemList.remove(orderItem);
                }

                if (orderItemList.size()==0){
                    cartList.remove(cart);
                }
            }

        }
        return cartList;
    }

    /**
     * 根据商品明细ID查询
     * @return
     */

    private TbOrderItem searchOrderItemByItemId( List<TbOrderItem> orderItemList,Long itemId){

        for (TbOrderItem orderItem:orderItemList){
            if (orderItem.getItemId().longValue()==itemId.longValue()){
                return orderItem;
            }
        }
        return null;
    }

    /**
     * 根据商家id查询购物车对象
     * @param cartList
     * @param sellerId
     * @return
     */
    private Cart searchCartBySellerId(List<Cart> cartList, String sellerId){

        for (Cart cart:cartList){
            if (cart.getSellerId().equals(sellerId)){
                return cart;
            }
        }
        return null;
    }
    /**
     * 创建订单明细
     * @param item
     * @param num
     * @return
     */
    private TbOrderItem createOrderItem(TbItem item,Integer num){
        if(num<=0){
            throw new RuntimeException("数量非法");
        }

        TbOrderItem orderItem=new TbOrderItem();
        orderItem.setGoodsId(item.getGoodsId());
        orderItem.setItemId(item.getId());
        orderItem.setNum(num);
        orderItem.setPicPath(item.getImage());
        orderItem.setPrice(item.getPrice());
        orderItem.setSellerId(item.getSellerId());
        orderItem.setTitle(item.getTitle());
        orderItem.setTotalFee(new BigDecimal(item.getPrice().doubleValue()*num));
        return orderItem;
    }

    @Override
    public List<Cart> findCartListFromRedis(String userName) {
        System.out.println("从redis中提取购物车数据....."+userName);
        List<Cart> cartList =(List<Cart>) redisTemplate.boundHashOps
                ("cartList").get(userName);
        if (cartList==null){
            cartList=new ArrayList();
        }

        return cartList;
    }

    @Override
    public void saveCartListToRedis(String userName, List<Cart> cartList) {
        System.out.println("向redis存入购物车数据....."+userName);
        redisTemplate.boundHashOps("cartList").put(userName, cartList);
    }
}
