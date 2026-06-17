# Login để lấy token
POST {{host}}/api/auth/login
Content-Type: application/json
{
  "email": "{{email}}",
  "password": "{{password}}"
}

HTTP 200
[Captures]
token: jsonpath "$.data.token"

# Tổng quan thống kê hệ thống → 200
GET {{host}}/admin/reports/overview
Authorization: Bearer {{token}}

HTTP 200
[Asserts]
jsonpath "$.success" == true
jsonpath "$.data.totalUsers" isInteger
jsonpath "$.data.activeUsers" isInteger
jsonpath "$.data.inactiveUsers" isInteger
jsonpath "$.data.totalGroups" isInteger
jsonpath "$.data.activeGroups" isInteger
jsonpath "$.data.totalPosts" isInteger
jsonpath "$.data.totalComments" isInteger

# Xuất báo cáo CSV → 200, content-type text/csv
GET {{host}}/admin/reports/export
Authorization: Bearer {{token}}

HTTP 200
[Asserts]
header "Content-Type" contains "text/csv"
header "Content-Disposition" contains "attachment"
body contains "metric,value"
body contains "totalUsers"

# Không có token → 401
GET {{host}}/admin/reports/overview

HTTP 401
