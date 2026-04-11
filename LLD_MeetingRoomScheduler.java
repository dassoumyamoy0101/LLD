import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class LLD_MeetingRoomScheduler {
    public static void main(String[] args) {
        MeetingRoomInitializer meetingRoomInitializer = new MeetingRoomInitializer();
        List<MeetingRoom> meetingRooms = meetingRoomInitializer.initializeMeetingRooms(new int[][]{{100, 5}, {50, 10}});

        int[][] requests  = {{8, 1230, 1400}, {11, 1330, 1530}};
        List<Integer> resList = new ArrayList<Integer>();

        ExecutorService executorService = Executors.newCachedThreadPool();

        for(int[] request:requests) {
            Future<Integer> future = executorService.submit(new MeetingRoomAllocatorService(meetingRooms, request));

            try {
                resList.add(future.get());
            } catch (Exception e) {
            }
        }

        executorService.shutdown();

        System.out.println(resList);

    }
}

class MeetingRoomAllocatorService implements Callable<Integer> {
    List<MeetingRoom> meetingRooms;
    int[] allocation;

    MeetingRoomAllocatorService(List<MeetingRoom> meetingRooms, int[] allocation) {
        this.meetingRooms = meetingRooms;
        this.allocation = allocation;
    }

    @Override
    public synchronized Integer call() throws Exception {
        int n = meetingRooms.size();
        for(int i=0; i<n; ++i) {
            MeetingRoom meetingRoom = meetingRooms.get(i);
            if(meetingRoom.capacity >= allocation[0]) {
                if(meetingRoom.checkAndAllocate(new int[]{allocation[1], allocation[2]})) {
                    return meetingRoom.roomId;
                }
            }
        }
        // cannot be allocated
        return -1;
    }
}

final class MeetingRoomInitializer {
    List<MeetingRoom> meetingRooms;
    int n;

    public MeetingRoomInitializer() {

    }

    // [roomId, capacity]
    public List<MeetingRoom> initializeMeetingRooms(int[][] rooms) {
        this.n = rooms.length;
        meetingRooms = new ArrayList<MeetingRoom>(n);
        for(int i=0; i<n; ++i) {
            MeetingRoom meetingRoom = new MeetingRoom(rooms[i][0], rooms[i][1]);
            meetingRooms.add(meetingRoom);
        }

        // sort based on capacity
        Collections.sort(meetingRooms, (a, b) -> {
            if(a.capacity == b.capacity) return a.roomId - b.roomId;
            return a.capacity - b.capacity;
        });

        return meetingRooms;
    }
}

class MeetingRoom {
    int roomId, capacity;
    boolean timeline[];

    public MeetingRoom() {
        timeline = new boolean[96];
    }

    public MeetingRoom(int roomId, int capacity) {
        this();
        this.roomId = roomId;
        this.capacity = capacity;
    }

    public boolean checkAndAllocate(int[] time) {
        int start = time[0], end = time[1], startIdx = (start/100) * 4 + (start%100) / 15, endIdx = (end/100) * 4 + (end%100) / 15;
        for(int i=startIdx; i<=endIdx; ++i) {
            if(timeline[i]) {
                return false;
            }
        }
        // can be allocated -- so allocate 
        for(int i=startIdx; i<=endIdx; ++i) {
            timeline[i] = true;
        }
        return true;
    }

    // use getters and setters for proper design
}


/*

Meeting room scheduler

Use case: We have a set of meeting rooms of varying capacity. Allocate the meeting room with least capacity that satisfies the allocation request.
Allocation requests would be treated in the format of incoming stream of data. 

Algorithm:

Use Line Sweep: Maintain a timeline for meeting rooms which represents the allocation of the room in that time. 
This time would be in the difference of 15 mins

So the length of the timeline array will be 24 hrs * 4 = 96 

Time can be treated in 24 hrs format for better translation with the timeline array index.
Time can be represented as 1845 

To get the time from the index:
idx -> time = (idx/4) * 100 + (idx%4) * 15

To get index from time:
time -> idx = (time/100) * 4 + (time%100) / 15

In the meeting rooms' collection, sort the rooms in the order of capacity. Where capacity matches, sort in the order of meeting room id.

While allocating a room for a specific time frame like [t1, t2], traverse all the meeting rooms from starting -> check the room that matches the capacity constraint -> check whether it is empty by traversing the line from t1 to t2 -> if empty, allocate and return, if not, continue loop.

Complexity: O(q * n * 96)

Classes:

MeetingRoom (int roomId, int capacity, boolean timeline[], boolean checkAndAllocate(int[] time))
MeetingRoomAllocatorService (List<MeetingRoom> meetingRooms, synchronized int allocateMeetingRoom(int[] time))
Singleton can be used for intialization of meetingRooms list


Extended functionalities:

User would be able to delete meetings -- input would be meeting room id and time
For this, we need a map storing all meeting room details as {roomId, meetingRooms_list_index}
For this, create another service as MeetingRoomDeallocatorService

*/