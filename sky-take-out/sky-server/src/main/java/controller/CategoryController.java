package controller;

import dto.CategoryDTO;
import dto.CategoryPageQueryDTO;
import entity.Category;
import result.PageResult;
import result.Result;
import service.CategoryService;
import valid.groups.Add;
import valid.groups.Update;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@Slf4j
@Validated
@RequestMapping("/admin/category")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @GetMapping("/page")
    public Result<PageResult<Category>> getCategoryList(@Valid CategoryPageQueryDTO categoryPageQueryDTO) {
        log.info("分类查询，参数为：{}", categoryPageQueryDTO);
        PageResult<Category> categoryPageResult = categoryService.getCategoryList(categoryPageQueryDTO);
        return Result.success(categoryPageResult);
    }

    @PostMapping
    public Result<?> addCategory(@RequestBody @Validated(Add.class) CategoryDTO categoryDTO) {
        log.info("新增分类，参数为：{}", categoryDTO);
        boolean result = categoryService.addCategory(categoryDTO);
        return result ? Result.success() : Result.error("新增失败");
    }

    @PutMapping
    public Result<?> updateCategory(@RequestBody @Validated(Update.class) CategoryDTO categoryDTO) {
        log.info("修改分类，参数为：{}", categoryDTO);
        boolean result = categoryService.updateCategory(categoryDTO);
        return result ? Result.success() : Result.error("更新失败");
    }

    @DeleteMapping

    public Result<?> delCategory(@RequestParam @ApiParam(value = "分类ID", required = true) Long id) {
        log.info("删除分类，参数为: {}", id);
        boolean result = categoryService.delCategory(id);
        return result ? Result.success() : Result.error("删除失败");
    }

    @PostMapping("/status/{status}")

    public Result<?> changeCategoryStatus(
            @PathVariable
            @ApiParam(value = "分类状态", allowableValues = "0, 1", required = true)
            @Min(value = 0, message = "status错误")
            @Max(value = 1, message = "status错误")
            Integer status,

            @RequestParam
            @ApiParam(value = "分类ID", required = true)
            Long id
    ) {
        log.info("修改分类状态，状态为：{}，ID为：{}", status, id);
        boolean result = categoryService.changeCategoryStatus(id, status);
        return result ? Result.success() : Result.error("更新失败");
    }

    @GetMapping("/list")

    public Result<List<Category>> listCategoryByType(
            @RequestParam
            @ApiParam(value = "分类类型", allowableValues = "1, 2", required = true)
            @Min(value = 1, message = "分类类型错误")
            @Max(value = 2, message = "分类类型错误")
            Integer type
    ) {
        log.info("根据类型查询分类，参数：{}", type);
        List<Category> categories = categoryService.listCategoryByType(type);
        return Result.success(categories);
    }

}
