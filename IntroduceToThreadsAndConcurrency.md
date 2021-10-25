# Basic know-how about threads

Concurrency is the ability of a program to execute several computations simultaneously.
To achieve a better understanding of parallel execution, we have to distinguish between processes and threads:
 - Processes are an execution environment provided by the operating system that has its own set of private resources(e.g. memory, open files, etc.)
 - Threads in contrast are processes that live within a process and share their resources (memory, open files, etc.) with the other threads of the process.

In java each thread have this property:
 - id
 - name
 - priority
 - state
 - threadGroupName

Each thread has an identifier, which is unique in the JVM.

The name of the threads helps to find certain threads within external applications that monitor a running JVM

When more than one threads are executed, the priority decides which task should be executed next.

Thread has a state, which can be one of the following:

- NEW: A thread that has not yet started is in this state.
- RUNNABLE: A thread executing in the Java virtual machine is in this state.
- BLOCKED: A thread that is blocked waiting for a monitor lock is in this state.
- WAITING: A thread that is waiting indefinitely for another thread to perform a particular action is in this state.
- TIMED_WAITING: A thread that is waiting for another thread to perform an action for up to a specified waiting time is in this state.
- TERMINATED: A thread that has exited is in this state.


threadGroupName indicates that threads are managed in groups. The JDK class java.lang.ThreadGroup provides some methods to handle a whole group of Threads. With these methods we can for example interrupt all threads of a group or set their maximum priority.

## Creating and starting threads

There are two ways to create a thread in java:

 - the first one is create a class that extends Thread.
 - the second way is create a class that implement interface Runnable.

```
public class MyThread extends Thread {
    public MyThread(String name) {
          super(name);
        }
    }

    @Override
    public void run() {
      System.out.println("Executing thread "+Thread.currentThread(). ←􏰀 getName());
    }
    
    public static void main(String[] args) throws InterruptedException {
            MyThread myThread = new MyThread("myThread");
            myThread.start();
    }
```

```
public class MyRunnable implements Runnable {

  public void run() {
    System.out.println("Executing thread "+Thread.currentThread(). getName());
  }


  public static void main(String[] args) throws InterruptedException {
          Thread myThread = new Thread(new MyRunnable(), "myRunnable");
          myThread.start();
  }
}
```

Whether you should use the subclassing or the interface approach, depends to some extend on your taste. The interface is a more light-weight approach as all you have to do is the implementation of an interface. The class can still be a subclass of some other class. You can also pass your own parameters to the constructor whereas subclassing Thread restricts you to the available constructors that the class Thread brings along.

## Sleeping and interrupting

Once we have started a Thread, it runs until the run() method reaches it end.

In real world applications you will normally have to implement some kind of background processing where you let the thread run until it has for example processed all files within a directory structure, for example. Another common use case is to have a background thread that looks every n seconds if something has happened (e.g. a file has been created) and starts some kind of action. In this case you will have to wait for n seconds or milliseconds. You could implement this by using a while loop whose body gets the current milliseconds and looks when the next second has passed. Although such an implementation would work, it is a waste of CPU processing time as your thread occupies the CPU and retrieves the current time again and again.
A better approach for such use cases is calling the method sleep() of the class java.lang.Thread like in the following example:

```

public void run() {
        while(true) {
          doSomethingUseful();
          try {
                Thread.sleep(1000);
          } catch (InterruptedException e) {
                e.printStackTrace();
          }
        
      }
}
```
An invocation of sleep() puts the current Thread to sleep without consuming any processing time
In the code example above you may have noticed the InterruptedException that sleep() may throw. Interrupts are a very basic feature for thread interaction that can be understood as a simple interrupt message that one thread sends to another thread. The receiving thread can explicitly ask if it has been interrupted by calling the method Thread.interrupted() or it is implicitly interrupted while spending his time within a method like sleep()

## Joining Threads

An important feature of threads that you will have to use from time to time is the ability of a thread to wait for the termination of another thread.
```
import java.util.Random;

class Scratch implements Runnable {
    private Random random = new Random(System.currentTimeMillis());

    public static void main(String[] args) throws InterruptedException {
        Thread[] threads = new Thread[5];
        for (int i = 0; i < threads.length; i++) {
            threads[i] = new Thread(new Scratch(), "Joining Thread " + i);
            threads[i].start();
        }

        for (Thread thread : threads) {
            thread.join();
        }

        System.out.println("[" + Thread.currentThread().getName() + "] All Threads have finished");
    }

    @Override
    public void run() {
        for (int i = 0; i < 10000000; i++) {
            random.nextInt();
        }
        System.out.println("[" + Thread.currentThread().getName() + "] Finished");
    }
}
```
Result:

```
[Joining Thread 3] Finished
[Joining Thread 0] Finished
[Joining Thread 2] Finished
[Joining Thread 1] Finished
[Joining Thread 4] Finished
[main] All Threads have finished
```

You will observe that the sequence of "finished" messages varies from execution to execution. If you execute the program more than once, you may see that the thread which finishes first is not always the same. But the last statement is always the main thread that waits for its children.

## Synchronization

As we have seen in the last examples, the exact sequence in which all running threads are executed depends next to the thread configuration like priority also on the available CPU resources and the way the scheduler chooses the next thread to execute.

Access to shared resources is exclusive, which means only one thread at a given point in time should access this resource without any other thread interfering this access.
The solution for problems like this is the synchronized key word in Java. With synchronized you can create blocks of statements which can only be accessed by a thread, which gets the lock on the synchronized resource.

## Atomic Access

```
class AtomicAssignment implements Runnable {

    public static SimpleDateFormat simpleDateFormat = new SimpleDateFormat();
    public static Map<String, String> configuration = new HashMap<>();

    @Override
    public void run() {
        for (int i = 0; i < 10000; i++) {
            Map<String, String> currConfig = configuration;
            String value1 = currConfig.get("key-1");
            String value2 = currConfig.get("key-2");
            String value3 = currConfig.get("key-3");
            if (!(value1.equals(value2) && value2.equals(value3))) {
                throw new IllegalStateException("Values are not equal.");
            }
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void readConfig(){
        Map<String, String> newConfig = new HashMap<String, String>();
        Date now = new Date();
        newConfig.put("key-1", simpleDateFormat.format(now));
        newConfig.put("key-2", simpleDateFormat.format(now));
        newConfig.put("key-3", simpleDateFormat.format(now));
        configuration = newConfig;
    }

    public static void main(String[] args) throws InterruptedException {
        readConfig();

        Thread configThread = new Thread(() -> {
            for (int i = 0; i < 10000; i++) {
                readConfig();
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }, "configuration-thread");
        configThread.start();

        Thread[] threads = new Thread[5];

        for (int i = 0; i < threads.length; i++) {
            threads[i] = new Thread(new AtomicAssignment(), "Thread-" + i);
            threads[i].start();
        }

        for (Thread thread : threads) {
            thread.join();
        }
        configThread.join();
        System.out.println("[" + Thread.currentThread().getName() + "] All threads have finished.");
    }
}
```
The above example is a little more complex, but not hard to understand. The Map, which is shared, is the configuration variable of AtomicAssignment. In the main() method we read the configuration initially one time and add three keys to the Map with the same value (here the current time including milliseconds). Then we start a "configuration-thread" that simulates the reading of the configuration by adding all the time the current timestamp three times to the map. The five worker threads then read the Map using the configuration variable and compare the three values. If they are not equal, they throw an IllegalStateException.
You can run the program for some time and you will not see any IllegalStateException. 

This is due the fact that we assign the new Map to the shared configuration variable in one atomic operation:

```configuration = newConfig;```

We also read the value of the shared variable within one atomic step:

``` Map<String, String> currConfig = configuration;```

As both steps are atomic, we will always get a reference to a valid Map instance where all three values are equal. If you change for example the run() method in a way that it uses the configuration variable directly instead of copying it first to a local variable, you will see IllegalStateExceptions very soon because the configuration variable always points to the "current" configuration. When it has been changed by the configuration-thread, subsequent read accesses to the Map will already read the new values and compare them with the values from the old map.
