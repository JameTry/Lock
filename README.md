# Lock
## 条件等待锁

1.根据数量作为条件

当addTask添加的三个任务都做完了之后,才会走for循环下面的代码

```java
public static void t3() {
  	//创建一个完成数量为3的条件锁
    Lock lock = new Lock(3);
    for (int i = 0; i < 3; i++) {
        lock.addTask(() -> {
          	//do something
        });
    }
  	//当3个任务都完成后执行的代码
}
```

2.根据等待时间作为条件

当addTask添加的三个任务都做完了之后/等待3个任务超时后才会走for循环下面的代码

**注意!如果指定了超时则在超过等待时间后会将其他任务打断**

```java
public static void t2() {
    //创建一个完成数量为3,同时指定最大等待时间为3000毫秒的条件锁
    Lock lock = new Lock(3, 3000);

    for (int i = 0; i < 3; i++) {
        lock.addTask(() -> {
         	//do something
        });
    }
  //当3个任务都完成后/超时后执行的代码
}
```

