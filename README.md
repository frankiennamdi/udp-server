## Description

UDP Server Application

## Required Technologies
1. gradle
2. Java 8.x - chosen for some of its appealing functionality like streams, functions and method references.
3. Developed with IDEA Intellij - chosen as my IDE of choice.
4. Spring Boot

## Note

The emitter should not be run back to back without changing the transaction numbers or allowing previous transactions to
conclude. There is no negative outcome except that the transaction will expire since it is possible to exceed the allowed
30 seconds execution time.

## Testing

1. More test and improved test coverage is desirable , I assumed more external integration
test will suffice for now and will reduce the amount of code to review.

2. MessageProcessingServiceTest is a slow test and was neccessary to get the
test coverage above 85 % for class  and 77% for methods. The UdpServer and
UdpServerRunner are currently not covered, and were tested manually.
It is not a difficult test to write, but will mean more code.


## NIO

This application uses NIO (Non-blocking IO) to select channels that are ready for reading, it then passes the data to a threadpool for more
throughput and fewer missed packets. After 30 seconds each transaction is timed out and its state evaluated and displayed.

## Execution
Below are some examples of how to execute the application. 

### Using bootRun

```
gradle clean build && gradle -q bootRun -Pargs="server -p=6789"
```

```
gradle clean build && gradle -q bootRun -Pargs="server"

```

```
gradle clean build && gradle -q bootRun
```

### Using java -jar 

```
gradle clean build
```


```
java -jar build/libs/udp-server-1.0-SNAPSHOT.jar server -p=6789

```


```
java -jar build/libs/udp-server-1.0-SNAPSHOT.jar server
```

```
java -jar build/libs/udp-server-1.0-SNAPSHOT.jar
```

## Usage
```
usage: server [-p <arg>]
 -p,--port <arg>   server port

```

## output
Message #1 length: 450409 sha256: b27006e844d91a8e0e9705b96ca34828f14a1e6640070f1fe5b20b16120c75a9