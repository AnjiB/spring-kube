# Database Consistency with Multiple Pods

## The Problem: Sidecar MySQL Pattern with Multiple Replicas

### Issue

When you deploy multiple replicas (pods) with MySQL as a sidecar container, **each pod has its own independent MySQL database**. This leads to data inconsistency:

```
┌─────────────────────────────────────────────────────────┐
│                    Kubernetes Cluster                    │
│                                                          │
│  ┌──────────────────┐         ┌──────────────────┐     │
│  │   Pod 1          │         │   Pod 2          │     │
│  │  ┌────────────┐  │         │  ┌────────────┐  │     │
│  │  │ Spring Boot│  │         │  │ Spring Boot│  │     │
│  │  └────────────┘  │         │  └────────────┘  │     │
│  │  ┌────────────┐  │         │  ┌────────────┐  │     │
│  │  │ MySQL      │  │         │  │ MySQL      │  │     │
│  │  │ (Database1)│  │         │  │ (Database2)│  │     │
│  │  └────────────┘  │         │  └────────────┘  │     │
│  └──────────────────┘         └──────────────────┘     │
│         │                            │                  │
│         └────────────┬───────────────┘                  │
│                      │                                   │
│              ┌───────▼────────┐                         │
│              │  NodePort      │                         │
│              │  Service       │                         │
│              └────────────────┘                         │
└─────────────────────────────────────────────────────────┘
```

### What Happens

1. **Request 1**: Create book "B001" → Routes to Pod 1 → Stored in MySQL in Pod 1
2. **Request 2**: Get book "B001" → Routes to Pod 2 → **NOT FOUND** (Pod 2 has different MySQL)
3. **Request 3**: Get book "B001" → Routes to Pod 1 → **FOUND** (Pod 1 has the data)

**Result**: Inconsistent data depending on which pod handles the request!

## Why This Happens

- Each pod has its own MySQL container
- Each MySQL container has its own database files (in `emptyDir` volume)
- No data sharing between pods
- Load balancer distributes requests randomly across pods

## Solutions

### Solution 1: Shared Database Service (Recommended for Production)

Use a **separate MySQL pod/service** that all application pods connect to:

```
┌─────────────────────────────────────────────────────────┐
│                    Kubernetes Cluster                    │
│                                                          │
│  ┌──────────┐  ┌──────────┐                            │
│  │ Pod 1    │  │ Pod 2    │                            │
│  │ Spring   │  │ Spring   │                            │
│  │ Boot     │  │ Boot     │                            │
│  └────┬─────┘  └────┬─────┘                            │
│       │             │                                   │
│       └──────┬──────┘                                   │
│              │                                          │
│       ┌──────▼──────────┐                              │
│       │ MySQL Service   │                              │
│       │ (ClusterIP)     │                              │
│       └──────┬──────────┘                              │
│              │                                          │
│       ┌──────▼──────────┐                              │
│       │ MySQL Pod       │                              │
│       │ (Single Instance│                              │
│       │  or Replicated) │                              │
│       └─────────────────┘                              │
└─────────────────────────────────────────────────────────┘
```

**Benefits:**
- ✅ All pods share the same database
- ✅ Consistent data across all pods
- ✅ Can scale application pods independently
- ✅ Database can be managed separately

**Implementation:**
See `kube/mysql-service.yml` and `kube/spring-boot-with-shared-mysql.yml` examples below.

### Solution 2: Single Replica (Development Only)

Use only **1 replica** for development/testing:

```yaml
spec:
  replicas: 1  # Only one pod = one MySQL = consistent
```

**Limitations:**
- ❌ No high availability
- ❌ No load distribution
- ❌ Not suitable for production

### Solution 3: StatefulSet with Shared Volume (Complex)

Use StatefulSet with a shared PersistentVolume, but this is complex and not recommended for MySQL sidecar pattern.

### Solution 4: Managed Database Service

Use a managed database service (AWS RDS, Google Cloud SQL, Azure Database):
- ✅ Fully managed
- ✅ High availability
- ✅ Automatic backups
- ✅ Scalable

## Implementation: Shared MySQL Service

### Step 1: Create MySQL Deployment and Service

Create `kube/mysql-service.yml`:

```yaml
---
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
---
apiVersion: apps/v1
kind: Deployment
metadata:
  name: mysql
  labels:
    app: mysql
spec:
  replicas: 1
  selector:
    matchLabels:
      app: mysql
  template:
    metadata:
      labels:
        app: mysql
    spec:
      containers:
      - name: mysql
        image: mysql:8.0
        ports:
        - containerPort: 3306
        env:
        - name: MYSQL_ROOT_PASSWORD
          value: "rootpassword"
        - name: MYSQL_DATABASE
          value: "springdb"
        - name: MYSQL_USER
          value: "springuser"
        - name: MYSQL_PASSWORD
          value: "springpassword"
        volumeMounts:
        - name: mysql-data
          mountPath: /var/lib/mysql
        resources:
          requests:
            memory: "512Mi"
            cpu: "500m"
          limits:
            memory: "1Gi"
            cpu: "1000m"
      volumes:
      - name: mysql-data
        persistentVolumeClaim:
          claimName: mysql-pvc
---
apiVersion: v1
kind: Service
metadata:
  name: mysql-service
spec:
  type: ClusterIP
  selector:
    app: mysql
  ports:
  - protocol: TCP
    port: 3306
    targetPort: 3306
```

### Step 2: Update Spring Boot Deployment

Create `kube/spring-boot-with-shared-mysql.yml`:

```yaml
---
apiVersion: apps/v1
kind: Deployment
metadata:
  name: spring-hello-world
  labels:
    app: spring-hello-world
spec:
  replicas: 2  # Can scale to any number
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
        env:
        # Connect to shared MySQL service
        - name: SPRING_DATASOURCE_URL
          value: "jdbc:mysql://mysql-service:3306/springdb?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true"
        - name: SPRING_DATASOURCE_USERNAME
          value: "root"
        - name: SPRING_DATASOURCE_PASSWORD
          value: "rootpassword"
        startupProbe:
          httpGet:
            path: /actuator/health
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 5
          failureThreshold: 30
        livenessProbe:
          httpGet:
            path: /actuator/health
            port: 8080
          initialDelaySeconds: 60
          periodSeconds: 10
        readinessProbe:
          httpGet:
            path: /actuator/health
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 5
        resources:
          requests:
            memory: "256Mi"
            cpu: "250m"
          limits:
            memory: "512Mi"
            cpu: "500m"
---
apiVersion: v1
kind: Service
metadata:
  name: spring-nodeport
  labels:
    app: spring-hello-world
spec:
  type: NodePort
  selector:
    app: spring-hello-world
  ports:
  - protocol: TCP
    port: 8080
    targetPort: 8080
    nodePort: 30080
```

### Step 3: Update application.properties

For shared MySQL, use the service name:

```properties
# Use MySQL service name (Kubernetes DNS)
spring.datasource.url=jdbc:mysql://mysql-service:3306/springdb?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true
```

### Step 4: Deploy

```bash
# 1. Deploy MySQL service
kubectl apply -f kube/mysql-service.yml

# 2. Wait for MySQL to be ready
kubectl wait --for=condition=ready pod -l app=mysql --timeout=120s

# 3. Deploy Spring Boot with shared MySQL
kubectl apply -f kube/spring-boot-with-shared-mysql.yml

# 4. Verify
kubectl get pods
kubectl get services
```

## Comparison: Sidecar vs Shared Service

| Aspect | Sidecar MySQL | Shared MySQL Service |
|--------|---------------|---------------------|
| **Data Consistency** | ❌ Inconsistent across pods | ✅ Consistent |
| **Scalability** | ❌ Can't scale independently | ✅ Scale app pods independently |
| **Use Case** | Development/Testing | Production |
| **Complexity** | Simple | Moderate |
| **Resource Usage** | Higher (MySQL per pod) | Lower (One MySQL) |
| **High Availability** | ❌ No | ✅ Yes (with replication) |

## Testing Data Consistency

### Test with Sidecar (Inconsistent):

```bash
# Create book in Pod 1
curl -X POST http://localhost:30080/api/books \
  -H "Content-Type: application/json" \
  -d '{"bookId": "B001", "bookName": "Test", "authorName": "Author"}'

# Try to get it - might fail if routed to Pod 2
curl http://localhost:30080/api/books/B001
# Result: Sometimes found, sometimes not found ❌
```

### Test with Shared Service (Consistent):

```bash
# Create book
curl -X POST http://localhost:30080/api/books \
  -H "Content-Type: application/json" \
  -d '{"bookId": "B001", "bookName": "Test", "authorName": "Author"}'

# Get it - always works regardless of pod
curl http://localhost:30080/api/books/B001
# Result: Always found ✅
```

## Best Practices

1. **Development/Testing**: Sidecar pattern is fine with 1 replica
2. **Production**: Always use shared database service
3. **High Availability**: Use MySQL replication or managed service
4. **Backups**: Configure regular backups for shared database
5. **Monitoring**: Monitor database performance separately

## Summary

**Your observation is 100% correct!** 

- Sidecar MySQL with multiple replicas = **Data inconsistency** ❌
- Shared MySQL service = **Data consistency** ✅

For production, always use a shared database service, not sidecar containers.

