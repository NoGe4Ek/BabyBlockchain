# BabyBlockchain
'_Done for the poly Software-Engineering-2022 course_'

Simple blockchain on java sockets.
## Links table
- [Task]
- [Other course projects]
## Usage
By passing the port parameter for the node (hardcoded three port options for each node from 8000 to 8002) you just can run several instances of projects, e.g. running a program using jar archive or docker compose.
Jar:
```
java -jar BabyBlockchain.jar <port>
```
Docker compose:
```
docker-compose up
```
You also can run docker image by
``` 
git clone https://github.com/NoGe4Ek/BabyBlockchain.git
docker build -t bb .
docker run bb
```
## License
Apache License 2.0

[Task]: <https://github.com/SemenMartynov/Software-Engineering-2022/blob/main/NetworkProgrammingTask.md>
[Other course projects]: <https://github.com/SemenMartynov/Software-Engineering-2022/pulls>