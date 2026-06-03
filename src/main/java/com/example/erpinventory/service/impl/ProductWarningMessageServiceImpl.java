package com.example.erpinventory.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.erpinventory.dto.request.WarningHandleRequest;
import com.example.erpinventory.entity.Product;
import com.example.erpinventory.entity.ProductWarningMessage;
import com.example.erpinventory.entity.SysUser;
import com.example.erpinventory.exception.BusinessException;
import com.example.erpinventory.mapper.ProductWarningMessageMapper;
import com.example.erpinventory.service.ProductService;
import com.example.erpinventory.service.ProductWarningMessageService;
import com.example.erpinventory.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ProductWarningMessageServiceImpl extends ServiceImpl<ProductWarningMessageMapper, ProductWarningMessage>
        implements ProductWarningMessageService {

    private static final int DEFAULT_MIN_STOCK = 10;
    private static final int DEFAULT_MAX_STOCK = 1000;

    private static final int HANDLE_SOURCE_MANUAL = 1;
    private static final int HANDLE_SOURCE_SYSTEM_RECOVERED = 2;
    private static final int HANDLE_SOURCE_SYSTEM_SWITCHED = 3;

    @Autowired
    private ProductService productService;

    @Autowired
    private UserService userService;

    @Override
    public Page<ProductWarningMessage> pageWarningMessages(Integer pageNum, Integer pageSize, Integer status, Integer warningType, String keyword) {
        Page<ProductWarningMessage> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<ProductWarningMessage> queryWrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            queryWrapper.eq(ProductWarningMessage::getStatus, status);
        }
        if (warningType != null) {
            queryWrapper.eq(ProductWarningMessage::getWarningType, warningType);
        }
        if (keyword != null && !keyword.isBlank()) {
            queryWrapper.and(wrapper -> wrapper.like(ProductWarningMessage::getProductName, keyword)
                    .or()
                    .like(ProductWarningMessage::getSku, keyword)
                    .or()
                    .like(ProductWarningMessage::getMessage, keyword)
                    .or()
                    .like(ProductWarningMessage::getHandleNote, keyword)
                    .or()
                    .like(ProductWarningMessage::getHandleByName, keyword)
                    .or()
                    .like(ProductWarningMessage::getOwnerName, keyword));
        }
        queryWrapper.orderByAsc(ProductWarningMessage::getStatus)
                .orderByDesc(ProductWarningMessage::getCreateTime)
                .orderByDesc(ProductWarningMessage::getId);
        return page(page, queryWrapper);
    }

    @Override
    public Map<String, Object> getWarningSummary() {
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("totalWarnings", count());
        summary.put("pendingWarnings", countByStatus(null, 0));
        summary.put("handledWarnings", countByStatus(null, 1));
        summary.put("lowWarnings", countByStatus(1, 0));
        summary.put("highWarnings", countByStatus(2, 0));
        summary.put("affectedProducts", listPendingProductCount());
        return summary;
    }

    @Override
    @Transactional
    public void syncAllWarnings() {
        List<Product> products = productService.list();
        for (Product product : products) {
            syncWarningsForProduct(product);
        }
    }

    @Override
    @Transactional
    public void syncWarningsForProduct(Product product) {
        if (product == null || product.getId() == null) {
            return;
        }

        Integer warningType = resolveWarningType(product);
        int currentStock = safeInt(product.getStock());
        int minStock = normalizeMinStock(product.getMinStock());
        int maxStock = normalizeMaxStock(product.getMaxStock(), minStock);
        String message = buildWarningMessage(product, warningType, currentStock, minStock, maxStock);

        LambdaQueryWrapper<ProductWarningMessage> activeWrapper = new LambdaQueryWrapper<>();
        activeWrapper.eq(ProductWarningMessage::getProductId, product.getId())
                .eq(ProductWarningMessage::getStatus, 0)
                .orderByDesc(ProductWarningMessage::getCreateTime)
                .orderByDesc(ProductWarningMessage::getId);
        List<ProductWarningMessage> activeWarnings = list(activeWrapper);

        boolean matched = false;
        LocalDateTime now = LocalDateTime.now();

        for (ProductWarningMessage activeWarning : activeWarnings) {
            if (warningType != null && warningType.equals(activeWarning.getWarningType())) {
                activeWarning.setProductName(product.getName());
                activeWarning.setSku(product.getSku());
                activeWarning.setCurrentStock(currentStock);
                activeWarning.setMinStock(minStock);
                activeWarning.setMaxStock(maxStock);
                activeWarning.setMessage(message);
                resetHandleTrace(activeWarning);
                updateById(activeWarning);
                matched = true;
            } else if (warningType == null) {
                closeWarning(activeWarning, now, HANDLE_SOURCE_SYSTEM_RECOVERED, null, null, null,
                        null, null, "库存已恢复到安全区间，系统自动关闭预警。");
            } else {
                closeWarning(activeWarning, now, HANDLE_SOURCE_SYSTEM_SWITCHED, null, null, null,
                        null, null, "库存风险类型已变更，旧预警自动关闭并生成新的预警消息。");
            }
        }

        if (warningType != null && !matched) {
            ProductWarningMessage warning = new ProductWarningMessage();
            warning.setProductId(product.getId());
            warning.setProductName(product.getName());
            warning.setSku(product.getSku());
            warning.setCurrentStock(currentStock);
            warning.setMinStock(minStock);
            warning.setMaxStock(maxStock);
            warning.setWarningType(warningType);
            warning.setMessage(message);
            warning.setStatus(0);
            warning.setCreateTime(now);
            resetHandleTrace(warning);
            save(warning);
        }
    }

    @Override
    @Transactional
    public boolean handleWarning(Long id, WarningHandleRequest request, Long currentUserId, String currentUsername) {
        validateHandleRequest(request);
        ProductWarningMessage warning = getPendingWarning(id);
        LocalDateTime now = LocalDateTime.now();
        closeWarning(warning, now, HANDLE_SOURCE_MANUAL, request.getHandleType(), currentUserId,
                resolveHandlerName(currentUserId, currentUsername),
                normalizeOwnerName(request.getOwnerName(), currentUsername, currentUserId),
                request.getFollowUpTime(), request.getHandleNote().trim());
        return true;
    }

    @Override
    @Transactional
    public boolean handleAllWarnings(WarningHandleRequest request, Long currentUserId, String currentUsername) {
        validateHandleRequest(request);
        LambdaQueryWrapper<ProductWarningMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProductWarningMessage::getStatus, 0);
        List<ProductWarningMessage> warnings = list(wrapper);
        if (warnings.isEmpty()) {
            return true;
        }

        LocalDateTime now = LocalDateTime.now();
        String handlerName = resolveHandlerName(currentUserId, currentUsername);
        String ownerName = normalizeOwnerName(request.getOwnerName(), currentUsername, currentUserId);
        for (ProductWarningMessage warning : warnings) {
            closeWarning(warning, now, HANDLE_SOURCE_MANUAL, request.getHandleType(), currentUserId,
                    handlerName, ownerName, request.getFollowUpTime(), request.getHandleNote().trim());
        }
        return true;
    }

    @Override
    @Transactional
    public void removeWarningsByProductId(Long productId) {
        if (productId == null) {
            return;
        }
        LambdaQueryWrapper<ProductWarningMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProductWarningMessage::getProductId, productId);
        remove(wrapper);
    }

    private ProductWarningMessage getPendingWarning(Long id) {
        ProductWarningMessage warning = getById(id);
        if (warning == null) {
            throw new BusinessException("预警消息不存在");
        }
        if (Integer.valueOf(1).equals(warning.getStatus())) {
            throw new BusinessException("该预警消息已处理");
        }
        return warning;
    }

    private void validateHandleRequest(WarningHandleRequest request) {
        if (request == null) {
            throw new BusinessException("处理表单不能为空");
        }
        if (request.getHandleType() == null) {
            throw new BusinessException("请选择处理方式");
        }
        if (!StringUtils.hasText(request.getHandleNote())) {
            throw new BusinessException("请填写处理说明");
        }
        if (request.getHandleNote().trim().length() > 500) {
            throw new BusinessException("处理说明不能超过 500 个字符");
        }
    }

    private long countByStatus(Integer warningType, Integer status) {
        LambdaQueryWrapper<ProductWarningMessage> wrapper = new LambdaQueryWrapper<>();
        if (warningType != null) {
            wrapper.eq(ProductWarningMessage::getWarningType, warningType);
        }
        if (status != null) {
            wrapper.eq(ProductWarningMessage::getStatus, status);
        }
        return count(wrapper);
    }

    private long listPendingProductCount() {
        LambdaQueryWrapper<ProductWarningMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProductWarningMessage::getStatus, 0)
                .select(ProductWarningMessage::getProductId)
                .groupBy(ProductWarningMessage::getProductId);
        return list(wrapper).size();
    }

    private Integer resolveWarningType(Product product) {
        int stock = safeInt(product.getStock());
        int minStock = normalizeMinStock(product.getMinStock());
        int maxStock = normalizeMaxStock(product.getMaxStock(), minStock);
        if (stock < minStock) {
            return 1;
        }
        if (stock > maxStock) {
            return 2;
        }
        return null;
    }

    private String buildWarningMessage(Product product, Integer warningType, int currentStock, int minStock, int maxStock) {
        if (warningType == null) {
            return "";
        }
        if (warningType == 1) {
            return "商品【" + product.getName() + "】库存过低，当前库存 " + currentStock
                    + "，低于最低库存 " + minStock + "，请及时补货。";
        }
        return "商品【" + product.getName() + "】库存过高，当前库存 " + currentStock
                + "，高于最高库存 " + maxStock + "，请评估促销或调拨。";
    }

    private void closeWarning(ProductWarningMessage warning, LocalDateTime now, Integer handleSource, Integer handleType,
                              Long handleBy, String handleByName, String ownerName, LocalDateTime followUpTime,
                              String handleNote) {
        warning.setStatus(1);
        warning.setHandleSource(handleSource);
        warning.setHandleType(handleType);
        warning.setHandleBy(handleBy);
        warning.setHandleByName(handleByName);
        warning.setOwnerName(ownerName);
        warning.setFollowUpTime(followUpTime);
        warning.setHandleNote(handleNote);
        warning.setHandleTime(now);
        updateById(warning);
    }

    private void resetHandleTrace(ProductWarningMessage warning) {
        warning.setHandleSource(null);
        warning.setHandleType(null);
        warning.setHandleBy(null);
        warning.setHandleByName(null);
        warning.setOwnerName(null);
        warning.setFollowUpTime(null);
        warning.setHandleNote(null);
        warning.setHandleTime(null);
    }

    private String resolveHandlerName(Long currentUserId, String currentUsername) {
        if (currentUserId != null) {
            SysUser user = userService.getById(currentUserId);
            if (user != null) {
                if (StringUtils.hasText(user.getRealName())) {
                    return user.getRealName();
                }
                if (StringUtils.hasText(user.getUsername())) {
                    return user.getUsername();
                }
            }
        }
        return StringUtils.hasText(currentUsername) ? currentUsername : "系统用户";
    }

    private String normalizeOwnerName(String ownerName, String currentUsername, Long currentUserId) {
        if (StringUtils.hasText(ownerName)) {
            return ownerName.trim();
        }
        return resolveHandlerName(currentUserId, currentUsername);
    }

    private int normalizeMinStock(Integer minStock) {
        if (minStock == null || minStock < 0) {
            return DEFAULT_MIN_STOCK;
        }
        return minStock;
    }

    private int normalizeMaxStock(Integer maxStock, int minStock) {
        if (maxStock == null || maxStock < minStock) {
            return Math.max(DEFAULT_MAX_STOCK, minStock);
        }
        return maxStock;
    }

    private int safeInt(Integer value) {
        return value == null ? 0 : value;
    }
}
