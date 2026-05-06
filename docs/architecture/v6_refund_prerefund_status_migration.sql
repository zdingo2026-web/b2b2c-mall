-- ============================================================
-- V6 Migration: Add pre_refund_status column to order_refund table
-- Stores the order status before refund was applied,
-- so that rejection can restore the correct status.
-- ============================================================

ALTER TABLE order_refund ADD COLUMN pre_refund_status INT DEFAULT NULL COMMENT '退款申请前的订单状态(用于驳回时恢复)' AFTER refund_status;
