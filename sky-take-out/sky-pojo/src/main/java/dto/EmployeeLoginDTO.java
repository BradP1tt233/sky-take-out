package dto;

import lombok.Data;
import lombok.ToString;

import jakarta.validation.constraints.NotBlank;
import java.io.Serializable;

@Data

public class EmployeeLoginDTO implements Serializable {

    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    @ToString.Exclude
    private String password;

}
