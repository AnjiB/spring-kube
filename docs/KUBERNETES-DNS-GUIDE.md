# Kubernetes DNS and Service Names Guide

## How Service Names Work in Kubernetes

### Service Name Definition

The service name comes from the `metadata.name` field in the Service YAML:

```yaml
apiVersion: v1
kind: Service
metadata:
  name: mysql-service    # ← This becomes the DNS name
spec:
  type: ClusterIP
  selector:
    app: mysql
  ports:
  - port: 3306
```

### Kubernetes DNS Resolution

Kubernetes has built-in DNS (CoreDNS) that automatically resolves service names to IP addresses.

#### Service Name Formats

1. **Same Namespace (Short Form)**:
   ```
   mysql-service
   ```
   - Resolves to: `mysql-service.default.svc.cluster.local`
   - Use this when your pods are in the same namespace

2. **Full DNS Name**:
   ```
   mysql-service.default.svc.cluster.local
   ```
   - Format: `<service-name>.<namespace>.svc.cluster.local`
   - Works from any namespace

3. **Different Namespace**:
   ```
   mysql-service.production.svc.cluster.local
   ```
   - Access service in a different namespace

### How It Works

```
┌─────────────────────────────────────────────────┐
│  Spring Boot Pod                                │
│                                                 │
│  JDBC URL:                                      │
│  jdbc:mysql://mysql-service:3306/springdb      │
│           └─────────┬─────────┘                │
│                     │                           │
│                     ▼                           │
│  Kubernetes DNS (CoreDNS)                       │
│  Resolves: mysql-service                        │
│  To: 10.96.xxx.xxx (ClusterIP)                 │
│                     │                           │
│                     ▼                           │
│  MySQL Service (ClusterIP)                      │
│  Routes to MySQL Pod                            │
└─────────────────────────────────────────────────┘
```

## Examples

### Example 1: Same Namespace (Default)

**Service Definition:**
```yaml
apiVersion: v1
kind: Service
metadata:
  name: mysql-service      # Service name
  namespace: default       # Namespace (default if not specified)
```

**Connection String:**
```properties
# Short form (same namespace)
spring.datasource.url=jdbc:mysql://mysql-service:3306/springdb

# Full form (works from any namespace)
spring.datasource.url=jdbc:mysql://mysql-service.default.svc.cluster.local:3306/springdb
```

### Example 2: Different Namespace

**Service in `production` namespace:**
```yaml
apiVersion: v1
kind: Service
metadata:
  name: mysql-service
  namespace: production
```

**Connection from `default` namespace:**
```properties
spring.datasource.url=jdbc:mysql://mysql-service.production.svc.cluster.local:3306/springdb
```

## Verifying Service Name

### Check Service Exists

```bash
# List all services
kubectl get services

# Output:
# NAME            TYPE        CLUSTER-IP      EXTERNAL-IP   PORT(S)    AGE
# mysql-service   ClusterIP   10.96.xxx.xxx   <none>        3306/TCP   5m
```

### Check Service Details

```bash
kubectl describe service mysql-service

# Output shows:
# Name:              mysql-service
# Namespace:         default
# Labels:            app=mysql
# Selector:          app=mysql
# Type:              ClusterIP
# IP:                10.96.xxx.xxx
# Port:              <unset>  3306/TCP
```

### Test DNS Resolution from Pod

```bash
# Get into a Spring Boot pod
kubectl exec -it <spring-boot-pod-name> -- sh

# Test DNS resolution
nslookup mysql-service

# Or
ping mysql-service

# Or test connection
nc -zv mysql-service 3306
```

## Comparison: localhost vs Service Name

### Sidecar Pattern (localhost)

```yaml
# MySQL in same pod
containers:
- name: mysql
  ports:
  - containerPort: 3306
- name: spring-boot
  env:
  - name: SPRING_DATASOURCE_URL
    value: "jdbc:mysql://localhost:3306/springdb"
    # ↑ Uses localhost because MySQL is in same pod
```

### Shared Service Pattern (Service Name)

```yaml
# MySQL in separate pod/service
containers:
- name: spring-boot
  env:
  - name: SPRING_DATASOURCE_URL
    value: "jdbc:mysql://mysql-service:3306/springdb"
    # ↑ Uses service name because MySQL is in different pod
```

## Changing the Service Name

If you want to use a different name:

1. **Change in Service YAML:**
   ```yaml
   metadata:
     name: my-custom-mysql-name  # Change this
   ```

2. **Update Connection String:**
   ```properties
   spring.datasource.url=jdbc:mysql://my-custom-mysql-name:3306/springdb
   ```

3. **Redeploy:**
   ```bash
   kubectl apply -f kube/mysql-service.yml
   ```

## Common Service Name Patterns

- `mysql-service` - Descriptive and clear
- `mysql` - Short but might conflict
- `database-service` - Generic
- `spring-db` - Application-specific
- `mysql-primary` - For primary database
- `mysql-replica` - For read replicas

## Troubleshooting

### Service Not Found

```bash
# Check if service exists
kubectl get service mysql-service

# Check service endpoints (shows which pods it routes to)
kubectl get endpoints mysql-service

# Check DNS resolution from pod
kubectl exec <pod-name> -- nslookup mysql-service
```

### Connection Refused

```bash
# Check if MySQL pod is running
kubectl get pods -l app=mysql

# Check MySQL pod logs
kubectl logs <mysql-pod-name>

# Test connection from Spring Boot pod
kubectl exec <spring-boot-pod> -- nc -zv mysql-service 3306
```

### Wrong Namespace

```bash
# Check which namespace service is in
kubectl get service mysql-service --all-namespaces

# Use full DNS name if in different namespace
mysql-service.<namespace>.svc.cluster.local
```

## Summary

- **Service name** = `metadata.name` in Service YAML
- **Kubernetes DNS** automatically resolves service names
- **Same namespace**: Use short name `mysql-service`
- **Different namespace**: Use full name `mysql-service.namespace.svc.cluster.local`
- **localhost** = Same pod (sidecar)
- **Service name** = Different pod (shared service)

