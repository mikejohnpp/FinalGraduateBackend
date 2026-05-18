# 1. Register a test user (optional, fails gracefully if already exists)
POST {{host}}/api/auth/register
{
  "email": "hurltest@example.com",
  "password": "Password123!",
  "confirmPassword": "Password123!"
}

# 2. Login to capture JWT token
POST {{host}}/api/auth/login
{
  "email": "tahoangphuc1901@gmail.com",
  "password": "phucta1901"
}
HTTP 200
[Captures]
token: jsonpath "$.data.token"
userId: jsonpath "$.data.userId"

# 3. Create a new Post
POST {{host}}/users/posts
Authorization: Bearer {{token}}
{
  "userId": {{userId}},
  "contents": ["Hello from Hurl!", "This is a second paragraph."]
}
HTTP 200
[Captures]
postId: jsonpath "$.data.id"

# 4. Get the created Post
GET {{host}}/users/posts/{{postId}}
Authorization: Bearer {{token}}
HTTP 200
[Asserts]
jsonpath "$.success" == true
jsonpath "$.data.id" == {{postId}}

# 5. Update the Post
PUT {{host}}/users/posts/{{postId}}
Authorization: Bearer {{token}}
{
  "contents": ["Updated Hurl content"]
}
HTTP 200
[Asserts]
jsonpath "$.success" == true
jsonpath "$.data.contents[0]" == "Updated Hurl content"

# 6. Get all Posts
GET {{host}}/users/posts
Authorization: Bearer {{token}}
HTTP 200
[Asserts]
jsonpath "$.success" == true

# 7. Delete the Post
DELETE {{host}}/users/posts/{{postId}}
Authorization: Bearer {{token}}
HTTP 200
[Asserts]
jsonpath "$.success" == true
