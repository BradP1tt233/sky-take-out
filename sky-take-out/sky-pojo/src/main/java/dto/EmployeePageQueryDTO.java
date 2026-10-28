package dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class EmployeePageQueryDTO {
    @Size(max = 32, message = "姓名查询条件不能超过32个字符")
    private String name;

    @Min(value = 1, message = "页码必须大于等于1")
    private int page = 1;

    @Min(value = 1, message = "每页条数必须大于等于1")
    @Max(value = 100, message = "每页条数不能超过100")
    private int pagesize = 10;
}
