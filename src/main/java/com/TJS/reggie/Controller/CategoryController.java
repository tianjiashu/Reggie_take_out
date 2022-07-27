package com.TJS.reggie.Controller;

import com.TJS.reggie.Service.CategoryService;
import com.TJS.reggie.common.R;
import com.TJS.reggie.domain.Category;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@Slf4j
@RequestMapping("/category")
public class CategoryController {
    @Autowired
    private CategoryService service;


    @PostMapping
    public R<String> save(@RequestBody Category category){
        service.save(category);
        return R.success("保存成功！");
    }
    @GetMapping("/page")
    public R<Page<Category>> GetPage(int page,int pageSize){
        Page<Category> P = new Page<>(page,pageSize);
        LambdaQueryWrapper<Category> lqw = new LambdaQueryWrapper<>();
        lqw.orderByDesc(Category::getSort);
        service.page(P,lqw);
        return R.success(P);
    }

    @DeleteMapping
    public R<String> delete(long ids){
        service.remove(ids);
        return R.success("删除成功！");
    }

    @PutMapping
    public R<String> update(@RequestBody Category category){
        log.info(category.toString());
        service.updateById(category);
        return R.success("修改成功!");
    }

    @GetMapping("/list")
    public R<List<Category>> GetCategoryList(Category category){//也可以用 int type接收，但用实体接收更通用
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(category.getType()!=null,Category::getType,category.getType());
        queryWrapper.orderByAsc(Category::getSort).orderByDesc(Category::getUpdateTime);
        List<Category> categoryList = service.list(queryWrapper);
        return R.success(categoryList);
    }
}
