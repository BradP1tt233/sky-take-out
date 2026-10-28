package com.brad.skyserver;

import config.SecurityConfig;
import handler.GlobalExceptionHandler;
import com.brad.skyserver.security.*;
import controller.EmployeeController;
import entity.Employee;
import mapper.EmployeeMapper;
import mapper.CategoryMapper;
import mapper.DishMapper;
import mapper.SetMealMapper;
import dto.EmployeeDTO;
import jakarta.validation.Validator;
import valid.groups.Add;
import valid.groups.Update;
import service.impl.EmployeeServiceImpl;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import result.PageResult;
import dto.EmployeePageQueryDTO;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = EmployeeController.class, properties = {
        "sky.jwt.admin-secret-key=test-only-secret-key-at-least-32-bytes-long", "sky.jwt.admin-ttl=7200000"})
@Import({SecurityConfig.class, EmployeeTokenService.class, EmployeeServiceImpl.class, GlobalExceptionHandler.class})
class EmployeeLoginTests {
    @Autowired MockMvc mvc;
    @Autowired PasswordEncoder passwords;
    @Autowired JwtEncoder encoder;
    @Autowired JwtDecoder decoder;
    @Autowired ObjectMapper json;
    @MockitoBean EmployeeMapper mapper;
    @MockitoBean CategoryMapper categoryMapper;
    @MockitoBean DishMapper dishMapper;
    @MockitoBean SetMealMapper setMealMapper;
    @Autowired Validator validator;
    @MockitoSpyBean EmployeeServiceImpl employeeService;
    private Employee employee;

    @BeforeEach
    void setup() {
        employee = new Employee();
        employee.setId(1L);
        employee.setUsername("admin");
        employee.setName("管理员");
        employee.setStatus(1);
        employee.setPassword(passwords.encode("123456"));
        when(mapper.getByUsername("admin")).thenReturn(employee);
    }
    private org.springframework.test.web.servlet.ResultActions login(String body) throws Exception {
        return mvc.perform(post("/admin/employee/login").contentType("application/json").content(body));
    }
    private String loginToken() throws Exception {
        String body = login("{\"username\":\"admin\",\"password\":\"123456\"}")
                .andExpect(status().isOk()).andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data.userName").value("admin"))
                .andExpect(jsonPath("$.data.password").doesNotExist())
                .andReturn().getResponse().getContentAsString();
        return json.readTree(body).path("data").path("token").asText();
    }
    @Test void loginReturnsSignedEmployeeToken() throws Exception {
        Jwt jwt = decoder.decode(loginToken());
        assertEquals("1", jwt.getSubject());
        assertEquals(1L, ((Number) jwt.getClaim("empId")).longValue());
        assertEquals(7200, jwt.getExpiresAt().getEpochSecond() - jwt.getIssuedAt().getEpochSecond());
    }
    @Test void wrongPasswordIsRejected() throws Exception {
        login("{\"username\":\"admin\",\"password\":\"wrong\"}")
                .andExpect(status().isUnauthorized()).andExpect(jsonPath("$.code").value(0));
        verify(mapper, never()).updatePassword(anyString(), anyString(), anyString());
    }
    @Test void unknownAccountIsRejected() throws Exception {
        login("{\"username\":\"missing\",\"password\":\"123456\"}")
                .andExpect(status().isUnauthorized()).andExpect(jsonPath("$.msg").value("用户名或密码错误"));
    }
    @Test void disabledAccountIsRejected() throws Exception {
        employee.setStatus(0);
        login("{\"username\":\"admin\",\"password\":\"123456\"}")
                .andExpect(status().isUnauthorized()).andExpect(jsonPath("$.msg").value("账号被锁定"));
    }
    @Test void blankCredentialsAreRejected() throws Exception {
        login("{\"username\":\" \",\"password\":\"\"}")
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value(0));
        verifyNoInteractions(mapper);
    }
    @Test void malformedBodyIsRejected() throws Exception {
        login("{").andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value(0));
    }
    @Test void legacyMd5IsUpgradedAfterLogin() throws Exception {
        String md5 = "e10adc3949ba59abbe56e057f20f883e";
        employee.setPassword(md5);
        loginToken();
        var captor = org.mockito.ArgumentCaptor.forClass(String.class);
        verify(mapper).updatePassword(eq("admin"), captor.capture(), eq(md5));
        assertTrue(captor.getValue().startsWith("$2"));
        assertTrue(passwords.matches("123456", captor.getValue()));
    }
    @Test void protectedEndpointRequiresToken() throws Exception {
        mvc.perform(get("/admin/employee/page")).andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(0));
        verifyNoInteractions(mapper);
    }
    @Test void bothTokenHeadersAreAccepted() throws Exception {
        String token = loginToken();
        mvc.perform(post("/admin/employee/logout").header("token", token)).andExpect(status().isOk());
        mvc.perform(post("/admin/employee/logout").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }
    @Test void badTokenIsRejected() throws Exception {
        mvc.perform(post("/admin/employee/logout").header("token", "invalid"))
                .andExpect(status().isUnauthorized()).andExpect(jsonPath("$.code").value(0));
    }
    @Test void expiredTokenIsRejected() throws Exception {
        Instant now = Instant.now();
        String token = encoder.encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(),
                JwtClaimsSet.builder().issuer("sky-admin").subject("1").claim("scope", "employee")
                        .issuedAt(now.minusSeconds(600)).expiresAt(now.minusSeconds(300)).build())).getTokenValue();
        mvc.perform(post("/admin/employee/logout").header("token", token))
                .andExpect(status().isUnauthorized());
    }
    @Test void loginIgnoresStaleToken() throws Exception {
        mvc.perform(post("/admin/employee/login").header("token", "expired")
                .contentType("application/json").content("{\"username\":\"admin\",\"password\":\"123456\"}"))
                .andExpect(status().isOk());
    }
    @Test void newEmployeesUseBcryptAndCurrentEmployeeId() throws Exception {
        employee.setId(42L);
        when(mapper.insert(any(Employee.class))).thenReturn(1);
        mvc.perform(post("/admin/employee").header("token", loginToken()).contentType("application/json")
                .content("{\"username\":\"new-user\",\"name\":\"新人\",\"createUser\":999,\"updateUser\":999}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.code").value(1));
        var captor = org.mockito.ArgumentCaptor.forClass(Employee.class);
        verify(mapper).insert(captor.capture());
        assertTrue(passwords.matches("123456", captor.getValue().getPassword()));
        assertTrue(captor.getValue().getPassword().startsWith("$2"));
        assertEquals(42L, captor.getValue().getCreateUser());
        assertEquals(42L, captor.getValue().getUpdateUser());
        assertEquals(captor.getValue().getCreateTime(), captor.getValue().getUpdateTime());
    }
    @Test void employeePageDatesUseReadableFormat() throws Exception {
        Employee record = new Employee();
        record.setId(2L);
        record.setUsername("worker");
        record.setCreateTime(LocalDateTime.of(2026, 10, 23, 9, 5, 7, 123456789));
        record.setUpdateTime(LocalDateTime.of(2026, 10, 23, 14, 30, 8));
        doReturn(new PageResult<Employee>(1L, List.of(record), 10, 1))
                .when(employeeService).page(any(EmployeePageQueryDTO.class));
        mvc.perform(get("/admin/employee/page").param("page", "1").param("pagesize", "10")
                .header("token", loginToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.records[0].createTime").value("2026-10-23 09:05:07"))
                .andExpect(jsonPath("$.data.records[0].updateTime").value("2026-10-23 14:30:08"));
    }
    @Test void employeeValidationRejectsBlankNamesBeforeInsert() throws Exception {
        mvc.perform(post("/admin/employee").header("token", loginToken())
                .contentType("application/json").content("{\"username\":\"worker\",\"name\":\" \"}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value(0));
        verify(mapper, never()).insert(any(Employee.class));
    }
    @Test void employeeEditRequiresId() throws Exception {
        mvc.perform(put("/admin/employee").header("token", loginToken())
                .contentType("application/json").content("{\"username\":\"worker\",\"name\":\"员工\"}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value(0));
        verify(mapper, never()).update(any(Employee.class));
    }
    @Test void invalidEmployeePaginationIsRejected() throws Exception {
        String token = loginToken();
        for (String[] params : new String[][] {{"0", "10"}, {"1", "0"}, {"1", "101"}}) {
            mvc.perform(get("/admin/employee/page").header("token", token)
                    .param("page", params[0]).param("pagesize", params[1]))
                    .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value(0));
        }
        verify(employeeService, never()).page(any(EmployeePageQueryDTO.class));
    }
    @Test void employeePaginationDefaultsAreApplied() throws Exception {
        doReturn(new PageResult<Employee>(0L, List.of(), 10, 1))
                .when(employeeService).page(any(EmployeePageQueryDTO.class));
        mvc.perform(get("/admin/employee/page").header("token", loginToken())).andExpect(status().isOk());
        var captor = org.mockito.ArgumentCaptor.forClass(EmployeePageQueryDTO.class);
        verify(employeeService).page(captor.capture());
        assertEquals(1, captor.getValue().getPage());
        assertEquals(10, captor.getValue().getPagesize());
    }
    @Test void employeeGroupsAndFieldFormatsAreValidated() {
        EmployeeDTO dto = new EmployeeDTO();
        dto.setName("测试员工"); dto.setUsername("worker");
        assertTrue(validator.validate(dto, Add.class).isEmpty());
        assertFalse(validator.validate(dto, Update.class).isEmpty());
        dto.setId(2L);
        assertTrue(validator.validate(dto, Update.class).isEmpty());
        assertFalse(validator.validate(dto, Add.class).isEmpty());
        dto.setId(-1L);
        assertFalse(validator.validate(dto, Update.class).isEmpty());
        dto.setId(2L); dto.setPhone("123"); dto.setSex("3"); dto.setIdNumber("abc"); dto.setStatus(2);
        var violations = validator.validate(dto, Update.class);
        for (String field : List.of("sex", "status")) {
            assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals(field)));
        }
        dto.setPhone("13800000000"); dto.setSex("1"); dto.setIdNumber("110101199001010011"); dto.setStatus(0);
        assertTrue(validator.validate(dto, Update.class).isEmpty());
        dto.setUsername(" ");
        assertFalse(validator.validate(dto, Update.class).isEmpty());
    }
}
