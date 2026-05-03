import java.util.*;

public class LLD_DeliveryPaymentService {
    public static void main(String[] args) {
        
    }
}

class DeliveryPaymentService {
    HashMap<String, Driver> driversMap;
    PriorityQueue<TripRequest> unpaidTrips;
    List<TripRequest> paidTrips;

    public DeliveryPaymentService() {
        driversMap = new HashMap<>();
        unpaidTrips = new PriorityQueue<>((a, b) -> a.trip.endTime - b.trip.endTime);
        paidTrips = new LinkedList<>();
    }

    public void initialiseDrivers() {
        for(int i=0; i<10; ++i) {
            String driverId = "D"+i;
            Driver driver = new Driver(driverId, 10+i);
            driversMap.put(driverId, driver);
        }
    }
    
    public void allocateDriver(String driverId, Trip trip) {
        Driver driver = driversMap.get(driverId);
        TripRequest tripRequest = new TripRequest(trip, driver);
        unpaidTrips.add(tripRequest);
        driver.tripsServed++;
        driver.trips.add(trip);
    }

    // return the total payments thus made
    public double payTripsTillEndTime(int targetEndTime) {
        double total = 0.0;
        while(!unpaidTrips.isEmpty() && unpaidTrips.peek().trip.endTime <= targetEndTime) {
            TripRequest tripRequest = unpaidTrips.poll();
            int diff = tripRequest.trip.endTime - tripRequest.trip.startTime, mins = (diff/100) * 60 + (diff%100);
            double amt = tripRequest.driver.costPerHr * (mins / 60);
            tripRequest.driver.totalEarned += amt;
            tripRequest.driver.paidTrips.add(tripRequest.trip);
            total += amt;
            paidTrips.add(tripRequest);
        }
        return total;
    }

    // getPaymentsMadeOverPast24Hrs
    // getPaymentPerDriverMadeOverPast24Hrs
}

class TripRequest {
    Trip trip;
    Driver driver;

    public TripRequest(Trip trip, Driver driver) {
        this.trip = trip;
        this.driver = driver;
    }
}

class Driver {
    String driverId;
    double costPerHr, totalEarned;
    int tripsServed = 0;
    List<Trip> trips;
    List<Trip> paidTrips;

    Driver(String driverId, double costPerHr) {
        this.driverId = driverId;
        this.costPerHr = costPerHr;
        totalEarned = 0.0;
        tripsServed = 0;
        trips = new LinkedList<>();
        paidTrips = new LinkedList<>();
    }



}

class Trip {
    String tripId;
    int startTime, endTime;

}

/*

class Driver --
class Trip -- 

class DeliveryPaymentService -- requirements -- allocateDriver(Trip trip, Driver driver), payTrips()

shall i use any strategy to allocate the driver?


*/
