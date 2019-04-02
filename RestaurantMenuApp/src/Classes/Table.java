package Classes;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;



public class Table {
	private int tableID;
	private int pax;
	private int occupied;
	private int orderID;
	private ArrayList<Reservations> reservationList = new ArrayList<>();

	static class Reservations{
		private String contact;
		private String name;
		private String date;
		private int pax;


		public Reservations (String contact,String name, String date, int pax){
			this.contact = contact;
			this.name = name;
			this.date = date;
			this.pax = pax;
		}
		public String getDate(){
			return date;
		}
		public String getName(){
			return name;
		}
		public String getContact(){
			return contact;
		}
		public int getPax(){
			return pax;
		}
		public String toStringTwo(){
			DateTimeFormatter format = DateTimeFormatter.ofPattern("ddMMyyyyhh:mma");
			LocalDateTime date = LocalDateTime.parse(getDate(),format);
			return "Reservation date :" + date.toLocalDate() + " " + date.toLocalTime() +
					", Name :" + getName() + ", Contact :" + getContact() + ", Pax :" + getPax();
		}
	}

	public Table(int tableID)
	{
		this.occupied = 0;
		this.orderID = -1;
		this.pax = 0;
		this.tableID = tableID;

	}

	public int getTableID() {
		return tableID;
	}
	public int getOrderID() {
		return orderID;
	}

	public  void setOccupied(int occupied){
		this.occupied = occupied;
	}

	public int isOccupied() {
		return occupied;
	}

	public void setOrderID(int orderID){
		this.orderID = orderID;
	}

	public void setPax(int pax){
		this.pax = pax;
	}

	public ArrayList<Reservations> getReservationList(){
		return reservationList;
	}

	public String toString(){
		String check=null;
		if (isOccupied()==-1)
			check = "Reserved.";
		if (isOccupied()==0)
			check = "not occupied.";
		if (isOccupied()==1)
			check = "occupied.";
		return "Table " + getTableID() + " is " + check;
	}


	public void isReserved(String contact,String name, String date, int pax){
		reservationList.add(new Reservations(contact, name, date, pax));
	}

}

