# Budowanie obrazu

* Znajdujemy sie w katalogu z plikeim Dockerfile
```
docker build -t registry.gitlab.com/edmpsi/aaas/engine/r:0.3 .
docker login registry.gitlab.com
docker push registry.gitlab.com/edmpsi/aaas/engine/r:0.3
```

# Uruchamianie 3AS
* tylko C:\Users (i poni¿ej) jest mo¿liwy do wspó³dzielenia z docker-toolboxem
```
docker run -it --rm -p 6311:6311 -v /c/Users/rbachorz/R/aaas/projects/engine-r/src/R/standard:/var/userScripts -v /c/Users/rbachorz/R/models:/var/trainedModels --entrypoint=bash registry.gitlab.com/edmpsi/aaas/engine/r:0.3
```