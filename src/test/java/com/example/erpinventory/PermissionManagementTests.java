package com.example.erpinventory;

import com.example.erpinventory.entity.InboundOrder;
import com.example.erpinventory.entity.Product;
import com.example.erpinventory.mapper.UserMapper;
import com.example.erpinventory.service.InboundOrderService;
import com.example.erpinventory.service.ProductService;
import com.example.erpinventory.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = ErpInventorySystemApplication.class)
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
class PermissionManagementTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProductService productService;

    @Autowired
    private InboundOrderService inboundOrderService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserMapper userMapper;

    @Test
    void loginShouldReturnRoleAndPermissionList() throws Exception {
        mockMvc.perform(post("/api/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "username", "admin",
                                "password", "admin123"
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.roleCode").value("ADMIN"))
                .andExpect(jsonPath("$.data.permissions").isArray())
                .andExpect(jsonPath("$.data.permissions[?(@ == 'user:view')]").exists())
                .andExpect(jsonPath("$.data.permissions[?(@ == 'inbound:audit')]").exists());
    }

    @Test
    void purchaseRoleShouldNotAuditInboundOrder() throws Exception {
        InboundOrder order = createInboundOrder();

        mockMvc.perform(put("/api/inbound/{id}/audit", order.getId())
                        .header("Authorization", bearerToken("purchase")))
                .andExpect(status().isForbidden());
    }

    @Test
    void creatorShouldNotAuditOwnInboundOrder() throws Exception {
        Product product = createProduct("maker-audit", 15);

        mockMvc.perform(post("/api/inbound")
                        .header("Authorization", bearerToken("admin"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "productId", product.getId(),
                                "quantity", 3,
                                "price", 18.5,
                                "supplier", "Self Supplier",
                                "remark", "self audit case"
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        InboundOrder latestOrder = inboundOrderService.list().stream()
                .max((left, right) -> Long.compare(left.getId(), right.getId()))
                .orElseThrow();

        assertThat(latestOrder.getCreateByName()).isEqualTo("系统管理员");

        mockMvc.perform(put("/api/inbound/{id}/audit", latestOrder.getId())
                        .header("Authorization", bearerToken("admin")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value("制单人不能审核自己的入库单"));
    }

    private InboundOrder createInboundOrder() {
        Product product = createProduct("permission", 20);
        InboundOrder order = new InboundOrder();
        order.setProductId(product.getId());
        order.setQuantity(5);
        order.setPrice(new BigDecimal("15.80"));
        order.setSupplier("Permission Supplier");
        order.setRemark("permission inbound");
        return inboundOrderService.createInboundOrder(order);
    }

    private Product createProduct(String suffix, int stock) {
        Product product = new Product();
        product.setName("Permission Product " + suffix);
        product.setDescription("permission test product");
        product.setSku("PERM-" + suffix.toUpperCase() + "-" + System.nanoTime());
        product.setPrice(new BigDecimal("88.80"));
        product.setCategory("permission-category");
        product.setUnit("pcs");
        product.setStatus(1);
        product.setStock(stock);
        product.setMinStock(2);
        product.setMaxStock(100);
        productService.save(product);
        return product;
    }

    private String bearerToken(String username) {
        Long userId = userMapper.selectByUsername(username).getId();
        return "Bearer " + jwtUtil.generateToken(username, userId);
    }
}
