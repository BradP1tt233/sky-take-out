package dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import valid.groups.Add;
import valid.groups.Update;
import java.io.Serializable;

@Data
public class EmployeeDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "姓名不能为空")
    @Size(max = 32, message = "姓名不能超过32个字符")
    private String name;

    private String phone;

    @Pattern(regexp = "[01]", message = "性别只能为0或1")
    private String sex;

    private String idNumber;

    @Null(groups = Add.class, message = "新增员工时不能指定ID")
    @NotNull(groups = Update.class, message = "员工ID不能为空")
    @Positive(message = "员工ID必须大于0")
    private Long id;

    @NotBlank(message = "用户名不能为空")
    @Size(max = 32, message = "用户名不能超过32个字符")
    private String username;

    @Min(value = 0, message = "账号状态只能为0或1")
    @Max(value = 1, message = "账号状态只能为0或1")
    private Integer status;
}
