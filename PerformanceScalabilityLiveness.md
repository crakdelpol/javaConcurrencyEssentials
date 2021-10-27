# Performance, Scalability and Liveness

## Perfomance 

In order to able to compute how much performance our application may gain when we add further resources, we need to identify the parts of the program that have to run serialized/synchronized and the parts of the program that can run in parallel.

If we denote the fraction of the program that has to run synchronized with B (e.g. the number of lines that are executed synchronized) and if we denote the number of available processors with n, then Amdahl’s Law lets us compute an upper limit for the speedup our application may be able to achieve:

[Amdahl’s Law](https://it.wikipedia.org/wiki/Legge_di_Amdahl)

![\Large x=\frac{1}{(1-F)+{\frac{F}{N}}}](https://latex.codecogs.com/svg.latex?\Large&space;x=\frac{1}{(1-F)+{\frac{F}{N}}}) 

If we let n approach infinity, the term (1-B)/n converges against zero. Hence we can neglect this term and the upper limit for the speedup converges against 1/B, where B is the fraction of the program runtime before the optimization that is spent within non-parallelizable code. If B is for example 0.5, meaning that half of the program cannot be parallelized, the reciprocal value of 0.5 is 2; hence even if we add an unlimited number of processor to our application, we would only gain a speedup of about two. Now let’s assume we can rewrite the code such that only 0.25 of the program runtime is spent in synchronized blocks. Now the reciprocal value of 0.25 is 4, meaning we have built an application that would run with a large number of processors about four times faster than with only one processor.

So, in short, the target is to have the 1-F to lower as possible.


The writings of this article up to this point indicate that adding further threads to an application can improve the performance and responsiveness. But on the other hand, this does not come for free. The first performance impact is the creation of the thread itself. This takes some time as the JVM has to acquire the resources for the thread from the underlying operating system and prepare the data structures in the scheduler, which decides which thread to execute next.
Another cost of having multiple threads is the need to synchronize access to shared data structures. If more than one thread competes for the shared data structured we have contention. The JVM has then to decide which thread to execute next. If this is not the current thread, costs for a context switch are introduced.Therefore it is reasonable to reduce the number of context switches that are necessary due to lock contention. 

Lock contention
As we have seen in the previous section, two or more thread competing for one lock introduce additional clock cycles as the contention may force the scheduler to either let one thread spin-waiting for the lock or let another thread occupy the processor with the cost of two context switches. In some cases lock contention can be reduced by applying one of the following techniques:
- The scope of the lock is reduced.
- The number of times a certain lock is acquired is reduced.
- Using hardware supported optimistic locking operations instead of synchronization. 
- Avoid synchronization where possible
- Avoid object pooling


#### Scope reduction
The first technique can be applied when the lock is hold longer than necessary. Often this can be achieved by moving one or more lines out of the synchronized block in order to reduce the time the current thread holds the lock.
The less number of lines of code to execute the earlier the current thread can leave the synchronized block and therewith let other threads acquire the lock. This is also aligned with Amdahl’s Law

#### Lock splitting

Another technique to reduce lock contention is to split one lock into a number of smaller scoped locks. This technique can be applied if you have one lock for guarding different aspects of your application.
Another possible improvement is to separate locks even more by distinguishing between read and write locks. The Counter class for example provides methods for reading and writing the counter value. While reading the current value can be done by more than one thread in parallel, all write operations have to be serialized. The java.util.concurrent package provides a ready to use implementation of such a ReadWriteLock.

#### Lock striping

The previous example has shown how to split one single lock into two separate locks. This allows the competing threads to acquire only the lock that protects the data structures they want to manipulate. On the other hand this technique also increases complexity and the risk of dead locks, if not implemented properly.
Lock striping on the other hand is a technique similar to lock splitting. Instead of splitting one lock that guards different code parts or aspects, we use different locks for different values.

#### Atomic operations

Another way to reduce lock contention is to use so called atomic operations. This principle is explained and evaluated in more detail in one of the following articles. The java.util.concurrent package offers support for atomic operations for some primitive data types. Atomic operations are implemented using the so called compare-and-swap (CAS) operation provided by the processor. The CAS instruction only updates the value of a certain register, if the current value equals the provided value. Only in this case the old value is replaced by the new value.


#### Avoid hotspots 

A typical implementation of a list will manage internally a counter that holds the number of items within the list. This counter is updated every time a new item is added to the list or removed from the list. If used within a single-threaded application, this optimization is reasonable, as the size() operation on the list will return the previously computed value directly. If the list does not hold the number of items in the list, the size() operation would have to iterate over all items in order to calculate it.
What is a common optimization in many data structures can become a problem in multi-threaded applications. Assume we want to share an instance of this list with a bunch of threads that insert and remove items from the list and query its size. The counter variable is now also a shared resource and all access to its value has to be synchronized. The counter has become a hotspot within the implementation.

#### Avoid object pooling

In the first versions of the Java VM object creation using the new operator was still an expensive operation. This led many programmers to the common pattern of object pooling. Instead of creating certain objects again and again, they constructed a pool of these objects and each time an instance was needed, one was taken from the pool. After having used the object, it was put back into the pool and could be used by another thread.
What makes sense at first glance can be a problem when used in multi-threaded applications. 
Now the object pool is shared between all threads and access to the objects within the pool has to be synchronized. This additional synchronization overhead can now be bigger than the costs of the object creation itself. This is even true when you consider the additional costs of the garbage collector for collecting the newly created object instances.
