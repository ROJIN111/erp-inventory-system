package com.example.erpinventory;

import com.example.erpinventory.entity.InboundOrder;
import com.example.erpinventory.entity.InventoryCheck;
import com.example.erpinventory.entity.InventoryCheckItem;
import com.example.erpinventory.entity.InventoryTransaction;
import com.example.erpinventory.entity.OutboundOrder;
import com.example.erpinventory.entity.Product;
import com.example.erpinventory.exception.BusinessException;
import com.example.erpinventory.mapper.UserMapper;
import com.example.erpinventory.service.InboundOrderService;
import com.example.erpinventory.service.InventoryCheckService;
import com.example.erpinventory.service.InventoryTransactionService;
import com.example.erpinventory.service.OutboundOrderService;
import com.example.erpinventory.service.ProductService;
import com.example.erpinventory.util.JwtUtil;
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
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = ErpInventorySystemApplication.class)
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
class InventoryTransactionModuleTests {

    private static final DateTimeFormatter QUERY_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Autowired
    private ProductService productService;

    @Autowired
    private InboundOrderService inboundOrderService;

    @Autowired
    private OutboundOrderService outboundOrderService;

    @Autowired
    private InventoryCheckService inventoryCheckService;

    @Autowired
    private InventoryTransactionService inventoryTransactionService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void auditInboundOrderShouldCreateInboundTransaction() {
        Product product = createProduct("inbound", 10);

        InboundOrder order = new InboundOrder();
        order.setProductId(product.getId());
        order.setQuantity(5);
        order.setPrice(new BigDecimal("12.50"));
        order.setSupplier("Phase8 Supplier");
        order.setRemark("phase8 inbound test");

        InboundOrder created = inboundOrderService.createInboundOrder(order);
        created.setCreateBy(999L);
        created.setCreateByName("tester");
        inboundOrderService.updateById(created);
        boolean audited = inboundOrderService.auditInboundOrder(created.getId());

        List<InventoryTransaction> transactions = inventoryTransactionService
                .listTransactionsByRelated(created.getId(), created.getOrderNo());

        assertThat(audited).isTrue();
        assertThat(transactions).hasSize(1);

        InventoryTransaction transaction = transactions.get(0);
        assertThat(transaction.getTransactionType()).isEqualTo(1);
        assertThat(transaction.getQuantity()).isEqualTo(5);
        assertThat(transaction.getBeforeStock()).isEqualTo(10);
        assertThat(transaction.getAfterStock()).isEqualTo(15);
        assertThat(transaction.getRelatedNo()).isEqualTo(created.getOrderNo());
    }

    @Test
    void auditOutboundOrderShouldCreateOutboundTransaction() {
        Product product = createProduct("outbound", 18);

        OutboundOrder order = new OutboundOrder();
        order.setProductId(product.getId());
        order.setQuantity(6);
        order.setPrice(new BigDecimal("22.80"));
        order.setCustomer("Phase8 Customer");
        order.setRemark("phase8 outbound test");

        OutboundOrder created = outboundOrderService.createOutboundOrder(order);
        created.setCreateBy(999L);
        created.setCreateByName("tester");
        outboundOrderService.updateById(created);
        boolean audited = outboundOrderService.auditOutboundOrder(created.getId());

        List<InventoryTransaction> transactions = inventoryTransactionService
                .listTransactionsByRelated(created.getId(), created.getOrderNo());

        assertThat(audited).isTrue();
        assertThat(transactions).hasSize(1);

        InventoryTransaction transaction = transactions.get(0);
        assertThat(transaction.getTransactionType()).isEqualTo(2);
        assertThat(transaction.getQuantity()).isEqualTo(6);
        assertThat(transaction.getBeforeStock()).isEqualTo(18);
        assertThat(transaction.getAfterStock()).isEqualTo(12);
        assertThat(transaction.getRelatedNo()).isEqualTo(created.getOrderNo());
    }

    @Test
    void auditInventoryCheckShouldCreateCheckAdjustmentTransaction() {
        Product product = createProduct("check", 20);

        InventoryCheckItem item = new InventoryCheckItem();
        item.setProductId(product.getId());
        item.setActualStock(24);
        item.setReasonType(1);
        item.setRemark("phase8 inventory gain");

        InventoryCheck check = new InventoryCheck();
        check.setCheckDate(LocalDateTime.now());
        check.setCheckMode(1);
        check.setScopeName("Phase8 scope");
        check.setBlindCheck(0);
        check.setRemark("phase8 check test");
        check.setItems(List.of(item));

        InventoryCheck created = inventoryCheckService.createInventoryCheck(check);
        created.setCreateBy(999L);
        created.setCreateByName("tester");
        inventoryCheckService.updateById(created);
        assertThat(inventoryCheckService.submitInventoryCheck(created.getId())).isTrue();
        assertThat(inventoryCheckService.auditInventoryCheck(created.getId())).isTrue();

        List<InventoryTransaction> transactions = inventoryTransactionService
                .listTransactionsByRelated(created.getId(), created.getCheckNo());

        assertThat(transactions).hasSize(1);

        InventoryTransaction transaction = transactions.get(0);
        assertThat(transaction.getTransactionType()).isEqualTo(3);
        assertThat(transaction.getQuantity()).isEqualTo(4);
        assertThat(transaction.getBeforeStock()).isEqualTo(20);
        assertThat(transaction.getAfterStock()).isEqualTo(24);
        assertThat(transaction.getRelatedNo()).isEqualTo(created.getCheckNo());
    }

    @Test
    void auditInventoryCheckShouldRejectWhenSnapshotStockChanges() {
        Product product = createProduct("check-conflict", 20);

        InventoryCheckItem item = new InventoryCheckItem();
        item.setProductId(product.getId());
        item.setActualStock(18);
        item.setReasonType(2);
        item.setRemark("phase10 conflict case");

        InventoryCheck check = new InventoryCheck();
        check.setCheckDate(LocalDateTime.now());
        check.setCheckMode(1);
        check.setScopeName("Phase10 conflict scope");
        check.setBlindCheck(0);
        check.setRemark("phase10 inventory conflict");
        check.setItems(List.of(item));

        InventoryCheck created = inventoryCheckService.createInventoryCheck(check);
        created.setCreateBy(999L);
        created.setCreateByName("tester");
        inventoryCheckService.updateById(created);
        assertThat(inventoryCheckService.submitInventoryCheck(created.getId())).isTrue();

        assertThat(productService.updateStockIfMatches(product.getId(), 20, 23, LocalDateTime.now())).isTrue();

        assertThatThrownBy(() -> inventoryCheckService.auditInventoryCheck(created.getId()))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("库存已发生变化");
    }

    @Test
    void pageEndpointShouldFilterTransactionsByProductTypeAndTime() throws Exception {
        Product targetProduct = createProduct("page-target", 10);
        Product otherProduct = createProduct("page-other", 8);

        LocalDateTime firstTime = LocalDateTime.now().minusHours(2).withNano(0);
        LocalDateTime secondTime = LocalDateTime.now().minusHours(1).withNano(0);
        LocalDateTime thirdTime = LocalDateTime.now().withNano(0);

        inventoryTransactionService.recordTransaction(
                targetProduct.getId(), 1, 3, 10, 13, 101L, "PAGE-IN-1", "target inbound", firstTime
        );
        inventoryTransactionService.recordTransaction(
                targetProduct.getId(), 2, 1, 13, 12, 102L, "PAGE-OUT-1", "target outbound", secondTime
        );
        inventoryTransactionService.recordTransaction(
                otherProduct.getId(), 1, 2, 8, 10, 103L, "PAGE-IN-2", "other inbound", thirdTime
        );

        mockMvc.perform(get("/api/inventory-transaction/page")
                        .header("Authorization", bearerToken())
                        .param("pageNum", "1")
                        .param("pageSize", "10")
                        .param("productId", String.valueOf(targetProduct.getId()))
                        .param("transactionType", "1")
                        .param("category", "test-category")
                        .param("operatorName", "system")
                        .param("keyword", "PAGE-IN")
                        .param("startTime", firstTime.minusMinutes(1).format(QUERY_TIME_FORMATTER))
                        .param("endTime", thirdTime.format(QUERY_TIME_FORMATTER)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.records[0].productId").value(targetProduct.getId()))
                .andExpect(jsonPath("$.data.records[0].transactionType").value(1))
                .andExpect(jsonPath("$.data.records[0].operatorName").value("system"))
                .andExpect(jsonPath("$.data.records[0].relatedNo").value("PAGE-IN-1"));
    }

    @Test
    void exportEndpointShouldReturnExcelAttachment() throws Exception {
        Product product = createProduct("export", 12);
        LocalDateTime operationTime = LocalDateTime.now().withNano(0);

        inventoryTransactionService.recordTransaction(
                product.getId(), 1, 2, 12, 14, 201L, "EXPORT-1", "export check", operationTime
        );

        mockMvc.perform(get("/api/inventory-transaction/export")
                        .header("Authorization", bearerToken())
                        .param("productId", String.valueOf(product.getId()))
                        .param("startTime", operationTime.minusMinutes(1).format(QUERY_TIME_FORMATTER))
                        .param("endTime", operationTime.plusMinutes(1).format(QUERY_TIME_FORMATTER)))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-disposition", org.hamcrest.Matchers.containsString(".xlsx")))
                .andExpect(content().contentTypeCompatibleWith(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                ));
    }

    private Product createProduct(String suffix, int stock) {
        Product product = new Product();
        product.setName("Phase8 Product " + suffix);
        product.setDescription("Inventory transaction module test product");
        product.setSku("PHASE8-" + suffix.toUpperCase() + "-" + System.nanoTime());
        product.setPrice(new BigDecimal("99.90"));
        product.setCategory("test-category");
        product.setUnit("pcs");
        product.setStatus(1);
        product.setStock(stock);
        product.setMinStock(2);
        product.setMaxStock(200);
        productService.save(product);
        assertThat(product.getId()).isNotNull();
        return product;
    }

    private String bearerToken() {
        Long userId = userMapper.selectByUsername("admin").getId();
        return "Bearer " + jwtUtil.generateToken("admin", userId);
    }
}
