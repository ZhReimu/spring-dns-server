# spring-dns-server

使用 dnsjava 库 和 SpringBoot 写的 `Dns 服务器`

# 实现功能

- [x] 基于 sqlite 的数据持久化
- [x] 支持 A 记录 的 dns 服务器
- [x] 支持 泛域名解析 的 dns 服务器
- [x] 支持 使用 Restful 请求对数据库进行 增加 的操作
  ### 例如
  #### 更新 dns
  ```http://localhost:8080/update?host=baidu.com&action=update&ip=127.0.0.1```
  #### 增加 dns
  ```http://localhost:8080/update?host=baidu.com&ip=127.0.0.1&ip=1.1.1.1```
- [x] 支持 使用 Restful 请求对数据库进行 查询 的操作
  ### 例如
  ```curl http://localhost:8080/query?host=baidu.com```