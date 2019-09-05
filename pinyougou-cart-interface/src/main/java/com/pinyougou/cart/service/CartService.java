package com.pinyougou.cart.service;

import com.pinyougou.pojogroup.Cart;

import java.util.List;

public interface CartService {


    public List<Cart> addGoodsToCartList(List<Cart> cartList, Long itemId,Integer num);

    public List<Cart> findCartListFromRedis(String userName);

    public void saveCartListToRedis(String username,List<Cart> cartList);
}
