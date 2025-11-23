# Commands Reference

A comprehensive reference guide for all commands used in this project, organized by tool.

## Table of Contents

1. [Docker](#docker)
2. [Minikube](#minikube)
3. [Kind](#kind)
4. [kubectl](#kubectl)
5. [Kubeshark](#kubeshark)

---

## Docker

### Build Commands

```bash
# Build Docker image
docker build -t anji-spring-hello-world:latest .

# Build with specific tag
docker build -t anji-spring-hello-world:anjibabu .

# Build with version tag
docker build -t anji-spring-hello-world:anjibabu-v2 .
```

### Run Commands

```bash
# Run container
docker run -p 8080:8080 anji-spring-hello-world:latest

# Run in detached mode
docker run -d -p 8080:8080 anji-spring-hello-world:latest

# Run with environment variables
docker run -p 8080:8080 -e SPRING_DATASOURCE_URL=jdbc:mysql://host.docker.internal:3306/springdb anji-spring-hello-world:latest
```

### Image Management

```bash
# List all images
docker images

# Remove image
docker rmi anji-spring-hello-world:latest

# Remove image by ID
docker rmi <image-id>

# Remove all unused images
docker image prune -a
```

### Container Management

```bash
# List running containers
docker ps

# List all containers (including stopped)
docker ps -a

# Stop container
docker stop <container-id>

# Start stopped container
docker start <container-id>

# Remove container
docker rm <container-id>

# Remove all stopped containers
docker container prune
```

### Docker Compose

```bash
# Start all services
docker compose up -d

# Start specific service
docker compose up -d mysql

# View logs
docker compose logs -f

# View logs for specific service
docker compose logs -f spring-boot-app

# Stop all services
docker compose down

# Stop and remove volumes
docker compose down -v

# Rebuild and start
docker compose up -d --build
```

### Inspect and Debug

```bash
# Inspect container
docker inspect <container-id>

# Get container IP
docker inspect <container-id> --format='{{range .NetworkSettings.Networks}}{{.IPAddress}}{{end}}'

# View container logs
docker logs <container-id>

# Follow container logs
docker logs -f <container-id>

# Execute command in running container
docker exec -it <container-id> /bin/sh

# Check port mappings
docker port <container-id>

# Check port mapping for specific port
docker port <container-id> | grep 30080
```

### Docker Exec (for Kind clusters)

```bash
# List images in Kind cluster node
docker exec -it dev-control-plane crictl images

# List images with filter
docker exec -it dev-control-plane crictl images | grep anji-spring-hello-world

# Execute command in Kind cluster node
docker exec -it dev-control-plane crictl images
```

---

## Minikube

### Start and Stop

```bash
# Start Minikube
minikube start

# Start with specific profile
minikube start -p dev-cluster
minikube start -p test-cluster

# Stop Minikube
minikube stop

# Stop specific profile
minikube stop -p dev-cluster

# Delete Minikube cluster
minikube delete

# Delete specific profile
minikube delete -p dev-cluster
```

### Image Management

```bash
# Load Docker image into Minikube
minikube image load anji-spring-hello-world:latest

# Load image with specific tag
minikube image load anji-spring-hello-world:anjibabu

# List images in Minikube
minikube image ls

# Remove image from Minikube
minikube image rm anji-spring-hello-world:latest

# Remove image by name
minikube image rm <imageName>
```

### Service Access

```bash
# Get service URL (NodePort)
minikube service spring-nodeport --url

# Open service in browser
minikube service spring-nodeport

# Get service URL for specific profile
minikube service spring-nodeport --url -p dev-cluster
```

### Cluster Information

```bash
# Get Minikube status
minikube status

# Get Minikube IP
minikube ip

# Get Minikube IP for specific profile
minikube ip -p dev-cluster

# Open Minikube dashboard
minikube dashboard

# Get Minikube Kubernetes config
minikube config view
```

### SSH and Debug

```bash
# SSH into Minikube node
minikube ssh

# SSH into specific profile
minikube ssh -p dev-cluster

# View Minikube logs
minikube logs
```

---

## Kind

### Cluster Creation

**Create cluster without configuration:**
```bash
# Create basic cluster
kind create cluster

# Create cluster with name
kind create cluster --name dev
kind create cluster --name test
```

**Create cluster with port mapping (recommended):**
```bash
# Dev cluster with port mapping (NodePort 30080 -> Host 30080)
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

# Test cluster with port mapping (NodePort 30080 -> Host 30081)
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

**Create cluster from config file:**
```bash
# Create from YAML file
kind create cluster --name dev --config=kube/kind-dev-cluster-config.yml
kind create cluster --name test --config=kube/kind-test-cluster-config.yml
```

### Cluster Management

```bash
# List all Kind clusters
kind get clusters

# Delete cluster
kind delete cluster --name dev
kind delete cluster --name test

# Delete all clusters
kind delete clusters --all

# Get cluster kubeconfig
kind get kubeconfig --name dev
kind get kubeconfig --name test

# Export kubeconfig
kind get kubeconfig --name dev > ~/.kube/kind-dev-config
```

### Image Management

```bash
# Load Docker image into Kind cluster
kind load docker-image anji-spring-hello-world:anjibabu --name dev
kind load docker-image anji-spring-hello-world:anjibabu --name test

# Load image with specific tag
kind load docker-image anji-spring-hello-world:anjibabu-v2 --name dev

# Verify image is loaded
docker exec -it dev-control-plane crictl images | grep anji-spring-hello-world
docker exec -it test-control-plane crictl images | grep anji-spring-hello-world
```

### Port Mapping Verification

```bash
# Check port mappings for dev cluster
docker port dev-control-plane

# Check specific port mapping
docker port dev-control-plane | grep 30080

# Check port mappings for test cluster
docker port test-control-plane | grep 30080
```

---

## kubectl

### Context Management

```bash
# List all contexts
kubectl config get-contexts

# View current context
kubectl config current-context

# Switch context
kubectl config use-context kind-dev
kubectl config use-context kind-test
kubectl config use-context minikube

# Set context alias (add to ~/.zshrc or ~/.bashrc)
alias kdev='kubectl --context=kind-dev'
alias ktest='kubectl --context=kind-test'

# Use alias
kdev get pods
ktest get pods
```

### Cluster Information

```bash
# Get cluster info
kubectl cluster-info

# Get cluster info for specific context
kubectl cluster-info --context=kind-dev

# Get nodes
kubectl get nodes

# Get nodes for specific context
kubectl get nodes --context=kind-dev

# Describe node
kubectl describe node <node-name>
```

### Deployment Management

```bash
# Deploy from YAML file
kubectl apply -f kube/spring-boot-with-mysql-sidecar.yml

# Deploy to specific context
kubectl apply -f kube/spring-boot-with-mysql-sidecar.yml --context=kind-dev
kubectl apply -f kube/spring-boot-with-mysql-sidecar.yml --context=kind-test

# Get deployments
kubectl get deployments
kubectl get deployment spring-hello-world

# Get deployments for specific context
kubectl get deployments --context=kind-dev

# Describe deployment
kubectl describe deployment spring-hello-world

# Delete deployment
kubectl delete deployment spring-hello-world

# Delete deployment from YAML
kubectl delete -f kube/spring-boot-with-mysql-sidecar.yml

# Delete for specific context
kubectl delete -f kube/spring-boot-with-mysql-sidecar.yml --context=kind-dev

# Update deployment (after changing YAML)
kubectl apply -f kube/spring-boot-with-mysql-sidecar.yml

# Restart deployment (to pick up new image)
kubectl rollout restart deployment/spring-hello-world

# Check rollout status
kubectl rollout status deployment/spring-hello-world

# View rollout history
kubectl rollout history deployment/spring-hello-world

# Rollback to previous version
kubectl rollout undo deployment/spring-hello-world
```

### Pod Management

```bash
# Get pods
kubectl get pods

# Get pods with labels
kubectl get pods -l app=spring-hello-world

# Get pods for specific context
kubectl get pods --context=kind-dev

# Get pods in all namespaces
kubectl get pods --all-namespaces

# Watch pods (real-time updates)
kubectl get pods -w
kubectl get pods -l app=spring-hello-world -w

# Describe pod
kubectl describe pod <pod-name>

# Delete pod (deployment will recreate it)
kubectl delete pod <pod-name>

# Delete pods by label
kubectl delete pods -l app=spring-hello-world

# Force delete pod
kubectl delete pod <pod-name> --force --grace-period=0

# Execute command in pod
kubectl exec -it <pod-name> -- /bin/sh

# Execute command in specific container (multi-container pod)
kubectl exec -it <pod-name> -c spring-hello-world -- /bin/sh
kubectl exec -it <pod-name> -c mysql -- /bin/bash
```

### Service Management

```bash
# Get services
kubectl get services
kubectl get svc

# Get specific service
kubectl get service spring-nodeport

# Get services for specific context
kubectl get services --context=kind-dev

# Describe service
kubectl describe service spring-nodeport

# Get service details (JSON)
kubectl get service spring-nodeport -o json

# Get NodePort value
kubectl get service spring-nodeport -o jsonpath='{.spec.ports[0].nodePort}'

# Delete service
kubectl delete service spring-nodeport

# Delete service from YAML
kubectl delete -f kube/spring-boot-service.yml
```

### Logs

```bash
# View logs for pod
kubectl logs <pod-name>

# Follow logs (stream)
kubectl logs -f <pod-name>

# View last N lines
kubectl logs <pod-name> --tail=20

# View logs for deployment
kubectl logs -f deployment/spring-hello-world

# View logs for pods with label
kubectl logs -f -l app=spring-hello-world

# View logs for specific container (multi-container pod)
kubectl logs <pod-name> -c spring-hello-world
kubectl logs <pod-name> -c mysql

# View logs with label and container
kubectl logs -l app=spring-hello-world -c spring-hello-world --tail=20

# View logs for specific context
kubectl logs -f <pod-name> --context=kind-dev

# View logs from previous container (if crashed)
kubectl logs <pod-name> --previous
```

### Port Forwarding

```bash
# Port forward to service
kubectl port-forward service/spring-nodeport 8080:8080

# Port forward to pod
kubectl port-forward pod/<pod-name> 8080:8080

# Port forward to deployment
kubectl port-forward deployment/spring-hello-world 8080:8080

# Port forward multiple ports
kubectl port-forward pod/<pod-name> 8080:8080 3306:3306

# Port forward for specific context
kubectl port-forward service/spring-nodeport 8080:8080 --context=kind-dev
kubectl port-forward service/spring-nodeport 8081:8080 --context=kind-test

# Port forward in background
kubectl port-forward service/spring-nodeport 8080:8080 &

# Port forward to specific namespace
kubectl port-forward service/spring-nodeport 8080:8080 -n default
```

### kubectl proxy

```bash
# Start proxy (default port 8001)
kubectl proxy

# Start proxy on different port
kubectl proxy --port=8002

# Start proxy for specific context
kubectl proxy --context=kind-dev

# Access service via proxy
curl http://localhost:8001/api/v1/namespaces/default/services/spring-nodeport/proxy/hi

# Access Swagger UI via proxy
open http://localhost:8001/api/v1/namespaces/default/services/spring-nodeport/proxy/swagger-ui.html

# Access API docs via proxy
curl http://localhost:8001/api/v1/namespaces/default/services/spring-nodeport/proxy/api-docs
```

### Resource Queries

```bash
# Get all resources
kubectl get all

# Get all resources for specific context
kubectl get all --context=kind-dev

# Get resources with labels
kubectl get all -l app=spring-hello-world

# Get resources in YAML format
kubectl get deployment spring-hello-world -o yaml

# Get resources in JSON format
kubectl get deployment spring-hello-world -o json

# Get resources with custom output
kubectl get pods -o wide
kubectl get pods -o jsonpath='{.items[*].metadata.name}'
```

### Update Pod with New Changes

```bash
# Method 1: Rollout restart (recommended)
# 1. Rebuild application
mvn clean package

# 2. Rebuild Docker image
docker build -t anji-spring-hello-world:latest .

# 3. Load new image into Minikube
minikube image load anji-spring-hello-world:latest

# Or load into Kind
kind load docker-image anji-spring-hello-world:anjibabu --name dev

# 4. Restart deployment
kubectl rollout restart deployment/spring-hello-world

# 5. Check rollout status
kubectl rollout status deployment/spring-hello-world

# 6. Verify pods are running
kubectl get pods -l app=spring-hello-world

# Method 2: Delete pods (deployment will recreate)
kubectl delete pods -l app=spring-hello-world

# Watch pods being recreated
kubectl get pods -l app=spring-hello-world -w
```

### Debugging and Troubleshooting

```bash
# Get events
kubectl get events

# Get events sorted by time
kubectl get events --sort-by='.lastTimestamp'

# Describe resource (shows events and status)
kubectl describe pod <pod-name>
kubectl describe deployment spring-hello-world
kubectl describe service spring-nodeport

# Get pod status
kubectl get pods -o wide

# Check pod conditions
kubectl get pod <pod-name> -o jsonpath='{.status.conditions}'

# Check pod IP
kubectl get pod <pod-name> -o jsonpath='{.status.podIP}'

# Check node resources
kubectl top nodes

# Check pod resources
kubectl top pods
```

---

## Kubeshark

### Installation

```bash
# Install Kubeshark (macOS)
brew install kubeshark

# Or download from: https://github.com/kubeshark/kubeshark/releases
```

### Basic Usage

```bash
# Start Kubeshark (default port 8899)
kubeshark tap

# Start Kubeshark on different port
kubeshark tap --proxy-front-port 8898

# Start Kubeshark for specific context
kubectl config use-context kind-dev
kubeshark tap
```

### Multi-Cluster Monitoring

```bash
# Terminal 1: Monitor dev cluster
kubectl config use-context kind-dev
kubeshark tap
# UI opens on: http://127.0.0.1:8899

# Terminal 2: Monitor test cluster
kubectl config use-context kind-test
kubeshark tap --proxy-front-port 8898
# UI opens on: http://127.0.0.1:8898
```

### Access Kubeshark UI

- **Default**: http://127.0.0.1:8899
- **Custom port**: http://127.0.0.1:8898 (when using `--proxy-front-port 8898`)

### Tips

- **Keep contexts separate**: Each Kubeshark instance must run with the correct cluster context
- **Use different ports**: Always specify `--proxy-front-port` for the second cluster to avoid port conflicts
- **Monitor both simultaneously**: Open both UIs in different browser tabs to compare traffic
- **Filter traffic**: Use Kubeshark's filtering features to focus on specific services or namespaces

---

## Quick Reference: Common Workflows

### Deploy to Minikube

```bash
# 1. Build application
mvn clean package

# 2. Build Docker image
docker build -t anji-spring-hello-world:latest .

# 3. Load image to Minikube
minikube image load anji-spring-hello-world:latest

# 4. Deploy
kubectl apply -f kube/spring-boot-with-mysql-sidecar.yml

# 5. Get service URL
minikube service spring-nodeport --url

# 6. Access application
curl $(minikube service spring-nodeport --url)/hi
```

### Deploy to Kind (Dev Cluster)

```bash
# 1. Build application
mvn clean package

# 2. Build Docker image
docker build -t anji-spring-hello-world:anjibabu .

# 3. Load image to Kind
kind load docker-image anji-spring-hello-world:anjibabu --name dev

# 4. Switch context
kubectl config use-context kind-dev

# 5. Deploy
kubectl apply -f kube/spring-boot-with-mysql-sidecar.yml

# 6. Access via port-forward
kubectl port-forward service/spring-nodeport 8080:8080

# Or access via NodePort (if port mapped)
curl http://localhost:30080/hi
```

### Update Application with New Changes

```bash
# 1. Make code changes
# ... edit your code ...

# 2. Rebuild application
mvn clean package

# 3. Rebuild Docker image
docker build -t anji-spring-hello-world:latest .

# 4. Load new image
minikube image load anji-spring-hello-world:latest
# OR
kind load docker-image anji-spring-hello-world:anjibabu --name dev

# 5. Restart deployment
kubectl rollout restart deployment/spring-hello-world

# 6. Check status
kubectl rollout status deployment/spring-hello-world

# 7. Verify
kubectl get pods -l app=spring-hello-world
```

### Monitor Traffic in Multiple Clusters

```bash
# Terminal 1: Dev cluster
kubectl config use-context kind-dev
kubeshark tap

# Terminal 2: Test cluster
kubectl config use-context kind-test
kubeshark tap --proxy-front-port 8898

# Terminal 3: Generate traffic
curl http://localhost:30080/hi  # Dev
curl http://localhost:30081/hi  # Test
```

---

## Command Aliases (Optional)

Add these to your `~/.zshrc` or `~/.bashrc` for convenience:

```bash
# kubectl aliases
alias k='kubectl'
alias kgp='kubectl get pods'
alias kgs='kubectl get services'
alias kgd='kubectl get deployments'
alias kdp='kubectl describe pod'
alias kds='kubectl describe service'
alias kdd='kubectl describe deployment'
alias kl='kubectl logs'
alias klf='kubectl logs -f'
alias kpf='kubectl port-forward'

# Context aliases
alias kdev='kubectl --context=kind-dev'
alias ktest='kubectl --context=kind-test'
alias kminikube='kubectl --context=minikube'

# Docker aliases
alias d='docker'
alias dc='docker compose'
alias dcu='docker compose up -d'
alias dcd='docker compose down'
alias dcl='docker compose logs -f'
```

Then reload your shell:
```bash
source ~/.zshrc  # or source ~/.bashrc
```

