package com.mall.model.dto.tenant;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class CategoryCommissionDTO {
    private List<CategoryCommissionItem> items;

    @Data
    public static class CategoryCommissionItem {
        private Long categoryId;
        private BigDecimal rateLevel1;
        private BigDecimal rateLevel2;
        private BigDecimal rateLevel3;
    }
}
