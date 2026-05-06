package com.mall.service.payment;

import com.mall.common.enums.PayTypeEnum;
import com.mall.common.exception.BusinessException;
import com.mall.common.response.ResultCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Payment strategy factory.
 * Returns the appropriate PayStrategy based on payType.
 */
@Component
@RequiredArgsConstructor
public class PaymentFactory {

    private final WechatPayStrategy wechatPayStrategy;
    private final AliPayStrategy aliPayStrategy;
    private final BalancePayStrategy balancePayStrategy;

    /**
     * Get PayStrategy by pay type code.
     *
     * @param payType pay type code (1-WeChat, 2-AliPay, 3-Balance)
     * @return corresponding PayStrategy
     */
    public PayStrategy getStrategy(Integer payType) {
        PayTypeEnum payTypeEnum = PayTypeEnum.fromCode(payType);
        switch (payTypeEnum) {
            case WECHAT:
                return wechatPayStrategy;
            case ALIPAY:
                return aliPayStrategy;
            case BALANCE:
                return balancePayStrategy;
            default:
                throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "不支持的支付方式: " + payType);
        }
    }
}
