
import java.util.*;

public class LLD_MusicSystem {
    public static void main(String[] args) {
        HashMap<String, User> userMap = new HashMap<>();
        
    }
}

class User {
    String userId;
    HashMap<String, Integer> songsFreq;
    List<SongFreq> freqList;
    ListNode head;      // head node of the recency list
    HashMap<String, ListNode> songsRecents;

    User(String userId) {
        songsFreq = new HashMap();
        this.userId = userId;
        head = null;
        songsRecents = new HashMap<>();
    }

    public boolean playSong(String songId) {
        songsFreq.put(songId, songsFreq.getOrDefault(songId, 0) + 1);
        
        // update recency list
        ListNode node = null;
        if(songsRecents.containsKey(songId)) {
            node = songsRecents.get(songId);
            node.freq++;
            if(node.next != null) node.next.prev = node.prev;
            if(node.prev != null) node.prev.next = node.next;
            node.prev = node.next = null;
        }
        else {
            node = new ListNode(songId, 1);
            songsRecents.put(songId, node);
        }
        node.next = head;
        if(head != null) head.prev = node;
        head = node;

        return true;
    }

    public List<String> getKMostPlayedSongs(int k) {
        freqList = new ArrayList<>(songsFreq.size());
        for(Map.Entry<String, Integer> m:songsFreq.entrySet()) {
            String key = m.getKey();
            int val = m.getValue();
            freqList.add(new SongFreq(key, val));
        }
        Collections.sort(freqList, (a, b) -> b.freq - a.freq);
        List<String> resList = new LinkedList<>();
        int idx = 0;
        while(k-- > 0) {
            resList.add(freqList.get(idx++).songId);
        }
        return resList;
    }

    public List<String> getKMostRecentlyPlayedSongs(int k) {
        ListNode ptr = head;
        List<String> resList = new LinkedList<>();
        while(k-- > 0 && ptr != null) {     
            resList.add(ptr.songId);
            ptr = ptr.next;
        }
        return resList;
    }

}

class SongFreq {
    String songId;
    int freq;

    public SongFreq(String songId, int freq) {
        this.songId = songId;
        this.freq = freq;
    }
}

class ListNode {
    String songId;
    ListNode prev, next;
    int freq;

    public ListNode(String songId, ListNode prev, ListNode next) {
        this.songId = songId;
        this.prev = prev;
        this.next = next;
    }

    ListNode(String songId, int freq) {
        this.songId = songId;
        this.freq = freq;
    }
}
