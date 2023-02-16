### 프로젝트 설명   

> 원하는 폴더의 json 파일들을 읽어와서  
key값 중 id값은 application.properties에 맞게 업데이트, timestamp 값은 현재 시간에 맞게 업데이트한다.   
이 json 데이터들은 id 개수만큼 반복되어   
원하는 kafka topic으로 producer한다.     
    

---
#### java version
8


#### 빌드 및 배포
```
gradle clean build jar
```
원하는 파일 위치에서   

#### 실행
```
gradle run
```

#### 배포 명령어
```
java -jar springboot-kafka-producer-0.0.1.jar --spring.config.location=[application.properties 파일 위치]
```


#### application.properties
- **server.port** : 서버의 포트
- spring.output.ansi.enabled: always로 설정하면 콘솔에서 뜨는 로그에 색이 입혀진다.
- **bootstrap-servers**: kafka 서버 및 포트 정보 ex) ip:9092
- **topic** : kafka producer 하고자하는 토픽명
- **id** : 공백 구분 없이 ","로 작성한다. 해당 id로 json 파일 내 id가 업데이트 된다.
- **scheduled_cron**: 스케줄링 하고자하는 시간 ex) 600000 : 1시간
- **file_path**: json파일이 위치하는 파일 위치 ex) /Data/conf
  


---

##### 시뮬레이터의 역할 및 작동 순서   
0. producer하고자 하는 장비들을 application.properties의 "id"에 공백 없이 "," 으로 구분해서 입력해준다.   
1. properties에 입력한 "id"를 ","로 구분하여 추출한다.    
2. /conf/ 폴더 안에 있는 json파일을 모두 읽어들인다.    
3. 읽어온 json파일 값 중 id값을 1번에서 추출한 id들로 교체해준다.    
4. 읽어온 json파일 값 중 timestamp를 현재 시간으로 교체해준다.   
5. 데이터들을 원하는 topic의 server로 kafka producer한다.   
(server정보와 topic명은 application.properties에서 각각 "bootstrap-servers", "topic"으로 지정해준다).    
++ 스케줄링 주기는 properties의 "scheduled_cron"에서 설정해준다 (현재는 600000초(1시간)으로 설정해 두었습니다.)    
    
   
예시)   
id : 10개 (1,2,3,4,5,6,7,8,9,10)    
conf안에 json 파일 개수 : 2개 (test1.json, test2.json)    
(1-test1.json, 2-test1.json, 3-test1.json, 4-test1.json, 5-test1.json, 6-test1.json, 7-test1.json, 8-test1.json, 9-test1.json, 10-test1.json
1-test2.json, 2-test2.json, 3-test2.json, 4-test2.json, 5-test2.json, 6-test2.json, 7-test2.json, 8-test2.json, 9-test2.json, 10-test2.json)
   
=> 총 20번의 프로듀스가 진행됨    
    
