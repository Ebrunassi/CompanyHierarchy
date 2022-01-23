# **Personio - Ever-Changing Hierarchy Problem**

This project was developed in order to solve the ever-changing hierarchy problem.

The service was developed using Java 11, and it contains endpoints for requests:

Verbo HTTP | Header * | Resource Path                            | Descrição
----|-------------------------------------|------------------------------------------|--------
POST| Authorization: Bearer *{jwt_token}* | http://localhost:8080/personio/hierarchy | Submitting a json containing the relation of employees, will return an hierarchy tree  
GET | Authorization: Bearer *{jwt_token}* | http://localhost:8080/personio/hierarchy | Returns the supervisor and supervisor's supervisor of given employee 


* The *jwt token* can be rescued from the *company-hierarchy-login* after a valid login be authenticated.

### **[POST] /personio/hierarchy**
This project receives a request containing the relations between employees as body, like the JSON below:
```json
{
	"Pete": "Nick",
	"Barbara": "Nick",
	"Nick": "Sophie",
	"Sophie": "Jonas"
}
``` 

Where Nick is a supervisor of Pete and Barbara, Sophie supervises Nick.

Sending this JSON in POST body's API, the following structure will be returned:
```json
{
	"Jonas": {
		"Sophie": {
			"Nick": {
				"Pete": {},
				"Barbara": {}
			}
		}
	}
}
```
### **[GET] /personio/hierarchy**

Receiving the following JSON as request's body:
```json
{
	"name":"Barbara"
}
```
The following JSON will return showing the supervisor and supervisor's supervisor of Barbara:
```json
{
	"Sophie": {
		"Nick": {
			"Barbara": {}
		}
	}
}
```

**Unit Tests**
--------
This project was developed using TDD, and there are tests for *controller*, *service* and *repository* layers. It was used `JUnit` and `Mockit` to develop and mock each testing success requests and possible failures that could have for users experience.

**Logs**
-------
Every received requests generate logs. Knowing that saving all the logs is essential for debugging, this projects uses `Log4j2` to generate and save all the logs in files.

All the created log's files will be saved in the `logs` folder, which will contains all this files organized by the creation day.

**Swagger**
---------
For a better user experiencie, it was used `swagger` to be accessed and to have a better knowledge about all the requests in this service.

You can access it in 
> http://localhost:8080/swagger-ui/index.html
