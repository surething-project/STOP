package pt.ulisboa.tecnico.captor.cl.clLogic;

import java.text.SimpleDateFormat;
import java.util.Date;

import pt.ulisboa.tecnico.captor.captorsharedlibrary.inspection.Inspection;
import pt.ulisboa.tecnico.captor.captorsharedlibrary.trip.LocationChain;
import pt.ulisboa.tecnico.captor.captorsharedlibrary.trip.LocationChainItem;
import pt.ulisboa.tecnico.captor.captorsharedlibrary.trip.Trip;
import pt.ulisboa.tecnico.captor.cl.clLogic.CLDataHolder.Session;
import pt.ulisboa.tecnico.captor.captorsharedlibrary.auth.User;
import pt.ulisboa.tecnico.captor.captorsharedlibrary.cryptography.CryptographyUtil;
import pt.ulisboa.tecnico.captor.captorsharedlibrary.evaluation.EvaluationDataHolder;
import pt.ulisboa.tecnico.captor.captorsharedlibrary.evaluation.Timer;

public class CLTransportLogic {
	
	private static final int NUMBER_TRANSPORTS = 10000;
	public static final String TRANSPORT_NAME_STRING = "TRANSPORT";

	public static Long createTransportUser(String publicKey) {
		System.out.println(new SimpleDateFormat("yyyyMMddHH:mm:ss:SSS").format(new Date()) + ": createTransportUser");
		
		Session session = CLDataHolder.getInstance().createSession();
		User user = new User();
		user.setPublicKey(publicKey);
		user.setUsername(TRANSPORT_NAME_STRING);
		if (CLDataHolder.getInstance().addUser(session, user)) {
			session.close();
			
			//Log: STOPLOG,Timestamp,Method,UserId
			System.out.println("STOPLOG," + new SimpleDateFormat("yyyyMMddHH:mm:ss:SSS").format(new Date()) + ",createTransportUser," + user.getId());
			return user.getId();
		}
		session.close();
		System.out.println("User not added");
		return null;
	}
	
	public static Long createTrip(Trip receivedTrip) {
		System.out.println(new SimpleDateFormat("yyyyMMddHH:mm:ss:SSS").format(new Date()) + ": createTrip");
		
		Session session = CLDataHolder.getInstance().createSession();
		User transport = CLDataHolder.getInstance().getUser(session, receivedTrip.getTransport().getId());
		if (!( transport != null && transport.getPublicKey().equals(receivedTrip.getTransport().getPublicKey())
				&& transport.getUsername().equals(TRANSPORT_NAME_STRING)) ) {
			session.close();
			return null;
		}
		// initial coordinates is validated with NotNull annotation
		
		receivedTrip.setStatus(Trip.StatusEnum.SCHEDULED);
		receivedTrip.setLocationChain(new LocationChain());		// Initialize LocationChain
		Long id = CLDataHolder.getInstance().addTrip(session, receivedTrip);
		session.close();
		
		//Log: STOPLOG,Timestamp,Method,TripId
		System.out.println("STOPLOG," + new SimpleDateFormat("yyyyMMddHH:mm:ss:SSS").format(new Date()) + ",createTrip," + id);
		return id;
	}
	
	public static boolean startTrip(Long tripId, LocationChainItem locationItem) {
		System.out.println(new SimpleDateFormat("yyyyMMddHH:mm:ss:SSS").format(new Date()) + ": startTrip");
		
		Session session = CLDataHolder.getInstance().createSession();
		Trip trip = CLDataHolder.getInstance().getTrip(session, tripId);
		if (trip == null){
			System.out.println("Trip not found");
			session.close();
			return false; // trip not found
		} else if (locationItem.getLocation() == null) {
			session.close();
			return false;
		}
		
		if (!trip.getStatus().equals(Trip.StatusEnum.SCHEDULED)) {
			System.out.println("not scheduled!");
			session.close();
			return false;
		}
		/**
		 * Validate location point signature
		 */
		if (! CryptographyUtil.verifySignature(locationItem.getLocation(), 
				CryptographyUtil.publicKeyFromString(trip.getTransport().getPublicKey()),
				locationItem.getTransportSignature())) {
			session.close();
			return false;
		} else if (locationItem.getLocation().getPreviousLocationSignature() != null) {		//  First location point has previous signature null
			session.close();
			return false;
		}
		
		trip.setStatus(Trip.StatusEnum.ON_ROUTE);
		System.out.println(trip.getStatus());
		
		/**
		 * Save location item
		 */
		locationItem = CLDataHolder.getInstance().saveLocationChainItem(session, locationItem);
		LocationChain locationChain = trip.getLocationChain();
		locationChain.addItem(locationItem);
		locationChain = CLDataHolder.getInstance().modifyLocationChain(session, locationChain);
		trip.setLocationChain(locationChain);
		CLDataHolder.getInstance().modifyTrip(session, tripId, trip);
		session.close();
		
		//Log: STOPLOG,Timestamp,Method,TripId
		System.out.println("STOPLOG," + new SimpleDateFormat("yyyyMMddHH:mm:ss:SSS").format(new Date()) + ",startTrip," + tripId);
		return true;
	}
	
	public static Trip getTrip(Long tripId) {
		System.out.println(new SimpleDateFormat("yyyyMMddHH:mm:ss:SSS").format(new Date()) + ": getTrip");
		Session session = CLDataHolder.getInstance().createSession();
		Trip trip = CLDataHolder.getInstance().getTrip(session, tripId);	// returns null if not existed
		//session.close();			Current bug here - session is closed before JSON response is built. Temporary fix: let the session timeout
		//Log: STOPLOG,Timestamp,Method,TripId
		System.out.println("STOPLOG," + new SimpleDateFormat("yyyyMMddHH:mm:ss:SSS").format(new Date()) + ",getTrip," + tripId);
		return trip;
	}
	
	public static boolean addLocation(Long tripId, LocationChainItem locationItem) {
		System.out.println(new SimpleDateFormat("yyyyMMddHH:mm:ss:SSS").format(new Date()) + ": addLocation");
		Session session = CLDataHolder.getInstance().createSession();
		Trip trip = CLDataHolder.getInstance().getTrip(session, tripId);
		if (trip == null){
			session.close();
			System.out.println("Trip not found");
			return false; // trip not found
		} else if (!trip.getStatus().equals(Trip.StatusEnum.ON_ROUTE)) {
			session.close();
			System.out.println("Trip not on route");
			return false;
		} else if (locationItem.getLocation() == null) {
			session.close();
			System.out.println("No location received");
			return false;
		} else if (locationItem.getLocationProof() != null && locationItem.getLocationProof().getNumber() > 0) {
			session.close();
			System.out.println("Location proof detected in Location Point");
			return false;
		}
		
		/**
		 * Validate location point signature
		 */
		
		if (! CryptographyUtil.verifySignature(locationItem.getLocation(), 
				CryptographyUtil.publicKeyFromString(trip.getTransport().getPublicKey()),
				locationItem.getTransportSignature())) {
			session.close();
			System.out.println("Signature of location item not valid");
			return false;
		}
		
		// validate if number received is next in list
		// validate hash of previous
		
		/**
		 * Save location item
		 */
		locationItem = CLDataHolder.getInstance().saveLocationChainItem(session, locationItem);
		LocationChain locationChain = trip.getLocationChain();
		locationChain.addItem(locationItem);
		locationChain = CLDataHolder.getInstance().modifyLocationChain(session, locationChain);
		trip.setLocationChain(locationChain);
		CLDataHolder.getInstance().modifyTrip(session,tripId, trip);
		
		//Log: STOPLOG,Timestamp,Method,TripId,LocationItemId,LocationItemNo
		System.out.println("STOPLOG," + new SimpleDateFormat("yyyyMMddHH:mm:ss:SSS").format(new Date()) + ",addLocation," + tripId +
				"," + locationItem.getId() + "," + locationItem.getLocation().getNumber());
		session.close();
		

		return true;
	}
	
	public static boolean addProof(Long tripId, LocationChainItem locationItem) {
		System.out.println(new SimpleDateFormat("yyyyMMddHH:mm:ss:SSS").format(new Date()) + ": addProof");
		
		Session session = CLDataHolder.getInstance().createSession();
		Trip trip = CLDataHolder.getInstance().getTrip(session, tripId);
		if (trip == null){
			session.close();
			System.out.println("Trip not found");
			return false; // trip not found
		}
		if (!trip.getStatus().equals(Trip.StatusEnum.ON_ROUTE)) {
			session.close();
			System.out.println("Trip not on route");
			return false;
		} else if (locationItem.getLocationProof() == null) {
			session.close();
			System.out.println("No location proof received");
			return false;
		} else if (locationItem.getLocation() != null && locationItem.getLocation().getNumber() > 0) {
			session.close();
			System.out.println("Location point detected in Location Proof");
			return false;
		}
		
		/**
		 * validate location proof signatures
		 */
		/*if (! CryptographyUtil.verifySignature(locationItem.getLocationProof(), 
				CryptographyUtil.publicKeyFromString(trip.getTransport().getPublicKey()),
				locationItem.getTransportSignature())) {
			session.close();
			System.out.println("Signature of location item not valid");
			return false;
		}*/
		// validate inspect signature?
		
		//validate number received
		// validate hash of previous
		
		/**
		 * Save location proof
		 */
		locationItem = CLDataHolder.getInstance().saveLocationChainItem(session, locationItem);
		LocationChain locationChain = trip.getLocationChain();
		locationChain.addItem(locationItem);
		locationChain = CLDataHolder.getInstance().modifyLocationChain(session, locationChain);
		trip.setLocationChain(locationChain);
		CLDataHolder.getInstance().modifyTrip(session, tripId, trip);
		
		//Log: STOPLOG,Timestamp,Method,TripId,LocationItemId,LocationItemNo
		System.out.println("STOPLOG," + new SimpleDateFormat("yyyyMMddHH:mm:ss:SSS").format(new Date()) + ",addProof," + tripId +
				"," + locationItem.getId() + "," + locationItem.getLocationProof().getNumber());	
		session.close();
		
		
		return true;
	}
	
	public static boolean endTrip(Long tripId, LocationChainItem locationItem) {
		System.out.println(new SimpleDateFormat("yyyyMMddHH:mm:ss:SSS").format(new Date()) + ": endTrip");
		/**
		 * Save last location item
		 */
		if (!addLocation(tripId, locationItem)) {
			System.out.println("Trip not ended");
			return false;
		}
		Session session = CLDataHolder.getInstance().createSession();
		Trip trip = CLDataHolder.getInstance().getTrip(session, tripId);
		
		// validate location chain
		
		trip.setStatus(Trip.StatusEnum.ARRIVED);
		CLDataHolder.getInstance().modifyTrip(session, tripId, trip);
		session.close();
		
		//Log: STOPLOG,Timestamp,Method,TripId
		System.out.println("STOPLOG," + new SimpleDateFormat("yyyyMMddHH:mm:ss:SSS").format(new Date()) + ",endTrip," + tripId);
		return true;
	}

	public static Inspection checkInspection(Long tripId) {
		System.out.println(new SimpleDateFormat("yyyyMMddHH:mm:ss:SSS").format(new Date()) + ": checkInspection");
		
		Session session = CLDataHolder.getInstance().createSession();
		Inspection inspection =  CLDataHolder.getInstance().getActiveInspection(session, tripId);
		
		
		//Log: STOPLOG,Timestamp,Method,TripId,Boolean(selected)
		System.out.println("STOPLOG," + new SimpleDateFormat("yyyyMMddHH:mm:ss:SSS").format(new Date()) + ",checkInspection," + tripId 
				+ "," + (inspection != null));
		
		if (inspection != null) {
			Timer timer = EvaluationDataHolder.getInstance().popInspectionTimer(inspection.getInspectionId());
			if (timer != null) {
				timer.stop();
				//Log: STOPLOG,Timestamp,InspectionNotification,InspectionId,Time
				System.out.println("STOPLOG," + new SimpleDateFormat("yyyyMMddHH:mm:ss:SSS").format(new Date()) + ",InspectionNotification," + inspection.getInspectionId() 
						+ "," + timer.getDifference() + "ms");
			}
		}
		session.close();
		
		return inspection;
	}
	
	public static boolean forceEnd(Long tripId) {
		System.out.println(new SimpleDateFormat("yyyyMMddHH:mm:ss:SSS").format(new Date()) + ": forceEnd");
		
		Session session = CLDataHolder.getInstance().createSession();
		Trip trip = CLDataHolder.getInstance().getTrip(session, tripId);
		
		if (trip != null) {
			trip.setStatus(Trip.StatusEnum.ARRIVED);
			if (CLDataHolder.getInstance().modifyTrip(session, tripId, trip)) {
				//Log: STOPLOG,Timestamp,forceEndTrip,TripId
				System.out.println("STOPLOG," + new SimpleDateFormat("yyyyMMddHH:mm:ss:SSS").format(new Date()) + ",forceEndTrip," + tripId);
				session.close();
				return true;
			}
		}
		session.close();
		return false;
	}
	
}
