## Users
#### CREATE USER
* **URL**
   ```
   /api/user/add
   ```
* **Method:**

  `POST`
*  **URL Params**

   None
* **Data Params**
   ```
   {  
     "firstName":"John",
     "lastName":"Smith",
     "email":"john.smith@gmail.com",
     "username":"johnsmith2020"
   }
   ```
* **Success Response:**

  * **Code:** 200 <br />
    **Content:** `{"status":"SUCCESS","message":"User x has been successfully created"}`
 
* **Error Response:**

  * **Code:** 200 <br />
    **Content:** `{"status":"ERROR","message":"Unable to create user"}` 

#### LIST ALL USERS
* **URL**
   ```
   /api/user/users
   ```
* **Method:**

  `GET`
*  **URL Params**

   None

*  **Data Params**

   None

* **Success Response:**

  * **Code:** 200 <br />
    **Content:** `{"status":"SUCCESS","message":[{ "firstName":"John", ...},{..}]}`
 
* **Error Response:**

  * **Code:** 200 <br />
    **Content:** `{"status":"SUCCESS","message":[]}`

#### GET USER BY ID
* **URL**
   ```
   "/api/user/"
   ```
* **Method:**

  `GET`
*  **URL Params**

   **Required:**
 
   `id=[integer]`

*  **Data Params**

   None

* **Success Response:**

  * **Code:** 200 <br />
    **Content:** `{"status":"SUCCESS","message":{ "firstName":"John", ...}}`
 
* **Error Response:**

  * **Code:** 200 <br />
    **Content:** `{"status":"SUCCESS","message":Unable to find user with id: X}`
    
#### EDIT EXISTING USER
* **URL**
   ```
   /api/user/edit
   ```
* **Method:**

  `PUT`
*  **URL Params**

   None
* **Data Params**
   ```
   {  
     "firstName":"Joe",
     "lastName":"Smith",
     "email":"john.smith@gmail.com",
     "username":"johnsmith2020"
   }
   ```
* **Success Response:**

  * **Code:** 200 <br />
    **Content:** `{"status":"SUCCESS","message":{"firstName":"Joe",...}}`
 
* **Error Response:**

  * **Code:** 200 <br />
    **Content:** `{"status":"ERROR","message":"Unable to update user"}` 
