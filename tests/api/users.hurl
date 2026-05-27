# Login để lấy token và userId
POST {{host}}/api/auth/login
Content-Type: application/json
{
  "email": "{{email}}",
  "password": "{{password}}"
}

HTTP 200
[Captures]
token: jsonpath "$.data.token"
userId: jsonpath "$.data.userId"

# Lấy profile của chính mình → 200
GET {{host}}/users/{{userId}}/profile
Authorization: Bearer {{token}}

HTTP 200
[Asserts]
jsonpath "$.success" == true
jsonpath "$.data.id" isInteger
jsonpath "$.data.userName" isString
jsonpath "$.data.email" isString
jsonpath "$.data.role" isString
jsonpath "$.data.isActive" isBoolean

# Không có token → 401
GET {{host}}/users/{{userId}}/profile

HTTP 401
