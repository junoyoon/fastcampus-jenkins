# echo -n "admin:발급받은토큰" | base64
# 위 값으로 nginx.conf 의 proxy_set_header Authorization 이후 수정

docker run --rm --name nginx -it \
     --network practice \
     -v ./nginx.conf:/etc/nginx/nginx.conf:ro \
     -v .:/usr/share/nginx/html \
     -p 8090:8080 \
     nginx