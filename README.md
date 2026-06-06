## Build và chạy ngầm (detached) các services
```bash
docker compose -f docker-compose.python.yml up --build -d
```

## Xem log của các service để đảm bảo Kafka kết nối tốt và model đã được tải về:
```bash
docker compose -f docker-compose.python.yml logs -f
```
## Tắt và xóa toàn bộ
```bash
docker compose -f docker-compose.python.yml down -v --rmi all
```