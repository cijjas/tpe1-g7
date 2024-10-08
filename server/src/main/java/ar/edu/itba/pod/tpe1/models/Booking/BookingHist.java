package ar.edu.itba.pod.tpe1.models.Booking;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class BookingHist extends Booking {
    private String sector;
    private int checkinCounter;

    public BookingHist(String airlineName, String flightCode, String bookingCode, int checkinCounter, String sector) {
        super(airlineName, flightCode, bookingCode);
        this.checkinCounter = checkinCounter;
        this.sector = sector;
    }

    public BookingHist(Booking booking, int checkinCounter){
        super(booking.getAirlineName(), booking.getFlightCode(), booking.getBookingCode());
        this.checkinCounter = checkinCounter;
    }

    public static BookingHist empty(int checkinCounter){
        return new BookingHist(null, null, null, checkinCounter, null);
    }

}
