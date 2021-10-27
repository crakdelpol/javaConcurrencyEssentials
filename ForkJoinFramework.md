# Fork/Join Framework

The base class of the Fork/Join Framework is java.util.concurrent.ForkJoinPool.
This class implements the two interfaces Executor and ExecutorService and subclasses the AbstractExecutorService.
While a call of fork() will start an asynchronous execution of the task, a call of join() will wait until the task has finished and retrieve its result.
Hence we can split a given task into multiple smaller tasks, fork each task and finally wait for all tasks to finish. This makes the implementation of complex problems easier.
In computer science this approach is also known as divide-and-conquer approach.

```
if(problem.getSize() > THRESHOLD) {
  SmallerProblem smallerProblem1 = new SmallerProblem();  
  smallerProblem1.fork();
  SmallerProblem smallerProblem2 = new SmallerProblem();
  smallerProblem2.fork();
  return problem.solve(smallerProblem1.join(), smallerProblem1.join());
} else { 
  return problem.solve();
}
```
RecursiveTask
Example: 

```
import java.util.Random;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

public class FindMin extends RecursiveTask<Integer> {
    private final int[] numbers;
    private final int startIndex;
    private final int endIndex;

    public FindMin(int[] numbers, int startIndex, int endIndex) {
        System.out.println("Costructor with " + startIndex + "end index" + endIndex);
        this.numbers = numbers;
        this.startIndex = startIndex;
        this.endIndex = endIndex;
    }

    @Override
    protected Integer compute() {
        int sliceLength = (endIndex - startIndex) + 1;
        if (sliceLength > 2) {
            FindMin lowerFindMin = new FindMin(numbers, startIndex, startIndex + (sliceLength / 2) - 1);
            lowerFindMin.fork();
            FindMin upperFindMin = new FindMin(numbers, startIndex + (sliceLength / 2), endIndex);
            upperFindMin.fork();
            return Math.min(lowerFindMin.join(), upperFindMin.join());
        } else {
            return Math.min(numbers[startIndex], numbers[endIndex]);
        }
    }

    public static void main(String[] args) {
        int[] numbers = new int[100];
        Random random = new Random(System.currentTimeMillis());
        for (int i = 0; i < numbers.length; i++) {
            numbers[i] = random.nextInt(100);
        }

        ForkJoinPool pool = new ForkJoinPool(Runtime.getRuntime().availableProcessors());
        Integer min = pool.invoke(new FindMin(numbers, 0, numbers.length - 1));
        System.out.println(min);
    }
}
```

### RecursiveAction

As mentioned above next to RecursiveTask we also have the class RecursiveAction. In contrast to RecursiveTask it does not have to return a value, hence it can be used for asynchronous computations that can be directly performed on a given data structure.

Such an example is the computation of a grayscale image out of a colored image. All we have to do is to iterate over each pixel of the image and compute the grayscale value out of the RGB value using the following formula:
```gray = 0.2126 * red + 0.7152 * green + 0.0722 * blue```


```
for (int row = 0; row < height; row++) {
        for (int column = 0; column < bufferedImage.getWidth(); column++) {
            int grayscale = computeGrayscale(image.getRGB(column, row));
            image.setRGB(column, row, grayscale)
        }
}

```
The above implementation works fine on a single CPU machine. But if we have more than one CPU available, we might want to distribute this work to the available cores.

```
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

class GrayscaleImageAction extends RecursiveAction {

    private int row;
    private BufferedImage bufferedImage;

    public GrayscaleImageAction(int row, BufferedImage bufferedImage) {
        this.row = row;
        this.bufferedImage = bufferedImage;
    }

    @Override
    protected void compute() {
        for (int column = 0; column < bufferedImage.getWidth(); column++) {
            int rgb = bufferedImage.getRGB(column, row);
            int r = (rgb >> 16) & 0xFF;
            int g = (rgb >> 8) & 0xFF;
            int b = (rgb & 0xFF);
            int gray = (int) (0.2126 * (float) r + 0.7152 * (float) g + 0.0722 * (float) b);
            gray = (gray << 16) + (gray << 8) + gray;
            bufferedImage.setRGB(column, row, gray);
        }
    }

    public static void main(String[] args) throws IOException {
        ForkJoinPool pool = new ForkJoinPool(Runtime.getRuntime().availableProcessors());
        BufferedImage bufferedImage = ImageIO.read(new File(args[0]));

        for (int row = 0; row < bufferedImage.getHeight(); row++) {
            GrayscaleImageAction action = new GrayscaleImageAction(row, bufferedImage);
            pool.execute(action);
        }
        pool.shutdown();
        ImageIO.write(bufferedImage, "jpg", new File(args[1]));

    }
}
```

### ForkJoinPool and ExecutorService

```
import java.util.Random;
import java.util.concurrent.*;

class FindMinTask implements Callable<Integer> {

    private int[] numbers;
    private int startIndex;
    private int endIndex;
    private ExecutorService executorService;

    public FindMinTask(ExecutorService executorService, int[] numbers, int startIndex, int endIndex) {
        this.numbers = numbers;
        this.startIndex = startIndex;
        this.endIndex = endIndex;
        this.executorService = executorService;
    }

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        int[] numbers = new int[100];
        Random random = new Random(System.currentTimeMillis());
        for (int i = 0; i < numbers.length; i++) {
            numbers[i] = random.nextInt(100);
        }
        ExecutorService executorService = Executors.newFixedThreadPool(64); Future<Integer> futureResult = executorService.submit(new FindMinTask( executorService, numbers, 0, numbers.length-1));
        System.out.println(futureResult.get());
        executorService.shutdown();
    }

    @Override
    public Integer call() throws Exception {
        int sliceLength = (endIndex - startIndex) + 1;
        if (sliceLength > 2) {
            FindMinTask lowerFindMin = new FindMinTask(executorService, numbers, startIndex, startIndex + (sliceLength / 2) - 1);
            Future<Integer> futureLowerFindMin = executorService.submit(lowerFindMin);
            FindMinTask upperFindMin = new FindMinTask(executorService, numbers , startIndex + (sliceLength / 2), endIndex);
            Future<Integer> futureUpperFindMin = executorService.submit(upperFindMin);
            return Math.min(futureLowerFindMin.get(), futureUpperFindMin.get());
        } else {
            return Math.min(numbers[startIndex], numbers[endIndex]);

        }
    }
}
```
The code looks very similar, expect the fact that we submit() our tasks to the ExecutorService and then use the returned instance of Future to wait() for the result. The main difference between both implementations can be found at the point where the thread pool is constructed. In the example above, we create a fixed thread pool with 64(!) threads. Why did I choose such a big number? The reason here is, that calling get() for each returned Future block the current thread until the result is available. If we would only provide as many threads to the pool as we have CPUs available, the program would run out of resources and hang indefinitely.
The ForkJoinPool implements the already mentioned work-stealing strategy, i.e. every time a running thread has to wait for some result; the thread removes the current task from the work queue and executes some other task ready to run. This way the current thread is not blocked and can be used to execute other tasks. Once the result for the originally suspended task has been computed the task gets executed again and the join() method returns the result. This is an important difference to the normal ExecutorService where you would have to block the current thread while waiting for a result.
