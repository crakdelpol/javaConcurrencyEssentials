# Performance, Scalability and Liveness

## Perfomance 

In order to able to compute how much performance our application may gain when we add further resources, we need to identify the parts of the program that have to run serialized/synchronized and the parts of the program that can run in parallel.

If we denote the fraction of the program that has to run synchronized with B (e.g. the number of lines that are executed synchronized) and if we denote the number of available processors with n, then Amdahl’s Law lets us compute an upper limit for the speedup our application may be able to achieve:

[Amdahl’s Law](https://it.wikipedia.org/wiki/Legge_di_Amdahl)

![\Large x=\frac{1}{(1-F)+{\frac{F}{N}}}](https://latex.codecogs.com/svg.latex?\Large&space;x=\frac{1}{(1-F)+{\frac{F}{N}}}) 

If we let n approach infinity, the term (1-B)/n converges against zero. Hence we can neglect this term and the upper limit for the speedup converges against 1/B, where B is the fraction of the program runtime before the optimization that is spent within non-parallelizable code. If B is for example 0.5, meaning that half of the program cannot be parallelized, the reciprocal value of 0.5 is 2; hence even if we add an unlimited number of processor to our application, we would only gain a speedup of about two. Now let’s assume we can rewrite the code such that only 0.25 of the program runtime is spent in synchronized blocks. Now the reciprocal value of 0.25 is 4, meaning we have built an application that would run with a large number of processors about four times faster than with only one processor.

So, in short, the target is to have the 1-B to lower as possible.
