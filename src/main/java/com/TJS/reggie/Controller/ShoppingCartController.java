package com.TJS.reggie.Controller;

import com.TJS.reggie.Service.ShoppingCartService;
import com.TJS.reggie.common.BaseContext;
import com.TJS.reggie.common.R;
import com.TJS.reggie.domain.ShoppingCart;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.xml.ws.soap.Addressing;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("shoppingCart")
@Slf4j
public class ShoppingCartController {

    @Autowired
    private ShoppingCartService shoppingCartService;


    @PostMapping("add")
    public R<ShoppingCart> add(@RequestBody ShoppingCart shoppingCart){
        //获取用户ID,菜品ID,套餐ID
        long userId = BaseContext.get();
        shoppingCart.setUserId(userId);
        log.info("userID:{}",userId);
        Long dishId = shoppingCart.getDishId();
        Long setmealId = shoppingCart.getSetmealId();
        //先查一下，这个菜品/套餐，之前是否已经加入购物车
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId,userId);
        queryWrapper.eq(dishId!=null,ShoppingCart::getDishId,dishId);
        queryWrapper.eq(setmealId!=null,ShoppingCart::getSetmealId,setmealId);
        ShoppingCart shopping = shoppingCartService.getOne(queryWrapper);

        if(shopping!=null){
            //之前已经下单过
            Integer number = shopping.getNumber();
            shopping.setNumber(number+1);
            shoppingCartService.updateById(shopping);
        }else {
            //之前没有下单过
            shopping = shoppingCart;
            shopping.setNumber(1);
            shopping.setCreateTime(LocalDateTime.now());
            shoppingCartService.save(shoppingCart);
        }
        return R.success(shopping);
    }

    @PostMapping("sub")
    public R<String> sub(@RequestBody ShoppingCart shoppingCart){
        //获取用户ID,菜品ID,套餐ID
        long userId = BaseContext.get();
        shoppingCart.setUserId(userId);
        Long dishId = shoppingCart.getDishId();
        Long setmealId = shoppingCart.getSetmealId();
        //先查一下，这个菜品/套餐，
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId,userId);
        queryWrapper.eq(dishId!=null,ShoppingCart::getDishId,dishId);
        queryWrapper.eq(setmealId!=null,ShoppingCart::getSetmealId,setmealId);
        ShoppingCart shopping = shoppingCartService.getOne(queryWrapper);

        Integer number = shopping.getNumber();
        if(number==1)shoppingCartService.removeById(shopping);
        else shoppingCartService.updateById(shopping);
        return R.success("OK!");
    }
    @GetMapping("list")
    public R<List<ShoppingCart>> list(){
        //获取用户ID
        long userId = BaseContext.get();
        //根据用户ID查数据库
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId,userId);
        List<ShoppingCart> shoppingCarts = shoppingCartService.list(queryWrapper);

        return R.success(shoppingCarts);
    }

    @DeleteMapping("clean")
    public R<String> delete(){
        //获取用户ID
        long userId = BaseContext.get();
        //根据用户ID查数据库
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId,userId);

        shoppingCartService.remove(queryWrapper);
        return R.success("OK!");
    }
}
