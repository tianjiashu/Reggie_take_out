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
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

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

    @PostMapping
    public R<String> save(@RequestBody DishDto dishDto){
        dishService.SaveWithDishFlavor(dishDto);
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
    public R<String> update(@RequestBody DishDto dishDto){

        dishService.UpdateWithDishFlavor(dishDto);
        return R.success("更新成功");
    }

    @GetMapping("/list")
    public R<List<DishDto>> list(Dish dish){
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();

        String name = dish.getName();
        Long categoryId = dish.getCategoryId();
        queryWrapper.eq(categoryId!=null,Dish::getCategoryId,categoryId);
        queryWrapper.like(name!=null,Dish::getName,name);
        queryWrapper.eq(Dish::getStatus,1);
        queryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);

        List<Dish> dishes = dishService.list(queryWrapper);

        List<DishDto> dishDtos = new ArrayList<>();


        for (Dish dish_ : dishes) {
            DishDto dishDto = new DishDto();
            BeanUtils.copyProperties(dish_,dishDto);
            //需要加 口味表
            Long dishId = dish_.getId();
            LambdaQueryWrapper<DishFlavor> lambdaQueryWrapper = new LambdaQueryWrapper<>();
            lambdaQueryWrapper.eq(DishFlavor::getDishId,dishId);
            lambdaQueryWrapper.orderByDesc(DishFlavor::getUpdateTime);
            List<DishFlavor> dishFlavors = dishFlavorService.list(lambdaQueryWrapper);
            dishDto.setFlavors(dishFlavors);
            dishDtos.add(dishDto);
        }
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
        return R.success("删除成功！");
    }

}
