#!/bin/bash
# ============================================================
# B2B2C Mall - MinIO Demo Image Initialization Script
# Downloads demo images from Calicat CDN and uploads to MinIO
# Prerequisites: mc (MinIO Client) installed
# Usage: ./scripts/init-minio-demo.sh
# ============================================================

set -e

MINIO_ALIAS="mall"
MINIO_ENDPOINT="${MINIO_ENDPOINT:-http://localhost:9000}"
MINIO_ACCESS_KEY="${MINIO_ACCESS_KEY:-minioadmin}"
MINIO_SECRET_KEY="${MINIO_SECRET_KEY:-minioadmin}"
MINIO_BUCKET="${MINIO_BUCKET:-mall-dev}"

CALICAT_CDN="https://prototype-prod-1254106194.cos.ap-beijing.myqcloud.com/calicat/file/ai/canvas/image"

TMP_DIR=$(mktemp -d)
trap "rm -rf $TMP_DIR" EXIT

UPLOAD_COUNT=0
FAIL_COUNT=0

echo "=== MinIO Demo Image Initialization ==="

# Configure mc alias
echo "[1/4] Configuring MinIO client..."
if ! mc alias set $MINIO_ALIAS $MINIO_ENDPOINT $MINIO_ACCESS_KEY $MINIO_SECRET_KEY; then
    echo "ERROR: Failed to configure MinIO client. Is MinIO running at $MINIO_ENDPOINT?"
    exit 1
fi

# Create bucket if not exists
echo "[2/4] Ensuring bucket '$MINIO_BUCKET' exists..."
mc mb $MINIO_ALIAS/$MINIO_BUCKET 2>/dev/null || true

# Set bucket public read policy (dev only — production should use presigned URLs)
echo "[3/4] Setting bucket public read policy (dev only)..."
mc anonymous set download $MINIO_ALIAS/$MINIO_BUCKET

# Download and upload images
echo "[4/4] Downloading and uploading demo images..."

download_and_upload() {
    local calicat_file=$1
    local minio_path=$2
    local local_file="$TMP_DIR/$(basename $minio_path)"

    curl -sL "$CALICAT_CDN/$calicat_file" -o "$local_file"
    if [ ! -s "$local_file" ]; then
        echo "  ERROR: Failed to download $calicat_file"
        FAIL_COUNT=$((FAIL_COUNT + 1))
        return 1
    fi
    if mc cp "$local_file" "$MINIO_ALIAS/$MINIO_BUCKET/$minio_path"; then
        echo "  OK: $minio_path"
        UPLOAD_COUNT=$((UPLOAD_COUNT + 1))
    else
        echo "  ERROR: Failed to upload $minio_path"
        FAIL_COUNT=$((FAIL_COUNT + 1))
    fi
}

# ---- Banner images ----
echo "  Banners..."
download_and_upload "2050098199135879168.jpg" "banner/banner-home.jpg"
download_and_upload "2026537648594554880.jpg" "banner/banner-phone.jpg"
download_and_upload "2026680242619887616.jpg" "banner/banner-appliance.jpg"
download_and_upload "2034838913045241856.jpg" "banner/banner-fashion.jpg"
download_and_upload "2026614218298044416.jpg" "banner/banner-flash-sale.jpg"

# ---- Product main images ----
echo "  Products..."
# Phones (4 products)
download_and_upload "2026537648594554880.jpg" "product/phone-1.jpg"
download_and_upload "2026614218298044416.jpg" "product/phone-2.jpg"
download_and_upload "2021419046744469504.jpg" "product/phone-3.jpg"
download_and_upload "2025192376039452672.jpg" "product/phone-4.jpg"

# Laptops (3 products)
download_and_upload "2018159039199154176.jpg" "product/laptop-1.jpg"
download_and_upload "2033557882354860032.jpg" "product/laptop-2.jpg"
download_and_upload "2031620942516006912.jpg" "product/laptop-3.jpg"

# Home appliances (3 products)
download_and_upload "2026680242619887616.jpg" "product/appliance-ac.jpg"
download_and_upload "2050099149083586560.jpg" "product/appliance-fridge.jpg"
download_and_upload "2050099158142517248.jpg" "product/appliance-washer.jpg"

# Food (3 products)
download_and_upload "2034838913045241856.jpg" "product/food-milk-1.jpg"
download_and_upload "2036284252557885440.jpg" "product/food-milk-2.jpg"
download_and_upload "2050099174466748416.jpg" "product/food-water.jpg"

# ---- Category icons (10 top-level categories) ----
echo "  Categories..."
download_and_upload "2026537648594554880.jpg" "category/mobile-digital.jpg"
download_and_upload "2018159039199154176.jpg" "category/computer-office.jpg"
download_and_upload "2026680242619887616.jpg" "category/home-appliance.jpg"
download_and_upload "2034838913045241856.jpg" "category/clothing.jpg"
download_and_upload "2036284252557885440.jpg" "category/shoes-bags.jpg"
download_and_upload "2021419046744469504.jpg" "category/sports-outdoor.jpg"
download_and_upload "2050099181399932928.jpg" "category/mom-baby.jpg"
download_and_upload "2033557882354860032.jpg" "category/food-drink.jpg"
download_and_upload "2050099149083586560.jpg" "category/home-furnishing.jpg"
download_and_upload "2025192376039452672.jpg" "category/beauty.jpg"

# ---- Brand logos (10 top brands) ----
echo "  Brands..."
download_and_upload "2026537648594554880.jpg" "brand/apple.jpg"
download_and_upload "2026614218298044416.jpg" "brand/huawei.jpg"
download_and_upload "2021419046744469504.jpg" "brand/xiaomi.jpg"
download_and_upload "2025192376039452672.jpg" "brand/samsung.jpg"
download_and_upload "2018159039199154176.jpg" "brand/lenovo.jpg"
download_and_upload "2033557882354860032.jpg" "brand/dell.jpg"
download_and_upload "2026680242619887616.jpg" "brand/haier.jpg"
download_and_upload "2050099149083586560.jpg" "brand/midea.jpg"
download_and_upload "2050099158142517248.jpg" "brand/gree.jpg"
download_and_upload "2036284252557885440.jpg" "brand/nike.jpg"

# ---- Tenant logo ----
echo "  Tenant..."
download_and_upload "2050098961665306624.jpg" "tenant/self-logo.jpg"

echo ""
echo "=== Done! ==="
echo "Uploaded: $UPLOAD_COUNT files"
echo "Failed:   $FAIL_COUNT files"
echo "Bucket:   $MINIO_BUCKET"
echo ""
echo "Next: Run v4_minio_seed_data.sql to update database image URLs"

if [ $FAIL_COUNT -gt 0 ]; then
    exit 1
fi
