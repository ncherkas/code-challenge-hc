# K8S Orchestrator API
_Code Challenge for the Hazelcast Cloud_

## Architecture and implementation details
Architecture is consists of the multiple layers working on top of the K8S API:
```
                        REST API consumers
                                |
                                |
                                V
            REST API implemented with the Spring Boot
                                |
                                |
                                V
                    Spring Data Hazelcast Repository
                                |
                                |
                                V
                Hazelcast IMDG Cluster Embedded Member(s)
                                |
                                
                               ...
                               
                               WAN
                               ...
                                
                                |
                                V
                            K8S API
                                |
                                |
                                V
                       K8S backend (AWS, GCP etc.)                                                                                                                      
``` 

Here are the reasons why I decided to use the Hazelcast IMDG:
 - we already have the K8S backend responsible for the persistent storage and operations
 - Hazelcast IMDG with embedded topology enables the High-Available deployments comparing to the embedded database
 - no need to define the static schema as we do in case of SQL, no need for transactions... less complexity     

## API
Orchestrator REST API consists of the following methods (all provided examples use [HTTPie](https://httpie.org/)): 
1) Login:
```
# Accepts JSON with two properties - username and password, returns `Authorization` header with the JWT token for further usage

$ http -v POST http://<HOST>:<PORT>/login username=ncherkas password=Qwerty1!

POST /login HTTP/1.1
Accept: application/json, */*
Accept-Encoding: gzip, deflate
Connection: keep-alive
Content-Length: 48
Content-Type: application/json
Host: localhost:8080
User-Agent: HTTPie/0.9.9

{
    "password": "Qwerty1!",
    "username": "ncherkas"
}

HTTP/1.1 200 
Authorization: Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJuY2hlcmthcyIsImV4cCI6MTUzNzgzNjM3NH0.OAB42AMGKb9i-ZB22PqScR6A3RwJyWB7VQs8C76FpItvsrVOOtjTltvjg5mWN20ugdzEUkJEAVIOtnTEdOQJHQ
Cache-Control: no-cache, no-store, max-age=0, must-revalidate
Content-Length: 0
Date: Thu, 20 Sep 2018 00:46:14 GMT
Expires: 0
Pragma: no-cache
X-Content-Type-Options: nosniff
X-Frame-Options: DENY
X-XSS-Protection: 1; mode=block  

``` 

2. Create new K8S Deployment:
```
# Accepts a JSON representing the Deployment info, one which you would use to sumbit the YAML palyload to `kubectl`

$ http -v POST <HOST>:<PORT>/deployments "Authorization: Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJuY2hlcmthcyIsImV4cCI6MTUzNzgzNjM3NH0.OAB42AMGKb9i-ZB22PqScR6A3RwJyWB7VQs8C76FpItvsrVOOtjTltvjg5mWN20ugdzEUkJEAVIOtnTEdOQJHQ" < nginx_deployment.json

POST /deployments HTTP/1.1
Accept: application/json, */*
Accept-Encoding: gzip, deflate
Authorization: Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJuY2hlcmthcyIsImV4cCI6MTUzNzgzNjM3NH0.OAB42AMGKb9i-ZB22PqScR6A3RwJyWB7VQs8C76FpItvsrVOOtjTltvjg5mWN20ugdzEUkJEAVIOtnTEdOQJHQ
Connection: keep-alive
Content-Length: 730
Content-Type: application/json
Host: localhost:8080
User-Agent: HTTPie/0.9.9

{
    "apiVersion": "apps/v1",
    "kind": "Deployment",
    "metadata": {
        "name": "nginx-deployment"
    },
    "spec": {
        "replicas": 2,
        "selector": {
            "matchLabels": {
                "app": "nginx"
            }
        },
        "template": {
            "metadata": {
                "labels": {
                    "app": "nginx"
                }
            },
            "spec": {
                "containers": [
                    {
                        "image": "nginx:1.8",
                        "name": "nginx",
                        "ports": [
                            {
                                "containerPort": 80
                            }
                        ],
                        "resources": {
                            "requests": {
                                "cpu": "100m",
                                "memory": "100Mi"
                            }
                        }
                    }
                ]
            }
        }
    }
}

HTTP/1.1 201 
Cache-Control: no-cache, no-store, max-age=0, must-revalidate
Content-Length: 0
Date: Thu, 20 Sep 2018 00:51:09 GMT
Expires: 0
Pragma: no-cache
Set-Cookie: JSESSIONID=8DA5DC7CC65742CD7B13CC5269565CFD; Path=/; HttpOnly
X-Content-Type-Options: nosniff
X-Frame-Options: DENY
X-XSS-Protection: 1; mode=block
```

3. List Deployments:
```
# It's either a paginated list, or the list with all available deployments (no pagination params provided)

$ http -v GET "<HOST>:<PORT>/deployments?page=0&size=2" "Authorization: Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJuY2hlcmthcyIsImV4cCI6MTUzNzgzNjM3NH0.OAB42AMGKb9i-ZB22PqScR6A3RwJyWB7VQs8C76FpItvsrVOOtjTltvjg5mWN20ugdzEUkJEAVIOtnTEdOQJHQ"

GET /deployments?page=0&size=2 HTTP/1.1
Accept: */*
Accept-Encoding: gzip, deflate
Authorization: Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJuY2hlcmthcyIsImV4cCI6MTUzNzgzNjM3NH0.OAB42AMGKb9i-ZB22PqScR6A3RwJyWB7VQs8C76FpItvsrVOOtjTltvjg5mWN20ugdzEUkJEAVIOtnTEdOQJHQ
Connection: keep-alive
Host: localhost:8080
User-Agent: HTTPie/0.9.9



HTTP/1.1 200 
Cache-Control: no-cache, no-store, max-age=0, must-revalidate
Content-Type: application/json;charset=UTF-8
Date: Thu, 20 Sep 2018 00:56:09 GMT
Expires: 0
Pragma: no-cache
Set-Cookie: JSESSIONID=BB91C457F9858E564CABC3EB115F9EBF; Path=/; HttpOnly
Transfer-Encoding: chunked
X-Content-Type-Options: nosniff
X-Frame-Options: DENY
X-XSS-Protection: 1; mode=block

{
    "count": 6,
    "items": [
        {
            "apiVersion": "apps/v1",
            "kind": "Deployment",
            "metadata": {
                ...
            },
            "spec": {
                ...
            },
            "status": {
                ...
            }
        },
        {
            "apiVersion": "apps/v1",
            "kind": "Deployment",
            "metadata": {
                ...
            },
            "spec": {
                ...
            },
            "status": {
                ...
            }
        }
    ],
    "page": 0,
    "total": 11
}
```

4. Also, the Spring Boot Actuator endpoints are available e.g. health-check:
```

$ http -v GET <HOST>:<PORT>/actuator/health

ET /actuator/health HTTP/1.1
Accept: */*
Accept-Encoding: gzip, deflate
Connection: keep-alive
Host: localhost:7777
User-Agent: HTTPie/0.9.9



HTTP/1.1 200 
Cache-Control: no-cache, no-store, max-age=0, must-revalidate
Content-Type: application/vnd.spring-boot.actuator.v2+json;charset=UTF-8
Date: Thu, 20 Sep 2018 01:04:08 GMT
Expires: 0
Pragma: no-cache
Transfer-Encoding: chunked
X-Content-Type-Options: nosniff
X-Frame-Options: DENY
X-XSS-Protection: 1; mode=block

{
    "status": "UP"
}

```

## Running and testing
Please follow the next steps:
1) Make sure to setup a K8S SDK. I tested primarily with GCP so I've setup `gcloud`,`kubectl` and enabled K8S API for the GCP K8S Engine.   
2) Build the project - `mvn clean install`
3) Run the produced jar, you can find it inside of the `target` folder - `java -jar target/k8s-orchestrator-api-0.0.1-SNAPSHOT.jar`
The following program args are available:
 - `--server.port` specifies a server port used by the Spring Boot 
 - `--k8s.orchestrator.api.user.username` username used for the login process  
 - `--k8s.orchestrator.api.user.password` password used for the login process (must be bcrypt-hashed)
 
Note, that you can run a cluster of the several applications, giving a different `--server.port` to each instance.   

## TODOs... many of them
Here are some of the required things as well as my ideas:
1) Unit tests. Unfortunately didn't find a time to do this. Good thing was that the Fabric8 Java client even had a support to setup the K8S API Mock Server.
2) UI. I did some research and had ideas to build it with Twitter Bootstrap + React.js. After running the React CLI I figure out that I won't complete this in time. We even could deploy it separately into NGINx or something.
3) Separate microservice for the authentication. As you can find, I've implemented a so-called dummy User Service which reads the data from application config.
4) Deploy the application itself into K8S. Docker image and YAMLs for the deployment and service should be easy to do. Plus, the Hazelcast IMDG has a support of the K8S deployment and discovery.
5) Issue with K8S Java Client. At my setup, each several hours the Java Client was failing due to the expired credentials. Only by invoking `kubectl` the credentials were getting refreshed. According to open GH issues seems like the refresh is not implemented yet. But I still double-checking and investigating.     
