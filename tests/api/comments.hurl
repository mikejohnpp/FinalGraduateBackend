# tests/api/comments.hurl

# Get token for tests
POST {{host}}/api/auth/login
{
  "email": "user1@example.com",
  "password": "password"
}
HTTP 200
[Captures]
token: jsonpath "$.data.token"
userId: jsonpath "$.data.userId"


# 1. Create a comment
POST {{host}}/users/posts/1/comments
Authorization: Bearer {{token}}
{
  "userId": {{userId}},
  "content": "This is a test comment from Hurl"
}
HTTP 200
[Captures]
commentId: jsonpath "$.data.id"


# 2. Get comments for post
GET {{host}}/users/posts/1/comments?userId={{userId}}
Authorization: Bearer {{token}}
HTTP 200
[Asserts]
jsonpath "$.success" == true


# 3. Create a reply
POST {{host}}/users/posts/1/comments
Authorization: Bearer {{token}}
{
  "userId": {{userId}},
  "content": "This is a reply to the test comment",
  "parentId": {{commentId}}
}
HTTP 200
[Captures]
replyId: jsonpath "$.data.id"


# 4. Get replies for comment
GET {{host}}/users/posts/1/comments/{{commentId}}/replies?userId={{userId}}
Authorization: Bearer {{token}}
HTTP 200
[Asserts]
jsonpath "$.success" == true


# 5. Update comment
PUT {{host}}/users/posts/1/comments/{{commentId}}?userId={{userId}}
Authorization: Bearer {{token}}
{
  "content": "This is an updated test comment from Hurl"
}
HTTP 200
[Asserts]
jsonpath "$.success" == true
jsonpath "$.data.content" == "This is an updated test comment from Hurl"


# 6. Like comment
POST {{host}}/users/posts/1/comments/{{commentId}}/like
Authorization: Bearer {{token}}
{
  "userId": {{userId}}
}
HTTP 200


# 7. Unlike comment
DELETE {{host}}/users/posts/1/comments/{{commentId}}/like
Authorization: Bearer {{token}}
{
  "userId": {{userId}}
}
HTTP 200


# 8. Delete comment
DELETE {{host}}/users/posts/1/comments/{{commentId}}?userId={{userId}}
Authorization: Bearer {{token}}
HTTP 200
