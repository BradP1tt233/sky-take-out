package controller;

import com.brad.skyserver.security.EmployeeTokenService;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import valid.groups.Add;
import valid.groups.Update;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import constant.PasswordConstant;
import constant.StatusConstant;
import dto.EmployeeDTO;
import dto.EmployeeLoginDTO;
import dto.EmployeePageQueryDTO;
import entity.Employee;

import org.springframework.beans.BeanUtils;

import org.springframework.web.bind.annotation.*;
import result.PageResult;
import result.Result;
import service.EmployeeService;


import java.time.LocalDateTime;



import lombok.extern.slf4j.Slf4j;
import vo.EmployeeLoginVO;

@Slf4j
@RestController
@RequestMapping("/admin/employee")
public class EmployeeController {
    private final EmployeeService employeeService;

    private final EmployeeTokenService tokenService;
    private final PasswordEncoder passwordEncoder;

    public EmployeeController(EmployeeService employeeService, EmployeeTokenService tokenService,
                              PasswordEncoder passwordEncoder) {
        this.employeeService = employeeService;
        this.tokenService = tokenService;
        this.passwordEncoder = passwordEncoder;
    }
    @PostMapping("/login")
    public Result login(@RequestBody @Valid EmployeeLoginDTO employeeLoginDTO) {
        log.info("员工登录：{}", employeeLoginDTO.getUsername());

        Employee employee = employeeService.login(employeeLoginDTO);

        String token = tokenService.createToken(employee);

        EmployeeLoginVO employeeLoginVO = EmployeeLoginVO.builder()
                .id(employee.getId())
                .userName(employee.getUsername())
                .name(employee.getName())
                .token(token)
                .build();

        return Result.success(employeeLoginVO);
    }

    /**
     * 退出
     *
     * @return
     */
    @PostMapping("/logout")
    public Result logout() {
        return Result.success();
    }

    @PostMapping
    public Result save(@RequestBody @Validated(Add.class) EmployeeDTO employeeDTO, @AuthenticationPrincipal Jwt jwt){
        log.info("新增员工");
        Employee employee = new Employee();
        BeanUtils.copyProperties(employeeDTO,employee);
        LocalDateTime now = LocalDateTime.now();
        employee.setCreateTime(now);
        employee.setUpdateTime(now);
        employee.setStatus(StatusConstant.ENABLE);
        employee.setPassword(passwordEncoder.encode(PasswordConstant.DEFAULT_PASSWORD));
        // JWT 已由 Spring Security 校验，subject 是登录员工的 ID。
      long employeeId = Long.parseLong(jwt.getSubject());
      employee.setCreateUser(employeeId);
      employee.setUpdateUser(employeeId);
        return employeeService.save(employee)?Result.success():Result.error("保存失败");
    }

    @GetMapping("/page")
    public Result page(@Valid EmployeePageQueryDTO employeePageQueryDTO){
        log.info("分页查询员工，参数为{}",employeePageQueryDTO);
      PageResult<Employee> employeePageResult = employeeService.page(employeePageQueryDTO);
      return Result.success(employeePageResult);
    }
    @PostMapping("/status/{status}")
    public Result startOrStop(@PathVariable int status,Long id){
        log.info("启用或禁用员工账号，参数为{},{}",status,id);
        EmployeeDTO employeeDTO = new EmployeeDTO();
        employeeDTO.setId(id);
        employeeDTO.setStatus(status);
        employeeService.update(employeeDTO);
        return Result.success();
    }
    @GetMapping("/{id}")
    public Result<Employee> getById(@PathVariable Long id){
        log.info("根据id获取员工信息,参数为{}",id);
        Employee employee = employeeService.getById(id);
        return Result.success(employee);
    }
    @PutMapping
    public Result updateEmployee(@RequestBody @Validated(Update.class) EmployeeDTO employeeDTO){
        log.info("编辑员工信息，参数{}",employeeDTO);
        employeeService.update(employeeDTO);
        return Result.success();
    }
}
