package dto;

import valid.groups.Add;
import valid.groups.Update;
import lombok.Data;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;

@Data

public class CategoryDTO implements Serializable {

    //主键

    @NotNull(groups = Update.class, message = "分类ID不能为空")
    private Long id;

    //类型 1 菜品分类 2 套餐分类

    @Min(value = 1, message = "分类类型有误，请输入1或2")
    @Max(value = 2, message = "分类类型有误，请输入1或2")
    @NotNull(groups = Add.class, message = "类型不能为空")
    private Integer type;

    //分类名称

    @NotBlank(message = "分类名称不能为空")
    private String name;

    //排序

    @NotNull(message = "排序值不能为空")
    private Integer sort;

}
