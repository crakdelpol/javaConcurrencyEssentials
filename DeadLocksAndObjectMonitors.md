# Concurrency Fundamentals: Deadlocks and Object Monitors

## Deadlock

Strictly spoken it means that two (or more) threads are each waiting on the other thread to free a resource that it has locked, while the thread itself has locked a resource the other thread is waiting on.

In general the following requirements for a deadlock can be identified:
- Mutual exclusion: There is a resource which can be accessed only by one thread at any point in time.
- Resourceholding:Whilehavinglockedoneresource,thethreadtriestoacquireanotherlockonsomeotherexclusiveresource.
- No preemption: There is no mechanism, which frees the resource if one threads holds the lock for a specific period of time.
- Circular wait: During runtime a constellation occurs in which two (or more) threads are each waiting on the other thread to free a resource that it has locked.

But you can try to avoid deadlocks if you are able to relax one of the requirements listed above:
- Mutual exclusion: This is a requirement that often cannot be relaxed, as the resource has to be used exclusively. But this must no always be the case. When using DBMS systems, a possible solution instead of using a pessimistic lock on some table row that has to be updated, one can use a technique called [Optimistic Locking]("https://en.wikipedia.org/wiki/Optimistic_concurrency_control").
- A possible solution to circumvent resource holding while waiting for another exclusive resource is to lock all necessary re- sources at the beginning of the algorithm and free all resources if it is not possible to obtain all locks. This is of course not always possible, maybe the resources to lock are not known ahead or it is just as waste of resources.
- If the lock cannot be obtained immediately, a possible solution to circumvent a possible deadlock is the introduction of a timeout. The SDK class [ReentrantLock]("https://docs.oracle.com/javase/7/docs/api/java/util/concurrent/locks/ReentrantLock.html") for example provides the possibility to specify a timeout for locking.
- As we have seen from the example code above, a deadlock does not appear if the sequence of lock requests does not differ between the different threads. This can be easily controlled if you are able to put all the locking code into one method where all threads have to pass through.

##  Thread Starvation

The scheduler decides which of the threads in state RUNNABLE it should execute next. The decision is based on the thread’s priority; hence threads with lower priority gain less CPU time than threads with a higher priority. What sounds like a reasonable feature can also cause problems when abused. If most of the time threads with a high priority are executed, the threads with lower priority seem to "starve" as they get not enough time to execute their work properly. Therefore it is recommended to set the priority of a thread only if there are strong reasons for it.

## Object monitors with wait() and notify()

The Java Programming Language therefore has another construct, that can be used in this scenario: wait() and notify(). The wait() method that every object inherits from the java.lang.Object class can be used to pause the current thread execution and wait until another threads wakes us up using the notify() method. In order to work correctly, the thread that calls the wait() method has to hold a lock that it has acquired before using the synchronized keyword. When calling wait() the lock is released and the threads waits until another thread that now owns the lock calls notify() on the same object instance.

In a multi-threaded application there may of course be more than one thread waiting on some object to be notified. Hence there are two different methods to wake up threads: notify() and notifyAll(). Whereas the first method only wakes up one of the waiting threads, the notifyAll() methods wakes them all up. 

## Designing for multi-threading

### Immutable object
One design rule that is considered to be very important in this context is Immutability. If you share object instances between different threads you have to pay attention that two threads do not modify the same object simultaneously. But objects that are not modifiable are easy to handle in such situations as you cannot change them. You always have to construct a new instance when you want to modify the data. The basic class java.lang.String is an example of an immutable class. Every time you want to change a string, you get a new instance

In the following you will find a set of rules to apply when you want to make a class immutable:
- All fields should be final and private.
- There should be not setter methods.
- The class itself should be declared final in order to prevent subclasses to violate the principle of immutability. 
- If fields are not of a primitive type but a reference to another object
