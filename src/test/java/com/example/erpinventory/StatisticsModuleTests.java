package com.example.erpinventory;

import com.example.erpinventory.entity.InboundOrder;
import com.example.erpinventory.entity.OutboundOrder;
import com.example.erpinventory.entity.Product;
import com.example.erpinventory.entity.SysUser;
import com.example.erpinventory.mapper.UserMapper;
import com.example.erpinventory.service.InboundOrderService;
import com.example.erpinventory.service.OutboundOrderService;
import com.example.erpinventory.service.ProductService;
import com.example.erpinventory.util.JwtUtil;
import com.fasterxml.jackson.databind.JsonNode;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = ErpInventorySystemApplication.class)
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
class StatisticsModuleTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProductService productService;

    @Autowired
    private InboundOrderService inboundOrderService;

    @Autowired
    private OutboundOrderService outboundOrderService;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private JwtUtil jwtUtil;

    @Test
    void productAndInventoryStatsShouldReflectCommittedData() throws Exception {
        String uniqueCategory = "stats-category-" + System.nanoTime();
        String lowStockProductName = "Stats Low Product " + System.nanoTime();

        JsonNode beforeProductStats = getDataNode("/api/statistics/product/stats", bearerToken("admin"));
        JsonNode beforeInventoryStats = getDataNode("/api/statistics/inventory/stats", bearerToken("admin"));

        Product lowStockProduct = createProduct(lowStockProductName, uniqueCategory, 4, new BigDecimal("10.00"));
        createProduct("Stats High Product " + System.nanoTime(), uniqueCategory, 20, new BigDecimal("5.00"));

        JsonNode afterProductStats = getDataNode("/api/statistics/product/stats", bearerToken("admin"));
        JsonNode afterInventoryStats = getDataNode("/api/statistics/inventory/stats", bearerToken("admin"));

        assertThat(afterProductStats.path("totalProducts").asLong())
                .isEqualTo(beforeProductStats.path("totalProducts").asLong() + 2);
        assertThat(afterProductStats.path("lowStockCount").asLong())
                .isEqualTo(beforeProductStats.path("lowStockCount").asLong() + 1);
        assertThat(afterProductStats.path("categoryDistribution").path(uniqueCategory).asLong()).isEqualTo(2);

        assertThat(afterInventoryStats.path("totalStock").asInt())
                .isEqualTo(beforeInventoryStats.path("totalStock").asInt() + 24);
        assertThat(afterInventoryStats.path("totalValue").decimalValue())
                .isEqualByComparingTo(beforeInventoryStats.path("totalValue").decimalValue().add(new BigDecimal("140.00")));
        assertThat(afterInventoryStats.path("lowStockProducts").toString()).contains(lowStockProduct.getName());
    }

    @Test
    void inboundAndOutboundStatsShouldReflectAuditedOrders() throws Exception {
        Product product = createProduct("Stats Flow Product " + System.nanoTime(), "stats-flow", 10, new BigDecimal("8.00"));

        JsonNode beforeInboundStats = getDataNode("/api/statistics/inbound/stats", bearerToken("admin"));
        JsonNode beforeOutboundStats = getDataNode("/api/statistics/outbound/stats", bearerToken("admin"));

        InboundOrder inboundOrder = new InboundOrder();
        inboundOrder.setProductId(product.getId());
        inboundOrder.setQuantity(6);
        inboundOrder.setPrice(new BigDecimal("12.50"));
        inboundOrder.setSupplier("Stats Supplier");
        inboundOrder.setRemark("stats inbound");

        InboundOrder createdInbound = inboundOrderService.createInboundOrder(inboundOrder);
        createdInbound.setCreateBy(999L);
        createdInbound.setCreateByName("tester");
        inboundOrderService.updateById(createdInbound);
        assertThat(inboundOrderService.auditInboundOrder(createdInbound.getId())).isTrue();

        OutboundOrder outboundOrder = new OutboundOrder();
        outboundOrder.setProductId(product.getId());
        outboundOrder.setQuantity(3);
        outboundOrder.setPrice(new BigDecimal("20.00"));
        outboundOrder.setCustomer("Stats Customer");
        outboundOrder.setRemark("stats outbound");

        OutboundOrder createdOutbound = outboundOrderService.createOutboundOrder(outboundOrder);
        createdOutbound.setCreateBy(999L);
        createdOutbound.setCreateByName("tester");
        outboundOrderService.updateById(createdOutbound);
        assertThat(outboundOrderService.auditOutboundOrder(createdOutbound.getId())).isTrue();

        JsonNode afterInboundStats = getDataNode("/api/statistics/inbound/stats", bearerToken("admin"));
        JsonNode afterOutboundStats = getDataNode("/api/statistics/outbound/stats", bearerToken("admin"));

        assertThat(afterInboundStats.path("totalInbound").asLong())
                .isEqualTo(beforeInboundStats.path("totalInbound").asLong() + 1);
        assertThat(afterInboundStats.path("totalQuantity").asInt())
                .isEqualTo(beforeInboundStats.path("totalQuantity").asInt() + 6);
        assertThat(afterInboundStats.path("totalAmount").decimalValue())
                .isEqualByComparingTo(beforeInboundStats.path("totalAmount").decimalValue().add(new BigDecimal("75.00")));

        assertThat(afterOutboundStats.path("totalOutbound").asLong())
                .isEqualTo(beforeOutboundStats.path("totalOutbound").asLong() + 1);
        assertThat(afterOutboundStats.path("totalQuantity").asInt())
                .isEqualTo(beforeOutboundStats.path("totalQuantity").asInt() + 3);
        assertThat(afterOutboundStats.path("totalAmount").decimalValue())
                .isEqualByComparingTo(beforeOutboundStats.path("totalAmount").decimalValue().add(new BigDecimal("60.00")));
    }

    @Test
    void statisticsEndpointShouldRejectUserWithoutDashboardPermission() throws Exception {
        SysUser user = createUserWithoutRole("stats-no-role-" + System.nanoTime());

        mockMvc.perform(get("/api/statistics/product/stats")
                        .header("Authorization", bearerToken(user.getUsername())))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(403));
    }

    @Test
    void inventoryTransactionExportShouldRejectUserWithoutExportPermission() throws Exception {
        mockMvc.perform(get("/api/inventory-transaction/export")
                        .header("Authorization", bearerToken("purchase")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(403));
    }

    @Test
    void inventoryTransactionExportShouldAllowUserWithExportPermission() throws Exception {
        Product product = createProduct("Export Allowed " + System.nanoTime(), "export", 8, new BigDecimal("9.90"));

        mockMvc.perform(get("/api/inventory-transaction/export")
                        .header("Authorization", bearerToken("manager"))
                        .param("productId", String.valueOf(product.getId())))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-disposition", org.hamcrest.Matchers.containsString(".xlsx")))
                .andExpect(header().string("Content-Type", org.hamcrest.Matchers.containsString(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                )));
    }

    private JsonNode getDataNode(String path, String authorization) throws Exception {
        String content = mockMvc.perform(get(path).header("Authorization", authorization))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readTree(content).path("data");
    }

    private Product createProduct(String name, String category, int stock, BigDecimal price) {
        Product product = new Product();
        product.setName(name);
        product.setDescription("statistics module test product");
        product.setSku("STAT-" + System.nanoTime());
        product.setPrice(price);
        product.setCategory(category);
        product.setUnit("pcs");
        product.setStatus(1);
        product.setStock(stock);
        product.setMinStock(2);
        product.setMaxStock(200);
        productService.save(product);
        assertThat(product.getId()).isNotNull();
        return product;
    }

    private SysUser createUserWithoutRole(String username) {
        SysUser user = new SysUser();
        user.setUsername(username);
        user.setPassword("unused");
        user.setRealName("No Role User");
        user.setEmail(username + "@example.com");
        user.setStatus(1);
        user.setDeleted(0);
        userMapper.insert(user);
        assertThat(user.getId()).isNotNull();
        return user;
    }

    private String bearerToken(String username) {
        Long userId = userMapper.selectByUsername(username).getId();
        return "Bearer " + jwtUtil.generateToken(username, userId);
    }
}
