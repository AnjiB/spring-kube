#!/bin/bash

# Multi-Cluster Deployment Script
# Deploys Spring Boot application to dev and test Kind clusters

set -e  # Exit on error

IMAGE_NAME="anji-spring-hello-world:anjibabu"
DEPLOYMENT_FILE="kube/spring-boot-with-mysql-sidecar.yml"

echo "=========================================="
echo "Multi-Cluster Deployment Script"
echo "=========================================="
echo ""

# Check if Kind clusters exist
echo "Checking Kind clusters..."
if ! kind get clusters | grep -q "dev"; then
    echo "ERROR: dev cluster not found. Create it with: kind create cluster --name dev"
    exit 1
fi

if ! kind get clusters | grep -q "test"; then
    echo "ERROR: test cluster not found. Create it with: kind create cluster --name test"
    exit 1
fi

echo "✓ Dev and test clusters found"
echo ""

# Build application
echo "Step 1: Building application..."
mvn clean package -q
echo "✓ Application built"
echo ""

# Build Docker image
echo "Step 2: Building Docker image..."
docker build -t $IMAGE_NAME . -q
echo "✓ Docker image built: $IMAGE_NAME"
echo ""

# Load image to dev cluster
echo "Step 3: Loading image to dev cluster..."
kind load docker-image $IMAGE_NAME --name dev
echo "✓ Image loaded to dev cluster"
echo ""

# Load image to test cluster
echo "Step 4: Loading image to test cluster..."
kind load docker-image $IMAGE_NAME --name test
echo "✓ Image loaded to test cluster"
echo ""

# Deploy to dev cluster
echo "Step 5: Deploying to dev cluster..."
kubectl apply -f $DEPLOYMENT_FILE --context=kind-dev
echo "✓ Deployed to dev cluster"
echo ""

# Deploy to test cluster
echo "Step 6: Deploying to test cluster..."
kubectl apply -f $DEPLOYMENT_FILE --context=kind-test
echo "✓ Deployed to test cluster"
echo ""

# Wait for deployments
echo "Step 7: Waiting for pods to be ready..."
echo "Waiting for dev cluster..."
kubectl wait --for=condition=ready pod -l app=spring-hello-world --timeout=120s --context=kind-dev 2>/dev/null || true
echo "Waiting for test cluster..."
kubectl wait --for=condition=ready pod -l app=spring-hello-world --timeout=120s --context=kind-test 2>/dev/null || true
echo ""

# Show status
echo "=========================================="
echo "Deployment Status"
echo "=========================================="
echo ""
echo "DEV CLUSTER:"
kubectl get pods --context=kind-dev -l app=spring-hello-world
echo ""
echo "TEST CLUSTER:"
kubectl get pods --context=kind-test -l app=spring-hello-world
echo ""

echo "=========================================="
echo "Access Your Applications"
echo "=========================================="
echo ""
echo "Dev Cluster:"
echo "  kubectl port-forward service/spring-nodeport 8080:8080 --context=kind-dev"
echo "  Then access: http://localhost:8080/swagger-ui.html"
echo ""
echo "Test Cluster:"
echo "  kubectl port-forward service/spring-nodeport 8081:8080 --context=kind-test"
echo "  Then access: http://localhost:8081/swagger-ui.html"
echo ""
echo "=========================================="
echo "Deployment Complete!"
echo "=========================================="

