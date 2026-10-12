#!/usr/bin/env bash
# Sinh /etc/nginx/conf.d/00-cloudflare.conf từ danh sách IP chính thức của Cloudflare:
#  - set_real_ip_from + CF-Connecting-IP: log và ứng dụng thấy IP thật của khách
#  - geo $from_cloudflare: = 1 nếu kết nối đến từ Cloudflare (hoặc localhost)
# $realip_remote_addr là địa chỉ TCP gốc trước khi bị module realip thay thế,
# nên vẫn kiểm tra đúng IP của Cloudflare. Chạy lại script khi Cloudflare đổi dải IP.
set -euo pipefail

OUT=/etc/nginx/conf.d/00-cloudflare.conf
V4=$(curl -fsS https://www.cloudflare.com/ips-v4)
V6=$(curl -fsS https://www.cloudflare.com/ips-v6)
[ -n "$V4" ] && [ -n "$V6" ] || { echo "Không tải được danh sách IP Cloudflare" >&2; exit 1; }

{
  echo "# Tạo tự động bởi cloudflare-ips.sh lúc $(date -u +%FT%TZ). Không sửa tay."
  for ip in $V4 $V6; do echo "set_real_ip_from $ip;"; done
  echo "real_ip_header CF-Connecting-IP;"
  echo
  echo "geo \$realip_remote_addr \$from_cloudflare {"
  echo "    default 0;"
  echo "    127.0.0.1/32 1;"
  echo "    ::1/128 1;"
  for ip in $V4 $V6; do echo "    $ip 1;"; done
  echo "}"
} > "$OUT.tmp"

mv "$OUT.tmp" "$OUT"
echo "Đã ghi $OUT ($(echo $V4 $V6 | wc -w) dải IP)"
