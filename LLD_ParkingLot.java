import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class LLD_ParkingLot {
    private static MultiLevelParkingLot multiLevelParkingLot;
    private static ParkingLotService parkingLotService;

    public static void main(String[] args) {
        multiLevelParkingLot = initializeMultiLevelParkingLot();
        parkingLotService = new ParkingLotService(multiLevelParkingLot);

        // for further calls, use as 
        parkingLotService.getAndAllocateParkingSpot(new Car("ABC"));     // use setter to set id
    }

    private static MultiLevelParkingLot initializeMultiLevelParkingLot() {
        MultiLevelParkingLot multiLevelParkingLot = new MultiLevelParkingLot();
        multiLevelParkingLot.initializeParkingSpotsPerLevel(new int[][]{{10, 20}, {15, 30}});
        return multiLevelParkingLot;
    }
}

class ParkingLotService {
    private MultiLevelParkingLot multiLevelParkingLot;
    ConcurrentHashMap<String, ParkedVehicleDetails> map;

    ParkingLotService(MultiLevelParkingLot multiLevelParkingLot) {
        this.multiLevelParkingLot = multiLevelParkingLot;
        map = new ConcurrentHashMap<>();
    }

    public synchronized int[] getAndAllocateParkingSpot(Vehicle vehicle) {
        int[] parkingSpot = null;

        switch (vehicle.getVehicleType()) {
            case VehicleType.CAR:
                if(multiLevelParkingLot.getAvailableCarSpots() > 0) {
                    List<ParkingLevel> parkingLevelList = multiLevelParkingLot.getParkingLevelList();
                    for(int i=0; i<multiLevelParkingLot.getLevels(); ++i) {
                        ParkingLevel parkingLevel = parkingLevelList.get(i);
                        if(parkingLevel.getAvailableCarParkingSpots() > 0) {
                            List<CarParkingSpot> carParkingSpots = parkingLevel.getCarParkingSpotList();
                            for(int j=0; j<carParkingSpots.size(); ++i) {
                                if(carParkingSpots.get(j).status == Status.AVAILABLE) {
                                    // allocate and add to map
                                    carParkingSpots.get(j).setStatus(Status.NOT_AVAILABLE);
                                    parkingLevel.setAvailableCarParkingSpots(parkingLevel.getAvailableCarParkingSpots() - 1);
                                    multiLevelParkingLot.setAvailableCarSpots(multiLevelParkingLot.getAvailableCarSpots() - 1);
                                    ParkedVehicleDetails pvd = new ParkedVehicleDetails(vehicle, LocalDateTime.now(), i, j);
                                    map.put(vehicle.getId(), pvd);
                                    parkingSpot = new int[]{i, j};
                                    break;
                                    // level, spot
                                }
                            } 
                        }
                    }
                }
                break;

            case VehicleType.BIKE:
                if(multiLevelParkingLot.getAvailableBikeSpots() > 0) {
                    List<ParkingLevel> parkingLevelList = multiLevelParkingLot.getParkingLevelList();
                    for(int i=0; i<multiLevelParkingLot.getLevels(); ++i) {
                        ParkingLevel parkingLevel = parkingLevelList.get(i);
                        if(parkingLevel.getAvailableBikeParkingSpots() > 0) {
                            List<BikeParkingSpot> bikeParkingSpots = parkingLevel.getBikeParkingSpotList();
                            for(int j=0; j<bikeParkingSpots.size(); ++i) {
                                if(bikeParkingSpots.get(j).status == Status.AVAILABLE) {
                                    // allocate and add to map
                                    bikeParkingSpots.get(j).setStatus(Status.NOT_AVAILABLE);
                                    parkingLevel.setAvailableBikeParkingSpots(parkingLevel.getAvailableBikeParkingSpots() - 1);
                                    multiLevelParkingLot.setAvailableBikeSpots(multiLevelParkingLot.getAvailableBikeSpots() - 1);
                                    ParkedVehicleDetails pvd = new ParkedVehicleDetails(vehicle, LocalDateTime.now(), i, j);
                                    map.put(vehicle.getId(), pvd);
                                    parkingSpot = new int[]{i, j};
                                    break;
                                    // level, spot
                                }
                            } 
                        }
                    }
                }
                break;
        }

        return parkingSpot;
    }

    public int leaveParkingSpotAndGetBill(Vehicle vehicle) {
        if(map.containsKey(vehicle.getId())) {
            ParkedVehicleDetails pvd = map.get(vehicle.getId());
            int level = pvd.level, spot = pvd.spot;
            VehicleType vehicleType = pvd.vehicle.getVehicleType();
            // calculate bill
            int bill = calculateBillDetails(level, spot, pvd.timeStamp, vehicleType);


            // mark the spot as empty
            if(vehicleType == VehicleType.BIKE) {
                multiLevelParkingLot.setAvailableBikeSpots(multiLevelParkingLot.getAvailableBikeSpots() + 1);
                multiLevelParkingLot.getParkingLevelList().get(level).setAvailableBikeParkingSpots(multiLevelParkingLot.getParkingLevelList().get(level).getAvailableBikeParkingSpots() + 1);
                multiLevelParkingLot.getParkingLevelList().get(level).getBikeParkingSpotList().get(spot).setStatus(Status.AVAILABLE);
            }
            else {
                multiLevelParkingLot.setAvailableCarSpots(multiLevelParkingLot.getAvailableCarSpots() + 1);
                multiLevelParkingLot.getParkingLevelList().get(level).setAvailableCarParkingSpots(multiLevelParkingLot.getParkingLevelList().get(level).getAvailableCarParkingSpots() + 1);
                multiLevelParkingLot.getParkingLevelList().get(level).getCarParkingSpotList().get(spot).setStatus(Status.AVAILABLE);
            }

            return bill;
        }

        return -1;
    }

    private int calculateBillDetails(int level, int spot, LocalDateTime from, VehicleType vehicleType) {
        LocalDateTime to = LocalDateTime.now();
        int diff = (int)(Duration.between(from, to).toHours());
        if(vehicleType == VehicleType.BIKE) return Math.max(70, 50*diff);
        return Math.max(100, 70*diff);
    }
}

class ParkedVehicleDetails {
    Vehicle vehicle;        // gives me id and type
    LocalDateTime timeStamp;
    int level, spot;

    ParkedVehicleDetails(Vehicle vehicle, LocalDateTime timeStamp, int level, int spot) {
        this.vehicle = vehicle;
        this.timeStamp = timeStamp;
        this.level = level;
        this.spot = spot;
    }
}

class MultiLevelParkingLot {
    private int levels, totalBikeSpots, totalCarSpots, availableBikeSpots, availableCarSpots;
    private List<ParkingLevel> parkingLevelList;

    MultiLevelParkingLot() {
    }

    public int getLevels() {
        return levels;
    }

    public int getAvailableBikeSpots() {
        return availableBikeSpots;
    }

    public int getAvailableCarSpots() {
        return availableCarSpots;
    }

    public int getTotalBikeSpots() {
        return totalBikeSpots;
    }

    public int getTotalCarSpots() {
        return totalCarSpots;
    }

    public List<ParkingLevel> getParkingLevelList() {
        return parkingLevelList;
    }

    // [bike, car]
    public void initializeParkingSpotsPerLevel(int[][] parkingSpots) {
        this.levels = parkingSpots.length;
        parkingLevelList = new ArrayList<>(levels);

        for(int level=0; level<levels; ++level) {
            ParkingLevel parkingLevel = new ParkingLevel(level, parkingSpots[level][0], parkingSpots[level][1]);
            parkingLevelList.add(parkingLevel);
            totalBikeSpots += parkingSpots[level][0];
            totalCarSpots += parkingSpots[level][1];
            availableBikeSpots = totalBikeSpots;
            availableCarSpots = totalCarSpots;
        }
    }

    public void addNewLevel(int[] parkingSpots) {
        // add a new level to the existing multiparking and update the total and availability count

    }

    public void setAvailableBikeSpots(int availableBikeSpots) {
        this.availableBikeSpots = availableBikeSpots;
    }

    public void setAvailableCarSpots(int availableCarSpots) {
        this.availableCarSpots = availableCarSpots;
    }
}

class ParkingLevel {
    private int level, totalBikeParkingSpots, totalCarParkingSpots, availableBikeParkingSpots, availableCarParkingSpots;
    private List<BikeParkingSpot> bikeParkingSpotList;
    private List<CarParkingSpot> carParkingSpotList;

    public ParkingLevel(int level, int totalBikeParkingSpots, int totalCarParkingSpots) {
        this.level = level;
        this.totalBikeParkingSpots = totalBikeParkingSpots;
        this.totalCarParkingSpots = totalCarParkingSpots;
        this.availableBikeParkingSpots = totalBikeParkingSpots;
        this.availableCarParkingSpots = totalCarParkingSpots;
        this.setBikeParkingSpotList();
        this.setCarParkingSpotList();
    }

    public int getAvailableBikeParkingSpots() {
        return availableBikeParkingSpots;
    }

    public int getAvailableCarParkingSpots() {
        return availableCarParkingSpots;
    }

    public List<BikeParkingSpot> getBikeParkingSpotList() {
        return bikeParkingSpotList;
    }

    public List<CarParkingSpot> getCarParkingSpotList() {
        return carParkingSpotList;
    }

    public int getLevel() {
        return level;
    }

    public int getTotalBikeParkingSpots() {
        return totalBikeParkingSpots;
    }

    public int getTotalCarParkingSpots() {
        return totalCarParkingSpots;
    }

    public void setAvailableBikeParkingSpots(int availableBikeParkingSpots) {
        this.availableBikeParkingSpots = availableBikeParkingSpots;
    }

    public void setAvailableCarParkingSpots(int availableCarParkingSpots) {
        this.availableCarParkingSpots = availableCarParkingSpots;
    }

    private void setBikeParkingSpotList() {
        this.bikeParkingSpotList = new ArrayList<>(this.totalBikeParkingSpots);
        for(int i=0; i<totalBikeParkingSpots; ++i) {
            bikeParkingSpotList.add(new BikeParkingSpot(i));
        }
    }

    private void setCarParkingSpotList() {
        this.carParkingSpotList = new ArrayList<>(this.totalCarParkingSpots);
        for(int i=0; i<totalCarParkingSpots; ++i) {
            carParkingSpotList.add(new CarParkingSpot(i));
        }
    }
}

abstract class ParkingSpot {
    protected int id;
    protected VehicleType vehicleType;
    protected Status status;
}

class BikeParkingSpot extends ParkingSpot {

    public BikeParkingSpot(int id) {
        this.id = id;
        vehicleType = VehicleType.BIKE;
        status = Status.AVAILABLE;
    }

    public int getId() {
        return id;
    }

    public VehicleType getVehicleType() {
        return vehicleType;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }
}

class CarParkingSpot extends ParkingSpot {

    public CarParkingSpot(int id) {
        this.id = id;
        vehicleType = VehicleType.CAR;
        status = Status.AVAILABLE;
    }

    public int getId() {
        return id;
    }

    public VehicleType getVehicleType() {
        return vehicleType;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }
}

class Bike extends Vehicle {

    public Bike(String id) {
        this.id = id;
        this.vehicleType = VehicleType.BIKE;
    }
}

class Car extends Vehicle {

    public Car(String id) {
        this.id = id;
        this.vehicleType = VehicleType.CAR;
    }
}

abstract class Vehicle {
    protected  String id;
    protected  VehicleType vehicleType;

    public String getId() {
        return id;
    }

    public VehicleType getVehicleType() {
        return vehicleType;
    }
}

enum VehicleType {
    BIKE, CAR
}

enum Status {
    AVAILABLE, NOT_AVAILABLE
}

/*

Design discussions:

- Multi level parking lot
- Vehicles for now: bike and car
- every level can have separate parking spot configs
- should be able to calculate the cost
- check and allocate parking lot in realtime, multi threaded
- persist details in db reg the parked cars, their bills, in and out time, lot info
- create extensible solution

Approach:

enum VehicleType {BIKE, CAR}
enum Status {AVAILABLE, NOT_AVAILABLE}
abstract class Vehicle (contains Vehicle ID, Vehicle Type) <---- Car, Bike
abstract class ParkingSpot (contains Spot ID, Status, Vehicle Type) <---- CarParkingSpot, BikeParkingSpot
class ParkingLevel (contains Level ID, List of CarParkingSpots, BikeParkingSpots, total and running count of bike and car spots for that level)
class MultiLevelParkingLot (contains List of ParkingLevels, total and running count of bike and car spots across all levels)
utility class ParkedVehicleDetails for storing in the map (map is for tracking the entry time of vehicles. Instead this can also be done by using a variable in ParkingSpot class that stores the allocation time. As the spot will be allocated to only one vehicle at a time, so it would be fine.)
service class ParkingLotService to allocate and deallocate parking (insteda of this service, we can add the utility methods in the MultiLevelParkingLot class)


*/