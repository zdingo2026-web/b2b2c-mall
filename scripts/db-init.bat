@echo off
chcp 65001 > nul
:: 数据库自动化初始化脚本（Windows版本）
:: 自动执行初始化SQL和模拟数据SQL，不需要人工手动操作

:: 配置信息
set DB_HOST=localhost
set DB_PORT=3306
set DB_USER=root
set DB_PASSWORD=root123
set DB_NAME=mall_b2b2c
set CHARSET=--default-character-set=utf8mb4
set INIT_SQL_PATH=docs\architecture\init_fixed.sql
set MOCK_DATA_SQL_PATH=docs\architecture\mock_data.sql
set FULL_MOCK_DATA_SQL_PATH=docs\architecture\full_mock_data.sql
set ORDER_SUPPLEMENT_SQL_PATH=docs\architecture\order_supplement.sql

echo ==================== 数据库自动化初始化开始 ====================

:: 检查SQL文件是否存在
if not exist "%INIT_SQL_PATH%" (
    echo 错误：初始化SQL文件不存在：%INIT_SQL_PATH%
    pause
    exit /b 1
)

if not exist "%MOCK_DATA_SQL_PATH%" (
    echo 错误：模拟数据SQL文件不存在：%MOCK_DATA_SQL_PATH%
    pause
    exit /b 1
)

echo 1. 检查SQL文件存在，开始连接数据库...

:: 检查数据库连接
mysql -h%DB_HOST% -P%DB_PORT% -u%DB_USER% -p%DB_PASSWORD% %CHARSET% -e "SELECT 1" > nul 2>&1
if %errorlevel% neq 0 (
    echo 错误：数据库连接失败，请检查数据库服务是否启动，配置信息是否正确
    echo 当前配置：HOST=%DB_HOST%, PORT=%DB_PORT%, USER=%DB_USER%, PASSWORD=%DB_PASSWORD%
    pause
    exit /b 1
)

echo 2. 数据库连接成功，开始执行初始化SQL脚本（含建库建表）...
mysql -h%DB_HOST% -P%DB_PORT% -u%DB_USER% -p%DB_PASSWORD% %CHARSET% < %INIT_SQL_PATH%
if %errorlevel% neq 0 (
    echo 错误：初始化SQL执行失败
    pause
    exit /b 1
)

echo 3. 开始执行模拟数据SQL脚本...
mysql -h%DB_HOST% -P%DB_PORT% -u%DB_USER% -p%DB_PASSWORD% %CHARSET% %DB_NAME% < %MOCK_DATA_SQL_PATH%
if %errorlevel% neq 0 (
    echo 错误：模拟数据SQL执行失败
    pause
    exit /b 1
)

echo 4. 开始执行完整模拟数据SQL脚本...
if exist "%FULL_MOCK_DATA_SQL_PATH%" (
    mysql -h%DB_HOST% -P%DB_PORT% -u%DB_USER% -p%DB_PASSWORD% %CHARSET% %DB_NAME% < %FULL_MOCK_DATA_SQL_PATH%
    if %errorlevel% neq 0 (
        echo 错误：完整模拟数据SQL执行失败
        pause
        exit /b 1
    )
) else (
    echo 跳过：完整模拟数据SQL文件不存在
)

echo 5. 开始执行订单补充数据SQL脚本...
if exist "%ORDER_SUPPLEMENT_SQL_PATH%" (
    mysql -h%DB_HOST% -P%DB_PORT% -u%DB_USER% -p%DB_PASSWORD% %CHARSET% %DB_NAME% < %ORDER_SUPPLEMENT_SQL_PATH%
    if %errorlevel% neq 0 (
        echo 错误：订单补充数据SQL执行失败
        pause
        exit /b 1
    )
) else (
    echo 跳过：订单补充数据SQL文件不存在
)

echo 6. 验证数据是否导入成功...
:: 验证管理员表数据
for /f %%i in ('mysql -h%DB_HOST% -P%DB_PORT% -u%DB_USER% -p%DB_PASSWORD% %CHARSET% %DB_NAME% -N -e "SELECT COUNT(*) FROM sys_admin WHERE username='admin'"') do set ADMIN_COUNT=%%i
if not "%ADMIN_COUNT%" == "1" (
    echo 错误：管理员数据验证失败
    pause
    exit /b 1
)

:: 验证中文数据
for /f %%i in ('mysql -h%DB_HOST% -P%DB_PORT% -u%DB_USER% -p%DB_PASSWORD% %CHARSET% %DB_NAME% -N -e "SELECT config_value FROM sys_config WHERE config_key='site_name'"') do set SITE_NAME=%%i
echo 站点名称：%SITE_NAME%

:: 验证商品数据
for /f %%i in ('mysql -h%DB_HOST% -P%DB_PORT% -u%DB_USER% -p%DB_PASSWORD% %CHARSET% %DB_NAME% -N -e "SELECT COUNT(*) FROM product_spu WHERE status=1"') do set PRODUCT_COUNT=%%i
if "%PRODUCT_COUNT%" == "0" (
    echo 错误：商品数据验证失败
    pause
    exit /b 1
)

:: 验证订单数据
for /f %%i in ('mysql -h%DB_HOST% -P%DB_PORT% -u%DB_USER% -p%DB_PASSWORD% %CHARSET% %DB_NAME% -N -e "SELECT COUNT(*) FROM order_main"') do set ORDER_COUNT=%%i
echo 订单数量：%ORDER_COUNT%

echo ==================== 数据库自动化初始化完成 ====================
echo 数据库：%DB_NAME%
echo 初始化SQL执行完成
echo 模拟数据导入完成
echo 数据验证通过
echo.
echo 测试账号：
echo 平台管理员：admin / 123456
echo 自营商家管理员：self / 123456
echo 测试会员：13800138001 / 123456
echo.
echo 请审核确认初始化结果是否正确！
pause
