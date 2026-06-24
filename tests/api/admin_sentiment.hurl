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

# Tổng quan cảm xúc toàn hệ thống → 200
GET {{host}}/admin/sentiment/overview
Authorization: Bearer {{token}}

HTTP 200
[Asserts]
jsonpath "$.success" == true
jsonpath "$.data.totalPosts" isInteger
jsonpath "$.data.positivePosts" isInteger
jsonpath "$.data.neutralPosts" isInteger
jsonpath "$.data.negativePosts" isInteger
jsonpath "$.data.totalComments" isInteger
jsonpath "$.data.positiveComments" isInteger
jsonpath "$.data.neutralComments" isInteger
jsonpath "$.data.negativeComments" isInteger

# Tổng quan có lọc theo nhãn negative → 200
GET {{host}}/admin/sentiment/overview?sentiment=negative
Authorization: Bearer {{token}}

HTTP 200
[Asserts]
jsonpath "$.success" == true
jsonpath "$.data.neutralPosts" == 0
jsonpath "$.data.positivePosts" == 0

# Drill-down danh sách bài viết (mặc định type=post) → 200
GET {{host}}/admin/sentiment/items?type=post&page=0&size=10
Authorization: Bearer {{token}}

HTTP 200
[Asserts]
jsonpath "$.success" == true
jsonpath "$.data.data" isCollection
jsonpath "$.data.page" == 0
jsonpath "$.data.size" == 10

# Drill-down danh sách bình luận → 200
GET {{host}}/admin/sentiment/items?type=comment&page=0&size=10
Authorization: Bearer {{token}}

HTTP 200
[Asserts]
jsonpath "$.success" == true
jsonpath "$.data.data" isCollection

# Không có token → 401
GET {{host}}/admin/sentiment/overview

HTTP 401
