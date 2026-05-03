
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class LLD_KVStoreTrans {
    public static void main(String[] args) {
        KVStore kvStore = new KVStore();

    }

}

class KVStore {
    ConcurrentHashMap<String, String> map;

    public KVStore() {
        map = new ConcurrentHashMap<>();
    }

    public boolean processTransaction(List<String> transactions) {
        boolean transactionStarted = false, isCommitted = false;
        HashMap<String, String> tempKVStore = null;
        for(String t:transactions) {    
            StringTokenizer st = new StringTokenizer(t, " ");
            String operation = st.nextToken();
            if(operation.equalsIgnoreCase("BEGIN")) {
                transactionStarted = true;
                tempKVStore = new HashMap<>();
            }
            else if(operation.equalsIgnoreCase("COMMIT")) {
                if(!transactionStarted) {
                    return false;
                }
                for(Map.Entry<String, String> m:tempKVStore.entrySet()) {
                    String key = m.getKey(), val = m.getValue();
                    if(val.equals("NULLDELETE")) {
                        delete(key);
                    }
                    else {
                        map.put(key, val);
                    }
                }
                isCommitted = true;
            }
            else if(operation.equalsIgnoreCase("ROLLBACK")) {
                if(!isCommitted) {
                    tempKVStore = null;
                    return true;
                }
                return false;
            }
        }
        return true;
    }

    public boolean add(String key, String value) {
        if(map.containsKey(key)) {
            return false;
        }
        map.put(key, value);
        return true;
    }

    public boolean update(String key, String value) {
        if(!map.containsKey(key)) {
            return false;
        }
        map.put(key, value);
        return true;
    }

    public boolean delete(String key) {
        if(!map.containsKey(key)) {
            return false;
        }
        map.remove(key);
        return true;
    }
}