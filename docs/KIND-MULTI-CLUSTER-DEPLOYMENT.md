# Kind Multi-Cluster Deployment Guide

This guide demonstrates how to deploy the Spring Boot application to multiple Kind clusters (dev and test) to mimic a typical software company's development and testing workflow.

## Prerequisites

- Docker installed and running
- Kind (Kubernetes in Docker) installed
- kubectl installed
- Docker image built: `anji-spring-hello-world:anjibabu`

## Overview

We'll set up two Kind clusters:
- **dev cluster**: Development environment
- **test cluster**: Testing environment

Each cluster will run independently with its own MySQL sidecar containers.

## Important: Kind vs Minikube Port Access

**Key Difference:**
- **Minikube**: NodePort services are automatically accessible via `minikube service <service-name> --url`
- **Kind**: NodePort services run inside Docker containers and are **NOT automatically accessible** from your host machine

**To access NodePort in Kind, you have two options:**

1. **Create clusters WITH port mapping** (recommended - similar to Minikube)
   - Map NodePort to a host port when creating the cluster
   - Access via `http://localhost:<host-port>`

2. **Use port-forward** (works without port mapping)
   - Use `kubectl port-forward` to forward the service port
   - Access via `http://localhost:<forwarded-port>`

This guide shows both methods, but **port mapping is recommended** for a Minikube-like experience.

## Quick Access Reference

After deployment, access your applications:

**If clusters were created WITH port mapping:**

**Dev Cluster (via localhost:30080):**
```bash
# Access directly via localhost (port mapped to host)
curl http://localhost:30080/hi
open http://localhost:30080/swagger-ui.html
```

**Test Cluster (via localhost:30081):**
```bash
# Access directly via localhost (port mapped to host)
curl http://localhost:30081/hi
open http://localhost:30081/swagger-ui.html
```

**If clusters were created WITHOUT port mapping:**

Use port-forward instead (see Step 5 for details).

## Step 1: Create Kind Clusters with Port Mapping

**Important**: Kind clusters run in Docker containers. To access NodePort services from your local machine, you need to map the ports when creating the cluster (similar to Minikube's port mapping).

### Create Dev Cluster with Port Mapping

```bash
# Create dev cluster with NodePort 30080 mapped to host port 30080
cat <<EOF | kind create cluster --name dev --config=-
kind: Cluster
apiVersion: kind.x-k8s.io/v1alpha4
nodes:
- role: control-plane
  extraPortMappings:
  - containerPort: 30080
    hostPort: 30080
    protocol: TCP
EOF
```

### Create Test Cluster with Port Mapping

```bash
# Create test cluster with NodePort 30080 mapped to host port 30081
cat <<EOF | kind create cluster --name test --config=-
kind: Cluster
apiVersion: kind.x-k8s.io/v1alpha4
nodes:
- role: control-plane
  extraPortMappings:
  - containerPort: 30080
    hostPort: 30081
    protocol: TCP
EOF
```

**Note**: If you already created clusters without port mapping, you'll need to delete and recreate them:

```bash
# Delete existing clusters
kind delete cluster --name dev
kind delete cluster --name test

# Then create them again with port mapping (commands above)
```

### Alternative: Create Clusters Without Port Mapping (Use Port-Forward Instead)

If you prefer not to use port mapping, you can create clusters normally and use `kubectl port-forward`:

```bash
# Create clusters without port mapping
kind create cluster --name dev
kind create cluster --name test
```

Then use port-forward to access (see Step 5).

### Verify Clusters

```bash
# List all Kind clusters
kind get clusters

# Should show:
# dev
# test
```

## Step 2: Configure kubectl Contexts

### View All Contexts

```bash
kubectl config get-contexts
```

You should see:
- `kind-dev` - Dev cluster context
- `kind-test` - Test cluster context

### Set Default Context (Optional)

```bash
# Set dev as default
kubectl config use-context kind-dev

# Or set test as default
kubectl config use-context kind-test
```

## Step 3: Build and Load Docker Image

### Build Docker Image

```bash
# Build the application
mvn clean package

# Build Docker image
docker build -t anji-spring-hello-world:anjibabu .
```

### Load Image to Dev Cluster

```bash
kind load docker-image anji-spring-hello-world:anjibabu --name dev
```

### Load Image to Test Cluster

```bash
kind load docker-image anji-spring-hello-world:anjibabu --name test
```

### Verify Images Loaded

```bash
# Check images in dev cluster
docker exec -it dev-control-plane crictl images | grep anji-spring-hello-world

# Check images in test cluster
docker exec -it test-control-plane crictl images | grep anji-spring-hello-world
```

## Step 4: Deploy to Dev Cluster

### Switch to Dev Context

```bash
kubectl config use-context kind-dev
```

### Verify You're in Dev Cluster

```bash
kubectl cluster-info
# Should show: Kubernetes control plane is running at https://127.0.0.1:xxxxx (dev cluster)
```

### Deploy Application

```bash
kubectl apply -f kube/spring-boot-with-mysql-sidecar.yml
```

### Verify Deployment

```bash
# Check deployment status
kubectl get deployment spring-hello-world

# Check pods
kubectl get pods -l app=spring-hello-world

# Check service
kubectl get service spring-nodeport

# View pod logs
kubectl logs -l app=spring-hello-world -c spring-hello-world --tail=20
```

### Access Dev Application

**Option 1: Using NodePort (If cluster created WITH port mapping)**

If you created the cluster with port mapping (Step 1), access directly via localhost:

```bash
# Access via localhost (port 30080 mapped to host)
curl http://localhost:30080/hi
open http://localhost:30080/swagger-ui.html
```

**Check if port is mapped:**
```bash
# Check Docker port mapping
docker port dev-control-plane | grep 30080

# If you see output like "0.0.0.0:30080->30080/tcp", port is mapped
# If no output, port is not mapped - use port-forward instead
```

**If port is NOT mapped**, you can check the container IP (but this may not work from host):
```bash
# Get the node IP (Kind cluster node is a Docker container)
NODE_IP=$(docker inspect dev-control-plane --format='{{range .NetworkSettings.Networks}}{{.IPAddress}}{{end}}')
NODEPORT=$(kubectl get service spring-nodeport -o jsonpath='{.spec.ports[0].nodePort}' --context=kind-dev)

# Note: This may not work from your host machine if port is not mapped
echo "Container IP: $NODE_IP, NodePort: $NODEPORT"
echo "Try: http://$NODE_IP:$NODEPORT/swagger-ui.html"
```

**Option 2: Port Forward (Recommended for Development)**

```bash
# Port forward to dev service
kubectl port-forward service/spring-nodeport 8080:8080

# Access in browser
open http://localhost:8080/swagger-ui.html
```

**Option 3: Using kubectl proxy**

```bash
# Start proxy
kubectl proxy

# Access via proxy
open http://localhost:8001/api/v1/namespaces/default/services/spring-nodeport/proxy/swagger-ui.html
```

## Step 5: Deploy to Test Cluster

### Switch to Test Context

```bash
kubectl config use-context kind-test
```

### Verify You're in Test Cluster

```bash
kubectl cluster-info
# Should show: Kubernetes control plane is running at https://127.0.0.1:xxxxx (test cluster)
```

### Deploy Application

```bash
kubectl apply -f kube/spring-boot-with-mysql-sidecar.yml
```

### Verify Deployment

```bash
# Check deployment status
kubectl get deployment spring-hello-world

# Check pods
kubectl get pods -l app=spring-hello-world

# Check service
kubectl get service spring-nodeport

# View pod logs
kubectl logs -l app=spring-hello-world -c spring-hello-world --tail=20
```

### Access Test Application

**Option 1: Using NodePort (If cluster created WITH port mapping)**

If you created the cluster with port mapping (Step 1), access directly via localhost:

```bash
# Access via localhost (port 30080 mapped to host port 30081)
curl http://localhost:30081/hi
open http://localhost:30081/swagger-ui.html
```

**Check if port is mapped:**
```bash
# Check Docker port mapping
docker port test-control-plane | grep 30080

# If you see output like "0.0.0.0:30081->30080/tcp", port is mapped
# If no output, port is not mapped - use port-forward instead
```

**Option 2: Port Forward (Use Different Port)**

```bash
# Port forward to test service (use different port to avoid conflict)
kubectl port-forward service/spring-nodeport 8081:8080

# Access in browser
open http://localhost:8081/swagger-ui.html
```

**Option 3: Using kubectl proxy (Different Terminal)**

```bash
# In a new terminal, start proxy for test cluster
kubectl proxy --port=8002

# Access via proxy
open http://localhost:8002/api/v1/namespaces/default/services/spring-nodeport/proxy/swagger-ui.html
```

## Accessing via NodePort in Kind Clusters

### Understanding Kind NodePort Access

**Important**: Kind clusters run in Docker containers. Unlike Minikube, NodePort services are **NOT automatically accessible** from your host machine. You have two options:

1. **Create clusters WITH port mapping** (recommended) - Similar to `minikube service --url`
2. **Use port-forward** - Works without port mapping

### Method 1: Create Clusters WITH Port Mapping (Recommended)

This is similar to Minikube's port mapping. When you create the cluster, map the NodePort to a host port:

**Dev Cluster:**
```bash
# NodePort 30080 -> Host port 30080
cat <<EOF | kind create cluster --name dev --config=-
kind: Cluster
apiVersion: kind.x-k8s.io/v1alpha4
nodes:
- role: control-plane
  extraPortMappings:
  - containerPort: 30080
    hostPort: 30080
    protocol: TCP
EOF
```

**Test Cluster:**
```bash
# NodePort 30080 -> Host port 30081
cat <<EOF | kind create cluster --name test --config=-
kind: Cluster
apiVersion: kind.x-k8s.io/v1alpha4
nodes:
- role: control-plane
  extraPortMappings:
  - containerPort: 30080
    hostPort: 30081
    protocol: TCP
EOF
```

**Then access via localhost:**
```bash
# Dev cluster
curl http://localhost:30080/hi
open http://localhost:30080/swagger-ui.html

# Test cluster
curl http://localhost:30081/hi
open http://localhost:30081/swagger-ui.html
```

**Verify port mapping:**
```bash
# Check if ports are mapped
docker port dev-control-plane | grep 30080
docker port test-control-plane | grep 30080
```

### Method 2: Check Existing Port Mappings

If your clusters were created WITHOUT port mapping, check if any ports are mapped:

```bash
# Check dev cluster port mappings
docker port dev-control-plane

# Check test cluster port mappings
docker port test-control-plane
```

If you see port 30080 mapped, use that host port. If not, you need to either:
1. Recreate clusters with port mapping (see Method 1 above)
2. Use port-forward (see Step 5)

**To recreate clusters with port mapping:**

```bash
# Delete existing clusters
kind delete cluster --name dev
kind delete cluster --name test

# Create dev cluster with port mapping (NodePort 30080 -> Host 30080)
cat <<EOF | kind create cluster --name dev --config=-
kind: Cluster
apiVersion: kind.x-k8s.io/v1alpha4
nodes:
- role: control-plane
  extraPortMappings:
  - containerPort: 30080
    hostPort: 30080
    protocol: TCP
EOF

# Create test cluster with port mapping (NodePort 30080 -> Host 30081)
cat <<EOF | kind create cluster --name test --config=-
kind: Cluster
apiVersion: kind.x-k8s.io/v1alpha4
nodes:
- role: control-plane
  extraPortMappings:
  - containerPort: 30080
    hostPort: 30081
    protocol: TCP
EOF
```

**Note**: Both clusters use NodePort 30080 (as defined in your service YAML), but they map to different host ports (30080 for dev, 30081 for test) to avoid conflicts.

After recreating, access at:
- Dev: `http://localhost:30080`
- Test: `http://localhost:30081`

### Quick Reference: Check Access Method

**Check if port mapping exists:**
```bash
# Check dev cluster
docker port dev-control-plane | grep 30080
# If output shows mapping like "0.0.0.0:30080->30080/tcp", use: http://localhost:30080
# If no output, use port-forward instead

# Check test cluster
docker port test-control-plane | grep 30080
# If output shows mapping, use the host port shown
# If no output, use port-forward instead
```

**Using Helper Script:**
```bash
./get-cluster-urls.sh
```

**If port mapping exists, access via:**
- Dev: `http://localhost:30080` (or the mapped host port)
- Test: `http://localhost:30081` (or the mapped host port)

**If port mapping does NOT exist, use port-forward:**
- Dev: `kubectl port-forward service/spring-nodeport 8080:8080 --context=kind-dev`
- Test: `kubectl port-forward service/spring-nodeport 8081:8080 --context=kind-test`

### Complete Access Commands

**Dev Cluster via NodePort:**
```bash
DEV_IP=$(docker inspect dev-control-plane --format='{{range .NetworkSettings.Networks}}{{.IPAddress}}{{end}}')
DEV_PORT=$(kubectl get service spring-nodeport -o jsonpath='{.spec.ports[0].nodePort}' --context=kind-dev)
echo "Access Dev at: http://$DEV_IP:$DEV_PORT/swagger-ui.html"
```

**Test Cluster via NodePort:**
```bash
TEST_IP=$(docker inspect test-control-plane --format='{{range .NetworkSettings.Networks}}{{.IPAddress}}{{end}}')
TEST_PORT=$(kubectl get service spring-nodeport -o jsonpath='{.spec.ports[0].nodePort}' --context=kind-test)
echo "Access Test at: http://$TEST_IP:$TEST_PORT/swagger-ui.html"
```

## Step 6: Managing Both Clusters

### Quick Context Switching

```bash
# Switch to dev
kubectl config use-context kind-dev

# Switch to test
kubectl config use-context kind-test
```

### Run Commands in Specific Cluster

```bash
# Get pods in dev cluster (without switching context)
kubectl get pods --context=kind-dev

# Get pods in test cluster
kubectl get pods --context=kind-test

# Deploy to dev cluster
kubectl apply -f kube/spring-boot-with-mysql-sidecar.yml --context=kind-dev

# Deploy to test cluster
kubectl apply -f kube/spring-boot-with-mysql-sidecar.yml --context=kind-test
```

### View Resources in Both Clusters

```bash
# Dev cluster
echo "=== DEV CLUSTER ==="
kubectl get all --context=kind-dev

# Test cluster
echo "=== TEST CLUSTER ==="
kubectl get all --context=kind-test
```

## Step 7: Typical Dev/Test Workflow

### Scenario: Deploy New Version to Dev First, Then Test

```bash
# 1. Make code changes
# ... edit your code ...

# 2. Rebuild application
mvn clean package

# 3. Rebuild Docker image with new tag
docker build -t anji-spring-hello-world:anjibabu-v2 .

# 4. Load new image to dev cluster
kind load docker-image anji-spring-hello-world:anjibabu-v2 --name dev

# 5. Update deployment YAML (change image tag)
# Edit kube/spring-boot-with-mysql-sidecar.yml:
#   image: anji-spring-hello-world:anjibabu-v2

# 6. Deploy to dev cluster
kubectl apply -f kube/spring-boot-with-mysql-sidecar.yml --context=kind-dev

# 7. Test in dev cluster
kubectl port-forward service/spring-nodeport 8080:8080 --context=kind-dev
# Test at http://localhost:8080

# 8. If dev tests pass, deploy to test cluster
kind load docker-image anji-spring-hello-world:anjibabu-v2 --name test
kubectl apply -f kube/spring-boot-with-mysql-sidecar.yml --context=kind-test

# 9. Test in test cluster
kubectl port-forward service/spring-nodeport 8081:8080 --context=kind-test
# Test at http://localhost:8081
```

## Step 8: Verify Data Isolation

Since each cluster has its own MySQL sidecar, data is completely isolated:

### Test Data Isolation

**Using NodePort (Direct Access):**

```bash
# Get node IPs and NodePorts
DEV_IP=$(docker inspect dev-control-plane --format='{{range .NetworkSettings.Networks}}{{.IPAddress}}{{end}}')
DEV_PORT=$(kubectl get service spring-nodeport -o jsonpath='{.spec.ports[0].nodePort}' --context=kind-dev)
TEST_IP=$(docker inspect test-control-plane --format='{{range .NetworkSettings.Networks}}{{.IPAddress}}{{end}}')
TEST_PORT=$(kubectl get service spring-nodeport -o jsonpath='{.spec.ports[0].nodePort}' --context=kind-test)

# Create a book in dev cluster
curl -X POST http://$DEV_IP:$DEV_PORT/api/books \
  -H "Content-Type: application/json" \
  -d '{"bookId": "DEV001", "bookName": "Dev Book", "authorName": "Dev Author"}'

# Create a book in test cluster
curl -X POST http://$TEST_IP:$TEST_PORT/api/books \
  -H "Content-Type: application/json" \
  -d '{"bookId": "TEST001", "bookName": "Test Book", "authorName": "Test Author"}'

# Verify they're separate
curl http://$DEV_IP:$DEV_PORT/api/books   # Dev cluster - should only show DEV001
curl http://$TEST_IP:$TEST_PORT/api/books # Test cluster - should only show TEST001
```

**Using Port Forward:**

```bash
# Create a book in dev cluster
kubectl port-forward service/spring-nodeport 8080:8080 --context=kind-dev &
curl -X POST http://localhost:8080/api/books \
  -H "Content-Type: application/json" \
  -d '{"bookId": "DEV001", "bookName": "Dev Book", "authorName": "Dev Author"}'

# Create a book in test cluster
kubectl port-forward service/spring-nodeport 8081:8080 --context=kind-test &
curl -X POST http://localhost:8081/api/books \
  -H "Content-Type: application/json" \
  -d '{"bookId": "TEST001", "bookName": "Test Book", "authorName": "Test Author"}'

# Verify they're separate
curl http://localhost:8080/api/books  # Dev cluster - should only show DEV001
curl http://localhost:8081/api/books  # Test cluster - should only show TEST001
```

## Step 9: Monitor Live Traffic with Kubeshark

Kubeshark is a powerful tool for monitoring and debugging Kubernetes traffic in real-time. You can use it to monitor traffic in both dev and test clusters simultaneously.

### Prerequisites

Install Kubeshark if you haven't already:
```bash
# Install Kubeshark (macOS)
brew install kubeshark

# Or download from: https://github.com/kubeshark/kubeshark/releases
```

### Monitor Dev Cluster

```bash
# Switch to dev cluster context
kubectl config use-context kind-dev

# Start Kubeshark for dev cluster
kubeshark tap

# UI opens on: http://127.0.0.1:8899
```

### Monitor Test Cluster

In a **separate terminal**, run:

```bash
# Switch to test cluster context
kubectl config use-context kind-test

# Start Kubeshark for test cluster (use different port to avoid conflict)
kubeshark tap --proxy-front-port 8898

# UI opens on: http://127.0.0.1:8898
```

### Access Kubeshark UIs

- **Dev Cluster**: http://127.0.0.1:8899
- **Test Cluster**: http://127.0.0.1:8898

### Tips

- **Keep contexts separate**: Each Kubeshark instance must run with the correct cluster context
- **Use different ports**: Always specify `--proxy-front-port` for the second cluster to avoid port conflicts
- **Monitor both simultaneously**: Open both UIs in different browser tabs to compare traffic
- **Filter traffic**: Use Kubeshark's filtering features to focus on specific services or namespaces

### Example: Monitor Traffic While Testing

```bash
# Terminal 1: Start Kubeshark for dev
kubectl config use-context kind-dev
kubeshark tap

# Terminal 2: Start Kubeshark for test
kubectl config use-context kind-test
kubeshark tap --proxy-front-port 8898

# Terminal 3: Generate traffic
# Dev cluster
curl http://localhost:30080/hi
curl http://localhost:30080/api/books

# Test cluster
curl http://localhost:30081/hi
curl http://localhost:30081/api/books

# Watch the traffic in both Kubeshark UIs
```

## Useful Commands

### Cluster Management

```bash
# List all Kind clusters
kind get clusters

# Delete a cluster
kind delete cluster --name dev
kind delete cluster --name test

# Get cluster info
kind get kubeconfig --name dev
kind get kubeconfig --name test
```

### Context Management

```bash
# List all contexts
kubectl config get-contexts

# View current context
kubectl config current-context

# Switch context
kubectl config use-context kind-dev
kubectl config use-context kind-test

# Set context alias (add to ~/.zshrc or ~/.bashrc)
alias kdev='kubectl --context=kind-dev'
alias ktest='kubectl --context=kind-test'

# Then use:
kdev get pods
ktest get pods
```

### Deployment Management

```bash
# Deploy to specific cluster
kubectl apply -f kube/spring-boot-with-mysql-sidecar.yml --context=kind-dev
kubectl apply -f kube/spring-boot-with-mysql-sidecar.yml --context=kind-test

# Check status in both clusters
kubectl get pods --context=kind-dev
kubectl get pods --context=kind-test

# View logs from specific cluster
kubectl logs -l app=spring-hello-world -c spring-hello-world --context=kind-dev --tail=20
kubectl logs -l app=spring-hello-world -c spring-hello-world --context=kind-test --tail=20

# Delete from specific cluster
kubectl delete -f kube/spring-boot-with-mysql-sidecar.yml --context=kind-dev
kubectl delete -f kube/spring-boot-with-mysql-sidecar.yml --context=kind-test
```

### Image Management

```bash
# Load image to specific cluster
kind load docker-image anji-spring-hello-world:anjibabu --name dev
kind load docker-image anji-spring-hello-world:anjibabu --name test

# List images in cluster
docker exec -it dev-control-plane crictl images
docker exec -it test-control-plane crictl images
```

## Complete Deployment Script

A deployment script is available in the root directory: `deploy-to-clusters.sh`

### Using the Script

```bash
# Make sure it's executable
chmod +x deploy-to-clusters.sh

# Run the script
./deploy-to-clusters.sh
```

The script will:
1. Build the application
2. Build Docker image
3. Load image to both clusters
4. Deploy to dev cluster
5. Deploy to test cluster
6. Wait for pods to be ready
7. Show deployment status

### Manual Deployment

If you prefer to deploy manually, follow the steps in this guide.

## Troubleshooting

### Image Not Found

```bash
# Verify image is loaded
docker exec -it dev-control-plane crictl images | grep anji-spring-hello-world

# Reload if needed
kind load docker-image anji-spring-hello-world:anjibabu --name dev
```

### Wrong Cluster

```bash
# Always verify current context
kubectl config current-context

# Check cluster info
kubectl cluster-info
```

### Port Conflicts

```bash
# Use different ports for each cluster
kubectl port-forward service/spring-nodeport 8080:8080 --context=kind-dev
kubectl port-forward service/spring-nodeport 8081:8080 --context=kind-test
```

### Pod Not Starting

```bash
# Check pod status
kubectl get pods --context=kind-dev
kubectl describe pod <pod-name> --context=kind-dev

# Check logs
kubectl logs <pod-name> -c spring-hello-world --context=kind-dev
kubectl logs <pod-name> -c mysql --context=kind-dev
```

## Best Practices

1. **Always verify context** before deploying:
   ```bash
   kubectl config current-context
   ```

2. **Use explicit context** for critical operations:
   ```bash
   kubectl apply -f file.yml --context=kind-dev
   ```

3. **Test in dev first**, then promote to test

4. **Use different ports** when port-forwarding multiple clusters

5. **Tag images** with version numbers for tracking

6. **Document deployments** - keep track of what's deployed where

## Summary

You now have:
- ✅ Two independent Kind clusters (dev and test)
- ✅ Application deployed to both clusters
- ✅ Complete data isolation between environments
- ✅ Ability to deploy and test independently
- ✅ Mimics real dev/test environment workflow

This setup allows you to:
- Test changes in dev before promoting to test
- Verify deployments work in both environments
- Practice multi-cluster management
- Understand context switching and cluster isolation

