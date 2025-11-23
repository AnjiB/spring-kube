# Port Forwarding Guide

## What is Port Forwarding?

**Port forwarding** (also called `kubectl port-forward`) creates a secure tunnel between your local machine and a pod or service in your Kubernetes cluster. It forwards traffic from a local port on your machine to a port on the pod/service.

## How It Works

```
Your Local Machine          kubectl port-forward          Kubernetes Pod/Service
     |                              |                              |
     |  http://localhost:8080       |                              |
     |------------------------------|                              |
     |                              |  Forwards traffic            |
     |                              |------------------------------>|
     |                              |                              |
     |<-----------------------------|<------------------------------|
     |     Response                 |      Response                |
```

## Basic Syntax

```bash
kubectl port-forward <resource-type>/<resource-name> <local-port>:<remote-port>
```

## Examples

### Port Forward to a Service

```bash
# Forward local port 8080 to service port 8080
kubectl port-forward service/spring-nodeport 8080:8080

# Now access your application at:
# http://localhost:8080
```

### Port Forward to a Pod

```bash
# Get pod name
POD_NAME=$(kubectl get pods -l app=spring-hello-world -o jsonpath='{.items[0].metadata.name}')

# Forward to pod
kubectl port-forward pod/$POD_NAME 8080:8080

# Or directly
kubectl port-forward spring-hello-world-65b844b6d-7mlqh 8080:8080
```

### Port Forward to a Deployment

```bash
# Forward to deployment (automatically selects a pod)
kubectl port-forward deployment/spring-hello-world 8080:8080
```

### Port Forward to Multiple Ports

```bash
# Forward multiple ports (e.g., app and MySQL)
kubectl port-forward pod/<pod-name> 8080:8080 3306:3306
```

## Port Forward vs kubectl proxy

| Feature | Port Forward | kubectl proxy |
|---------|-------------|---------------|
| **What it forwards to** | Pod or Service | Kubernetes API server |
| **URL format** | Simple: `http://localhost:8080` | Complex: `http://localhost:8001/api/v1/...` |
| **CORS issues** | No (same origin) | Yes (different origin) |
| **Use case** | Direct access to application | Access to Kubernetes API |
| **Caching issues** | No | Yes (endpoint caching) |
| **Multiple services** | One at a time | All services via API |

## Port Forward vs NodePort Service

| Feature | Port Forward | NodePort Service |
|---------|-------------|------------------|
| **Access from** | Local machine only | Any machine on network |
| **Port range** | Any port | 30000-32767 |
| **Setup** | Simple command | Requires Service YAML |
| **Use case** | Development/testing | Production/external access |
| **Security** | Secure tunnel | Exposed on node IP |

## Practical Examples

### Example 1: Access Spring Boot Application

```bash
# Forward service
kubectl port-forward service/spring-nodeport 8080:8080

# Access in browser
open http://localhost:8080/swagger-ui.html

# Test API
curl http://localhost:8080/hi
curl http://localhost:8080/api/books
```

### Example 2: Access MySQL Database

```bash
# Forward MySQL port from pod
kubectl port-forward pod/<pod-name> 3306:3306 -c mysql

# Connect with MySQL client
mysql -h 127.0.0.1 -P 3306 -u root -prootpassword springdb
```

### Example 3: Access Multiple Services

```bash
# Terminal 1: Spring Boot
kubectl port-forward service/spring-nodeport 8080:8080

# Terminal 2: MySQL
kubectl port-forward pod/<pod-name> 3306:3306 -c mysql
```

## Advantages of Port Forwarding

1. **Simple URLs**: Access at `http://localhost:8080` instead of complex proxy URLs
2. **No CORS issues**: Same origin as localhost
3. **Direct connection**: No API server in between
4. **No caching**: Always uses current endpoints
5. **Easy debugging**: Works like local development

## Disadvantages

1. **Local only**: Only accessible from your machine
2. **Temporary**: Stops when you close the terminal
3. **One at a time**: Each port-forward needs a separate terminal
4. **Not for production**: Not suitable for external access

## Common Use Cases

### Development and Testing
```bash
# Test your application locally
kubectl port-forward service/spring-nodeport 8080:8080
```

### Database Access
```bash
# Connect to database for debugging
kubectl port-forward pod/<pod-name> 3306:3306 -c mysql
```

### Debugging
```bash
# Access application logs and test endpoints
kubectl port-forward deployment/spring-hello-world 8080:8080
```

## Running Port Forward in Background

```bash
# Run in background
kubectl port-forward service/spring-nodeport 8080:8080 &

# Stop background process
kill %1
# Or find and kill
ps aux | grep "kubectl port-forward"
kill <PID>
```

## Troubleshooting

### Port Already in Use

```bash
# Error: Unable to listen on port 8080: listen tcp4 :8080: bind: address already in use

# Solution 1: Use different port
kubectl port-forward service/spring-nodeport 9090:8080
# Access at http://localhost:9090

# Solution 2: Free the port
lsof -i :8080
kill <PID>
```

### Connection Refused

```bash
# Check if pod is running
kubectl get pods -l app=spring-hello-world

# Check if pod is ready
kubectl get pods -l app=spring-hello-world -o jsonpath='{.items[0].status.conditions[?(@.type=="Ready")].status}'

# Check pod logs
kubectl logs <pod-name> -c spring-hello-world
```

### Port Forward Dies Unexpectedly

```bash
# Check if pod was recreated
kubectl get pods -l app=spring-hello-world

# Restart port-forward
kubectl port-forward service/spring-nodeport 8080:8080
```

## Best Practices

1. **Use for development**: Port-forward is perfect for local development
2. **Use service name**: Forward to service instead of pod (survives pod restarts)
3. **Document ports**: Keep track of which ports you're forwarding
4. **Use different ports**: If forwarding multiple services, use different local ports
5. **Stop when done**: Close port-forwards when not needed

## Comparison Summary

### When to Use Port Forward
- ✅ Local development and testing
- ✅ Debugging applications
- ✅ Accessing databases
- ✅ Quick testing without exposing services

### When to Use kubectl proxy
- ✅ Accessing Kubernetes API
- ✅ Testing service discovery
- ✅ Accessing multiple services via API

### When to Use NodePort Service
- ✅ External access from network
- ✅ Production deployments
- ✅ Load balancing across pods

## Quick Reference

```bash
# Forward service
kubectl port-forward service/<service-name> <local-port>:<remote-port>

# Forward pod
kubectl port-forward pod/<pod-name> <local-port>:<remote-port>

# Forward deployment
kubectl port-forward deployment/<deployment-name> <local-port>:<remote-port>

# Forward with specific container (for multi-container pods)
kubectl port-forward pod/<pod-name> <local-port>:<remote-port> -c <container-name>

# Forward multiple ports
kubectl port-forward pod/<pod-name> <local-port1>:<remote-port1> <local-port2>:<remote-port2>
```

