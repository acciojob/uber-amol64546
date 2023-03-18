package com.driver.services.impl;

import com.driver.model.TripBooking;
import com.driver.services.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.driver.model.Customer;
import com.driver.model.Driver;
import com.driver.repository.CustomerRepository;
import com.driver.repository.DriverRepository;
import com.driver.repository.TripBookingRepository;
import com.driver.model.TripStatus;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Service
public class CustomerServiceImpl implements CustomerService {

	@Autowired
	CustomerRepository customerRepository2;

	@Autowired
	DriverRepository driverRepository2;

	@Autowired
	TripBookingRepository tripBookingRepository2;

	@Override
	public void register(Customer customer) {
		//Save the customer in database
		customerRepository2.save(customer);
	}

	@Override
	public void deleteCustomer(Integer customerId) {
		// Delete customer without using deleteById function
//		customerRepository2.deleteById(customerId);
		Customer customer = customerRepository2.findById(customerId).get();
		customer.getTripBookingList().clear();
		customerRepository2.delete(customer);
	}

	@Override
	public TripBooking bookTrip(int customerId, String fromLocation, String toLocation, int distanceInKm) throws Exception{
		//Book the driver with lowest driverId who is free (cab available variable is Boolean.TRUE). If no driver is available, throw "No cab available!" exception
		//Avoid using SQL query

		// getting nearest driver who's cab is available
		Driver driver = null;
		List<Driver> driverList = driverRepository2.findAll();

		for(Driver driver1: driverList){
			if(driver1.getCab().getAvailable()){
				driver = driver1;
				break;
			}
		}
		if(driver==null) throw new Exception("No cab available!");

		// setting cab status
		driver.getCab().setAvailable(false);

		// getting customer
		Customer customer = customerRepository2.findById(customerId).get();

		// booking cab
		TripBooking tripBooking = new TripBooking();
		tripBooking.setFromLocation(fromLocation);
		tripBooking.setToLocation(toLocation);
		tripBooking.setDistanceInKm(distanceInKm);
		tripBooking.setStatus(TripStatus.CONFIRMED);
		tripBooking.setCustomer(customer);
		tripBooking.setDriver(driver);


		// adding booking in booking list in driver
		driver.getTripBookingList().add(tripBooking);
		// adding booking in booking list in customer
		customer.getTripBookingList().add(tripBooking);

		// saving driver in db
		driverRepository2.save(driver);
		// saving customer in db
		customerRepository2.save(customer);
		// booking will be saved in db by cascade using customer

		return tripBooking;

	}

	@Override
	public void cancelTrip(Integer tripId){
		//Cancel the trip having given trip Id and update TripBooking attributes accordingly
		TripBooking tripBooking = tripBookingRepository2.findById(tripId).get();

		// trip is canceled
		tripBooking.setStatus(TripStatus.CANCELED);
		tripBooking.setBill(0);
		// cab is available now
		tripBooking.getDriver().getCab().setAvailable(true);

		tripBookingRepository2.save(tripBooking);

	}

	@Override
	public void completeTrip(Integer tripId){
		//Complete the trip having given trip Id and update TripBooking attributes accordingly
		TripBooking tripBooking = tripBookingRepository2.findById(tripId).get();
		// trip complete
		tripBooking.setStatus(TripStatus.COMPLETED);
		// cab is available now
		tripBooking.getDriver().getCab().setAvailable(true);

		tripBookingRepository2.save(tripBooking);

	}
}
