import java.util.*;

public class Client {

    public static Random generator = new Random();
    public static final int NUM_STATIONS = 5;
    public static final int TRAIN_CAPACITY = 50;
    public static final int TRAIN_INTERVAL = 10;
    public static final int TIME_INTERVAL = 200;
    public static int trainCount = 0;
    public static int passengersOnTrains = 0;
    public static int passengersDelivered = 0;
    public static void main(String[] args) {
        List<Station> trainStations = new ArrayList<>();
        QueueInterface<Train> trainList = new LinkedQueue<>();
        QueueInterface<Passenger> passengerList = new LinkedQueue<>();
        int passengersCreated = 0, passengersWaiting = 0;
        // Generate all stations
        createStations(trainStations);
        for (int time = 0; time < TIME_INTERVAL; time++) {
            // The number of passengers waiting is equivalent to anyone who isn't currently on a train and hasn't been delivered yet.
            passengersWaiting = passengersCreated - passengersOnTrains - passengersDelivered;
            System.out.println("Time: " + time + " Waiting: " + passengersWaiting + " On trains: " + passengersOnTrains);
            startTrain(time, trainList);
            // Simulate the existence of new passengers by generating them and adding them to the total count of created.
            passengersCreated += generatePassengers(trainStations, passengerList, time);
            moveTrains(trainList, trainStations, time);
        }
        finalReport(TIME_INTERVAL, passengersCreated, passengerList);
    }

    public static void createStations(List<Station> stations) {
        int min = 5, max = 14;
        for (int i = 0; i < NUM_STATIONS; i++) {
            // Generates a station with a time to the next between the specified range of 5 - 14 ticks.
            int timeToNext = generator.nextInt(max - min + 1) + min;
            stations.add(new Station(timeToNext));
            System.out.println("A new station has been added. The time to the next station is " + timeToNext + " ticks.");
        }
    }

    public static void startTrain(int time, QueueInterface<Train> trains) {
        // If the current time is divisible by the train interval then add a new train to the queue and increase the train count.
        if (time % TRAIN_INTERVAL == 0) {
            trains.enqueue(new Train(TRAIN_CAPACITY));
            trainCount++;
        }
    }

    public static int generatePassengers(List<Station> stations, QueueInterface<Passenger> passengerQueue, int time) {
        int passengers = generator.nextInt(6);
        for (int i = 0; i < passengers; i++) {
            int start = 0, stop = 0;
            // Continue generating random starting and stopping stations for every passenger until the starting station is less than the ending station.
            while (start >= stop) {
                start = generator.nextInt(NUM_STATIONS);
                stop = generator.nextInt(NUM_STATIONS);
            }
            // Passenger's first argument is stored as startedWaiting, which waitTime() subtracts from the clock, so it must be the current time. (The print message of Passenger calls it "station," but it shows the time.)
            Passenger p = new Passenger(time, stop);
            stations.get(start).addPassenger(p);
            passengerQueue.enqueue(p);
        }
        return passengers;
    }

    public static void moveTrains(QueueInterface<Train> trains, List<Station> stations, int clock) {
        int numTrains = trainCount;
        for (int i = 0; i < numTrains; i++) {
            // Dequeue the train in the front and retrieve it.
            Train t = trains.dequeue();
            // Simulate the train moving closer to its next station.
            t.move();
            // The train has arrived at a station.
            if (t.timeToNext() == 0) {
                // The train has arrived, so its next station is its current station.
                int stationNum = t.nextStation();
                // Retrieve the station to unload the passengers on the current train, and load the passengers waiting at the station into the train.
                Station s = stations.get(stationNum);
                int leaving = t.unloadPassengers(stationNum);
                int entering = t.loadPassengers(s, clock);
                passengersDelivered += leaving;
                passengersOnTrains += (entering - leaving);
                // This train is leaving its station, so set the travel time needed to reach the next station and advance its next station.
                t.updateStation(s.getTimeToNextStation());
            }
            // Keep putting the train back into the queue while it still has stations on its path, otherwise it has completed its route.
            if (t.nextStation() < NUM_STATIONS) {
                trains.enqueue(t);
            }
            else {
                trainCount--;
            }
        }
    }


    /** Reports the final situations of the trains and passengers waiting and some statistics for passengers' wait times.
    @param clock The time that train operations have ceased. */
    public static void finalReport(int clock, int passengersCreated, QueueInterface<Passenger> passengers) {
        System.out.println("Final Report");
        System.out.println("The total number of passengers is " + passengersCreated);
        System.out.println("The number of passengers currently on a train " + passengersOnTrains);
        System.out.println("The number of passengers delivered is " + passengersDelivered);
        int waitBoardedSum = 0;
        int waitNotBoardedSum = 0;
        for (int i=0; i < passengersCreated; i++) {
            Passenger p = passengers.dequeue();
            if(p.boarded())
                waitBoardedSum += p.waitTime(clock);
            else
                waitNotBoardedSum += p.waitTime(clock);
        } // end for
        System.out.println("The average wait time for passengers " + "that have boarded is");
        System.out.println((double)waitBoardedSum/(passengersOnTrains + passengersDelivered));
        System.out.println("The average wait time for passengers " + "that have not yet boarded is");
        System.out.println((double)waitNotBoardedSum / (passengersCreated - passengersOnTrains - passengersDelivered));
    } // end finalReport
}

// Note: In "Created passenger at station X heading to Y", X is the clock tick when the passenger was created (the provided passenger class however labels it as "station".)
// Note: Each run was checked: total passengers = delivered + on trains + still waiting, and average wait times stayed around 3-5 ticks across all 3 runs.

/* Program Output:
Run #1:
A new station has been added. The time to the next station is 12 ticks.
A new station has been added. The time to the next station is 5 ticks.
A new station has been added. The time to the next station is 9 ticks.
A new station has been added. The time to the next station is 14 ticks.
A new station has been added. The time to the next station is 11 ticks.
Time: 0 Waiting: 0 On trains: 0
        Created train 1
        Created passenger at station 0 heading to 2
        Train 1 unloaded 0 passengers at station 0
                loaded 0 passengers; Space left 50
Time: 1 Waiting: 1 On trains: 0
        Created passenger at station 1 heading to 4
        Created passenger at station 1 heading to 1
        Created passenger at station 1 heading to 4
        Created passenger at station 1 heading to 4
        Created passenger at station 1 heading to 1
Time: 2 Waiting: 6 On trains: 0
        Created passenger at station 2 heading to 2
        Created passenger at station 2 heading to 4
        Created passenger at station 2 heading to 3
        Created passenger at station 2 heading to 4
Time: 3 Waiting: 10 On trains: 0
Time: 4 Waiting: 10 On trains: 0
        Created passenger at station 4 heading to 4
        Created passenger at station 4 heading to 4
        Created passenger at station 4 heading to 3
Time: 5 Waiting: 13 On trains: 0
        Created passenger at station 5 heading to 3
        Created passenger at station 5 heading to 1
        Created passenger at station 5 heading to 3
        Created passenger at station 5 heading to 3
Time: 6 Waiting: 17 On trains: 0
        Created passenger at station 6 heading to 1
        Created passenger at station 6 heading to 3
        Created passenger at station 6 heading to 2
Time: 7 Waiting: 20 On trains: 0
        Created passenger at station 7 heading to 4
        Created passenger at station 7 heading to 3
        Created passenger at station 7 heading to 4
        Created passenger at station 7 heading to 1
Time: 8 Waiting: 24 On trains: 0
        Created passenger at station 8 heading to 4
        Created passenger at station 8 heading to 3
        Created passenger at station 8 heading to 1
        Created passenger at station 8 heading to 3
        Created passenger at station 8 heading to 4
Time: 9 Waiting: 29 On trains: 0
        Created passenger at station 9 heading to 4
        Created passenger at station 9 heading to 3
Time: 10 Waiting: 31 On trains: 0
        Created train 2
        Created passenger at station 10 heading to 2
        Created passenger at station 10 heading to 3
        Created passenger at station 10 heading to 2
        Train 2 unloaded 0 passengers at station 0
                loaded 16 passengers; Space left 34
Time: 11 Waiting: 18 On trains: 16
Time: 12 Waiting: 18 On trains: 16
        Created passenger at station 12 heading to 1
        Created passenger at station 12 heading to 2
        Created passenger at station 12 heading to 2
        Created passenger at station 12 heading to 4
        Train 1 unloaded 0 passengers at station 1
                loaded 10 passengers; Space left 40
Time: 13 Waiting: 12 On trains: 26
        Created passenger at station 13 heading to 4
        Created passenger at station 13 heading to 4
Time: 14 Waiting: 14 On trains: 26
        Created passenger at station 14 heading to 4
        Created passenger at station 14 heading to 4
Time: 15 Waiting: 16 On trains: 26
        Created passenger at station 15 heading to 1
        Created passenger at station 15 heading to 1
        Created passenger at station 15 heading to 2
        Created passenger at station 15 heading to 4
        Created passenger at station 15 heading to 4
Time: 16 Waiting: 21 On trains: 26
        Created passenger at station 16 heading to 4
        Created passenger at station 16 heading to 2
        Created passenger at station 16 heading to 4
        Created passenger at station 16 heading to 3
Time: 17 Waiting: 25 On trains: 26
        Created passenger at station 17 heading to 4
        Created passenger at station 17 heading to 3
        Train 1 unloaded 4 passengers at station 2
                loaded 11 passengers; Space left 33
Time: 18 Waiting: 16 On trains: 33
        Created passenger at station 18 heading to 4
        Created passenger at station 18 heading to 3
        Created passenger at station 18 heading to 4
        Created passenger at station 18 heading to 3
Time: 19 Waiting: 20 On trains: 33
        Created passenger at station 19 heading to 2
        Created passenger at station 19 heading to 4
        Created passenger at station 19 heading to 3
        Created passenger at station 19 heading to 3
        Created passenger at station 19 heading to 2
Time: 20 Waiting: 25 On trains: 33
        Created train 3
        Created passenger at station 20 heading to 3
        Created passenger at station 20 heading to 3
        Created passenger at station 20 heading to 4
        Created passenger at station 20 heading to 4
        Train 3 unloaded 0 passengers at station 0
                loaded 8 passengers; Space left 42
Time: 21 Waiting: 21 On trains: 41
        Created passenger at station 21 heading to 3
        Created passenger at station 21 heading to 4
        Created passenger at station 21 heading to 3
        Created passenger at station 21 heading to 2
Time: 22 Waiting: 25 On trains: 41
        Created passenger at station 22 heading to 3
        Created passenger at station 22 heading to 1
        Train 2 unloaded 6 passengers at station 1
                loaded 17 passengers; Space left 23
Time: 23 Waiting: 10 On trains: 52
        Created passenger at station 23 heading to 2
        Created passenger at station 23 heading to 2
Time: 24 Waiting: 12 On trains: 52
        Created passenger at station 24 heading to 3
        Created passenger at station 24 heading to 4
        Created passenger at station 24 heading to 2
        Created passenger at station 24 heading to 4
        Created passenger at station 24 heading to 4
Time: 25 Waiting: 17 On trains: 52
Time: 26 Waiting: 17 On trains: 52
        Train 1 unloaded 10 passengers at station 3
                loaded 6 passengers; Space left 37
Time: 27 Waiting: 11 On trains: 48
        Created passenger at station 27 heading to 4
        Created passenger at station 27 heading to 4
        Created passenger at station 27 heading to 4
        Created passenger at station 27 heading to 2
        Train 2 unloaded 7 passengers at station 2
                loaded 4 passengers; Space left 26
Time: 28 Waiting: 11 On trains: 45
        Created passenger at station 28 heading to 4
Time: 29 Waiting: 12 On trains: 45
        Created passenger at station 29 heading to 3
Time: 30 Waiting: 13 On trains: 45
        Created train 4
        Created passenger at station 30 heading to 2
        Created passenger at station 30 heading to 4
        Train 4 unloaded 0 passengers at station 0
                loaded 6 passengers; Space left 44
Time: 31 Waiting: 9 On trains: 51
        Created passenger at station 31 heading to 4
Time: 32 Waiting: 10 On trains: 51
        Created passenger at station 32 heading to 4
        Train 3 unloaded 3 passengers at station 1
                loaded 6 passengers; Space left 39
Time: 33 Waiting: 5 On trains: 54
        Created passenger at station 33 heading to 4
        Created passenger at station 33 heading to 3
Time: 34 Waiting: 7 On trains: 54
        Created passenger at station 34 heading to 4
        Created passenger at station 34 heading to 4
        Created passenger at station 34 heading to 4
        Created passenger at station 34 heading to 2
Time: 35 Waiting: 11 On trains: 54
        Created passenger at station 35 heading to 2
        Created passenger at station 35 heading to 4
Time: 36 Waiting: 13 On trains: 54
        Created passenger at station 36 heading to 3
        Created passenger at station 36 heading to 3
        Created passenger at station 36 heading to 4
        Created passenger at station 36 heading to 3
        Train 2 unloaded 8 passengers at station 3
                loaded 6 passengers; Space left 28
Time: 37 Waiting: 11 On trains: 52
        Train 3 unloaded 5 passengers at station 2
                loaded 4 passengers; Space left 40
Time: 38 Waiting: 7 On trains: 51
        Created passenger at station 38 heading to 4
        Created passenger at station 38 heading to 3
Time: 39 Waiting: 9 On trains: 51
        Created passenger at station 39 heading to 3
Time: 40 Waiting: 10 On trains: 51
        Created train 5
        Created passenger at station 40 heading to 3
        Train 1 unloaded 13 passengers at station 4
                loaded 0 passengers; Space left 50
        Train 5 unloaded 0 passengers at station 0
                loaded 7 passengers; Space left 43
Time: 41 Waiting: 4 On trains: 45
        Created passenger at station 41 heading to 4
        Created passenger at station 41 heading to 4
        Created passenger at station 41 heading to 1
Time: 42 Waiting: 7 On trains: 45
        Created passenger at station 42 heading to 3
        Train 4 unloaded 1 passengers at station 1
                loaded 4 passengers; Space left 41
Time: 43 Waiting: 4 On trains: 48
        Created passenger at station 43 heading to 4
        Created passenger at station 43 heading to 3
        Created passenger at station 43 heading to 4
Time: 44 Waiting: 7 On trains: 48
        Created passenger at station 44 heading to 4
        Created passenger at station 44 heading to 3
        Created passenger at station 44 heading to 4
Time: 45 Waiting: 10 On trains: 48
        Created passenger at station 45 heading to 3
Time: 46 Waiting: 11 On trains: 48
        Train 3 unloaded 5 passengers at station 3
                loaded 6 passengers; Space left 39
Time: 47 Waiting: 5 On trains: 49
        Created passenger at station 47 heading to 3
        Created passenger at station 47 heading to 4
        Created passenger at station 47 heading to 4
        Created passenger at station 47 heading to 3
        Train 4 unloaded 2 passengers at station 2
                loaded 1 passengers; Space left 42
Time: 48 Waiting: 8 On trains: 48
        Created passenger at station 48 heading to 2
        Created passenger at station 48 heading to 3
        Created passenger at station 48 heading to 4
        Created passenger at station 48 heading to 4
        Created passenger at station 48 heading to 3
Time: 49 Waiting: 13 On trains: 48
        Created passenger at station 49 heading to 4
Time: 50 Waiting: 14 On trains: 48
        Created train 6
        Created passenger at station 50 heading to 3
        Created passenger at station 50 heading to 2
        Created passenger at station 50 heading to 4
        Train 2 unloaded 22 passengers at station 4
                loaded 0 passengers; Space left 50
        Train 6 unloaded 0 passengers at station 0
                loaded 6 passengers; Space left 44
Time: 51 Waiting: 11 On trains: 32
        Created passenger at station 51 heading to 2
        Created passenger at station 51 heading to 3
Time: 52 Waiting: 13 On trains: 32
        Created passenger at station 52 heading to 4
        Created passenger at station 52 heading to 4
        Created passenger at station 52 heading to 4
        Created passenger at station 52 heading to 4
        Train 5 unloaded 0 passengers at station 1
                loaded 5 passengers; Space left 38
Time: 53 Waiting: 12 On trains: 37
        Created passenger at station 53 heading to 4
        Created passenger at station 53 heading to 1
        Created passenger at station 53 heading to 3
Time: 54 Waiting: 15 On trains: 37
        Created passenger at station 54 heading to 3
        Created passenger at station 54 heading to 3
        Created passenger at station 54 heading to 4
        Created passenger at station 54 heading to 4
Time: 55 Waiting: 19 On trains: 37
        Created passenger at station 55 heading to 4
        Created passenger at station 55 heading to 3
        Created passenger at station 55 heading to 4
        Created passenger at station 55 heading to 3
        Created passenger at station 55 heading to 4
Time: 56 Waiting: 24 On trains: 37
        Created passenger at station 56 heading to 3
        Created passenger at station 56 heading to 3
        Created passenger at station 56 heading to 2
        Train 4 unloaded 4 passengers at station 3
                loaded 8 passengers; Space left 38
Time: 57 Waiting: 19 On trains: 41
        Created passenger at station 57 heading to 3
        Train 5 unloaded 2 passengers at station 2
                loaded 5 passengers; Space left 35
Time: 58 Waiting: 15 On trains: 44
        Created passenger at station 58 heading to 4
        Created passenger at station 58 heading to 4
        Created passenger at station 58 heading to 3
        Created passenger at station 58 heading to 4
        Created passenger at station 58 heading to 2
Time: 59 Waiting: 20 On trains: 44
        Created passenger at station 59 heading to 2
        Created passenger at station 59 heading to 1
        Created passenger at station 59 heading to 4
        Created passenger at station 59 heading to 3
Time: 60 Waiting: 24 On trains: 44
        Created train 7
        Created passenger at station 60 heading to 4
        Created passenger at station 60 heading to 3
        Train 3 unloaded 11 passengers at station 4
                loaded 0 passengers; Space left 50
        Train 7 unloaded 0 passengers at station 0
                loaded 13 passengers; Space left 37
Time: 61 Waiting: 13 On trains: 46
        Created passenger at station 61 heading to 1
        Created passenger at station 61 heading to 2
        Created passenger at station 61 heading to 4
        Created passenger at station 61 heading to 4
Time: 62 Waiting: 17 On trains: 46
        Created passenger at station 62 heading to 4
        Created passenger at station 62 heading to 4
        Created passenger at station 62 heading to 4
        Created passenger at station 62 heading to 1
        Created passenger at station 62 heading to 1
        Train 6 unloaded 1 passengers at station 1
                loaded 17 passengers; Space left 28
Time: 63 Waiting: 5 On trains: 62
        Created passenger at station 63 heading to 4
        Created passenger at station 63 heading to 4
        Created passenger at station 63 heading to 3
        Created passenger at station 63 heading to 3
Time: 64 Waiting: 9 On trains: 62
        Created passenger at station 64 heading to 4
        Created passenger at station 64 heading to 3
Time: 65 Waiting: 11 On trains: 62
        Created passenger at station 65 heading to 2
        Created passenger at station 65 heading to 3
        Created passenger at station 65 heading to 3
Time: 66 Waiting: 14 On trains: 62
        Created passenger at station 66 heading to 2
        Created passenger at station 66 heading to 2
        Created passenger at station 66 heading to 2
        Created passenger at station 66 heading to 4
        Train 5 unloaded 10 passengers at station 3
                loaded 2 passengers; Space left 43
Time: 67 Waiting: 16 On trains: 54
        Train 6 unloaded 3 passengers at station 2
                loaded 3 passengers; Space left 28
Time: 68 Waiting: 13 On trains: 54
        Created passenger at station 68 heading to 4
Time: 69 Waiting: 14 On trains: 54
        Created passenger at station 69 heading to 4
        Created passenger at station 69 heading to 1
        Created passenger at station 69 heading to 2
        Created passenger at station 69 heading to 3
        Created passenger at station 69 heading to 2
Time: 70 Waiting: 19 On trains: 54
        Created train 8
        Created passenger at station 70 heading to 3
        Created passenger at station 70 heading to 3
        Created passenger at station 70 heading to 3
        Created passenger at station 70 heading to 4
        Train 4 unloaded 12 passengers at station 4
                loaded 0 passengers; Space left 50
        Train 8 unloaded 0 passengers at station 0
                loaded 14 passengers; Space left 36
Time: 71 Waiting: 9 On trains: 56
        Created passenger at station 71 heading to 4
        Created passenger at station 71 heading to 2
        Created passenger at station 71 heading to 4
Time: 72 Waiting: 12 On trains: 56
        Created passenger at station 72 heading to 4
        Created passenger at station 72 heading to 3
        Train 7 unloaded 2 passengers at station 1
                loaded 9 passengers; Space left 30
Time: 73 Waiting: 5 On trains: 63
Time: 74 Waiting: 5 On trains: 63
        Created passenger at station 74 heading to 2
        Created passenger at station 74 heading to 4
        Created passenger at station 74 heading to 4
Time: 75 Waiting: 8 On trains: 63
        Created passenger at station 75 heading to 4
        Created passenger at station 75 heading to 4
        Created passenger at station 75 heading to 1
        Created passenger at station 75 heading to 3
        Created passenger at station 75 heading to 4
Time: 76 Waiting: 13 On trains: 63
        Created passenger at station 76 heading to 2
        Created passenger at station 76 heading to 4
        Created passenger at station 76 heading to 4
        Train 6 unloaded 12 passengers at station 3
                loaded 4 passengers; Space left 36
Time: 77 Waiting: 12 On trains: 55
        Train 7 unloaded 6 passengers at station 2
                loaded 6 passengers; Space left 30
Time: 78 Waiting: 6 On trains: 55
        Created passenger at station 78 heading to 2
        Created passenger at station 78 heading to 1
Time: 79 Waiting: 8 On trains: 55
        Created passenger at station 79 heading to 4
        Created passenger at station 79 heading to 4
        Created passenger at station 79 heading to 4
        Created passenger at station 79 heading to 2
        Created passenger at station 79 heading to 4
Time: 80 Waiting: 13 On trains: 55
        Created train 9
        Train 5 unloaded 7 passengers at station 4
                loaded 0 passengers; Space left 50
        Train 9 unloaded 0 passengers at station 0
                loaded 3 passengers; Space left 47
Time: 81 Waiting: 10 On trains: 51
        Created passenger at station 81 heading to 1
        Created passenger at station 81 heading to 4
        Created passenger at station 81 heading to 1
        Created passenger at station 81 heading to 4
        Created passenger at station 81 heading to 4
Time: 82 Waiting: 15 On trains: 51
        Train 8 unloaded 4 passengers at station 1
                loaded 9 passengers; Space left 31
Time: 83 Waiting: 6 On trains: 56
        Created passenger at station 83 heading to 2
        Created passenger at station 83 heading to 1
        Created passenger at station 83 heading to 4
        Created passenger at station 83 heading to 4
Time: 84 Waiting: 10 On trains: 56
        Created passenger at station 84 heading to 3
        Created passenger at station 84 heading to 3
Time: 85 Waiting: 12 On trains: 56
        Created passenger at station 85 heading to 4
        Created passenger at station 85 heading to 4
        Created passenger at station 85 heading to 4
        Created passenger at station 85 heading to 3
Time: 86 Waiting: 16 On trains: 56
        Created passenger at station 86 heading to 4
        Created passenger at station 86 heading to 4
        Created passenger at station 86 heading to 3
        Created passenger at station 86 heading to 4
        Train 7 unloaded 8 passengers at station 3
                loaded 2 passengers; Space left 36
Time: 87 Waiting: 18 On trains: 50
        Created passenger at station 87 heading to 4
        Created passenger at station 87 heading to 4
        Created passenger at station 87 heading to 4
        Train 8 unloaded 7 passengers at station 2
                loaded 6 passengers; Space left 32
Time: 88 Waiting: 15 On trains: 49
        Created passenger at station 88 heading to 2
        Created passenger at station 88 heading to 1
        Created passenger at station 88 heading to 4
        Created passenger at station 88 heading to 2
        Created passenger at station 88 heading to 1
Time: 89 Waiting: 20 On trains: 49
Time: 90 Waiting: 20 On trains: 49
        Created train 10
        Train 6 unloaded 14 passengers at station 4
                loaded 0 passengers; Space left 50
        Train 10 unloaded 0 passengers at station 0
                loaded 11 passengers; Space left 39
Time: 91 Waiting: 9 On trains: 46
        Created passenger at station 91 heading to 2
        Created passenger at station 91 heading to 4
        Created passenger at station 91 heading to 3
Time: 92 Waiting: 12 On trains: 46
        Created passenger at station 92 heading to 3
        Created passenger at station 92 heading to 4
        Train 9 unloaded 2 passengers at station 1
                loaded 9 passengers; Space left 40
Time: 93 Waiting: 5 On trains: 53
        Created passenger at station 93 heading to 3
Time: 94 Waiting: 6 On trains: 53
        Created passenger at station 94 heading to 1
        Created passenger at station 94 heading to 3
        Created passenger at station 94 heading to 4
        Created passenger at station 94 heading to 1
Time: 95 Waiting: 10 On trains: 53
        Created passenger at station 95 heading to 2
        Created passenger at station 95 heading to 4
Time: 96 Waiting: 12 On trains: 53
        Created passenger at station 96 heading to 4
        Created passenger at station 96 heading to 4
        Created passenger at station 96 heading to 2
        Train 8 unloaded 7 passengers at station 3
                loaded 3 passengers; Space left 36
Time: 97 Waiting: 12 On trains: 49
        Created passenger at station 97 heading to 3
        Created passenger at station 97 heading to 4
        Created passenger at station 97 heading to 3
        Created passenger at station 97 heading to 1
        Train 9 unloaded 4 passengers at station 2
                loaded 3 passengers; Space left 41
Time: 98 Waiting: 13 On trains: 48
        Created passenger at station 98 heading to 3
Time: 99 Waiting: 14 On trains: 48
        Created passenger at station 99 heading to 4
Time: 100 Waiting: 15 On trains: 48
        Created train 11
        Created passenger at station 100 heading to 4
        Created passenger at station 100 heading to 3
        Created passenger at station 100 heading to 4
        Created passenger at station 100 heading to 4
        Created passenger at station 100 heading to 2
        Train 7 unloaded 14 passengers at station 4
                loaded 0 passengers; Space left 50
        Train 11 unloaded 0 passengers at station 0
                loaded 8 passengers; Space left 42
Time: 101 Waiting: 12 On trains: 42
        Created passenger at station 101 heading to 2
Time: 102 Waiting: 13 On trains: 42
        Created passenger at station 102 heading to 2
        Created passenger at station 102 heading to 2
        Created passenger at station 102 heading to 2
        Created passenger at station 102 heading to 4
        Train 10 unloaded 5 passengers at station 1
                loaded 12 passengers; Space left 32
Time: 103 Waiting: 5 On trains: 49
Time: 104 Waiting: 5 On trains: 49
        Created passenger at station 104 heading to 4
Time: 105 Waiting: 6 On trains: 49
        Created passenger at station 105 heading to 3
        Created passenger at station 105 heading to 4
        Created passenger at station 105 heading to 4
        Created passenger at station 105 heading to 2
        Created passenger at station 105 heading to 2
Time: 106 Waiting: 11 On trains: 49
        Train 9 unloaded 4 passengers at station 3
                loaded 2 passengers; Space left 43
Time: 107 Waiting: 9 On trains: 47
        Created passenger at station 107 heading to 2
        Created passenger at station 107 heading to 3
        Created passenger at station 107 heading to 1
        Train 10 unloaded 6 passengers at station 2
                loaded 1 passengers; Space left 37
Time: 108 Waiting: 11 On trains: 42
Time: 109 Waiting: 11 On trains: 42
        Created passenger at station 109 heading to 1
        Created passenger at station 109 heading to 3
        Created passenger at station 109 heading to 3
        Created passenger at station 109 heading to 2
        Created passenger at station 109 heading to 3
Time: 110 Waiting: 16 On trains: 42
        Created train 12
        Created passenger at station 110 heading to 4
        Created passenger at station 110 heading to 4
        Train 8 unloaded 14 passengers at station 4
                loaded 0 passengers; Space left 50
        Train 12 unloaded 0 passengers at station 0
                loaded 9 passengers; Space left 41
Time: 111 Waiting: 9 On trains: 37
Time: 112 Waiting: 9 On trains: 37
        Train 11 unloaded 3 passengers at station 1
                loaded 6 passengers; Space left 39
Time: 113 Waiting: 3 On trains: 40
        Created passenger at station 113 heading to 2
        Created passenger at station 113 heading to 4
        Created passenger at station 113 heading to 3
        Created passenger at station 113 heading to 4
        Created passenger at station 113 heading to 4
Time: 114 Waiting: 8 On trains: 40
        Created passenger at station 114 heading to 3
        Created passenger at station 114 heading to 2
        Created passenger at station 114 heading to 3
        Created passenger at station 114 heading to 1
Time: 115 Waiting: 12 On trains: 40
        Created passenger at station 115 heading to 3
Time: 116 Waiting: 13 On trains: 40
        Train 10 unloaded 5 passengers at station 3
                loaded 0 passengers; Space left 42
Time: 117 Waiting: 13 On trains: 35
        Created passenger at station 117 heading to 1
        Created passenger at station 117 heading to 3
        Train 11 unloaded 2 passengers at station 2
                loaded 5 passengers; Space left 36
Time: 118 Waiting: 10 On trains: 38
        Created passenger at station 118 heading to 3
Time: 119 Waiting: 11 On trains: 38
        Created passenger at station 119 heading to 4
Time: 120 Waiting: 12 On trains: 38
        Created train 13
        Created passenger at station 120 heading to 4
        Created passenger at station 120 heading to 2
        Created passenger at station 120 heading to 4
        Created passenger at station 120 heading to 4
        Created passenger at station 120 heading to 3
        Train 9 unloaded 7 passengers at station 4
                loaded 0 passengers; Space left 50
        Train 13 unloaded 0 passengers at station 0
                loaded 10 passengers; Space left 40
Time: 121 Waiting: 7 On trains: 41
        Created passenger at station 121 heading to 3
        Created passenger at station 121 heading to 4
        Created passenger at station 121 heading to 2
Time: 122 Waiting: 10 On trains: 41
        Created passenger at station 122 heading to 4
        Created passenger at station 122 heading to 4
        Train 12 unloaded 2 passengers at station 1
                loaded 6 passengers; Space left 37
Time: 123 Waiting: 6 On trains: 45
        Created passenger at station 123 heading to 2
        Created passenger at station 123 heading to 1
        Created passenger at station 123 heading to 3
Time: 124 Waiting: 9 On trains: 45
        Created passenger at station 124 heading to 3
        Created passenger at station 124 heading to 4
Time: 125 Waiting: 11 On trains: 45
        Created passenger at station 125 heading to 2
        Created passenger at station 125 heading to 2
        Created passenger at station 125 heading to 3
Time: 126 Waiting: 14 On trains: 45
        Created passenger at station 126 heading to 4
        Train 11 unloaded 7 passengers at station 3
                loaded 2 passengers; Space left 41
Time: 127 Waiting: 13 On trains: 40
        Created passenger at station 127 heading to 4
        Created passenger at station 127 heading to 4
        Created passenger at station 127 heading to 1
        Created passenger at station 127 heading to 3
        Created passenger at station 127 heading to 3
        Train 12 unloaded 8 passengers at station 2
                loaded 7 passengers; Space left 38
Time: 128 Waiting: 11 On trains: 39
        Created passenger at station 128 heading to 4
Time: 129 Waiting: 12 On trains: 39
        Created passenger at station 129 heading to 3
        Created passenger at station 129 heading to 4
Time: 130 Waiting: 14 On trains: 39
        Created train 14
        Created passenger at station 130 heading to 4
        Created passenger at station 130 heading to 3
        Created passenger at station 130 heading to 3
        Train 10 unloaded 8 passengers at station 4
                loaded 0 passengers; Space left 50
        Train 14 unloaded 0 passengers at station 0
                loaded 9 passengers; Space left 41
Time: 131 Waiting: 8 On trains: 40
        Created passenger at station 131 heading to 2
        Created passenger at station 131 heading to 4
Time: 132 Waiting: 10 On trains: 40
        Created passenger at station 132 heading to 4
        Created passenger at station 132 heading to 4
        Created passenger at station 132 heading to 4
        Train 13 unloaded 2 passengers at station 1
                loaded 6 passengers; Space left 36
Time: 133 Waiting: 7 On trains: 44
        Created passenger at station 133 heading to 2
        Created passenger at station 133 heading to 3
        Created passenger at station 133 heading to 4
Time: 134 Waiting: 10 On trains: 44
        Created passenger at station 134 heading to 3
        Created passenger at station 134 heading to 4
        Created passenger at station 134 heading to 4
        Created passenger at station 134 heading to 4
Time: 135 Waiting: 14 On trains: 44
        Created passenger at station 135 heading to 4
Time: 136 Waiting: 15 On trains: 44
        Created passenger at station 136 heading to 3
        Created passenger at station 136 heading to 3
        Created passenger at station 136 heading to 2
        Train 12 unloaded 6 passengers at station 3
                loaded 2 passengers; Space left 42
Time: 137 Waiting: 16 On trains: 40
        Created passenger at station 137 heading to 3
        Created passenger at station 137 heading to 3
        Created passenger at station 137 heading to 2
        Train 13 unloaded 0 passengers at station 2
                loaded 6 passengers; Space left 30
Time: 138 Waiting: 13 On trains: 46
        Created passenger at station 138 heading to 3
Time: 139 Waiting: 14 On trains: 46
Time: 140 Waiting: 14 On trains: 46
        Created train 15
        Created passenger at station 140 heading to 4
        Created passenger at station 140 heading to 2
        Created passenger at station 140 heading to 3
        Train 11 unloaded 9 passengers at station 4
                loaded 0 passengers; Space left 50
        Train 15 unloaded 0 passengers at station 0
                loaded 9 passengers; Space left 41
Time: 141 Waiting: 8 On trains: 46
        Created passenger at station 141 heading to 2
        Created passenger at station 141 heading to 3
Time: 142 Waiting: 10 On trains: 46
        Train 14 unloaded 2 passengers at station 1
                loaded 8 passengers; Space left 35
Time: 143 Waiting: 2 On trains: 52
        Created passenger at station 143 heading to 2
        Created passenger at station 143 heading to 4
        Created passenger at station 143 heading to 2
Time: 144 Waiting: 5 On trains: 52
        Created passenger at station 144 heading to 4
        Created passenger at station 144 heading to 3
        Created passenger at station 144 heading to 4
        Created passenger at station 144 heading to 2
Time: 145 Waiting: 9 On trains: 52
Time: 146 Waiting: 9 On trains: 52
        Created passenger at station 146 heading to 2
        Created passenger at station 146 heading to 1
        Created passenger at station 146 heading to 4
        Train 13 unloaded 11 passengers at station 3
                loaded 0 passengers; Space left 41
Time: 147 Waiting: 12 On trains: 41
        Created passenger at station 147 heading to 1
        Created passenger at station 147 heading to 2
        Created passenger at station 147 heading to 4
        Train 14 unloaded 3 passengers at station 2
                loaded 4 passengers; Space left 34
Time: 148 Waiting: 11 On trains: 42
        Created passenger at station 148 heading to 4
        Created passenger at station 148 heading to 2
        Created passenger at station 148 heading to 4
        Created passenger at station 148 heading to 2
Time: 149 Waiting: 15 On trains: 42
        Created passenger at station 149 heading to 4
        Created passenger at station 149 heading to 1
        Created passenger at station 149 heading to 2
        Created passenger at station 149 heading to 1
Time: 150 Waiting: 19 On trains: 42
        Created train 16
        Created passenger at station 150 heading to 3
        Created passenger at station 150 heading to 4
        Train 12 unloaded 8 passengers at station 4
                loaded 0 passengers; Space left 50
        Train 16 unloaded 0 passengers at station 0
                loaded 10 passengers; Space left 40
Time: 151 Waiting: 11 On trains: 44
        Created passenger at station 151 heading to 4
Time: 152 Waiting: 12 On trains: 44
        Created passenger at station 152 heading to 3
        Created passenger at station 152 heading to 4
        Train 15 unloaded 0 passengers at station 1
                loaded 10 passengers; Space left 31
Time: 153 Waiting: 4 On trains: 54
        Created passenger at station 153 heading to 4
        Created passenger at station 153 heading to 4
        Created passenger at station 153 heading to 3
Time: 154 Waiting: 7 On trains: 54
        Created passenger at station 154 heading to 4
        Created passenger at station 154 heading to 4
        Created passenger at station 154 heading to 3
        Created passenger at station 154 heading to 3
Time: 155 Waiting: 11 On trains: 54
        Created passenger at station 155 heading to 4
        Created passenger at station 155 heading to 2
Time: 156 Waiting: 13 On trains: 54
        Created passenger at station 156 heading to 1
        Created passenger at station 156 heading to 2
        Created passenger at station 156 heading to 3
        Created passenger at station 156 heading to 1
        Created passenger at station 156 heading to 4
        Train 14 unloaded 6 passengers at station 3
                loaded 3 passengers; Space left 37
Time: 157 Waiting: 15 On trains: 51
        Created passenger at station 157 heading to 3
        Created passenger at station 157 heading to 4
        Created passenger at station 157 heading to 4
        Created passenger at station 157 heading to 4
        Train 15 unloaded 9 passengers at station 2
                loaded 4 passengers; Space left 36
Time: 158 Waiting: 15 On trains: 46
        Created passenger at station 158 heading to 1
Time: 159 Waiting: 16 On trains: 46
        Created passenger at station 159 heading to 1
        Created passenger at station 159 heading to 4
        Created passenger at station 159 heading to 1
        Created passenger at station 159 heading to 2
        Created passenger at station 159 heading to 4
Time: 160 Waiting: 21 On trains: 46
        Created train 17
        Created passenger at station 160 heading to 4
        Created passenger at station 160 heading to 4
        Created passenger at station 160 heading to 2
        Train 13 unloaded 9 passengers at station 4
                loaded 0 passengers; Space left 50
        Train 17 unloaded 0 passengers at station 0
                loaded 14 passengers; Space left 36
Time: 161 Waiting: 10 On trains: 51
Time: 162 Waiting: 10 On trains: 51
        Created passenger at station 162 heading to 3
        Created passenger at station 162 heading to 4
        Train 16 unloaded 4 passengers at station 1
                loaded 8 passengers; Space left 36
Time: 163 Waiting: 4 On trains: 55
Time: 164 Waiting: 4 On trains: 55
        Created passenger at station 164 heading to 3
        Created passenger at station 164 heading to 3
        Created passenger at station 164 heading to 4
        Created passenger at station 164 heading to 4
Time: 165 Waiting: 8 On trains: 55
        Created passenger at station 165 heading to 4
        Created passenger at station 165 heading to 3
Time: 166 Waiting: 10 On trains: 55
        Created passenger at station 166 heading to 4
        Train 15 unloaded 6 passengers at station 3
                loaded 4 passengers; Space left 38
Time: 167 Waiting: 7 On trains: 53
        Created passenger at station 167 heading to 4
        Train 16 unloaded 7 passengers at station 2
                loaded 6 passengers; Space left 37
Time: 168 Waiting: 2 On trains: 52
        Created passenger at station 168 heading to 3
        Created passenger at station 168 heading to 4
        Created passenger at station 168 heading to 3
        Created passenger at station 168 heading to 3
Time: 169 Waiting: 6 On trains: 52
        Created passenger at station 169 heading to 1
        Created passenger at station 169 heading to 3
        Created passenger at station 169 heading to 4
        Created passenger at station 169 heading to 4
Time: 170 Waiting: 10 On trains: 52
        Created train 18
        Created passenger at station 170 heading to 1
        Train 14 unloaded 13 passengers at station 4
                loaded 0 passengers; Space left 50
        Train 18 unloaded 0 passengers at station 0
                loaded 3 passengers; Space left 47
Time: 171 Waiting: 8 On trains: 42
Time: 172 Waiting: 8 On trains: 42
        Created passenger at station 172 heading to 3
        Created passenger at station 172 heading to 4
        Created passenger at station 172 heading to 2
        Created passenger at station 172 heading to 4
        Created passenger at station 172 heading to 2
        Train 17 unloaded 5 passengers at station 1
                loaded 6 passengers; Space left 35
Time: 173 Waiting: 7 On trains: 43
        Created passenger at station 173 heading to 2
        Created passenger at station 173 heading to 2
Time: 174 Waiting: 9 On trains: 43
        Created passenger at station 174 heading to 4
        Created passenger at station 174 heading to 4
        Created passenger at station 174 heading to 1
        Created passenger at station 174 heading to 3
        Created passenger at station 174 heading to 1
Time: 175 Waiting: 14 On trains: 43
        Created passenger at station 175 heading to 4
        Created passenger at station 175 heading to 3
        Created passenger at station 175 heading to 3
        Created passenger at station 175 heading to 4
Time: 176 Waiting: 18 On trains: 43
        Created passenger at station 176 heading to 3
        Created passenger at station 176 heading to 3
        Train 16 unloaded 4 passengers at station 3
                loaded 1 passengers; Space left 40
Time: 177 Waiting: 19 On trains: 40
        Train 17 unloaded 2 passengers at station 2
                loaded 7 passengers; Space left 30
Time: 178 Waiting: 12 On trains: 45
Time: 179 Waiting: 12 On trains: 45
        Created passenger at station 179 heading to 4
        Created passenger at station 179 heading to 4
Time: 180 Waiting: 14 On trains: 45
        Created train 19
        Created passenger at station 180 heading to 4
        Created passenger at station 180 heading to 4
        Created passenger at station 180 heading to 4
        Created passenger at station 180 heading to 2
        Created passenger at station 180 heading to 4
        Train 15 unloaded 12 passengers at station 4
                loaded 0 passengers; Space left 50
        Train 19 unloaded 0 passengers at station 0
                loaded 10 passengers; Space left 40
Time: 181 Waiting: 9 On trains: 43
        Created passenger at station 181 heading to 4
        Created passenger at station 181 heading to 3
Time: 182 Waiting: 11 On trains: 43
        Created passenger at station 182 heading to 3
        Created passenger at station 182 heading to 2
        Created passenger at station 182 heading to 3
        Created passenger at station 182 heading to 2
        Train 18 unloaded 2 passengers at station 1
                loaded 10 passengers; Space left 39
Time: 183 Waiting: 5 On trains: 51
        Created passenger at station 183 heading to 3
Time: 184 Waiting: 6 On trains: 51
        Created passenger at station 184 heading to 2
        Created passenger at station 184 heading to 4
        Created passenger at station 184 heading to 4
        Created passenger at station 184 heading to 2
        Created passenger at station 184 heading to 4
Time: 185 Waiting: 11 On trains: 51
        Created passenger at station 185 heading to 3
        Created passenger at station 185 heading to 4
        Created passenger at station 185 heading to 2
        Created passenger at station 185 heading to 2
        Created passenger at station 185 heading to 3
Time: 186 Waiting: 16 On trains: 51
        Train 17 unloaded 11 passengers at station 3
                loaded 2 passengers; Space left 39
Time: 187 Waiting: 14 On trains: 42
        Created passenger at station 187 heading to 4
        Created passenger at station 187 heading to 1
        Created passenger at station 187 heading to 4
        Train 18 unloaded 1 passengers at station 2
                loaded 3 passengers; Space left 37
Time: 188 Waiting: 14 On trains: 44
        Created passenger at station 188 heading to 4
        Created passenger at station 188 heading to 1
        Created passenger at station 188 heading to 2
        Created passenger at station 188 heading to 1
        Created passenger at station 188 heading to 4
Time: 189 Waiting: 19 On trains: 44
        Created passenger at station 189 heading to 1
Time: 190 Waiting: 20 On trains: 44
        Created train 20
        Created passenger at station 190 heading to 3
        Created passenger at station 190 heading to 4
        Train 16 unloaded 10 passengers at station 4
                loaded 0 passengers; Space left 50
        Train 20 unloaded 0 passengers at station 0
                loaded 14 passengers; Space left 36
Time: 191 Waiting: 8 On trains: 48
        Created passenger at station 191 heading to 3
        Created passenger at station 191 heading to 4
        Created passenger at station 191 heading to 2
        Created passenger at station 191 heading to 4
Time: 192 Waiting: 12 On trains: 48
        Created passenger at station 192 heading to 1
        Created passenger at station 192 heading to 4
        Created passenger at station 192 heading to 4
        Created passenger at station 192 heading to 4
        Created passenger at station 192 heading to 4
        Train 19 unloaded 2 passengers at station 1
                loaded 8 passengers; Space left 34
Time: 193 Waiting: 9 On trains: 54
        Created passenger at station 193 heading to 3
        Created passenger at station 193 heading to 2
        Created passenger at station 193 heading to 4
Time: 194 Waiting: 12 On trains: 54
        Created passenger at station 194 heading to 3
        Created passenger at station 194 heading to 4
Time: 195 Waiting: 14 On trains: 54
        Created passenger at station 195 heading to 3
        Created passenger at station 195 heading to 1
        Created passenger at station 195 heading to 1
        Created passenger at station 195 heading to 4
        Created passenger at station 195 heading to 2
Time: 196 Waiting: 19 On trains: 54
        Created passenger at station 196 heading to 4
        Created passenger at station 196 heading to 2
        Created passenger at station 196 heading to 2
        Train 18 unloaded 3 passengers at station 3
                loaded 5 passengers; Space left 35
Time: 197 Waiting: 17 On trains: 56
        Train 19 unloaded 8 passengers at station 2
                loaded 5 passengers; Space left 37
Time: 198 Waiting: 12 On trains: 53
        Created passenger at station 198 heading to 3
        Created passenger at station 198 heading to 2
        Created passenger at station 198 heading to 4
Time: 199 Waiting: 15 On trains: 53
        Created passenger at station 199 heading to 2
Final Report
The total number of passengers is 511
The number of passengers currently on a train 53
The number of passengers delivered is 442
The average wait time for passengers that have boarded is
4.717171717171717
The average wait time for passengers that have not yet boarded is
5.125

Run #2 (only final report):
Final Report
The total number of passengers is 476
The number of passengers currently on a train 45
The number of passengers delivered is 412
The average wait time for passengers that have boarded is
4.487964989059081
The average wait time for passengers that have not yet boarded is
3.4210526315789473

Run #3 (only final report):
Final Report
The total number of passengers is 494
The number of passengers currently on a train 21
The number of passengers delivered is 455
The average wait time for passengers that have boarded is
4.407563025210084
The average wait time for passengers that have not yet boarded is
4.833333333333333
*/
