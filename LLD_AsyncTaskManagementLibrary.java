import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class LLD_AsyncTaskManagementLibrary {
    public static void main(String[] args) {
        // enter the tasks in a list
        List<Task> tasks = new ArrayList<Task>();

        tasks.add(new Add(2, 5, 100, 5, "2plus5"));
        tasks.add(new Add(10, 20, 50, 10, "10plus20"));
        tasks.add(new Product(20, 30, 120, 12, "20into30"));

        // enter the dependencies of tasks
        int[][] taskDependencies = new int[0][2];

        scheduleTasks(taskDependencies, tasks);

        // read tasks logs
        TaskInfoLogger.getLogMap();
    }

    private static void scheduleTasks(int[][] taskDependencies, List<Task> tasks) {
        int n = tasks.size(), indegree[] = new int[n];
        List<List<Integer>> adjList = new ArrayList<>(n);
        for(int i=0; i<n; ++i) {
            adjList.add(new ArrayList<Integer>());
        }
        for(int[] taskDepenedency:taskDependencies) {
            int u = taskDepenedency[0], v = taskDepenedency[1];
            adjList.get(u).add(v);
            ++indegree[v];
        }

        // heap for PBS
        PriorityQueue<Integer> maxHeap  = new PriorityQueue<>((a, b) -> tasks.get(b).getPriority() - tasks.get(a).getPriority());

        for(int i=0; i<n; ++i) {
            if(indegree[i] == 0) {
                maxHeap.add(i);
            }
        }

        int availableThreads = 8;

        while(!maxHeap.isEmpty()) {
            int size = maxHeap.size();

            List<CompletableFuture> completableFutures = new ArrayList<>();

            while(size-- > 0 && availableThreads-- > 0) {
                int topNode = maxHeap.poll();
                for(int next:adjList.get(topNode)) {
                    --indegree[next];
                    if(indegree[next] == 0) {
                        maxHeap.add(next);
                    }
                }
                Task task = tasks.get(topNode);
                CompletableFuture completableFuture = CompletableFuture.runAsync(new AsyncTaskExecutor(task));
                completableFutures.add(completableFuture);
            }

            availableThreads += completableFutures.size();
            CompletableFuture.allOf(completableFutures.toArray(new CompletableFuture[completableFutures.size()])).join();
        }
    }
}

final class TaskInfoLogger {
    private static ConcurrentHashMap<Integer, TaskLogInformation> logMap;

    // prevent users from instantiating this class
    private TaskInfoLogger() {
    }

    public static ConcurrentHashMap<Integer, TaskLogInformation> getLogMap() {
        if(logMap == null) {
            synchronized (TaskInfoLogger.class) {
                logMap = new ConcurrentHashMap<>();
            }
        }
        return logMap;
    }
}

class AsyncTaskExecutor implements Runnable {
    private Task task;
    private ConcurrentHashMap<Integer, TaskLogInformation> logMap;

    public AsyncTaskExecutor() {
    }

    public AsyncTaskExecutor(Task task) {
        this.task = task;
        logMap = TaskInfoLogger.getLogMap();
    }

    @Override
    public void run() {
        task.execute();
        this.logInformationAboutTask();
    }

    private void logInformationAboutTask() {
        Integer taskId = task.getTaskId();
        TaskLogInformation taskLogInformation = new TaskLogInformation(taskId, task.getExecutionStartTime(), task.getExecutionCompletionTime(), Thread.currentThread().getName());
        logMap.put(taskId, taskLogInformation);
    }
}

class TaskLogInformation {
    private Integer taskId;
    private LocalDateTime executionStartTime, executionEndTime;
    private String threadName;

    public TaskLogInformation(Integer taskId, LocalDateTime executionStartTime, LocalDateTime executionEndTime, String threadName) {
        this.taskId = taskId;
        this.executionStartTime = executionStartTime;
        this.executionEndTime = executionEndTime;
        this.threadName = threadName;
    }

    // declare getters for the above variables
}

class Add extends Task {
    private int a, b;
    
    public Add(int a, int b) {
        this.a = a;
        this.b = b;
    }

    public Add(int a, int b, int taskId, int priority, String taskName) {
        super(taskId, priority, taskName);
        this.a = a;
        this.b = b;
    }

    @Override
    public void execute() {
        this.setExecutionStartTime(LocalDateTime.now());
        System.out.println("Addition of " + a + " and " + b + " = " + (a + b));
        this.setExecutionCompletionTime(LocalDateTime.now());
    }
}

class Product extends Task {
    private int a, b;

    public Product(int a, int b) {
        this.a = a;
        this.b = b;
    }

    public Product(int a, int b, int taskId, int priority, String taskName) {
        super(taskId, priority, taskName);
        this.a = a;
        this.b = b;
    }

    @Override
    public void execute() {
        this.setExecutionStartTime(LocalDateTime.now());
        System.out.println("Product of " + a + " and " + b + " = " + (a * b));
        this.setExecutionCompletionTime(LocalDateTime.now());
    }
}

abstract class Task {
    private int taskId, priority;
    private String taskName;
    private LocalDateTime executionStartTime, executionCompletionTime;

    abstract public void execute();

    public Task() {

    }

    public Task(int taskId, int priority, String taskName) {
        this.taskId = taskId;
        this.priority = priority;
        this.taskName = taskName;
    }

    public int getPriority() {
        return priority;
    }

    public int getTaskId() {
        return taskId;
    }

    public String getTaskName() {
        return taskName;
    }

    public LocalDateTime getExecutionCompletionTime() {
        return executionCompletionTime;
    }

    public LocalDateTime getExecutionStartTime() {
        return executionStartTime;
    }

    public void setExecutionCompletionTime(LocalDateTime executionCompletionTime) {
        this.executionCompletionTime = executionCompletionTime;
    }

    public void setExecutionStartTime(LocalDateTime executionStartTime) {
        this.executionStartTime = executionStartTime;
    }
}


/*

Asynchronous Task Management Library:

- Should be able to schedule tasks based on dependency on other tasks
- Should be able to schedule the independent tasks based on assigned priority

For the input format -- I will have [A, B] -- this means -- A -> B

So create the DAG and use topo sort to schedule the tasks initially. Then use heap to do the PBS.

Class structure is quite simple.

abstract class Task (taskId, priority, taskName, execute()) <------ Add, Product and other task classes
class AsyncTaskExecutor implements Runnable => Creates a new thread everytime containing the specific Task object

Concepts used: CompletableFuture to schedule threads in the background and wait for their completion.

Additional feature of logging/persisting task information has been done. 

*/