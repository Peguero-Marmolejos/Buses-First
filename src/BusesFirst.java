import java.util.*;
import java.io.*;


public class BusesFirst {
	
	public static float simulated_clock = 0; // clock for simulated event
	public int [] route_stops = {0,0,0,0}; // stops on the route numbered 
	private static double lamda = 5; //average arrival time for passengers to one of four stations 
	private static int num_of_passengers = 0; // keep track of passengers
	private static int num_of_departures = 0; // keeping track of number of departures
	public static int pass_current_stop = 1; // keeps track of stops for the entrance of passengers
	public static int next_stop ;
	public static int num_of_buses = 0;
	public static float next_Bus_Arr = 0;
	public static Bus bus;
	public static int CURRENT_STOP = -1;
	public static float total_time_on_bus = 0;
	public static int total_waiting_for_round_trips = 0;
	public static int passengers_got_on = 0;
	public static boolean more_pass = true;
	
	public static LinkedList <Passenger> stop_1 = new LinkedList<Passenger>();
	public static LinkedList <Passenger> stop_2 = new LinkedList<Passenger>();
	public static LinkedList <Passenger> stop_3 = new LinkedList<Passenger>();
	public static LinkedList <Passenger> stop_4 = new LinkedList<Passenger>();
	
	public static void write_to_debug(String str) throws IOException {
			    BufferedWriter writer1 = new BufferedWriter(new FileWriter("DebugOutput.txt", true));
			    writer1.append("\n");
			    writer1.append(str);
			    writer1.close();
	}
	public static void write_to_output(String str) throws IOException {
			    BufferedWriter writer2 = new BufferedWriter(new FileWriter("GeneralOutput.txt", true));
			    writer2.append("\n");
			    writer2.append(str);
			    writer2.close();
	}

	public static class Passenger {
		private String name = "Passenger ";
		public int initial_stop, departure_stop;
		public float arrival_time;
		public int exit_stop = 0;
		public float time_in_bus;
		private int wait_for_round_trip = 0;
		private String word = "";
		private int number;
		
		public Passenger(int i, float arrival, int stop) {
			this.number = i;
			this.name = name + this.number;
			this.arrival_time = arrival;
			this.initial_stop = stop;
		}// constructor
		public String getName() {
			return this.name;
		}
		public void ride() {
			if (departure_stop > 0) {
				departure_stop --;
			}
		}
		public void wait_again() {
			this.wait_for_round_trip ++;
		}
		public int how_many_trips_waited() {
			return this.wait_for_round_trip; 
		}
		public void to_String(String a) {
			this.word += "\n" + a;
		}
		public String word() {
			return this.word;
		}
	}// Passenger Object
	
	public static class Bus {
		public static final int CAPACITY = 20;
		
		private String name = "Bus ";
		public int [] stops_ON = {0,0,0,0};
		public int [] stops_OFF = {0,0,0,0};
		private float next_Arrival = 0;
		public int next_bus_stop = -1;
		public static int passengers_ON = 0;
		public int curr_stop = -1;
		private boolean full = false;
		public static Passenger [] seats = new Passenger[20];
		
		public Bus(int i) {
			this.name = name + i;
		}// constructor
		
		public String getName() {
			return this.name;
		}
		public int next_bus_stop() {
			return next_bus_stop;
		}
		public void set_next_arrival(float a, int stop){
			this.next_Arrival = a;
			this.next_bus_stop = stop;
			System.out.println(simulated_clock + " : " + this.getName() + " will arrive at station " + stop +" at " + this.next_Arrival);
		}
		public void step_up(Passenger p) {
			if(passengers_ON < 20) {
				passengers_ON++;
				seats[passengers_ON-1] = p;
			}
			if(passengers_ON == 20) {
				this.full = true;
			}
		}
		public void ride() {
			for(int i =0; i < seats.length; i++) {
				if(seats[i] != null && seats[i].departure_stop == 0) {
					passengers_ON --;
					full = false;
					depart(seats[i]);
					seats[i] = null;
				}
				else if(seats[i] != null) {
					seats[i].ride(); // decreases the amount of stops until departure station
				}
			}
		}
	}// Bus object
	
	
	/***********Pass****************/
	
	public static void passenger_arrival(int stop){
		float pass_arr_time = (float) ((-lamda) * Math.log(Math.random()));// 5s average arrival time
		if(pass_arr_time + simulated_clock > next_Bus_Arr) {
			pass_current_stop = stop;
			simulated_clock = next_Bus_Arr;
			more_pass = false;
			queue_down(next_stop);
		}else {
			num_of_passengers ++; // a new passenger was created
			Passenger newone = new Passenger(num_of_passengers, pass_arr_time, stop);
			queue_up(newone);
			simulated_clock += pass_arr_time;
			more_pass = true;
			System.out.println(simulated_clock + " : " + newone.getName() + " enters station " + stop + ".");
		
			if ((newone.number > 0 && newone.number < 21) || (newone.number > 99 && newone.number <121)){
				try {
					write_to_debug(simulated_clock + " : " + newone.getName() + " enters station " + stop + ".");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			stop++;
			if (stop > 4) {
				stop = stop % 4;
			}
			pass_current_stop = stop;
			//passenger_arrival(stop);
		}
	}// creates passengers with exponential distribution of arrival lambda = 5
	
	public static void queue_up(Passenger p) {
		switch (p.initial_stop) {
        	case 1:  stop_1.add(p);
                 break;
        	case 2:  stop_2.add(p);
                 break;
        	case 3:  stop_3.add(p);
                 break;
        	case 4:  stop_4.add(p);
                 break;
		}
	}
	
	/*************Buses*************/
	
	
	public static Bus create_bus_arrival() {
		Random random = new Random();
		Bus bus = new Bus(1);// create bus object
		float  time_to_next_stop = (random.nextFloat()*((float)40.0)) +1; //returns random values between 1(included) and 40(included)
		if(time_to_next_stop <= 20) {// half of the outcomes above only take 15 s to travel from stop to stop
			bus.set_next_arrival(simulated_clock + (float)15, next_stop);
			next_Bus_Arr = simulated_clock + (float)15;
			return bus;
		}else {
			bus.set_next_arrival(time_to_next_stop, next_stop);
			next_Bus_Arr = time_to_next_stop;
			return bus;
		}
	}
	
	public static void next_bus_arrival() {
		next_stop++;
		if(next_stop > 4) {
			next_stop = next_stop % 4;
		}
		Random random = new Random();
		float  time_to_next_stop = (random.nextFloat()*((float)40.0)) +1; //returns random values between 1(included) and 40(included)
		if(time_to_next_stop <= 20) {// half of the outcomes above only take 15 s to travel from stop to stop
			bus.set_next_arrival(simulated_clock + (float)15, next_stop);
			next_Bus_Arr = simulated_clock + (float)15;
		}else {
			bus.set_next_arrival(simulated_clock + time_to_next_stop, next_stop);
			next_Bus_Arr = simulated_clock + time_to_next_stop;
		}
	}
	
	public static void queue_down(int stop) {
			switch (stop) {
	    	case 1: 
	    		CURRENT_STOP = 1;
	    		System.out.println();
	    		System.out.println(simulated_clock + " : " + bus.getName() + " is arriving at station " + stop + ".");
	    		System.out.println();
	    		if(bus.seats.length != 0 || bus.full == true) {
		    		bus.ride();
	    		}
	    		while((!(stop_1.isEmpty())) && bus.full == false) {
	    			board(stop_1.remove(0));
	    		}
	    		if((!(stop_1.isEmpty())) && bus.full == true) {
	    			ListIterator <Passenger> iter = stop_1.listIterator();
	    			while(iter.hasNext()) {
	    				iter.next().wait_again();
	    			}
	    		}
	    		System.out.println("THERE ARE " + bus.passengers_ON + " PASSENGERS ON THE BUS");
	    		stop++;
	    		if(stop > 4) {
	    			stop = stop %4;
	    		}
	    		next_bus_arrival();
	    		more_pass = true;
	            break;
	    	case 2:  
	    		CURRENT_STOP = 2;
	    		System.out.println();
	    		System.out.println(bus.getName() + " is arriving at station " + stop + ".");
	    		System.out.println();
	    		if(bus.seats.length != 0 || bus.full == true) {
		    		bus.ride();
	    		}
	    		while((!(stop_2.isEmpty())) && bus.full == false) {
	    			board(stop_2.remove(0));
	    		}	
	    		if((!(stop_2.isEmpty())) && bus.full == true) {
	    			ListIterator <Passenger> iter = stop_2.listIterator();
	    			while(iter.hasNext()) {
	    				iter.next().wait_again();
	    			}
	    		}	
	    		System.out.println("THERE ARE " + bus.passengers_ON + " PASSENGERS ON THE BUS");
	    		stop++;
	    		if(stop > 4) {
	    			stop = stop %4;
	    		}
	    		next_bus_arrival();
	    		more_pass =true;
	    		//passenger_arrival(pass_current_stop);
	    		break;
	    	case 3:
	    		CURRENT_STOP = 3;
	    		System.out.println();
	    		System.out.println(simulated_clock + " : " + bus.getName() + " is arriving at station " + stop + ".");
	    		System.out.println();
	    		if(bus.seats.length != 0 || bus.full == true) {
		    		bus.ride();
	    		}
	    		while((!(stop_3.isEmpty())) && bus.full == false) {
	    			board(stop_3.remove(0));
	    		}	
	    		if((!(stop_3.isEmpty())) && bus.full == true) {
	    			ListIterator <Passenger> iter = stop_3.listIterator();
	    			while(iter.hasNext()) {
	    				iter.next().wait_again();
	    			}
	    		}	
	    		System.out.println("THERE ARE " + bus.passengers_ON + " PASSENGERS ON THE BUS");
	    		stop++;
	    		if(stop > 4) {
	    			stop = stop %4;
	    		}
	    		next_bus_arrival();
	    		//passenger_arrival(pass_current_stop);
	    		more_pass = true;
	    		break;
	    	case 4: 
	    		CURRENT_STOP = 4;
	    		System.out.println();
	    		System.out.println(simulated_clock + " : " + bus.getName() + " is arriving at station " + stop + ".");
	    		System.out.println();
	    		if(bus.seats.length != 0 || bus.full == true) {
		    		bus.ride();
	    		}
	    		while((!(stop_4.isEmpty())) && bus.full == false) {
	    			board(stop_4.remove(0));
	    		}	
	    		if((!(stop_4.isEmpty())) && bus.full == true) {
	    			ListIterator <Passenger> iter = stop_4.listIterator();
	    			while(iter.hasNext()) {
	    				iter.next().wait_again();
	    			}
	    		}	
	    		System.out.println("THERE ARE " + bus.passengers_ON + " PASSENGERS ON THE BUS");
	    		stop++;
	    		if(stop > 4) {
	    			stop = stop %4;
	    		}
	    		next_bus_arrival();
	    		passenger_arrival(pass_current_stop);
	    		more_pass = true;
	            break;
			}
	}
	
	public static void board(Passenger p) {
		float two = 2;
		simulated_clock += two;
		passengers_got_on ++;
		p.time_in_bus = simulated_clock;
		Random random = new Random();
		int depart = random.nextInt(3) + 1;
		p.departure_stop = depart;
		System.out.println(simulated_clock + " : " + p.getName() + " boarded the bus at station " + p.initial_stop + ".");
		if ((p.number > 0 && p.number < 21) || (p.number > 99 && p.number <121)){
			try {
				write_to_debug(simulated_clock + " : " + p.getName() + " boarded the bus at station " + p.initial_stop + ".");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		bus.step_up(p);
	}
	
	public static void depart(Passenger p) {
		float one = 1;
		p.time_in_bus = simulated_clock - p.time_in_bus;//finding total time in bus
		total_time_on_bus += p.time_in_bus;
		System.out.println(simulated_clock + " : " + p.getName() + " spent " + p.time_in_bus + " on a bus.");
		System.out.println(simulated_clock + " : " + p.getName() + " is departing at station " + CURRENT_STOP);
		System.out.println(p.getName() + " had to wait for " + p.how_many_trips_waited() + " round trips before entering the bus.");

			try {
				if ((p.number > 0 && p.number < 21) || (p.number > 99 && p.number <121)){
					write_to_debug(simulated_clock + " : " + p.getName() + " spent " + p.time_in_bus + " on a bus.");
					write_to_debug(simulated_clock + " : " + p.getName() + " is departing at station " + CURRENT_STOP);
					write_to_debug(p.getName() + " had to wait for " + p.how_many_trips_waited() + " round trips before entering the bus.");
				}
				write_to_output(p.getName() + " had to wait for " + p.how_many_trips_waited() + " round trips before entering the bus.");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
		}// for recording specific outputs
		
		total_waiting_for_round_trips += p.wait_for_round_trip;
		simulated_clock += one; // one second to depart
		p.exit_stop = CURRENT_STOP;
		num_of_departures++;
	}
	public void record() {
	}
	
	
	/*******SIM*********/
	
	public static void simulation() {
		
		try {
			write_to_output("Emily Peguero Marmolejos CSCI 381 Buses First \n");
			write_to_debug("Emily Peguero Marmolejos CSCI 381 Buses First \n");
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		next_stop = 1; //the first "next stop is one
		bus = create_bus_arrival();	// creates arrival for next bus and returns a bus object
		while(num_of_departures < 10000) {
			if(more_pass == true) {
				passenger_arrival(pass_current_stop); // add first passenger to queue on the first station
			}
			
		}
		System.out.println(total_time_on_bus/num_of_passengers + "   s is the average time a passenger is on a the bus.");
		System.out.println("The average of waiting for round trips for passengers that got on the bus is : " + total_waiting_for_round_trips / passengers_got_on + " .");
		try {
			write_to_output("\n" + total_time_on_bus/num_of_passengers + " is the average time a passenger is on the bus.");
			write_to_output("The average of waiting for round trips for passengers that got on the bus is : " + total_waiting_for_round_trips / passengers_got_on + " .");
			write_to_output("The average of waiting for round trips for passengers that got off the bus is : " + total_waiting_for_round_trips / num_of_departures + " .");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	static float simulated_Clock = 0;
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		BusesFirst sim = new BusesFirst();
		sim.simulation();
	}

}
