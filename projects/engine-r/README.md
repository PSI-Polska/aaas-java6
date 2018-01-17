## Budowanie obrazu
Znajdujemy sie w katalogu z plikeim Dockerfile
docker build -t registry.gitlab.com/edmpsi/aaas/engine/r:latest .
docker login registry.gitlab.com
docker push registry.gitlab.com/edmpsi/aaas/engine/r:latest