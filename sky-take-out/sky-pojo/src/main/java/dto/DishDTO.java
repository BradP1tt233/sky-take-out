package dto;

import entity.DishFlavor;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Data
public class DishDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    // 菜品 ID，非必填
    private Long id;

    @NotNull(message = "分类ID不能为空")
    private Long categoryId;

    @NotBlank(message = "菜品名称不能为空")
    private String name;

    @NotNull(message = "菜品价格不能为空")
    private BigDecimal price;

    @NotBlank(message = "菜品图片路径不能为空")
    private String image;

    private String description;

    // 1：起售，0：停售；非必填
    @Min(value = 0, message = "菜品状态只能为0或1")
    @Max(value = 1, message = "菜品状态只能为0或1")
    private Integer status;

    // 口味列表非必填；提供口味对象时，级联校验其名称和值
    @Valid
    private List<DishFlavor> flavors;
}
