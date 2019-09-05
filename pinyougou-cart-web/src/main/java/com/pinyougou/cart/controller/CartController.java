package com.pinyougou.cart.controller;


import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.pinyougou.cart.service.CartService;
import com.pinyougou.pojogroup.Cart;
import entity.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import util.CookieUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@RestController
@RequestMapping("/cart")
public class CartController {

    @Reference
    private CartService cartService;

    @Autowired
    private HttpServletRequest request;
    @Autowired
    private HttpServletResponse response;

    @RequestMapping("/findCartList")
     public List<Cart> findCartList(){

        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        if ("anonymousUser".equals(username)){//已登录

        }else {//未登录

        }

        String cartListStr = CookieUtil.getCookieValue(request, "cartList", "UTF-8");
       if (cartListStr==null||"".equals(cartListStr)){
           cartListStr="[]";
       }
       return JSON.parseArray(cartListStr, Cart.class);
   }

    @RequestMapping("/addGoodsToCartList")
     public Result addGoodsToCartList(Long itemId,Integer num){
        try {
            //从cookie中查到购物车列表
            List<Cart> cartList = findCartList();

            //调用cartServive操作购物车
            cartList =cartService.addGoodsToCartList(cartList, itemId, num);
            //将购物车添加到cooki中
            CookieUtil.setCookie(request, response, "cartList",
                    JSON.toJSONString(cartList),3600*24, "UTF-8");
            return new Result(true, "添加购物车成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(true, "添加购物车失败");
        }
    }
}
