# GoSec

# Container

Start container

```
docker-compose up --build
```

Access container:

```
docker exec -it gosec bash
```

## Run gosec

Text output:

```
GO111MODULE=on gosec ./...
```

SARIF output:

```
GO111MODULE=on gosec -fmt=sarif -out=/results/result.json ./...
```

## Sample App

https://github.com/Hardw01f/Vulnerability-goapp
