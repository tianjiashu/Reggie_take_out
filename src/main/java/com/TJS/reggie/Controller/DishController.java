package com.TJS.reggie.Controller;

import com.TJS.reggie.Dto.DishDto;
import com.TJS.reggie.Service.CategoryService;
import com.TJS.reggie.Service.DishFlavorService;
import com.TJS.reggie.Service.DishService;
import com.TJS.reggie.Service.SetmealDishService;
import com.TJS.reggie.common.CustomException;
import com.TJS.reggie.common.R;
import com.TJS.reggie.domain.Category;
import com.TJS.reggie.domain.Dish;
import com.TJS.reggie.domain.DishFlavor;
import com.TJS.reggie.domain.SetmealDish;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
/*
缓存菜品数据，categoryID
清理缓存（凡是和菜品有关的都需要清理）：
    更新菜品
    新增菜品
    删除菜品
    修改状态
    如果缓存了套餐，套餐也需要清理
 */

@Slf4j
@RestController
@RequestMapping("/dish")
public class DishController {

    @Autowired
    private DishService dishService;
    @Autowired
    private DishFlavorService dishFlavorService;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private SetmealDishService setmealDishService;
    @Autowired
    private RedisTemplate redisTemplate;

    @PostMapping
    /*
    保存菜品数据
     */
    public R<String> save(@RequestBody DishDto dishDto){
        dishService.SaveWithDishFlavor(dishDto);
        CleanCahe(dishDto.getCategoryId());//清理缓存
        return R.success("保存成功！");
    }

    @GetMapping("/page")
    public R<Page<DishDto>> GetPage(int page, int pageSize, String name){
        //---------------------------------------------------
        Page<Dish> page_dish = new Page<>(page,pageSize);
        Page<DishDto> page_dishdto = new Page<>(page,pageSize);

        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(name!=null,Dish::getName,name);
        queryWrapper.orderByDesc(Dish::getUpdateTime);

        dishService.page(page_dish, queryWrapper);
        /*
        使用page_dish查出来的数据，然后拷贝到page_dishdto，除了records
         */
        BeanUtils.copyProperties(page_dish,page_dishdto,"records");
        //---------------------------------------------------

        /*
        因为page_dish和page_dishdto中的records泛型不一致，需要手动的添加。
         */
        List<Dish> records = page_dish.getRecords();
        List<DishDto> list = new ArrayList<>();


        for (Dish record : records) {
            DishDto dishDto = new DishDto();
            BeanUtils.copyProperties(record,dishDto);
            //需要加CategoryName
            Long categoryId = record.getCategoryId();
            Category category = categoryService.getById(categoryId);
            if(category!=null){
                dishDto.setCategoryName(category.getName());
            }
            list.add(dishDto);
        }
        page_dishdto.setRecords(list);
        return R.success(page_dishdto);
    }

    @GetMapping("{id}")
    public R<DishDto> GetData(@PathVariable Long id){

        DishDto dishDto = dishService.GetWithDishFlavor(id);
        return R.success(dishDto);
    }

    @PutMapping
    /*
    更新菜品数据
     */
    public R<String> update(@RequestBody DishDto dishDto){

        dishService.UpdateWithDishFlavor(dishDto);
        CleanCahe(dishDto.getCategoryId());//清理缓存
        return R.success("更新成功");
    }

    @GetMapping("/list")
    /*
    http://localhost/dish/list?categoryId=1547136304621363202&status=1?name=...
    三个字段：categoryId   status  name
     */
    public R<List<DishDto>> list(Dish dish){
        List<DishDto> dishDtos = null;
        //动态key
        String key = "dish_"+dish.getCategoryId()+"_"+dish.getStatus()+(dish.getName()==null?"":"_"+dish.getName());

        log.info("key = {}",key);
        //查缓存数据
        dishDtos = (List<DishDto>) redisTemplate.opsForValue().get(key);
        if(dishDtos!=null){
            //如果缓存中有，就直接返回
            return R.success(dishDtos);
        }

        //缓存中没有就查询数据库
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        //根据传入的字段查询数据库
        String name = dish.getName();
        Long categoryId = dish.getCategoryId();
        queryWrapper.eq(categoryId!=null,Dish::getCategoryId,categoryId);
        queryWrapper.like(name!=null,Dish::getName,name);
        queryWrapper.eq(Dish::getStatus,1);
        queryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);

        List<Dish> dishes = dishService.list(queryWrapper);

        //封装dishdto
        dishDtos = new ArrayList<>();


        for (Dish dish_ : dishes) {
            DishDto dishDto = new DishDto();
            BeanUtils.copyProperties(dish_,dishDto);
            //需要加 口味表  根据dishID查询口味表
            Long dishId = dish_.getId();
            LambdaQueryWrapper<DishFlavor> lambdaQueryWrapper = new LambdaQueryWrapper<>();
            lambdaQueryWrapper.eq(DishFlavor::getDishId,dishId);
            lambdaQueryWrapper.orderByDesc(DishFlavor::getUpdateTime);
            List<DishFlavor> dishFlavors = dishFlavorService.list(lambdaQueryWrapper);
            dishDto.setFlavors(dishFlavors);
            dishDtos.add(dishDto);
        }
        //数据库查到之后就缓存到redis中
        redisTemplate.opsForValue().set(key,dishDtos,60, TimeUnit.MINUTES);
        return R.success(dishDtos);
    }

    //POST
    //	http://localhost/dish/status/0?ids=1547490145237786626  停售
    @PostMapping("/status/{status}")
    public R<String> SetStutas(@RequestParam List<Long> ids,@PathVariable int status){
        /*
        菜品可能会关联套餐！，如果关联也不能删除。
         */
        if(status==0){
            LambdaQueryWrapper<SetmealDish> queryWrapper1 = new LambdaQueryWrapper<>();
            queryWrapper1.in(SetmealDish::getDishId,ids);
            int count = setmealDishService.count(queryWrapper1);
            if(count>0)throw new CustomException("存在菜品还关联套餐，不能停售");
        }
        List<Dish> dishes = new ArrayList<>();
        for (Long id : ids) {
            Dish dish = new Dish();
            dish.setId(id);
            dish.setStatus(status);
            dishes.add(dish);
        }
        boolean b = dishService.updateBatchById(dishes);
        //根据dishid查categoryid
        dishes = dishService.listByIds(ids);
        for (Dish dish : dishes) {
            CleanCahe(dish.getCategoryId());  //清理缓存
        }
        return b?R.success("更新成功"):R.error("更新失败");
    }

    /*
    DELETE
	http://localhost/dish?ids=1547490145237786626  删除
     */
    @DeleteMapping
    public R<String> deleteDish(@RequestParam List<Long> ids){
        /*
        如果菜品还在出售就不能删除
         */
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(Dish::getId,ids);
        queryWrapper.eq(Dish::getStatus,1);
        int count = dishService.count(queryWrapper);
        if(count>0){
            throw new CustomException("存在菜品还在售卖，不能删除！");
        }
        /*
        菜品可能会关联套餐！，如果关联也不能删除。
         */
        LambdaQueryWrapper<SetmealDish> queryWrapper1 = new LambdaQueryWrapper<>();
        queryWrapper1.in(SetmealDish::getDishId,ids);
        int count1 = setmealDishService.count(queryWrapper1);
        if(count1>0)throw new CustomException("存在菜品还关联套餐，不能删除");

        //删除dish
        boolean remove = dishService.removeByIds(ids);
        if(!remove)throw new CustomException("删除失败");
        LambdaQueryWrapper<DishFlavor> queryWrapper2 = new LambdaQueryWrapper<>();
        queryWrapper2.in(DishFlavor::getDishId,ids);
        boolean remove1 = dishFlavorService.remove(queryWrapper2);
        if(!remove1)throw new CustomException("删除失败_2");

        //根据dishid查categoryid
        List<Dish> dishes = dishService.listByIds(ids);
        for (Dish dish : dishes) {
            CleanCahe(dish.getCategoryId());  //清理缓存
        }
        return R.success("删除成功！");
    }



    //单独抽取一个方法  传入categoryid
    private void CleanCahe(long Categoryid){
        String key = "dish_"+Categoryid+"*";
        log.info("要删除的key = {}",key);
        Set keys = redisTemplate.keys(key);
        log.info("keySet:{}",keys.toString());
        redisTemplate.delete(keys);
    }
}
