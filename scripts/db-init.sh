#!/bin/bash
# 数据库自动化初始化脚本
# 自动执行初始化SQL和模拟数据SQL，不需要人工手动操作

# 配置信息
DB_HOST="localhost"
DB_PORT="3306"
DB_USER="root"
DB_PASSWORD="root123"
DB_NAME="mall_b2b2c"
CHARSET="--default-character-set=utf8mb4"
INIT_SQL_PATH="docs/architecture/init_fixed.sql"
MOCK_DATA_SQL_PATH="docs/architecture/mock_data.sql"
FULL_MOCK_DATA_SQL_PATH="docs/architecture/full_mock_data.sql"
ORDER_SUPPLEMENT_SQL_PATH="docs/architecture/order_supplement.sql"

# 颜色输出
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

echo -e "${YELLOW}==================== 数据库自动化初始化开始 ====================${NC}"

# 检查SQL文件是否存在
if [ ! -f "$INIT_SQL_PATH" ]; then
    echo -e "${RED}错误：初始化SQL文件不存在：$INIT_SQL_PATH${NC}"
    exit 1
fi

if [ ! -f "$MOCK_DATA_SQL_PATH" ]; then
    echo -e "${RED}错误：模拟数据SQL文件不存在：$MOCK_DATA_SQL_PATH${NC}"
    exit 1
fi

echo -e "${GREEN}1. 检查SQL文件存在，开始连接数据库...${NC}"

# 检查数据库连接
mysql -h$DB_HOST -P$DB_PORT -u$DB_USER -p$DB_PASSWORD $CHARSET -e "SELECT 1" > /dev/null 2>&1
if [ $? -ne 0 ]; then
    echo -e "${RED}错误：数据库连接失败，请检查数据库服务是否启动，配置信息是否正确${NC}"
    echo -e "当前配置：HOST=$DB_HOST, PORT=$DB_PORT, USER=$DB_USER, PASSWORD=$DB_PASSWORD"
    exit 1
fi

echo -e "${GREEN}2. 数据库连接成功，开始执行初始化SQL脚本（含建库建表）...${NC}"
mysql -h$DB_HOST -P$DB_PORT -u$DB_USER -p$DB_PASSWORD $CHARSET < $INIT_SQL_PATH
if [ $? -ne 0 ]; then
    echo -e "${RED}错误：初始化SQL执行失败${NC}"
    exit 1
fi

echo -e "${GREEN}3. 开始执行模拟数据SQL脚本...${NC}"
mysql -h$DB_HOST -P$DB_PORT -u$DB_USER -p$DB_PASSWORD $CHARSET $DB_NAME < $MOCK_DATA_SQL_PATH
if [ $? -ne 0 ]; then
    echo -e "${RED}错误：模拟数据SQL执行失败${NC}"
    exit 1
fi

echo -e "${GREEN}4. 开始执行完整模拟数据SQL脚本...${NC}"
if [ -f "$FULL_MOCK_DATA_SQL_PATH" ]; then
    mysql -h$DB_HOST -P$DB_PORT -u$DB_USER -p$DB_PASSWORD $CHARSET $DB_NAME < $FULL_MOCK_DATA_SQL_PATH
    if [ $? -ne 0 ]; then
        echo -e "${RED}错误：完整模拟数据SQL执行失败${NC}"
        exit 1
    fi
else
    echo -e "${YELLOW}跳过：完整模拟数据SQL文件不存在${NC}"
fi

echo -e "${GREEN}5. 开始执行订单补充数据SQL脚本...${NC}"
if [ -f "$ORDER_SUPPLEMENT_SQL_PATH" ]; then
    mysql -h$DB_HOST -P$DB_PORT -u$DB_USER -p$DB_PASSWORD $CHARSET $DB_NAME < $ORDER_SUPPLEMENT_SQL_PATH
    if [ $? -ne 0 ]; then
        echo -e "${RED}错误：订单补充数据SQL执行失败${NC}"
        exit 1
    fi
else
    echo -e "${YELLOW}跳过：订单补充数据SQL文件不存在${NC}"
fi

echo -e "${GREEN}6. 验证数据是否导入成功...${NC}"
# 验证管理员表数据
ADMIN_COUNT=$(mysql -h$DB_HOST -P$DB_PORT -u$DB_USER -p$DB_PASSWORD $CHARSET $DB_NAME -N -e "SELECT COUNT(*) FROM sys_admin WHERE username='admin'")
if [ "$ADMIN_COUNT" -ne 1 ]; then
    echo -e "${RED}错误：管理员数据验证失败${NC}"
    exit 1
fi

# 验证中文数据
SITE_NAME=$(mysql -h$DB_HOST -P$DB_PORT -u$DB_USER -p$DB_PASSWORD $CHARSET $DB_NAME -N -e "SELECT config_value FROM sys_config WHERE config_key='site_name'")
echo -e "站点名称：$SITE_NAME"

# 验证商品数据
PRODUCT_COUNT=$(mysql -h$DB_HOST -P$DB_PORT -u$DB_USER -p$DB_PASSWORD $CHARSET $DB_NAME -N -e "SELECT COUNT(*) FROM product_spu WHERE status=1")
if [ "$PRODUCT_COUNT" -eq 0 ]; then
    echo -e "${RED}错误：商品数据验证失败${NC}"
    exit 1
fi

# 验证订单数据
ORDER_COUNT=$(mysql -h$DB_HOST -P$DB_PORT -u$DB_USER -p$DB_PASSWORD $CHARSET $DB_NAME -N -e "SELECT COUNT(*) FROM order_main")
echo -e "订单数量：$ORDER_COUNT"

echo -e "${GREEN}==================== 数据库自动化初始化完成 ====================${NC}"
echo -e "数据库：$DB_NAME"
echo -e "初始化SQL执行完成"
echo -e "模拟数据导入完成"
echo -e "数据验证通过"
echo -e ""
echo -e "测试账号："
echo -e "平台管理员：admin / 123456"
echo -e "自营商家管理员：self / 123456"
echo -e "测试会员：13800138001 / 123456"
echo -e ""
echo -e "请审核确认初始化结果是否正确！"
