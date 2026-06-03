package com.example.erpinventory.controller;

import com.alibaba.excel.EasyExcel;
import com.example.erpinventory.entity.InboundOrder;
import com.example.erpinventory.entity.OutboundOrder;
import com.example.erpinventory.entity.Product;
import com.example.erpinventory.service.InboundOrderService;
import com.example.erpinventory.service.OutboundOrderService;
import com.example.erpinventory.service.ProductService;
import com.example.erpinventory.util.PermissionGuard;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/api/export")
public class ExportController {

    @Autowired
    private ProductService productService;

    @Autowired
    private InboundOrderService inboundOrderService;

    @Autowired
    private OutboundOrderService outboundOrderService;

    @Autowired
    private PermissionGuard permissionGuard;

    @PreAuthorize("hasAuthority('product:view')")
    @GetMapping("/products")
    public void exportProducts(HttpServletResponse response) throws IOException {
        permissionGuard.require("product:view");
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        String fileName = URLEncoder.encode("商品列表", StandardCharsets.UTF_8).replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");
        List<Product> list = productService.list();
        EasyExcel.write(response.getOutputStream(), Product.class).sheet("商品列表").doWrite(list);
    }

    @PreAuthorize("hasAuthority('inbound:view')")
    @GetMapping("/inbound")
    public void exportInbound(HttpServletResponse response) throws IOException {
        permissionGuard.require("inbound:view");
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        String fileName = URLEncoder.encode("入库单列表", StandardCharsets.UTF_8).replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");
        List<InboundOrder> list = inboundOrderService.listInboundOrders();
        EasyExcel.write(response.getOutputStream(), InboundOrder.class).sheet("入库单列表").doWrite(list);
    }

    @PreAuthorize("hasAuthority('outbound:view')")
    @GetMapping("/outbound")
    public void exportOutbound(HttpServletResponse response) throws IOException {
        permissionGuard.require("outbound:view");
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        String fileName = URLEncoder.encode("出库单列表", StandardCharsets.UTF_8).replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");
        List<OutboundOrder> list = outboundOrderService.listOutboundOrders();
        EasyExcel.write(response.getOutputStream(), OutboundOrder.class).sheet("出库单列表").doWrite(list);
    }
}
