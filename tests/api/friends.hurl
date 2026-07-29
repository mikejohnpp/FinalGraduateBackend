# Friends API Tests
# Run: hurl --variables-file vars/dev.env friends.hurl

# ── 1. Login to get token ────────────────────────────────────────────────────
POST {{host}}/api/auth/login
Content-Type: application/json
{
  "email": "user1@example.com",
  "password": "password"
}

HTTP 200
[Captures]
token: jsonpath "$.data.token"
userId1: jsonpath "$.data.userId"

# ── 2. Login as user 2 ───────────────────────────────────────────────────────
POST {{host}}/api/auth/login
Content-Type: application/json
{
  "email": "user2@example.com",
  "password": "password"
}

HTTP 200
[Captures]
token2: jsonpath "$.data.token"
userId2: jsonpath "$.data.userId"

# ── 3. Get pending requests (empty initially) ─────────────────────────────────
GET {{host}}/users/friends/requests?userId={{userId1}}
Authorization: Bearer {{token}}

HTTP 200
[Asserts]
jsonpath "$.success" == true
jsonpath "$.data.data" count == 0
jsonpath "$.data.hasMore" == false

# ── 4. Get pending request count (should be 0) ────────────────────────────────
GET {{host}}/users/friends/requests/count?userId={{userId1}}
Authorization: Bearer {{token}}

HTTP 200
[Asserts]
jsonpath "$.success" == true
jsonpath "$.data" == 0

# ── 4b. Check initial status (should be NOT_FRIENDS) ──────────────────────────
GET {{host}}/users/friends/status?userId={{userId1}}&targetId={{userId2}}
Authorization: Bearer {{token}}

HTTP 200
[Asserts]
jsonpath "$.success" == true
jsonpath "$.data.status" == "NOT_FRIENDS"
jsonpath "$.data.requestId" == null

# ── 5. Send friend request (user1 → user2) ───────────────────────────────────
POST {{host}}/users/friends/requests
Authorization: Bearer {{token}}
Content-Type: application/json
{
  "userId": {{userId1}},
  "targetUserId": {{userId2}}
}

HTTP 201
[Asserts]
jsonpath "$.success" == true

# ── 5b. Check status after send (PENDING_SENT for user1, PENDING_RECEIVED for user2) ──
GET {{host}}/users/friends/status?userId={{userId1}}&targetId={{userId2}}
Authorization: Bearer {{token}}

HTTP 200
[Asserts]
jsonpath "$.success" == true
jsonpath "$.data.status" == "PENDING_SENT"
jsonpath "$.data.requestId" == {{userId1}}

GET {{host}}/users/friends/status?userId={{userId2}}&targetId={{userId1}}
Authorization: Bearer {{token2}}

HTTP 200
[Asserts]
jsonpath "$.success" == true
jsonpath "$.data.status" == "PENDING_RECEIVED"
jsonpath "$.data.requestId" == {{userId1}}

# ── 5c. Cancel friend request ─────────────────────────────────────────────────
DELETE {{host}}/users/friends/requests/cancel?userId={{userId1}}&targetId={{userId2}}
Authorization: Bearer {{token}}

HTTP 200
[Asserts]
jsonpath "$.success" == true

# ── 5d. Check status after cancel (back to NOT_FRIENDS) ───────────────────────
GET {{host}}/users/friends/status?userId={{userId1}}&targetId={{userId2}}
Authorization: Bearer {{token}}

HTTP 200
[Asserts]
jsonpath "$.success" == true
jsonpath "$.data.status" == "NOT_FRIENDS"

# ── 5e. Re-send friend request for subsequent tests (user1 → user2) ───────────
POST {{host}}/users/friends/requests
Authorization: Bearer {{token}}
Content-Type: application/json
{
  "userId": {{userId1}},
  "targetUserId": {{userId2}}
}

HTTP 201

# ── 6. Duplicate request → 409 ────────────────────────────────────────────────
POST {{host}}/users/friends/requests
Authorization: Bearer {{token}}
Content-Type: application/json
{
  "userId": {{userId1}},
  "targetUserId": {{userId2}}
}

HTTP 409

# ── 7. Self-add → 400 ─────────────────────────────────────────────────────────
POST {{host}}/users/friends/requests
Authorization: Bearer {{token}}
Content-Type: application/json
{
  "userId": {{userId1}},
  "targetUserId": {{userId1}}
}

HTTP 400

# ── 8. user2 sees the pending request ────────────────────────────────────────
GET {{host}}/users/friends/requests?userId={{userId2}}
Authorization: Bearer {{token2}}

HTTP 200
[Asserts]
jsonpath "$.success" == true
jsonpath "$.data.data" count == 1
jsonpath "$.data.data[0].sender.id" == {{userId1}}

# ── 9. Pending count = 1 for user2 ────────────────────────────────────────────
GET {{host}}/users/friends/requests/count?userId={{userId2}}
Authorization: Bearer {{token2}}

HTTP 200
[Asserts]
jsonpath "$.data" == 1

# ── 10. Accept the request ────────────────────────────────────────────────────
PUT {{host}}/users/friends/requests/{{userId1}}/accept?userId={{userId2}}
Authorization: Bearer {{token2}}

HTTP 200
[Asserts]
jsonpath "$.success" == true

# ── 10b. Check status after accept (should be FRIENDS) ────────────────────────
GET {{host}}/users/friends/status?userId={{userId1}}&targetId={{userId2}}
Authorization: Bearer {{token}}

HTTP 200
[Asserts]
jsonpath "$.success" == true
jsonpath "$.data.status" == "FRIENDS"
jsonpath "$.data.requestId" == null

# ── 11. Already friends → cannot send again → 409 ────────────────────────────
POST {{host}}/users/friends/requests
Authorization: Bearer {{token}}
Content-Type: application/json
{
  "userId": {{userId1}},
  "targetUserId": {{userId2}}
}

HTTP 409

# ── 12. Friends list for user1 ────────────────────────────────────────────────
GET {{host}}/users/friends?userId={{userId1}}
Authorization: Bearer {{token}}

HTTP 200
[Asserts]
jsonpath "$.success" == true
jsonpath "$.data.data" count == 1
jsonpath "$.data.data[0].user.id" == {{userId2}}

# ── 13. Suggestions for user1 (user2 should NOT appear) ──────────────────────
GET {{host}}/users/friends/suggestions?userId={{userId1}}
Authorization: Bearer {{token}}

HTTP 200
[Asserts]
jsonpath "$.success" == true

# ── 14. Unfriend ──────────────────────────────────────────────────────────────
DELETE {{host}}/users/friends/{{userId2}}?userId={{userId1}}
Authorization: Bearer {{token}}

HTTP 200
[Asserts]
jsonpath "$.success" == true

# ── 15. Friends list now empty ────────────────────────────────────────────────
GET {{host}}/users/friends?userId={{userId1}}
Authorization: Bearer {{token}}

HTTP 200
[Asserts]
jsonpath "$.data.data" count == 0

# ── 16. Decline flow: user1 sends again, user2 declines ──────────────────────
POST {{host}}/users/friends/requests
Authorization: Bearer {{token}}
Content-Type: application/json
{
  "userId": {{userId1}},
  "targetUserId": {{userId2}}
}

HTTP 201

PUT {{host}}/users/friends/requests/{{userId1}}/decline?userId={{userId2}}
Authorization: Bearer {{token2}}

HTTP 200
[Asserts]
jsonpath "$.success" == true

# ── 17. No more pending requests ──────────────────────────────────────────────
GET {{host}}/users/friends/requests?userId={{userId2}}
Authorization: Bearer {{token2}}

HTTP 200
[Asserts]
jsonpath "$.data.data" count == 0
