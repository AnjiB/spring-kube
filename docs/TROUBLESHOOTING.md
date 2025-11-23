# Troubleshooting Guide

## Common Issues and Solutions

### Issue: "no endpoints available for service" via kubectl proxy

**Symptoms:**
- Service exists and has endpoints
- Pods are running and ready
- kubectl proxy returns 503 "no endpoints available"

**Solution:**
1. **Restart kubectl proxy:**
   ```bash
   # Stop current proxy (Ctrl+C)
   # Then restart
   kubectl proxy
   ```

2. **Use port-forward instead:**
   ```bash
   kubectl port-forward service/spring-nodeport 8080:8080
   # Then access: http://localhost:8080/swagger-ui.html
   ```

3. **Verify endpoints:**
   ```bash
   kubectl get endpoints spring-nodeport
   kubectl describe service spring-nodeport
   ```

### Issue: CORS errors in Swagger UI

**Symptoms:**
- Swagger UI loads but API calls fail with CORS errors
- Works with docker compose but not with Kubernetes

**Solution:**
1. **Rebuild and redeploy** with CORS configuration
2. **Use port-forward** instead of kubectl proxy (avoids CORS issues)
3. **Check CORS configuration** in `CorsConfig.java` and `CorsFilterConfig.java`

### Issue: Swagger UI shows Petstore instead of your API

**Symptoms:**
- Swagger UI loads but shows Petstore example
- Your API docs are available at `/api-docs`

**Solution:**
1. **Use correct URL:**
   - ✅ `http://localhost:8080/swagger-ui.html`
   - ❌ `http://localhost:8080/swagger-ui/index.html`

2. **Check configuration:**
   ```properties
   springdoc.swagger-ui.url=/api-docs
   springdoc.swagger-ui.disable-swagger-default-url=true
   ```

### Issue: Pod not starting or crashing

**Symptoms:**
- Pod status: `CrashLoopBackOff` or `Error`

**Solution:**
1. **Check pod logs:**
   ```bash
   kubectl logs <pod-name> -c spring-hello-world
   kubectl logs <pod-name> -c mysql
   ```

2. **Check pod events:**
   ```bash
   kubectl describe pod <pod-name>
   ```

3. **Check image:**
   ```bash
   # Verify image is loaded
   minikube image ls | grep anji-spring-hello-world
   
   # Reload if needed
   minikube image load anji-spring-hello-world:latest
   ```

### Issue: Database connection errors

**Symptoms:**
- Spring Boot can't connect to MySQL
- Application starts but database operations fail

**Solution:**
1. **Check MySQL is running:**
   ```bash
   kubectl logs <pod-name> -c mysql
   ```

2. **Test MySQL connection:**
   ```bash
   kubectl exec <pod-name> -c mysql -- mysqladmin ping -h localhost -u root -prootpassword
   ```

3. **Verify connection string:**
   - Sidecar: `jdbc:mysql://localhost:3306/springdb`
   - Shared service: `jdbc:mysql://mysql-service:3306/springdb`

### Issue: Service not accessible

**Symptoms:**
- Service exists but can't access endpoints
- Connection refused or timeout

**Solution:**
1. **Check service endpoints:**
   ```bash
   kubectl get endpoints <service-name>
   ```

2. **Check pod readiness:**
   ```bash
   kubectl get pods -l app=spring-hello-world
   # Should show READY 2/2 (for sidecar) or 1/1 (for shared MySQL)
   ```

3. **Test from inside cluster:**
   ```bash
   kubectl run -it --rm debug --image=busybox --restart=Never -- sh
   # Inside pod:
   wget -O- http://spring-nodeport:8080/hi
   ```

### Issue: Image not found

**Symptoms:**
- Pod status: `ImagePullBackOff` or `ErrImagePull`

**Solution:**
1. **Load image into minikube:**
   ```bash
   minikube image load anji-spring-hello-world:latest
   ```

2. **Verify imagePullPolicy:**
   ```yaml
   imagePullPolicy: Never  # For local images in minikube
   ```

3. **Check image exists:**
   ```bash
   minikube image ls | grep anji-spring-hello-world
   ```

## Quick Diagnostic Commands

```bash
# Check all resources
kubectl get all -l app=spring-hello-world

# Check pod status
kubectl get pods -l app=spring-hello-world -o wide

# Check service
kubectl get service spring-nodeport

# Check endpoints
kubectl get endpoints spring-nodeport

# View pod logs
kubectl logs -l app=spring-hello-world -c spring-hello-world --tail=50

# View MySQL logs
kubectl logs -l app=spring-hello-world -c mysql --tail=50

# Describe pod (see events)
kubectl describe pod <pod-name>

# Test health endpoint
kubectl exec <pod-name> -c spring-hello-world -- curl http://localhost:8080/actuator/health

# Check if kubectl proxy is running
ps aux | grep "kubectl proxy"
```

## Getting Help

If issues persist:
1. Check pod logs for errors
2. Check service endpoints
3. Verify image is loaded in minikube
4. Try port-forward instead of kubectl proxy
5. Restart kubectl proxy if using it

