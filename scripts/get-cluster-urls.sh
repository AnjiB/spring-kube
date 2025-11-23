#!/bin/bash

# Get Cluster Access URLs
# Shows NodePort access URLs for dev and test clusters
# Checks for port mappings first (like Minikube)

echo "=========================================="
echo "Cluster Access URLs"
echo "=========================================="
echo ""

# Dev Cluster
if docker ps | grep -q "dev-control-plane"; then
    NODEPORT=$(kubectl get service spring-nodeport -o jsonpath='{.spec.ports[0].nodePort}' --context=kind-dev 2>/dev/null)
    
    if [ ! -z "$NODEPORT" ]; then
        # Check if port is mapped to host
        PORT_MAPPING=$(docker port dev-control-plane 2>/dev/null | grep "$NODEPORT" || echo "")
        
        echo "DEV CLUSTER:"
        echo "  NodePort: $NODEPORT"
        
        if [ ! -z "$PORT_MAPPING" ]; then
            # Extract host port from mapping (format: 0.0.0.0:30080->30080/tcp)
            HOST_PORT=$(echo "$PORT_MAPPING" | sed 's/.*->\([0-9]*\)\/.*/\1/' | head -1)
            echo "  ✅ Port mapped to host: $HOST_PORT"
            echo "  Swagger UI: http://localhost:$HOST_PORT/swagger-ui.html"
            echo "  API: http://localhost:$HOST_PORT/hi"
            echo "  Books API: http://localhost:$HOST_PORT/api/books"
        else
            # Port not mapped, show container IP (may not work from host)
            DEV_IP=$(docker inspect dev-control-plane --format='{{range .NetworkSettings.Networks}}{{.IPAddress}}{{end}}' 2>/dev/null)
            echo "  ⚠️  Port NOT mapped to host"
            echo "  Container IP: $DEV_IP"
            echo "  Try: http://$DEV_IP:$NODEPORT/swagger-ui.html (may not work from host)"
            echo "  💡 Use port-forward instead (see below)"
        fi
        echo ""
    else
        echo "DEV CLUSTER: Service not found or not deployed"
        echo ""
    fi
else
    echo "DEV CLUSTER: Cluster not running"
    echo ""
fi

# Test Cluster
if docker ps | grep -q "test-control-plane"; then
    NODEPORT=$(kubectl get service spring-nodeport -o jsonpath='{.spec.ports[0].nodePort}' --context=kind-test 2>/dev/null)
    
    if [ ! -z "$NODEPORT" ]; then
        # Check if port is mapped to host
        PORT_MAPPING=$(docker port test-control-plane 2>/dev/null | grep "$NODEPORT" || echo "")
        
        echo "TEST CLUSTER:"
        echo "  NodePort: $NODEPORT"
        
        if [ ! -z "$PORT_MAPPING" ]; then
            # Extract host port from mapping
            HOST_PORT=$(echo "$PORT_MAPPING" | sed 's/.*->\([0-9]*\)\/.*/\1/' | head -1)
            echo "  ✅ Port mapped to host: $HOST_PORT"
            echo "  Swagger UI: http://localhost:$HOST_PORT/swagger-ui.html"
            echo "  API: http://localhost:$HOST_PORT/hi"
            echo "  Books API: http://localhost:$HOST_PORT/api/books"
        else
            # Port not mapped, show container IP (may not work from host)
            TEST_IP=$(docker inspect test-control-plane --format='{{range .NetworkSettings.Networks}}{{.IPAddress}}{{end}}' 2>/dev/null)
            echo "  ⚠️  Port NOT mapped to host"
            echo "  Container IP: $TEST_IP"
            echo "  Try: http://$TEST_IP:$NODEPORT/swagger-ui.html (may not work from host)"
            echo "  💡 Use port-forward instead (see below)"
        fi
        echo ""
    else
        echo "TEST CLUSTER: Service not found or not deployed"
        echo ""
    fi
else
    echo "TEST CLUSTER: Cluster not running"
    echo ""
fi

echo "=========================================="
echo "Port Forward Commands"
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

