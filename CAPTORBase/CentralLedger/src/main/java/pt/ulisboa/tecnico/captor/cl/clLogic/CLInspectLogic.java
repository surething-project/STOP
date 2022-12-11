package pt.ulisboa.tecnico.captor.cl.clLogic;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;

import pt.ulisboa.tecnico.captor.captorsharedlibrary.inspection.Checkpoint;
import pt.ulisboa.tecnico.captor.captorsharedlibrary.inspection.Inspection;
import pt.ulisboa.tecnico.captor.captorsharedlibrary.inspection.InspectorLocationProof;
import pt.ulisboa.tecnico.captor.captorsharedlibrary.trip.Trip;
import pt.ulisboa.tecnico.captor.cl.clLogic.CLDataHolder.Session;
import pt.ulisboa.tecnico.captor.captorsharedlibrary.auth.User;
import pt.ulisboa.tecnico.captor.captorsharedlibrary.cryptography.CryptographyUtil;
import pt.ulisboa.tecnico.captor.captorsharedlibrary.evaluation.EvaluationDataHolder;
import pt.ulisboa.tecnico.captor.captorsharedlibrary.evaluation.Timer;

public class CLInspectLogic {
	
	private static final int NUMBER_PSEUDONYMS = 100000;
	private static final int NONCE_INTERVAL = 50000;
	private static final int DISTANCE_INSPECTION_SELECTION = 350; // in meters
	private static final int PREVIOUS_LOCATION_NUMBER = 20;		// number of previous location to compare with current
	private static final int TOLERANCE_STOPPED = 10; // in meters
	public static final String INSPECT_NAME_STRING = "INSPECT";
	
	public static Long createInspectUser(String publicKey) {
		System.out.println(new SimpleDateFormat("yyyyMMddHH:mm:ss:SSS").format(new Date()) + ": createInspectUser");
		
		Session session = CLDataHolder.getInstance().createSession();
		User user = new User();
		user.setPublicKey(publicKey);
		user.setUsername(INSPECT_NAME_STRING);
		if (CLDataHolder.getInstance().addUser(session, user)) {
			session.close();
			//Log: STOPLOG,Timestamp,Method,UserId
			System.out.println("STOPLOG," + new SimpleDateFormat("yyyyMMddHH:mm:ss:SSS").format(new Date()) + ",createInspectUser," + user.getId());
			return user.getId();
		}
		session.close();
		System.out.println("User not added");
		return null;
	}
	
	public static Long createCheckpoint(Checkpoint checkpoint) {
		System.out.println(new SimpleDateFormat("yyyyMMddHH:mm:ss:SSS").format(new Date()) + ": createCheckpoint");
		
		Session session = CLDataHolder.getInstance().createSession();
		User userStored = CLDataHolder.getInstance().getUser(session, checkpoint.getInspector().getId());
		System.out.println(userStored.getUsername());
		/**
		 * Validate username and public key of inspector
		 */
		if (userStored != null && checkpoint.getInspector().getPublicKey().equals(userStored.getPublicKey()) 
				&& userStored.getUsername().equals(INSPECT_NAME_STRING)) {
			Long id = CLDataHolder.getInstance().addCheckpoint(session, checkpoint);
			session.close();
			
			//Log: STOPLOG,Timestamp,Method,CheckpointId
			System.out.println("STOPLOG," + new SimpleDateFormat("yyyyMMddHH:mm:ss:SSS").format(new Date()) + ",createCheckpoint," + id);
			return id;
		}
		/**
		 * User not valid
		 */
		session.close();
		System.out.println("User not valid");
		return null;
	}
	
	public static Checkpoint getCheckpoint(Long checkpointId) {
		System.out.println(new SimpleDateFormat("yyyyMMddHH:mm:ss:SSS").format(new Date()) + ": getCheckpoint");
		
		Session session = CLDataHolder.getInstance().createSession();
		/**
		 * Returns null if checkpoint does not exist
		 */
		Checkpoint checkpoint = CLDataHolder.getInstance().getCheckpoint(session, checkpointId);
		session.close();
		return checkpoint;
	}
	
	public static Inspection createInspection(Long checkpointId) {
		System.out.println(new SimpleDateFormat("yyyyMMddHH:mm:ss:SSS").format(new Date()) + ": createInspection");
		
		Session session = CLDataHolder.getInstance().createSession();
		Checkpoint checkpoint = CLDataHolder.getInstance().getCheckpoint(session, checkpointId);
		/**
		 * Check if checkpoint exists
		 */
		if (checkpoint == null) {
			session.close();
			
			System.out.println("Checkpoint not found");
			return null; //checkpoint not found
		}
		Inspection inspection = new Inspection();
		inspection.setCheckpoint(checkpoint);
		/**
		 * Get available trips
		 */
		ArrayList<Long> availableTrips = CLDataHolder.getInstance().getAvailableTrips(session);
		/**
		 * If no trips exists, not possible to select
		 */
		if (availableTrips == null || availableTrips.isEmpty()) {
			session.close();
			System.out.println("No trips available");
			return null;
		}
		/**
		 * Select random
		 */
		Random random = new Random();
		int indexSelected = random.nextInt(availableTrips.size());
		/**
		 * Get selected trip
		 */
		Trip selected = CLDataHolder.getInstance().getTrip(session, availableTrips.get(indexSelected));
		/**
		 * While selected trip not close to checkpoint, select another one
		 */
		System.out.println("Searching for vehicle to select");
//		while (selected != null)	{
//			LocationChainItem item = selected.getLastItem();
//			if (item != null && ! item.isProof()) {				// if vehicle has just been inspected, ignore	
//				System.out.println("Last point is not proof");
//			
//				int distance = HaversineAlgorithm.HaversineInM(item.getLocation().getCoordinates().getLatitude(), 
//						item.getLocation().getCoordinates().getLongitude(), 
//						checkpoint.getCoordinates().getLatitude(), 
//						checkpoint.getCoordinates().getLongitude());		// calculate current distance to checkpoint
//				
//				if ( distance <= DISTANCE_INSPECTION_SELECTION && // if vehicle currently in range	
//						item.getLocation().getNumber() > PREVIOUS_LOCATION_NUMBER) {	// and if it has previous points
//					
//					System.out.println("In range with previous points");
//	
//					LocationChainItem previousItem = selected.getLocationChainItem(item.getLocation().getNumber() - PREVIOUS_LOCATION_NUMBER); // get previous item
//					if (! previousItem.isProof()) {		// if vehicle has just been inspected, ignore
//						System.out.println("Previous point is not a proof");
//						
//						if (HaversineAlgorithm.HaversineInM(item.getLocation().getCoordinates().getLatitude(), 	// measure distance between items
//								item.getLocation().getCoordinates().getLongitude(), 
//								previousItem.getLocation().getCoordinates().getLatitude(), 
//								previousItem.getLocation().getCoordinates().getLongitude()) < TOLERANCE_STOPPED ||	// if the vehicle is stopped (points are going to be close then)
//								HaversineAlgorithm.HaversineInM(previousItem.getLocation().getCoordinates().getLatitude(), 
//										previousItem.getLocation().getCoordinates().getLongitude(), 
//										checkpoint.getCoordinates().getLatitude(), 				// if current item is closer to checkpoint than previous item,
//										checkpoint.getCoordinates().getLongitude()) >= distance	//  then vehicle is heading towards checkpoint
//								) {
//							System.out.println("Vehicle selected");
//							break;			// vehicle has been selected, then break cycle
//							}
//						}	
//					}
//				}
//			
//			availableTrips.remove(indexSelected);	// remove invalid selected trip from list
//			if (availableTrips.isEmpty()) {			// If no more trips available for selection
//				session.close();
//				System.out.println("No close trips available");
//				return null;
//			}
//			/**
//			 * Select another one
//			 */
//			indexSelected = random.nextInt(availableTrips.size());
//			selected = CLDataHolder.getInstance().getTrip(session, availableTrips.get(indexSelected));
//		}
		
		if (selected == null) {
			session.close();
			System.out.println("Error in selection. Trip selected but not retrived");
			return null;
		}
		System.out.println("Vehicle selected");
		
		
		inspection.setTrip(selected);
		/**
		 * Generate nonce and pseudonyms
		 */
		inspection.setNonce(random.nextInt(NONCE_INTERVAL));
		// Generate pseudonyms
		String username = INSPECT_NAME_STRING + String.valueOf(random.nextInt(NUMBER_PSEUDONYMS));
		inspection.setInspectP(username);
		username = CLTransportLogic.TRANSPORT_NAME_STRING + String.valueOf(random.nextInt(NUMBER_PSEUDONYMS));
		inspection.setTransportP(username);
		/**
		 * Save inspection
		 */
		inspection = CLDataHolder.getInstance().addInspection(session, inspection);		// Updates inspection id
		checkpoint.addInspection(inspection);
		CLDataHolder.getInstance().modifyCheckpoint(session, checkpoint.getCheckpointId(), checkpoint);
		
		Timer timer = new Timer();
		timer.run();
		EvaluationDataHolder.getInstance().insertInspectionTimer(inspection.getInspectionId(), timer);
		
		//Log: STOPLOG,Timestamp,Method,CheckpointId,InspectionId,TripId
		System.out.println("STOPLOG," + new SimpleDateFormat("yyyyMMddHH:mm:ss:SSS").format(new Date()) + ",createInspection," + checkpointId + 
				"," + inspection.getInspectionId() + "," + selected.getId());
		session.close();
		return inspection;
	}
	
	public static Inspection getInspection(Long checkpointId, Long inspectionId) {
		System.out.println(new SimpleDateFormat("yyyyMMddHH:mm:ss:SSS").format(new Date()) + ": getInspection");
		
		Session session = CLDataHolder.getInstance().createSession();
		Checkpoint checkpoint = CLDataHolder.getInstance().getCheckpoint(session, checkpointId);
		Inspection inspection = CLDataHolder.getInstance().getInspection(session, inspectionId);
		if (checkpoint == null || inspection == null || ! inspection.getCheckpoint().getCheckpointId().equals(checkpoint.getCheckpointId())) {
			session.close();
			return null; //checkpoint not found
		}
		//return checkpoint.getInspection(inspectionId);
		session.close();
		return inspection;
		
	}
	
	public static Inspection closeInspection(Long checkpointId, Long inspectionId, InspectorLocationProof locationProof) {
		System.out.println(new SimpleDateFormat("yyyyMMddHH:mm:ss:SSS").format(new Date()) + ": closeInspection");
		Session session = CLDataHolder.getInstance().createSession();
		Checkpoint checkpoint = CLDataHolder.getInstance().getCheckpoint(session, checkpointId);
		if (checkpoint == null) {
			session.close();
			System.out.println("checkpoint not found");
			return null; //checkpoint not found
		}
		//Inspection insp = checkpoint.getInspection(inspectionId);
		Inspection inspection = CLDataHolder.getInstance().getInspection(session, inspectionId);
		if (inspection == null) {
			session.close();
			System.out.println("inspection not found");
			return null; //inspection not found
		}
		if (! inspection.getCheckpoint().getCheckpointId().equals(checkpointId)) {
			session.close();
			System.out.println("Checkpoint id not correct");
			return null;
		}
		if (inspection.getTrip() == null) {
			session.close();
			System.out.println("Inspection does not contain trip");
			return null;	// inspection does not have trip associated
		}
		
		
		/**
		 * validate proof
		 */
		if (! CryptographyUtil.verifySignature(locationProof.getProofMessage(), 				// validate signature
				CryptographyUtil.publicKeyFromString(checkpoint.getInspector().getPublicKey()),
				locationProof.getSignature())) {
			session.close();
			System.out.println("Signature not correct");
			return null;
		} else if (! locationProof.getProofMessage().getTransport().equals(inspection.getTransportP())) {	// validate transport username
			session.close();
			System.out.println("Transport pseudonym not correct");
			return null;
		} else if (! locationProof.getProofMessage().getInspect().equals(inspection.getInspectP())) {		// validate inspect username
			session.close();
			System.out.println("Inspect pseudonym not correct");
			return null;
		} else if (! locationProof.getProofMessage().getInspectionId().equals(inspection.getInspectionId())) {		// validate inspectionId
			session.close();
			System.out.println("Inspection id not correct");
			return null;
		} else if (! locationProof.getProofMessage().getTripId().equals(inspection.getTrip().getId())) {			// validate tripId
			session.close();
			System.out.println("Trip id not correct");
			return null;
		} else if (locationProof.getProofMessage().getNonce() != inspection.getNonce()) {			// validate nonce
			session.close();
			System.out.println("Nonce not correct");
			return null;
		}
		// timestamp and coordinates need validation?
			// coordinates close to coordinates of inspection?
			// timestamp close to real runtime?
		
		
		inspection.setProof(locationProof);
		
		/**
		 * Save inspection
		 */
		inspection = CLDataHolder.getInstance().addInspection(session, inspection);		// Updates inspection id
		//CLDataHolder.getInstance().modifyCheckpoint(checkpoint.getCheckpointId(), checkpoint);
		session.close();
		
		//Log: STOPLOG,Timestamp,Method,CheckpointId,InspectionId
		System.out.println("STOPLOG," + new SimpleDateFormat("yyyyMMddHH:mm:ss:SSS").format(new Date()) + ",closeInspection," + checkpointId + 
				"," + inspectionId);
		return inspection;
	}
	
	public static ArrayList<Inspection> getInspections(Long checkpointId) {			// TO DO: implement in api
		System.out.println(new SimpleDateFormat("yyyyMMddHH:mm:ss:SSS").format(new Date()) + ": getInspections");
		
		Session session = CLDataHolder.getInstance().createSession();
		Checkpoint checkpoint = CLDataHolder.getInstance().getCheckpoint(session, checkpointId);
		if (checkpoint == null) {
			session.close();
			return null; //checkpoint not found
		}
		ArrayList<Inspection> array = (ArrayList<Inspection>) checkpoint.getInspections();
		session.close();
		return array;
	}
	
	// To use in evaluation in case something goes wrong
	public static boolean forceEndInspection(Long checkpointId, Long inspectionId) {
		System.out.println(new SimpleDateFormat("yyyyMMddHH:mm:ss:SSS").format(new Date()) + ": forceEndInspection");
		
		Session session = CLDataHolder.getInstance().createSession();
		Checkpoint checkpoint = CLDataHolder.getInstance().getCheckpoint(session, checkpointId);
		Inspection inspection = CLDataHolder.getInstance().getInspection(session, inspectionId);
		if (checkpoint == null || inspection == null || ! inspection.getCheckpoint().getCheckpointId().equals(checkpoint.getCheckpointId())) {
			session.close();
			return false; //checkpoint or inspection not found
		}
		inspection.getProof().setSignature("ERROR");
		if (CLDataHolder.getInstance().addInspection(session, inspection) != null) {
			//Log: STOPLOG,Timestamp,forceEndInspection,CheckpointId,InspectionId
			System.out.println("STOPLOG," + new SimpleDateFormat("yyyyMMddHH:mm:ss:SSS").format(new Date()) + ",forceEndInspection," 
					+ checkpointId + "," + inspectionId);
			session.close();
			return true;
		}
		session.close();
		return false;
	}
}
