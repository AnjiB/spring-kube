# Managing Multiple Clusters and Services Guide

## 1. Managing Multiple Clusters (Dev, Test, Prod)

### Understanding kubectl Contexts

Kubectl uses **contexts** to manage multiple clusters. Each context contains:
- Cluster information (API server URL, certificates)
- User authentication
- Default namespace

### Viewing and Managing Contexts

```bash
# List all contexts
kubectl config get-contexts

# Output example:
# CURRENT   NAME           CLUSTER        AUTHINFO       NAMESPACE
# *         dev-cluster    dev-cluster    dev-user       default
#           test-cluster   test-cluster   test-user      default
#           prod-cluster   prod-cluster   prod-user      production

# View current context
kubectl config current-context

# Switch to a different cluster
kubectl config use-context dev-cluster
kubectl config use-context test-cluster
kubectl config use-context prod-cluster
```

### Working with Multiple Clusters

**Option 1: Switch Context (Changes default for all commands)**
```bash
# Switch to dev
kubectl config use-context dev-cluster
kubectl get pods  # Runs in dev cluster

# Switch to test
kubectl config use-context test-cluster
kubectl get pods  # Runs in test cluster
```

**Option 2: Specify Context Per Command (Recommended)**
```bash
# Run commands in specific clusters without switching
kubectl get pods --context=dev-cluster
kubectl get pods --context=test-cluster
kubectl get pods --context=prod-cluster

# Deploy to specific cluster
kubectl apply -f kube/spring-boot-service.yml --context=dev-cluster
kubectl apply -f kube/spring-boot-service.yml --context=test-cluster
```

**Option 3: Use Aliases (Convenience)**
```bash
# Add to ~/.zshrc or ~/.bashrc
alias kdev='kubectl --context=dev-cluster'
alias ktest='kubectl --context=test-cluster'
alias kprod='kubectl --context=prod-cluster'

# Then use:
kdev get pods
ktest apply -f kube/spring-boot-service.yml
kprod get services
```

### Setting Up New Clusters

When you add a new cluster (via cloud provider, minikube, etc.), the context is usually added automatically:

```bash
# Minikube example
minikube start -p dev-cluster
minikube start -p test-cluster

# Cloud provider (example for GKE)
gcloud container clusters get-credentials dev-cluster --zone us-central1-a
gcloud container clusters get-credentials test-cluster --zone us-central1-a
```

### Best Practices for Multiple Clusters

1. **Use descriptive context names**: `dev-cluster`, `test-cluster`, `prod-cluster`
2. **Always verify context before destructive operations**:
   ```bash
   kubectl config current-context
   kubectl get nodes  # Verify you're in the right cluster
   ```
3. **Use namespaces for organization**:
   ```bash
   kubectl create namespace dev
   kubectl create namespace test
   kubectl create namespace prod
   ```
4. **Set default namespace per context**:
   ```bash
   kubectl config set-context dev-cluster --namespace=dev
   kubectl config set-context test-cluster --namespace=test
   ```

## 2. Managing Multiple Services with Different NodePorts

### NodePort Limitations

- **Port Range**: NodePorts must be between 30000-32767
- **Uniqueness**: Each NodePort must be unique within a cluster
- **Planning**: You need to track which ports are in use

### Service Port Planning

Create a port allocation document or use a naming convention:

| Service Name | Application | NodePort | Internal Port | Notes |
|-------------|-------------|----------|---------------|-------|
| spring-hello-world-service | Spring Boot Hello World | 30080 | 8080 | Main API |
| another-app-service | Another Application | 30090 | 8080 | Secondary service |
| database-service | Database | 30100 | 5432 | PostgreSQL |
| redis-service | Redis | 30101 | 6379 | Cache |

### Example: Multiple Services Configuration

```yaml
# Service 1: Spring Boot (Port 30080)
apiVersion: v1
kind: Service
metadata:
  name: spring-hello-world-service
spec:
  type: NodePort
  selector:
    app: spring-hello-world
  ports:
    - port: 8080
      targetPort: 8080
      nodePort: 30080

---
# Service 2: Another App (Port 30090)
apiVersion: v1
kind: Service
metadata:
  name: another-app-service
spec:
  type: NodePort
  selector:
    app: another-app
  ports:
    - port: 8080
      targetPort: 8080
      nodePort: 30090
```

### Accessing Multiple Services

**Using minikube service (Docker driver):**
```bash
# Get URL for service 1
minikube service spring-hello-world-service --url
# Output: http://127.0.0.1:58645

# Get URL for service 2
minikube service another-app-service --url
# Output: http://127.0.0.1:58646
```

**Using kubectl proxy (Recommended for multiple services):**
```bash
# Start proxy once
kubectl proxy

# Access any service using service name (no port conflicts!)
curl http://localhost:8001/api/v1/namespaces/default/services/spring-hello-world-service/proxy/hi
curl http://localhost:8001/api/v1/namespaces/default/services/another-app-service/proxy/api/endpoint
```

**Direct NodePort access (if using VM driver):**
```bash
# Get minikube IP
minikube ip
# Output: 192.168.49.2

# Access services
curl http://192.168.49.2:30080/hi
curl http://192.168.49.2:30090/api/endpoint
```

### Managing Services Across Clusters

**Deploy same service to multiple clusters:**
```bash
# Deploy to dev
kubectl apply -f kube/spring-boot-service.yml --context=dev-cluster

# Deploy to test
kubectl apply -f kube/spring-boot-service.yml --context=test-cluster

# Deploy to prod
kubectl apply -f kube/spring-boot-service.yml --context=prod-cluster
```

**Check services in all clusters:**
```bash
echo "=== DEV CLUSTER ==="
kubectl get services --context=dev-cluster

echo "=== TEST CLUSTER ==="
kubectl get services --context=test-cluster

echo "=== PROD CLUSTER ==="
kubectl get services --context=prod-cluster
```

### Finding Available NodePorts

```bash
# List all NodePort services in current cluster
kubectl get services -o jsonpath='{range .items[*]}{.metadata.name}{"\t"}{.spec.type}{"\t"}{.spec.ports[0].nodePort}{"\n"}{end}' | grep NodePort

# Or more readable:
kubectl get services -o wide | grep NodePort
```

### Avoiding Port Conflicts

**Option 1: Let Kubernetes assign NodePort (Recommended)**
```yaml
apiVersion: v1
kind: Service
metadata:
  name: my-service
spec:
  type: NodePort
  ports:
    - port: 8080
      targetPort: 8080
      # nodePort not specified - Kubernetes assigns automatically
```

**Option 2: Use ClusterIP + Ingress (Best for Production)**
Instead of NodePort, use ClusterIP services with an Ingress controller:
- No port conflicts
- Single entry point
- Better for production

**Option 3: Use kubectl proxy (Best for Development)**
- No port management needed
- Access services by name
- Works across all clusters

## 3. Complete Workflow Example

### Scenario: Deploy Spring Boot to Dev, Test, and Prod

```bash
# 1. Build and tag image for each environment
docker build -t spring-hello-world:dev .
docker build -t spring-hello-world:test .
docker build -t spring-hello-world:prod .

# 2. Load images into each cluster's registry/minikube
# Dev
kubectl config use-context dev-cluster
minikube image load spring-hello-world:dev -p dev-cluster

# Test
kubectl config use-context test-cluster
minikube image load spring-hello-world:test -p test-cluster

# Prod
kubectl config use-context prod-cluster
# Push to production registry instead
docker push registry.example.com/spring-hello-world:prod

# 3. Deploy to each cluster
kubectl apply -f kube/spring-boot-service.yml --context=dev-cluster
kubectl apply -f kube/spring-boot-service.yml --context=test-cluster
kubectl apply -f kube/spring-boot-service.yml --context=prod-cluster

# 4. Verify deployments
kubectl get pods --context=dev-cluster
kubectl get pods --context=test-cluster
kubectl get pods --context=prod-cluster

# 5. Access services
# Dev
minikube service spring-nodeport --url -p dev-cluster

# Test
minikube service spring-nodeport --url -p test-cluster

# Prod (using kubectl proxy)
kubectl proxy --context=prod-cluster
curl http://localhost:8001/api/v1/namespaces/default/services/spring-nodeport/proxy/hi
```

## 4. Best Practices Summary

### Multiple Clusters
- ✅ Use descriptive context names
- ✅ Always verify context before operations
- ✅ Use `--context` flag for explicit cluster targeting
- ✅ Set up aliases for convenience
- ✅ Use namespaces for organization

### Multiple Services
- ✅ Document NodePort allocations
- ✅ Consider using kubectl proxy to avoid port management
- ✅ Use ClusterIP + Ingress for production
- ✅ Let Kubernetes assign NodePorts when possible
- ✅ Use consistent service naming conventions

### Security
- ✅ Use different credentials per cluster
- ✅ Limit prod cluster access
- ✅ Use RBAC for fine-grained permissions
- ✅ Never commit kubeconfig files with credentials

