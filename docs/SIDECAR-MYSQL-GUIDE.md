# MySQL Sidecar Container Guide

This guide explains how to deploy Spring Boot application with MySQL as a sidecar container in the same pod.

## Overview

In this setup:
- **Spring Boot container**: Your application running on port 8080
- **MySQL sidecar container**: MySQL database running on port 3306
- Both containers share the same pod and can communicate via `localhost`

## Architecture

```
┌─────────────────────────────────────┐
│           Pod                       │
│  ┌──────────────────────────────┐   │
│  │  Spring Boot Container       │   │
│  │  Port: 8080                  │   │
│  │  Connects to: localhost:3306 │   │
│  └──────────────────────────────┘   │
│  ┌──────────────────────────────┐   │
│  │  MySQL Sidecar Container     │   │
│  │  Port: 3306                  │   │
│  │  Data: /var/lib/mysql        │   │
│  └──────────────────────────────┘   │
│  ┌──────────────────────────────┐   │
│  │  Shared Volume (mysql-data)  │   │
│  └──────────────────────────────┘   │
└─────────────────────────────────────┘
```

## Key Components

### 1. Multiple Containers in Same Pod

The deployment YAML defines two containers:
- `spring-hello-world`: Your Spring Boot application
- `mysql`: MySQL database sidecar

### 2. Localhost Communication

Since both containers are in the same pod, they share the same network namespace. Spring Boot connects to MySQL using `localhost:3306`.

### 3. Shared Volume

Both containers can access the same volume for MySQL data persistence:
```yaml
volumes:
- name: mysql-data
  emptyDir: {}  # Development - use PVC for production
```

## Deployment Steps

### Step 1: Update Dependencies

The `pom.xml` has been updated with:
- `spring-boot-starter-data-jpa`: For JPA/Hibernate
- `mysql-connector-j`: MySQL JDBC driver
- `spring-boot-starter-actuator`: For health checks

### Step 2: Configure Application Properties

The `application.properties` file contains:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/springdb?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true
spring.datasource.username=root
spring.datasource.password=rootpassword
```

**Key Points:**
- Uses `localhost:3306` (same pod network)
- Database `springdb` is created automatically if it doesn't exist

### Step 3: Build and Deploy

```bash
# 1. Build the application
mvn clean package

# 2. Build Docker image
docker build -t anji-spring-hello-world:latest .

# 3. Load image into minikube
minikube image load anji-spring-hello-world:latest

# 4. Deploy with MySQL sidecar
kubectl apply -f kube/spring-boot-with-mysql-sidecar.yml

# 5. Check deployment status
kubectl get pods -l app=spring-hello-world

# 6. Check both containers are running
kubectl get pods -l app=spring-hello-world -o jsonpath='{.items[0].spec.containers[*].name}'
# Should show: spring-hello-world mysql
```

### Step 4: Verify Containers

```bash
# View pod details
kubectl describe pod <pod-name>

# Check Spring Boot logs
kubectl logs <pod-name> -c spring-hello-world

# Check MySQL logs
kubectl logs <pod-name> -c mysql

# Execute command in MySQL container
kubectl exec -it <pod-name> -c mysql -- mysql -uroot -prootpassword -e "SHOW DATABASES;"
```

### Step 5: Access the Application

```bash
# Get service URL
minikube service spring-nodeport --url

# Or use kubectl proxy
kubectl proxy
curl http://localhost:8001/api/v1/namespaces/default/services/spring-nodeport/proxy/hi
```

## Container Startup Order

Both containers start simultaneously, but:

1. **MySQL container** has readiness probe that waits until MySQL is ready
2. **Spring Boot container** has startup probe that allows time for MySQL to be ready
3. Spring Boot will retry database connection automatically

### Health Checks

**MySQL Container:**
```yaml
readinessProbe:
  exec:
    command: ["sh", "-c", "mysqladmin ping -h localhost -u root -prootpassword"]
  initialDelaySeconds: 10
  periodSeconds: 5
```

**Spring Boot Container:**
```yaml
startupProbe:
  httpGet:
    path: /actuator/health
    port: 8080
  initialDelaySeconds: 10
  periodSeconds: 5
  failureThreshold: 30  # Up to 150 seconds for startup
```

## Connecting to MySQL from Outside Pod

### Option 1: Port Forward to MySQL Container

```bash
# Port forward to MySQL container
kubectl port-forward <pod-name> 3306:3306 -c mysql

# Connect using MySQL client
mysql -h 127.0.0.1 -P 3306 -u root -prootpassword
```

### Option 2: Exec into MySQL Container

```bash
# Execute MySQL client inside container
kubectl exec -it <pod-name> -c mysql -- mysql -uroot -prootpassword

# Run SQL commands
kubectl exec -it <pod-name> -c mysql -- mysql -uroot -prootpassword -e "SHOW DATABASES;"
kubectl exec -it <pod-name> -c mysql -- mysql -uroot -prootpassword -e "USE springdb; SHOW TABLES;"
```

## Data Persistence

### Current Setup (Development)

Uses `emptyDir` volume:
```yaml
volumes:
- name: mysql-data
  emptyDir: {}
```

**Note**: Data is lost when pod is deleted!

### Production Setup (Persistent Volume)

For production, use PersistentVolumeClaim:

```yaml
volumes:
- name: mysql-data
  persistentVolumeClaim:
    claimName: mysql-pvc
```

Create PVC:
```yaml
apiVersion: v1
kind: PersistentVolumeClaim
metadata:
  name: mysql-pvc
spec:
  accessModes:
    - ReadWriteOnce
  resources:
    requests:
      storage: 10Gi
```

## Advantages of Sidecar Pattern

1. **Simplified Networking**: Containers communicate via localhost
2. **Co-location**: Application and database are always together
3. **Shared Lifecycle**: Pod lifecycle manages both containers
4. **Resource Efficiency**: Can share resources more efficiently

## Disadvantages / Considerations

1. **Scaling**: Can't scale database independently from application
2. **Data Loss Risk**: If using emptyDir, data is lost on pod deletion
3. **Not Production-Ready**: For production, use separate database pods/services
4. **Resource Limits**: Both containers compete for pod resources
5. **⚠️ CRITICAL: Data Inconsistency with Multiple Replicas**: 
   - Each pod has its own MySQL instance
   - Data created in Pod 1 is NOT visible in Pod 2
   - Requests can return inconsistent results depending on which pod handles them
   - **Solution**: Use only 1 replica OR use a shared MySQL service (see DATABASE-CONSISTENCY-GUIDE.md)

## Troubleshooting

### Spring Boot Can't Connect to MySQL

```bash
# Check MySQL is running
kubectl logs <pod-name> -c mysql

# Check Spring Boot logs for connection errors
kubectl logs <pod-name> -c spring-hello-world

# Verify MySQL is ready
kubectl exec <pod-name> -c mysql -- mysqladmin ping -h localhost -u root -prootpassword
```

### Pod Not Starting

```bash
# Check pod events
kubectl describe pod <pod-name>

# Check container status
kubectl get pod <pod-name> -o jsonpath='{.status.containerStatuses[*]}'
```

### Database Connection Issues

```bash
# Test MySQL connection from Spring Boot container
kubectl exec <pod-name> -c spring-hello-world -- nc -zv localhost 3306

# Check MySQL logs
kubectl logs <pod-name> -c mysql | grep -i error
```

## Example: Creating a Simple Entity

To test the database connection, you can create a simple entity:

```java
@Entity
@Table(name = "greetings")
public class Greeting {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String name;
    private String message;
    
    // Getters and setters
}
```

Then create a repository and controller to test database operations.

## Best Practices

1. **Use PersistentVolumeClaim** for production data
2. **Set appropriate resource limits** for both containers
3. **Use ConfigMaps/Secrets** for database credentials
4. **Monitor both containers** separately
5. **Consider separate database** for production workloads

## Comparison: Sidecar vs Separate Pod

| Aspect | Sidecar (Same Pod) | Separate Pod |
|--------|-------------------|--------------|
| **Networking** | localhost | Service DNS |
| **Scaling** | Together | Independent |
| **Data Persistence** | Shared volume | Separate storage |
| **Use Case** | Development/Testing | Production |
| **Complexity** | Simpler | More complex |
| **Resource Sharing** | Shared | Separate |

## Next Steps

1. Test the deployment
2. Create database entities and repositories
3. Test database operations
4. Consider migrating to separate database pod for production

