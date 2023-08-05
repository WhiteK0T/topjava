### get All Users
`curl -X GET --location "http://localhost:8080/topjava/rest/admin/users"`

### get Users 100001
`curl -X GET --location "http://localhost:8080/topjava/rest/admin/users/100001"`

### get All Meals
`curl -X GET --location "http://localhost:8080/topjava/rest/profile/meals"`

### get Meal 100003
`curl -X GET --location "http://localhost:8080/topjava/rest/profile/meals/100003"`

### filter Meals
`curl -X GET --location "http://localhost:8080/topjava/rest/profile/meals/between?startDate=2020-01-30&startTime=07:00:00&endDate=2020-01-31&endTime=11:00:00"`

### get Meals not found
`curl -X GET --location "http://localhost:8080/topjava/rest/profile/meals/1000010"`

### delete Meals
`curl -X DELETE --location "http://localhost:8080/topjava/rest/profile/meals/100003"`

### create Meals
`curl -X POST --location "http://localhost:8080/topjava/rest/profile/meals" \
-H "Content-Type: application/json;charset=UTF-8" \
-d "{\"dateTime\":\"2020-02-01T12:00\",\"description\":\"Created lunch\",\"calories\":300}"`

### update Meals
`curl -X PUT --location "http://localhost:8080/topjava/rest/profile/meals/100003" \
-H "Content-Type: application/json" \
-d "{\"dateTime\":\"2020-01-30T07:00\", \"description\":\"Updated breakfast\", \"calories\":200}"`
