import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class LLD_RateLimiter {
    public static void main(String[] args) {
        
    }

    private static void tokenBucketAlgo() {
        ConcurrentHashMap<String, Integer> tokenMap = new ConcurrentHashMap<>();
        int maxTokens = 5;

        Thread tokenGenerator = new Thread(new GenerateTokensForTokenBucket(tokenMap, maxTokens));
        tokenGenerator.setPriority(Thread.MAX_PRIORITY);
        tokenGenerator.start();

        // process every request in new thread, like controller
        for(int i=0; i<100; ++i) {
            Request request = new Request("sessionId-" + (i%10), "Print " + i);
            Thread tokenBucketThread = new Thread(new TokenBucket(request, tokenMap));
            tokenBucketThread.start();
        }
    }

    private static void leakyBucketAlgo() {
        ConcurrentHashMap<String, Integer> reqCntMap = new ConcurrentHashMap<>();
        int bucketSize = 5;

        // process every request in new thread, like controller
        for(int i=0; i<100; ++i) {
            Request request = new Request("sessionId-" + (i%10), "Print " + i);
            Thread leakyBucketThread = new Thread(new LeakyBucket(reqCntMap, request, bucketSize));
            leakyBucketThread.start();
        }
    }
}

class LeakyBucket implements Runnable {
    ConcurrentHashMap<String, Integer> reqCntMap;
    Request request;
    final int bucketSize;
    
    public LeakyBucket(ConcurrentHashMap<String, Integer> reqCntMap, Request request, int bucketSize) {
        this.reqCntMap = reqCntMap;
        this.request = request;
        this.bucketSize = bucketSize;
    }

    @Override
    public void run() {
        String sessionId = request.sessionId;
        synchronized (reqCntMap) {
            while(reqCntMap.containsKey(sessionId) && reqCntMap.get(sessionId) >= bucketSize) {
                try {
                    reqCntMap.wait();
                } catch (InterruptedException e) {
                }
            }
            reqCntMap.put(sessionId, reqCntMap.getOrDefault(sessionId, 0) + 1);
            this.processTask();
            reqCntMap.notifyAll();
        }
    }

    private void processTask() {
        synchronized (reqCntMap) {
            String sessionId = request.sessionId;
            reqCntMap.put(sessionId, reqCntMap.get(sessionId) - 1);
            System.out.println(request.toDo);
            reqCntMap.notifyAll();
        }
    }
}

class TokenBucket implements Runnable {
    final Request request;
    final ConcurrentHashMap<String, Integer> tokenMap;

    TokenBucket(Request request, ConcurrentHashMap<String, Integer> tokenMap) {
        this.request = request;
        this.tokenMap = tokenMap;
    }

    @Override
    public void run() {
        String sessionId = request.sessionId;
        if(tokenMap.containsKey(sessionId)) {
            synchronized (tokenMap) {
                while(tokenMap.get(sessionId) == 0) {
                    try {
                        tokenMap.wait();
                    } catch (InterruptedException e) {
                    }
                }
                // process req
                this.processTask();
                tokenMap.put(sessionId, tokenMap.get(sessionId) - 1);
                tokenMap.notifyAll();
            }
        }
        else {
            tokenMap.put(sessionId, 0);
        }
    }

    private void processTask() {
        System.out.println(request.toDo);
    }
}

class GenerateTokensForTokenBucket implements Runnable {
    final ConcurrentHashMap<String, Integer> tokenMap;
    final int maxTokens;

    GenerateTokensForTokenBucket(ConcurrentHashMap<String, Integer> tokenMap, int maxTokens) {
        this.tokenMap = tokenMap;
        this.maxTokens = maxTokens;
    }

    @Override
    public void run() {
        while(true) {
            synchronized (tokenMap) {
                Set<String> sessionIds = tokenMap.keySet();
                for(String sessionId:sessionIds) {
                    tokenMap.put(sessionId, maxTokens);
                }
                try {
                    tokenMap.wait(1000);
                } catch (InterruptedException e) {
                }
            }
        }
    }
}

class Request {
    String sessionId;
    String toDo;

    public Request() {
    }

    public Request(String sessionId, String toDo) {
        this.sessionId = sessionId;
        this.toDo = toDo;
    }
}

/*

Leaky Bucket
Token Bucket

*/


