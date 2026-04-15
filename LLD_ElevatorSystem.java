import java.util.*;

public class LLD_ElevatorSystem {
    public static void main(String[] args) {
        
    }
}

class Floor {
    int floorNo;
    Button up, down;

}

class ElevatorCar {
    ElevatorState elevatorState;
    int carNo, maxCapacity, currentCapacity;

    public int getDistanceFromFloor(int floor) {

    }


}

class ElevatorSystem {
    List<Floor> floors = new ArrayList<>(25);
    List<ElevatorCar> elevatorCars = new ArrayList<>(5);

    public int allocateElevatorCar(int srcFloorId, int destFloorId) {

    }

}

class MonitoringSystem {
    // for emergencies
    // for monitoring and analytics

}

abstract class Button {
    ButtonState buttonState;

    Button() {
        this.buttonState = ButtonState.OFF;
    }
}

class FloorButton extends Button {
    

    public FloorButton() {
    }
}

class ElevatorCarButton extends Button {

}

enum ElevatorState {
    UP, DOWN, IDLE
}

enum ButtonState {
    ON, OFF
}

enum DirectionButtonState {
    UP, DOWN
}


/*

Elevator System Design

Classes:

abstract class Button
class FloorButton extends Button
class ElevatorCarButton extends Button




*/
