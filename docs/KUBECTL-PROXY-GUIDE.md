# kubectl proxy Guide: Accessing Multiple Services

## Key Concept: kubectl proxy Uses Service Names, NOT NodePorts

**Important**: When using `kubectl proxy`, you access services by their **service name**, not by NodePort. The NodePort becomes irrelevant because kubectl proxy routes through the Kubernetes API server using service discovery.

## How kubectl proxy Works

```
Your Local Machine          kubectl proxy          Kubernetes API Server          Service          Pods
     |                          |                          |                        |                |
     |  http://localhost:8001   |                          |                        |                |
     |  /api/v1/namespaces/     |                          |                        |                |
     |  default/services/       |                          |                        |                |
     |  SERVICE-NAME/proxy      |                          |                        |                |
     |------------------------->|                          |                        |                |
     |                          |  Authenticated Request   |                        |                |
     |                          |------------------------->|                        |                |
     |                          |                          |  Service Discovery     |                |
     |                          |                          |  (finds pods by        |                |
     |                          |                          |   labels/selectors)    |                |
     |                          |                          |------------------------>|                |
     |                          |                          |                        |  Load Balance  |
     |                          |                          |                        |--------------->|
     |                          |                          |                        |                |
     |<-------------------------|<-------------------------|<-----------------------|<---------------|
     |     Response              |      Response            |      Response          |    Response    |
```

## Example: Accessing Multiple Services in TEST Cluster

### Scenario Setup

You have two services in your TEST cluster:
1. **Service 1**: `spring-hello-world-service` (NodePort 30080)
2. **Service 2**: `another-app-service` (NodePort 30090)

### Step 1: Start kubectl proxy for TEST Cluster

```bash
# Make sure you're using TEST cluster context
kubectl config use-context test-cluster

# Or start proxy with explicit context
kubectl proxy --context=test-cluster

# Output:
# Starting to serve on 127.0.0.1:8001
```

**Note**: The proxy runs on `http://localhost:8001` by default. This is the **only port** you need to remember!

### Step 2: Access Services by Name (NOT NodePort)

The URL format for accessing services via kubectl proxy is:

```
http://localhost:8001/api/v1/namespaces/<namespace>/services/<service-name>/proxy/<path>
```

#### Access Service 1 (spring-hello-world-service on NodePort 30080)

```bash
# GET request
curl http://localhost:8001/api/v1/namespaces/default/services/spring-hello-world-service/proxy/hi

# POST request
curl -X POST http://localhost:8001/api/v1/namespaces/default/services/spring-hello-world-service/proxy/hi \
  -H "Content-Type: application/json" \
  -d '{"name": "Anji"}'
```

#### Access Service 2 (another-app-service on NodePort 30090)

```bash
# GET request
curl http://localhost:8001/api/v1/namespaces/default/services/another-app-service/proxy/api/endpoint

# POST request
curl -X POST http://localhost:8001/api/v1/namespaces/default/services/another-app-service/proxy/api/data \
  -H "Content-Type: application/json" \
  -d '{"key": "value"}'
```

### Important Points

1. **NodePort is NOT used**: You don't need to know or use 30080 or 30090
2. **Service name is used**: `spring-hello-world-service` and `another-app-service`
3. **Same proxy port**: Both services accessed through `localhost:8001`
4. **No port conflicts**: Multiple services, one proxy port

## Complete Example: TEST Cluster with Two Services

### Service Definitions

```yaml
# Service 1: Spring Boot Hello World
apiVersion: v1
kind: Service
metadata:
  name: spring-hello-world-service
  namespace: default
spec:
  type: NodePort
  selector:
    app: spring-hello-world
  ports:
    - protocol: TCP
      port: 8080
      targetPort: 8080
      nodePort: 30080  # This is IGNORED when using kubectl proxy

---
# Service 2: Another Application
apiVersion: v1
kind: Service
metadata:
  name: another-app-service
  namespace: default
spec:
  type: NodePort
  selector:
    app: another-app
  ports:
    - protocol: TCP
      port: 8080
      targetPort: 8080
      nodePort: 30090  # This is IGNORED when using kubectl proxy
```

### Accessing Both Services

```bash
# Terminal 1: Start proxy for TEST cluster
kubectl proxy --context=test-cluster

# Terminal 2: Access both services
# Service 1 (NodePort 30080 - but we don't use the port!)
curl http://localhost:8001/api/v1/namespaces/default/services/spring-hello-world-service/proxy/hi

# Service 2 (NodePort 30090 - but we don't use the port!)
curl http://localhost:8001/api/v1/namespaces/default/services/another-app-service/proxy/api/endpoint
```

## Comparison: NodePort vs kubectl proxy

### Using NodePort Directly (Without Proxy)

```bash
# Get minikube IP
MINIKUBE_IP=$(minikube ip -p test-cluster)

# Access Service 1
curl http://$MINIKUBE_IP:30080/hi

# Access Service 2
curl http://$MINIKUBE_IP:30090/api/endpoint
```

**Issues:**
- Need to know NodePort numbers (30080, 30090)
- Need to get cluster IP
- Port conflicts possible
- Different URLs for each service

### Using kubectl proxy (Recommended)

```bash
# Start proxy once
kubectl proxy --context=test-cluster

# Access Service 1
curl http://localhost:8001/api/v1/namespaces/default/services/spring-hello-world-service/proxy/hi

# Access Service 2
curl http://localhost:8001/api/v1/namespaces/default/services/another-app-service/proxy/api/endpoint
```

**Benefits:**
- ✅ Don't need NodePort numbers
- ✅ Don't need cluster IP
- ✅ No port conflicts (all use port 8001)
- ✅ Consistent URL pattern
- ✅ Works with any service type (ClusterIP, NodePort, LoadBalancer)

## Practical Workflow: TEST Cluster

### Step-by-Step

```bash
# 1. Verify you're in TEST cluster
kubectl config current-context
# Should show: test-cluster

# 2. List services to get exact service names
kubectl get services --context=test-cluster

# Output:
# NAME                        TYPE       CLUSTER-IP      EXTERNAL-IP   PORT(S)          AGE
# spring-hello-world-service  NodePort   10.96.xxx.xxx   <none>        8080:30080/TCP   5m
# another-app-service         NodePort   10.96.yyy.yyy   <none>        8080:30090/TCP   5m

# 3. Start kubectl proxy
kubectl proxy --context=test-cluster

# 4. In another terminal, test Service 1
curl http://localhost:8001/api/v1/namespaces/default/services/spring-hello-world-service/proxy/hi
# Response: Hello

# 5. Test Service 2
curl http://localhost:8001/api/v1/namespaces/default/services/another-app-service/proxy/api/endpoint
# Response: <service 2 response>
```

## Advanced: Using Different Namespaces

If your services are in different namespaces:

```bash
# Service in 'dev' namespace
curl http://localhost:8001/api/v1/namespaces/dev/services/spring-hello-world-service/proxy/hi

# Service in 'prod' namespace
curl http://localhost:8001/api/v1/namespaces/prod/services/spring-hello-world-service/proxy/hi
```

## Advanced: Custom Proxy Port

```bash
# Start proxy on custom port
kubectl proxy --port=9000 --context=test-cluster

# Access services
curl http://localhost:9000/api/v1/namespaces/default/services/spring-hello-world-service/proxy/hi
```

## Troubleshooting

### Service Not Found

```bash
# Verify service exists
kubectl get services --context=test-cluster

# Verify service name spelling (case-sensitive!)
kubectl get service spring-hello-world-service --context=test-cluster
```

### Proxy Connection Refused

```bash
# Check if proxy is running
ps aux | grep "kubectl proxy"

# Restart proxy
kubectl proxy --context=test-cluster
```

### Wrong Cluster

```bash
# Check current context
kubectl config current-context

# Switch to TEST cluster
kubectl config use-context test-cluster

# Or use explicit context in proxy command
kubectl proxy --context=test-cluster
```

## Summary

**Key Takeaway**: With `kubectl proxy`, NodePort numbers (30080, 30090) are **completely irrelevant**. You access services by their **service name** through a single proxy port (8001). This makes it much easier to manage multiple services without worrying about port conflicts or remembering NodePort numbers.

