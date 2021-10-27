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
- Using hardware supported optimistic locking operations instead of synchronization. • Avoid synchronization where possible
- Avoid object pooling
