package com.TJS.reggie.Controller;

import com.TJS.reggie.Dto.DishDto;
import com.TJS.reggie.Dto.SetmealDto;
import com.TJS.reggie.Service.*;
import com.TJS.reggie.common.R;
import com.TJS.reggie.domain.*;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("setmeal")
@Slf4j
public class SetmealController {

    @Autowired
    private DishFlavorService dishFlavorService;
    @Autowired
    private DishService dishService;
    @Autowired
    private SetmealService setmealService;
    @Autowired
    private SetmealDishService setmealDishService;
    @Autowired
    private CategoryService categoryService;


    @PostMapping
    public R<String> save(@RequestBody SetmealDto dto){
        setmealService.saveWithmeal(dto);
        return R.success("存储成功");
    }
    @GetMapping("/page")
    public R<Page<SetmealDto>> GetPage(int page, int pageSize, String name){
        Page<Setmeal> P = new Page<>(page,pageSize);
        Page<SetmealDto> P_dto = new Page<>(page,pageSize);

        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(name!=null,Setmeal::getName,name);
        setmealService.page(P,queryWrapper);
        BeanUtils.copyProperties(P,P_dto,"records");

        List<SetmealDto> records_dto = new ArrayList<>();
        List<Setmeal> records = P.getRecords();
        for (Setmeal record : records) {
            SetmealDto dto = new SetmealDto();
            BeanUtils.copyProperties(record,dto);
            Category category = categoryService.getById(record.getCategoryId());
            dto.setCategoryName(category.getName());
            records_dto.add(dto);
        }
        P_dto.setRecords(records_dto);

        return R.success(P_dto);
    }

    @DeleteMapping
    public R<String> delete(@RequestParam List<Long> ids){
        setmealService.deleteWithdish(ids);
        return R.success("删除成功！");
    }
    @PostMapping("/status/{status}")
    public R<String> update(@RequestParam List<Long> ids,@PathVariable int status){
        List<Setmeal> setmeals = new ArrayList<>();
        for (Long id : ids) {
            Setmeal setmeal = new Setmeal();
            setmeal.setId(id);
            setmeal.setStatus(status);
            setmeals.add(setmeal);
        }
        boolean b = setmealService.updateBatchById(setmeals);
        log.info("修改成功：{}",b);
        String msg = status==1?"起售成功":"停售成功";
        return R.success(msg);
    }

    @GetMapping("list")
    public R<List<Setmeal>> list(Setmeal setmeal){
        log.info("{}",setmeal.toString());
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();

        Long categoryId = setmeal.getCategoryId();
        queryWrapper.eq(categoryId!=null,Setmeal::getCategoryId,categoryId);
        queryWrapper.eq(Setmeal::getStatus,1);
        queryWrapper.orderByDesc(Setmeal::getUpdateTime);

        List<Setmeal> setmeals = setmealService.list(queryWrapper);


        return R.success(setmeals);
    }
}
