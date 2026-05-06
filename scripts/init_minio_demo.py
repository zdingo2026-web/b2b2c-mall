"""
B2B2C Mall - MinIO Demo Image Initialization
Downloads demo images from Calicat CDN and uploads to MinIO
"""
import io
import urllib.request
from minio import Minio
from minio.error import S3Error

MINIO_ENDPOINT = "localhost:9000"
MINIO_ACCESS_KEY = "minioadmin"
MINIO_SECRET_KEY = "minioadmin"
MINIO_BUCKET = "mall-dev"

CALICAT_CDN = "https://prototype-prod-1254106194.cos.ap-beijing.myqcloud.com/calicat/file/ai/canvas/image"

# Image mapping: (calicat_file, minio_path)
IMAGES = [
    # Banners
    ("2050098199135879168.jpg", "banner/banner-home.jpg"),
    ("2026537648594554880.jpg", "banner/banner-phone.jpg"),
    ("2026680242619887616.jpg", "banner/banner-appliance.jpg"),
    ("2034838913045241856.jpg", "banner/banner-fashion.jpg"),
    ("2026614218298044416.jpg", "banner/banner-flash-sale.jpg"),
    # Products - phones
    ("2026537648594554880.jpg", "product/phone-1.jpg"),
    ("2026614218298044416.jpg", "product/phone-2.jpg"),
    ("2021419046744469504.jpg", "product/phone-3.jpg"),
    ("2025192376039452672.jpg", "product/phone-4.jpg"),
    # Products - laptops
    ("2018159039199154176.jpg", "product/laptop-1.jpg"),
    ("2033557882354860032.jpg", "product/laptop-2.jpg"),
    ("2031620942516006912.jpg", "product/laptop-3.jpg"),
    # Products - appliances
    ("2026680242619887616.jpg", "product/appliance-ac.jpg"),
    ("2050099149083586560.jpg", "product/appliance-fridge.jpg"),
    ("2050099158142517248.jpg", "product/appliance-washer.jpg"),
    # Products - food
    ("2034838913045241856.jpg", "product/food-milk-1.jpg"),
    ("2036284252557885440.jpg", "product/food-milk-2.jpg"),
    ("2050099174466748416.jpg", "product/food-water.jpg"),
    # Categories
    ("2026537648594554880.jpg", "category/mobile-digital.jpg"),
    ("2018159039199154176.jpg", "category/computer-office.jpg"),
    ("2026680242619887616.jpg", "category/home-appliance.jpg"),
    ("2034838913045241856.jpg", "category/clothing.jpg"),
    ("2036284252557885440.jpg", "category/shoes-bags.jpg"),
    ("2021419046744469504.jpg", "category/sports-outdoor.jpg"),
    ("2050099181399932928.jpg", "category/mom-baby.jpg"),
    ("2033557882354860032.jpg", "category/food-drink.jpg"),
    ("2050099149083586560.jpg", "category/home-furnishing.jpg"),
    ("2025192376039452672.jpg", "category/beauty.jpg"),
    # Brands
    ("2026537648594554880.jpg", "brand/apple.jpg"),
    ("2026614218298044416.jpg", "brand/huawei.jpg"),
    ("2021419046744469504.jpg", "brand/xiaomi.jpg"),
    ("2025192376039452672.jpg", "brand/samsung.jpg"),
    ("2018159039199154176.jpg", "brand/lenovo.jpg"),
    ("2033557882354860032.jpg", "brand/dell.jpg"),
    ("2026680242619887616.jpg", "brand/haier.jpg"),
    ("2050099149083586560.jpg", "brand/midea.jpg"),
    ("2050099158142517248.jpg", "brand/gree.jpg"),
    ("2036284252557885440.jpg", "brand/nike.jpg"),
    # Tenant
    ("2050098961665306624.jpg", "tenant/self-logo.jpg"),
]

CONTENT_TYPE_MAP = {
    "jpg": "image/jpeg",
    "jpeg": "image/jpeg",
    "png": "image/png",
    "gif": "image/gif",
    "webp": "image/webp",
}


def main():
    client = Minio(MINIO_ENDPOINT, access_key=MINIO_ACCESS_KEY, secret_key=MINIO_SECRET_KEY, secure=False)

    # Create bucket if not exists
    if not client.bucket_exists(MINIO_BUCKET):
        client.make_bucket(MINIO_BUCKET)
        print(f"Created bucket: {MINIO_BUCKET}")

    # Set public read policy
    policy = {
        "Version": "2012-10-17",
        "Statement": [{
            "Effect": "Allow",
            "Principal": {"AWS": "*"},
            "Action": ["s3:GetObject"],
            "Resource": [f"arn:aws:s3:::{MINIO_BUCKET}/*"],
        }],
    }
    import json
    client.set_bucket_policy(MINIO_BUCKET, json.dumps(policy))
    print(f"Set public read policy on bucket: {MINIO_BUCKET}")

    # Download and upload
    uploaded = 0
    failed = 0
    seen_cdn = {}  # cache: calicat_file -> bytes

    for calicat_file, minio_path in IMAGES:
        try:
            # Download from CDN (cache to avoid re-downloading same file)
            if calicat_file not in seen_cdn:
                url = f"{CALICAT_CDN}/{calicat_file}"
                print(f"  Downloading: {calicat_file}...", end="", flush=True)
                req = urllib.request.Request(url, headers={"User-Agent": "Mozilla/5.0"})
                with urllib.request.urlopen(req, timeout=30) as resp:
                    data = resp.read()
                seen_cdn[calicat_file] = data
                print(f" {len(data)} bytes")
            else:
                data = seen_cdn[calicat_file]

            if len(data) == 0:
                print(f"  ERROR: Empty download for {calicat_file}")
                failed += 1
                continue

            # Determine content type
            ext = minio_path.rsplit(".", 1)[-1].lower()
            content_type = CONTENT_TYPE_MAP.get(ext, "application/octet-stream")

            # Upload to MinIO
            client.put_object(
                MINIO_BUCKET,
                minio_path,
                io.BytesIO(data),
                length=len(data),
                content_type=content_type,
            )
            print(f"  OK: {minio_path}")
            uploaded += 1

        except Exception as e:
            print(f"  ERROR: {minio_path} - {e}")
            failed += 1

    print(f"\nDone! Uploaded: {uploaded}, Failed: {failed}")
    return 0 if failed == 0 else 1


if __name__ == "__main__":
    exit(main())
