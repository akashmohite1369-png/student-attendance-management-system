# Replacement / deployment steps

## 1. Local test
Open PowerShell in `backend` and run:

```powershell
mvn clean package
mvn spring-boot:run
```

Do not run Maven from `C:\Users\akash`; run it from the folder containing `pom.xml`.

## 2. Replace the old GitHub project
Safest approach:
1. Back up the current GitHub repo.
2. Replace the repository files with this package.
3. Commit and push to `main`.
4. Netlify and Render can then redeploy from the new commit.

## 3. Netlify
`netlify.toml` publishes `frontend` and proxies `/api/*` to the Render backend.

## 4. Render
`Dockerfile` builds the Spring Boot backend with Java 21. `render.yaml` uses `/health` for the health check.

## 5. Important
The ZIP does not include H2 runtime data or Maven `target/` files. This avoids shipping a stale/locked local database into a fresh copy.
