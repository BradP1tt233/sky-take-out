package dto;

import lombok.Data;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;

import java.io.Serializable;

@Data

public class CategoryPageQueryDTO implements Serializable {

    //页码

    private int page = 1;

    //每页记录数

    private int pageSize = 10;

    //分类名称

    private String name;

    //分类类型 1菜品分类  2套餐分类

    @Min(value = 1, message = "分类类型错误")
    @Max(value = 2, message = "分类类型错误")
    private Integer type;
}
