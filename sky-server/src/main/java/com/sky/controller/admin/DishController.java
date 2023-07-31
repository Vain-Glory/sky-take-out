package com.sky.controller.admin;


import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

/**
 * 菜品管理
 */
@RestController
@Api(tags = "菜品相关接口")
@Slf4j
@RequestMapping("/admin/dish")
public class DishController {

    @Autowired
    private DishService dishService;
    @Autowired
    private RedisTemplate redisTemplate;
    /**
     * 新增菜品
     * @param dishDTO
     * @return
     */
    @ApiOperation("新增菜品")
    @PostMapping
    public Result save(@RequestBody DishDTO dishDTO){
        dishService.saveWithFlavor(dishDTO);
        String key = "dish_" + dishDTO.getCategoryId();
        cleanCache(key);
        return Result.success();
    }

    /**
     * 分页查询菜品
     * @param dishPageQueryDTO
     * @return
     */
    @ApiOperation("分页查询菜品")
    @GetMapping("/page")
    public Result<PageResult>  page(DishPageQueryDTO dishPageQueryDTO){
        log.info("菜品分页查询:{}",dishPageQueryDTO);
        PageResult pageResult = dishService.pageQuery(dishPageQueryDTO);
        return Result.success(pageResult);
    }

    /**
     * 批量删除菜品
     * @param ids
     * @return
     */
    @ApiOperation("批量删除菜品")
    @DeleteMapping
    public Result deleteBatch(@RequestParam List<Long> ids){
        log.info("删除菜品:{}",ids);
        dishService.deleteBatch(ids);
        cleanCache("dish_*");
        return Result.success();
    }

    /**
     * 根据id查询菜品
     * @param id
     * @return
     */
    @ApiOperation("根据id查询菜品")
    @GetMapping("/{id}")
    public Result<DishVO> getById(@PathVariable Long id){
        log.info("根据id查询菜品:{}",id);
        DishVO dishVO = dishService.getByIdWithFlavor(id);
        return Result.success(dishVO);
    }

    /**
     * 根据分类id查询菜品
     * @param categoryId
     * @return
     */
    @GetMapping("/list")
    @ApiOperation("根据分类id查询菜品")
    public Result<List<Dish>> list(Long categoryId){
        List<Dish> list = dishService.list(categoryId);
        return Result.success(list);
    }

    @ApiOperation("修改菜品")
    @PutMapping
    public Result update(@RequestBody DishDTO dishDTO){
        log.info("修改菜品:{}",dishDTO);
        dishService.updateWithFlavor(dishDTO);
        cleanCache("dish_*");
        return Result.success();
    }


    /**
     * 菜品启售停售
     * @param status
     * @param id
     * @return
     */
    @ApiOperation("菜品启售、停售")
    @PostMapping("/status/{status}")
    public Result startOrStop(@PathVariable Integer status,Long id){
        dishService.startOrStop(status,id);
        cleanCache("dish_*");
        return  Result.success();
    }

    private void cleanCache(String pattern){
        Set keys = redisTemplate.keys(pattern);
        redisTemplate.delete(keys);
    }
}
