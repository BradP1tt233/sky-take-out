package controller;

import dto.DishDTO;
import dto.DishPageQueryDTO;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import result.PageResult;
import result.Result;
import service.DishService;

import java.util.List;

@RestController
@RequestMapping("/admin/dish")
@Slf4j
public class DishController {
    @Autowired
    private DishService dishService;
    @PostMapping
    public Result save(@RequestBody @Valid DishDTO dishDTO){
    dishService.save(dishDTO);
    return Result.success();
    }

    @GetMapping("/page")
    public Result<PageResult> page( @Valid DishPageQueryDTO dishPageQueryDTO){
        PageResult pageResult = dishService.page(dishPageQueryDTO);
        return Result.success(pageResult);
    }
    @DeleteMapping
    public Result deleteBatch(@RequestParam List<Long> ids){
       dishService.deleteBatch(ids);
       return Result.success("菜品删除成功");
    }

}
