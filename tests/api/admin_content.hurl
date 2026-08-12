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

# Tạo một bài viết để khóa
POST {{host}}/posts
Authorization: Bearer {{token}}
Content-Type: application/json
{
  "content": "Test post for locking",
  "isGroupPosted": false
}
HTTP 201
[Captures]
post_id: jsonpath "$.data.id"

# Lock bài viết
POST {{host}}/admin/content/posts/lock
Authorization: Bearer {{token}}
Content-Type: application/json
{
  "ids": [{{post_id}}]
}
HTTP 200
[Asserts]
jsonpath "$.success" == true

# Unlock bài viết
POST {{host}}/admin/content/posts/unlock
Authorization: Bearer {{token}}
Content-Type: application/json
{
  "ids": [{{post_id}}]
}
HTTP 200
[Asserts]
jsonpath "$.success" == true

# Xóa bài viết dọn rác
DELETE {{host}}/posts/{{post_id}}
Authorization: Bearer {{token}}
HTTP 200
