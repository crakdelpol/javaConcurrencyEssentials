# The java.util.concurrent Package

The java.util.concurrent package defines a set of interfaces whose implementations execute tasks. The simplest one of these is the Executor interface

```
public interface Executor {
void execute(Runnable command);
}
```

It take a Runnable and execute it, an example of simple implementation could be :

```
public class MyExecutor implements Executor {
public void execute(Runnable r) {
    (new Thread(r)).start();
}
}

```

Along with the mere interface the JDK also ships a fully-fledged and extendable implementation named ThreadPoolExec utor. Under the hood the ThreadPoolExecutor maintains a pool of threads and dispatches the instances of Runnable given the execute() method to the pool. The arguments passed to the constructor control the behavior of the thread pool. The constructor with the most arguments is the following one:
```
ThreadPoolExecutor (int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory, RejectedExecutionHandler handler)
```

Analyze arguments step by step:

- corePoolSize: The ThreadPoolExecutor has an attribute corePoolSize that determines how many threads it will start until new threads are only started when the queue is full.
- maximumPoolSize: This attribute determines how many threads are started at the maximum. You can set this to Integer. MAX_VALUE in order to have no upper boundary.
- keepAliveTime: When the ThreadPoolExecutor has created more than corePoolSize threads, a thread will be re- moved from the pool when it idles for the given amount of time.
- unit: This is just the TimeUnit for the keepAliveTime.
- workQueue:ThisqueueholdstheinstancesofRunnablegiventhroughtheexecute()methoduntiltheyareactuallystarted.
- threadFactory: An implementation of this interface gives you control over the creation of the threads used by the ThreadP oolExecutor .
- handler: When you specify a fixed size for the workQueue and provide a maximumPoolSize then it may happen, that the ThreadPoolExecutor is not able to execute your Runnable instance due to saturation. In this case the provided handler is called and gives you control over what should happen in this case.


The Executor interface is very simple, it only forces the underlying implementation to implement the execute() method. The ExecutorService goes on step further as it extends the Executor interface and adds a series of utility methods.

But how does the JDK handle the fact that a task returns a value but is submitted to a thread pool for execution?
The work to check if the result is already available with the feature to block or to wait a certain amount of time is implemented in another class: java.util.concurrent.Future<V>. This class has only a few methods to check whether the task is done, to cancel the task as well as to retrieve its result.

Last but not least we have another interface which extends the Executor interface as well as the ExecutorService interface by some methods to schedule a task at a given point in time. The name of the interface is ScheduledExecutorService and it provides basically a schedule() method that takes an argument how long to wait until the task gets executed

## Concurrent collections

The Java collections framework encompasses a wide range of data structures that every Java programmers uses in his day to day work. This collection is extended by the data structures within the java.util.concurrent package. These implementa- tions provided thread-safe collections to be used within a multi-threaded environment.

HashMap and ArrayList aren't thread-safe.
One method to make a map thread-safe is wrap it into syncronixedMap:

```
HashMap<Long,String> map = new HashMap<Long, String>();
Map<Long, String> synchronizedMap = Collections.synchronizedMap(map);

```
As we see in the code above, the Collections class lets us create at runtime a synchronized version of a formerly unsynchro- nized collections class.

Another example of thread-safe map is ConcurrentHashMap in java.util.concurrent package:

``` 
ConcurrentHashMap<Long,String> map = new ConcurrentHashMap<Long,String>();
map.put(key, value);
String value2 = map.get(key);
```

The code above looks nearly the same as for a normal HashMap, but the underlying implementation is completely different. Instead of using only one lock for the whole table the ConcurrentHashMap subdivides the whole table into many small partitions. Each partition has its own lock. Hence write operations to this map from different threads, assuming they are writing at different partitions of the table, do not compete and can use their own lock.

Implement a performance test to see the main differences in term of performance:

```
public class MapComparison implements Runnable {
private static Map<Integer, String> map;
private Random random = new Random(System.currentTimeMillis());
public static void main(String[] args) throws InterruptedException {
        runPerfTest(new Hashtable<Integer, String>());
        runPerfTest(Collections.synchronizedMap(new HashMap<Integer,String>()));
        runPerfTest(new ConcurrentHashMap<Integer, String>());
        runPerfTest(new ConcurrentSkipListMap<Integer, String>());
private static void runPerfTest(Map<Integer, String> map) throws InterruptedException {

MapComparison.map = map;
fillMap(map);
ExecutorService executorService = Executors.newFixedThreadPool(10);
long startMillis = System.currentTimeMillis();
for (int i = 0; i < 10; i++) {
        executorService.execute(new MapComparison());
}
executorService.shutdown();
executorService.awaitTermination(1, TimeUnit.MINUTES); System.out.println(map.getClass().getSimpleName() + " took " + (System.currentTimeMillis() - startMillis) + " ms");
private static void fillMap(Map<Integer, String> map) {
        for (int i = 0; i < 100; i++) {
            map.put(i, String.valueOf(i));
        }
}
public void run() {
        for (int i = 0; i < 100000; i++) {
        int randomInt = random.nextInt(100);
        map.get(randomInt);
        randomInt = random.nextInt(100);
      map.put(randomInt, String.valueOf(randomInt));
  }
}
}

```

The output of this program is the following:
```
Hashtable took 436 ms
SynchronizedMap took 433 ms
ConcurrentHashMap took 75 ms
ConcurrentSkipListMap took 89 ms
```

### Atomic Variables 

When having multiple threads sharing a single variable, we have the task to synchronize access to this variable. The reason for this is the fact, that even a simple instruction like i++ is not atomic.
To cope with situations like this you can of course synchronize the access to this specific variable:
```
synchronized(i) {
  i++;
}
```

But this also means the current thread has to acquire the lock on i which needs some internal synchronization and computation within the JVM. This approach is also called pessimistic locking as we assume that it is highly probable that another thread currently holds the lock we want to acquire. A different approach called optimistic locking, assumes that there are not so many threads competing for the resource and hence we just try to update the value and see if this has worked. One implementation of this approach is the compare-and-swap (CAS) method.


It compares the content of a given memory location with a given value (the "expected value") and updates it to a new value if the current value equals to the expected value. In pseudo code this looks like:

```
int currentValue = getValueAtMemoryPosition(pos);
if(currentValue == excepctedValue) {
        setValueAtMemoryPosition(pos, newValue);
}

```
One representative of these classes is java.util.concurrent.atomic.AtomicInteger. The CAS operation discussed above is implemented by the method
```
boolean compareAndSet(int expect, int update)
```

Next to AtomicInteger the JDK also offers classes for atomic operations on long values, integer and long arrays and refer- ences.
    
    
### Semaphore
Semaphores are used to control access to a shared resource. In contrast to simple synchronized blocks a semaphore has an internal counter that is increased each time a thread acquires a lock and decreased each time a thread releases a lock it obtained before. The increasing and decreasing operations are of course synchronized, hence a semaphore can be used to control how many threads pass simultaneously through a critical section. The two basic operations of a thread are:
``` 
void acquire();
void release();
```
    
### CountDownLatch

The CountDownLatch class is another helpful class for thread synchronization from the JDK. Similar to the Semaphore class it provides a counter, but the counter of the CountDownLatch can only be decreased until it reaches zero
    
### CyclicBarrier
    
In contrast to the CountDownLatch, the CyclicBarrier class implements a counter that can be reset after being counted down to zero. All threads have to call its method await() until the internal counter is set to zero. The waiting threads are then woken up and can proceed. Internally the counter is then reset to its original value and the whole procedure can start again
    
    
