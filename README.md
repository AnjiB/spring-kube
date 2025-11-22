# Spring Hello World API

A simple Spring Boot REST API application with two endpoints.

## Features

- **GET `/hi`** - Returns "Hello"
- **POST `/hi`** - Accepts a name in the request body and returns a personalized greeting

## Prerequisites

- Java 17 or higher
- Maven 3.6+ (or use Maven wrapper)
- Docker (optional, for containerized deployment)
- Minikube and kubectl (for Kubernetes deployment)

## Building the Project

```bash
mvn clean package
```

This will create a JAR file in the `target` directory: `spring-hello-world-1.0.0.jar`

## Running the Application

### Option 1: Using Maven

```bash
mvn spring-boot:run
```

### Option 2: Using the JAR file

```bash
java -jar target/spring-hello-world-1.0.0.jar
```

### Option 3: Using Docker

Build the Docker image:
```bash
docker build -t anji-spring-hello-world:latest .
```

Run the container:
```bash
docker run -p 8080:8080 anji-spring-hello-world:latest
```

### Option 4: Deploying to Minikube (Kubernetes)

#### Prerequisites
- Minikube installed and running
- kubectl configured to use minikube

#### Step 1: Start Minikube
```bash
minikube start
```

#### Step 2: Build the Docker Image
```bash
docker build -t anji-spring-hello-world:latest .
```

#### Step 3: Load Image into Minikube
Since minikube uses its own Docker daemon, you need to load the image:
```bash
minikube image load anji-spring-hello-world:latest
```

Alternatively, you can build the image directly in minikube's Docker environment:
```bash
eval $(minikube docker-env)
docker build -t anji-spring-hello-world:latest .
eval $(minikube docker-env -u)  # Switch back to host Docker
```

#### Step 4: Deploy to Kubernetes
```bash
kubectl apply -f kube/spring-boot-deployment.yml
```

#### Step 5: Check Deployment Status
```bash
# Check deployment status
kubectl get deployment anji-spring-boot-deployment

# Check pods
kubectl get pods -l app=anji-spring-boot

# View pod logs
kubectl logs -l app=anji-spring-boot
```

#### Step 6: Access the Application

**Option A: Port Forward (Recommended for Testing)**
```bash
kubectl port-forward deployment/anji-spring-boot-deployment 8080:8080
```
Then access the API at `http://localhost:8080/hi`

**Option B: Using Minikube Service**
If you have a Service configured, you can use:
```bash
minikube service <service-name>
```

**Option C: Direct Pod Access**
```bash
# Get pod name
POD_NAME=$(kubectl get pods -l app=anji-spring-boot -o jsonpath='{.items[0].metadata.name}')

# Port forward to specific pod
kubectl port-forward $POD_NAME 8080:8080
```

#### Step 7: Test the API
Once port-forwarding is active, test the endpoints:
```bash
# GET request
curl http://localhost:8080/hi

# POST request
curl -X POST http://localhost:8080/hi \
  -H "Content-Type: application/json" \
  -d '{"name": "Anji"}'
```

#### Useful Commands
```bash
# View deployment details
kubectl describe deployment anji-spring-boot-deployment

# View pod details
kubectl describe pod -l app=anji-spring-boot

# Scale deployment
kubectl scale deployment anji-spring-boot-deployment --replicas=3

# Delete deployment
kubectl delete -f kube/spring-boot-deployment.yml

# View all resources
kubectl get all -l app=anji-spring-boot
```

### Option 5: Deploying with NodePort Service (Recommended for Production-like Testing)

This section demonstrates deploying the application with a Kubernetes Service using NodePort strategy. This approach provides a stable endpoint for accessing your application, even when pods are recreated.

#### Why Use a Service?

When you deploy only a Deployment, pods get dynamic IP addresses that change when pods are recreated. A Service provides:
- **Stable endpoint**: Access your application via a consistent URL
- **Load balancing**: Automatically distributes traffic across multiple pods
- **Service discovery**: Uses labels and selectors to find pods automatically
- **Resilience**: If a pod is deleted, the service automatically routes traffic to remaining pods

#### Step-by-Step Deployment with NodePort Service

##### Step 1: Prepare Minikube and Docker Image

```bash
# Start minikube
minikube start

# Build Docker image
docker build -t anji-spring-hello-world:latest .

# Load image into minikube
minikube image load anji-spring-hello-world:latest
```

##### Step 2: Deploy Deployment and Service

The `kube/spring-boot-service.yml` file contains both the Deployment and Service definitions:

```bash
kubectl apply -f kube/spring-boot-service.yml
```

This will create:
- A Deployment with 2 replicas (pods)
- A NodePort Service named `spring-nodeport`

##### Step 3: Verify Deployment

```bash
# Check deployment status
kubectl get deployment spring-hello-world

# Check pods (you should see 2 pods running)
kubectl get pods -l app=spring-hello-world

# Check service
kubectl get service spring-nodeport
```

Expected output:
```
NAME                 TYPE       CLUSTER-IP      EXTERNAL-IP   PORT(S)          AGE
spring-nodeport      NodePort   10.96.xxx.xxx   <none>        8080:30080/TCP   1m
```

##### Step 4: Get Service URL

Since you're using Docker driver for minikube, `localhost:nodePort` won't work. Use minikube's service command:

```bash
minikube service spring-nodeport --url
```

This will output something like:
```
http://127.0.0.1:58645
```

**Note**: The port number (58645) is dynamically assigned by minikube when using Docker driver. The `nodePort: 30080` in the service YAML is the port on the minikube VM, but minikube creates a tunnel with a different port.

##### Step 5: Access the Application

Use the URL from Step 4 to access your API:

```bash
# GET request
curl http://127.0.0.1:58645/hi

# POST request
curl -X POST http://127.0.0.1:58645/hi \
  -H "Content-Type: application/json" \
  -d '{"name": "Anji"}'
```

##### Step 6: Monitor Traffic with Kubeshark (Optional)

Install and run Kubeshark to see live traffic:

```bash
# Install kubeshark (if not already installed)
# Visit: https://kubeshark.co/

# Run kubeshark
kubeshark tap
```

This will open a web interface showing all network traffic in your cluster.

##### Step 7: Test Load Balancing with Concurrent Requests

Send multiple concurrent requests to see traffic distribution:

```bash
for i in {1..50}; do curl http://127.0.0.1:58645/hi & done
wait
```

In Kubeshark, you'll see requests being distributed across both pods, demonstrating Kubernetes' built-in load balancing.

##### Step 8: Test Pod Resilience

Delete one pod and observe automatic recovery:

```bash
# Get pod names
kubectl get pods -l app=spring-hello-world

# Delete one pod
kubectl delete pod <pod-name>

# Watch pods being recreated
kubectl get pods -l app=spring-hello-world -w
```

You'll see:
1. One pod enters `Terminating` state
2. A new pod is automatically created by the Deployment
3. The Service automatically routes traffic to the new pod

##### Step 9: Verify Service Discovery

After pod recreation, test the API again:

```bash
curl http://127.0.0.1:58645/hi
```

The request will still work because:
- The Service maintains a stable endpoint
- It uses labels (`app: spring-hello-world`) to discover pods
- It automatically updates its endpoints when pods are created/deleted

#### Understanding the Service YAML File

Let's break down the `kube/spring-boot-service.yml` file:

```yaml
---
apiVersion: apps/v1
kind: Deployment
metadata:
  name: spring-hello-world
spec:
  replicas: 2
  selector:
    matchLabels:
      app: spring-hello-world
  template:
    metadata:
      labels:
        app: spring-hello-world
    spec:
      containers:
        - name: spring-hello-world
          imagePullPolicy: Never
          image: anji-spring-hello-world:latest
          ports:
            - containerPort: 8080
---
apiVersion: v1
kind: Service
metadata:
  name: spring-nodeport
spec:
  type: NodePort
  selector:
    app: spring-hello-world
  ports:
    - protocol: TCP
      port: 8080       # Cluster internal port
      targetPort: 8080 # Container port
      nodePort: 30080  # External port
```

##### Deployment Section Explanation

- **`apiVersion: apps/v1`**: Kubernetes API version for Deployment resource
- **`kind: Deployment`**: Resource type that manages pod replicas
- **`metadata.name`**: Unique name for the deployment
- **`spec.replicas: 2`**: Number of pod replicas to maintain
- **`spec.selector.matchLabels`**: Labels used to select pods managed by this deployment
- **`spec.template.metadata.labels`**: Labels applied to pods created by this deployment
  - **Critical**: These labels must match the selector labels
- **`spec.template.spec.containers`**: Container specification
  - **`imagePullPolicy: Never`**: Use local image, don't pull from registry
  - **`image`**: Docker image to use
  - **`ports.containerPort`**: Port the container listens on

##### Service Section Explanation

- **`apiVersion: v1`**: Kubernetes API version for Service resource
- **`kind: Service`**: Resource type that provides network access to pods
- **`metadata.name`**: Unique name for the service
- **`spec.type: NodePort`**: Service type that exposes the service on each node's IP at a static port
- **`spec.selector`**: Labels used to find pods to route traffic to
  - **Critical**: Must match the pod labels from the Deployment
  - This is how Service Discovery works - the service finds pods with matching labels
- **`spec.ports`**: Port configuration
  - **`protocol: TCP`**: Network protocol to use
  - **`port: 8080`**: Port exposed internally within the cluster
  - **`targetPort: 8080`**: Port on the container to forward traffic to
  - **`nodePort: 30080`**: Port exposed on each node (30000-32767 range)

##### How Service Discovery Works

1. **Label Matching**: The Service's `selector` (`app: spring-hello-world`) matches pods with the same label
2. **Endpoint Updates**: Kubernetes automatically creates Endpoints objects that list all pod IPs matching the selector
3. **Load Balancing**: When traffic arrives at the Service, it's distributed across all healthy pods
4. **Automatic Updates**: When pods are created/deleted, the Endpoints are automatically updated

#### Comparison: Deployment Only vs Deployment + Service

| Aspect | Deployment Only | Deployment + Service |
|--------|----------------|---------------------|
| **Access Method** | Direct pod IP or port-forward | Stable service URL |
| **Pod IP Changes** | Must update client when pod IP changes | Service IP/URL remains constant |
| **Load Balancing** | Manual (client must handle) | Automatic across all pods |
| **Resilience** | Client breaks if pod deleted | Service automatically routes to remaining pods |
| **Service Discovery** | Manual pod lookup required | Automatic via labels/selectors |

#### Useful Commands for Service Management

```bash
# View service details
kubectl describe service spring-nodeport

# View service endpoints (shows pod IPs)
kubectl get endpoints spring-nodeport

# View all services
kubectl get services

# Get service URL (minikube)
minikube service spring-nodeport --url

# Open service in browser (minikube)
minikube service spring-nodeport

# View service logs (if using a service mesh)
kubectl logs -l app=spring-hello-world

# Scale deployment (service automatically picks up new pods)
kubectl scale deployment spring-hello-world --replicas=3

# Delete service and deployment
kubectl delete -f kube/spring-boot-service.yml
```

## Configuring the Port

By default, the application runs on port **8080**. You can change the port using several methods:

### Method 1: Command Line Argument

```bash
java -jar target/spring-hello-world-1.0.0.jar --server.port=9090
```

Or with Maven:
```bash
mvn spring-boot:run -Dspring-boot.run.arguments=--server.port=9090
```

### Method 2: Environment Variable

```bash
export SERVER_PORT=9090
java -jar target/spring-hello-world-1.0.0.jar
```

Or inline:
```bash
SERVER_PORT=9090 java -jar target/spring-hello-world-1.0.0.jar
```

### Method 3: Application Properties File

Create or modify `src/main/resources/application.properties`:
```properties
server.port=9090
```

### Method 4: Docker with Port Mapping

When running with Docker, you can map any host port to the container's port:
```bash
docker run -p 9090:8080 anji-spring-hello-world:latest
```

This maps host port 9090 to container port 8080.

## API Endpoints

### GET /hi

Returns a simple greeting.

**Request:**
```bash
curl http://localhost:8080/hi
```

**Response:**
```
Hello
```

### POST /hi

Accepts a JSON body with a name and returns a personalized greeting.

**Request:**
```bash
curl -X POST http://localhost:8080/hi \
  -H "Content-Type: application/json" \
  -d '{"name": "Anji"}'
```

**Response:**
```
Hi Anji
```

## Testing the API

### Using curl

**GET request:**
```bash
curl http://localhost:8080/hi
```

**POST request:**
```bash
curl -X POST http://localhost:8080/hi \
  -H "Content-Type: application/json" \
  -d '{"name": "Anji"}'
```

### Using Postman

#### Import Postman Collection

1. Open Postman
2. Click **Import** button (top left)
3. Select the `Spring-Hello-World.postman_collection.json` file from this project
4. The collection will be imported with both endpoints pre-configured

#### Using curl in Postman

Postman can generate curl commands for you, or you can import curl commands:

**GET /hi:**
```bash
curl --location 'http://localhost:8080/hi'
```

**POST /hi:**
```bash
curl --location 'http://localhost:8080/hi' \
--header 'Content-Type: application/json' \
--data '{
    "name": "Anji"
}'
```

To import curl into Postman:
1. Click **Import** in Postman
2. Select **Raw text** tab
3. Paste the curl command
4. Click **Continue** and **Import**

#### Manual Setup in Postman

**GET /hi:**
- Method: `GET`
- URL: `http://localhost:8080/hi`
- Click **Send**

**POST /hi:**
- Method: `POST`
- URL: `http://localhost:8080/hi`
- Headers: `Content-Type: application/json`
- Body: Select **raw** and **JSON**, then enter:
  ```json
  {
    "name": "Anji"
  }
  ```
- Click **Send**

### Using Other REST Clients

You can also use tools like Insomnia, HTTPie, or any REST client to test the endpoints using the curl commands above.

## Project Structure

```
spring-hello-world/
├── src/
│   └── main/
│       ├── java/
│       │   └── com/example/springhelloworld/
│       │       ├── SpringHelloWorldApplication.java
│       │       ├── controller/
│       │       │   └── HelloController.java
│       │       └── dto/
│       │           └── NameRequest.java
│       └── resources/
├── kube/
│   ├── spring-boot-deployment.yml
│   └── spring-boot-service.yml
├── pom.xml
├── Dockerfile
├── Spring-Hello-World.postman_collection.json
└── README.md
```

## Technologies Used

- Spring Boot 3.4.5
- Java 17
- Maven
- Docker
- Kubernetes (Minikube)

