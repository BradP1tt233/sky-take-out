package service;

import dto.EmployeeDTO;
import dto.EmployeePageQueryDTO;
import dto.EmployeeLoginDTO;
import entity.Employee;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import result.PageResult;

public interface EmployeeService {
    Employee login(EmployeeLoginDTO loginDTO);
    public boolean save(Employee employee);

    PageResult<Employee> page(EmployeePageQueryDTO employeePageQueryDTO);

    void update(EmployeeDTO employeeDTO);

    Employee getById(Long id);
}
