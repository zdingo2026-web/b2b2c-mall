package com.mall.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.mall.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 会员优惠券表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("member_coupon")
public class MemberCoupon extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 会员ID */
    @TableField("member_id")
    private Long memberId;

    /** 优惠券名称 */
    @TableField("coupon_name")
    private String couponName;

    /** 优惠券类型: 1-满减 2-折扣 3-无门槛 */
    @TableField("coupon_type")
    private Integer couponType;

    /** 优惠券面值 */
    @TableField("coupon_value")
    private BigDecimal couponValue;

    /** 最低消费金额 */
    @TableField("min_amount")
    private BigDecimal minAmount;

    /** 状态: 0-未使用 1-已使用 2-已过期 */
    @TableField("status")
    private Integer status;

    /** 过期时间 */
    @TableField("expire_time")
    private LocalDateTime expireTime;

    /** 使用时间 */
    @TableField("use_time")
    private LocalDateTime useTime;

    /** 优惠券模板ID */
    @TableField("coupon_template_id")
    private Long couponTemplateId;

    /** 来源类型: 1-主动领取 2-系统发放 3-活动赠送 */
    @TableField("source_type")
    private Integer sourceType;

    /** 来源ID */
    @TableField("source_id")
    private Long sourceId;

    /** 使用时关联订单ID */
    @TableField("order_id")
    private Long orderId;

    /** 生效时间 */
    @TableField("valid_start_time")
    private LocalDateTime validStartTime;

    /** 发放商户ID */
    @TableField("tenant_id")
    private Long tenantId;

    /** 秒杀商品是否可用 */
    @TableField("can_stack_seckill")
    private Integer canStackSeckill;
}
