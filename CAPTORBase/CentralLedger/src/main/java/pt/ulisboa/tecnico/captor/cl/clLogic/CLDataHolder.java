package pt.ulisboa.tecnico.captor.cl.clLogic;

import java.util.ArrayList;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import jakarta.persistence.TypedQuery;
import pt.ulisboa.tecnico.captor.captorsharedlibrary.inspection.Checkpoint;
import pt.ulisboa.tecnico.captor.captorsharedlibrary.inspection.Inspection;
import pt.ulisboa.tecnico.captor.captorsharedlibrary.trip.LocationChain;
import pt.ulisboa.tecnico.captor.captorsharedlibrary.trip.LocationChainItem;
import pt.ulisboa.tecnico.captor.captorsharedlibrary.trip.Trip;
import pt.ulisboa.tecnico.captor.cl.repository.CheckpointRepository;
import pt.ulisboa.tecnico.captor.cl.repository.CheckpointRepositoryImpl;
import pt.ulisboa.tecnico.captor.cl.repository.InspectionRepository;
import pt.ulisboa.tecnico.captor.cl.repository.InspectionRepositoryImpl;
import pt.ulisboa.tecnico.captor.cl.repository.LocationChainItemRepository;
import pt.ulisboa.tecnico.captor.cl.repository.LocationChainItemRepositoryImpl;
import pt.ulisboa.tecnico.captor.cl.repository.LocationChainRepository;
import pt.ulisboa.tecnico.captor.cl.repository.LocationChainRepositoryImpl;
import pt.ulisboa.tecnico.captor.cl.repository.TripRepository;
import pt.ulisboa.tecnico.captor.cl.repository.TripRepositoryImpl;
import pt.ulisboa.tecnico.captor.cl.repository.UserRepository;
import pt.ulisboa.tecnico.captor.cl.repository.UserRepositoryImpl;
import pt.ulisboa.tecnico.captor.captorsharedlibrary.auth.User;

/**
 * This class handles all data saving and access of the Central Ledger in the database.
 * To use the methods of this class, a Session must be created and closed at the end of the usage
 */
public class CLDataHolder {
	private static CLDataHolder dataHolderInstance = null;
	
	private static final String PERSISTENCE_UNIT_NAME_STRING = "cl";
	
	private EntityManagerFactory entityManagerFactory;
	
	public static CLDataHolder getInstance() {
		if (dataHolderInstance == null) {
			dataHolderInstance = new CLDataHolder();
		}
		return dataHolderInstance;
	}
	
	public CLDataHolder() {
		checkDB();
	}
	
	/*
	 * User storage
	 */
	
	private void checkDB() {
		if (entityManagerFactory == null) {
			entityManagerFactory = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME_STRING);
		}
	}
	
	public boolean addUser(Session s, User user) {
		EntityManager em = s.getEntityManager();
		UserRepository userRepository = new UserRepositoryImpl(em);
		user = userRepository.saveUser(user);
		return user != null;
		
	}
	
	public User getUser(Session s, Long id) {
		EntityManager em = s.getEntityManager();
		UserRepository userRepository = new UserRepositoryImpl(em);
		return userRepository.getUserById(id);

	}
	
	/*
	 * Trip storage
	 */
	
	public Long addTrip(Session s, Trip trip) {
		EntityManager em = s.getEntityManager();
		TripRepository tripRepository = new TripRepositoryImpl(em);
		trip = tripRepository.addTrip(trip);
		if (trip != null) {
			return trip.getId();
		}
		return null;	
	}
	
	public Trip getTrip(Session s, Long tripId) {
		EntityManager em = s.getEntityManager();
		TripRepository tripRepository = new TripRepositoryImpl(em);
		return tripRepository.getTripById(tripId);
	}
	
	public boolean modifyTrip(Session s, Long tripId, Trip trip) {
		EntityManager em = s.getEntityManager();
		TripRepository tripRepository = new TripRepositoryImpl(em);
		LocationChainRepository locationChainRepository = new LocationChainRepositoryImpl(em);
		trip.setLocationChain(locationChainRepository.addLocationChain(trip.getLocationChain()));
		return tripRepository.addTrip(trip) != null;
	}
	
	
	public LocationChain modifyLocationChain(Session s, LocationChain chain) {
		EntityManager em = s.getEntityManager();
		LocationChainRepository locationChainRepository = new LocationChainRepositoryImpl(em);
		return locationChainRepository.addLocationChain(chain);
	}
	
	public LocationChainItem saveLocationChainItem(Session s, LocationChainItem item) {
		EntityManager em = s.getEntityManager();
		LocationChainItemRepository locationChainItemRepository = new LocationChainItemRepositoryImpl(em);
		return locationChainItemRepository.addLocationChainItem(item);
	}
	
	
	/*
	 * Checkpoint storage
	 */
	
	public Long addCheckpoint(Session s, Checkpoint checkpoint) {
		EntityManager em = s.getEntityManager();
		CheckpointRepository checkpointRepository = new CheckpointRepositoryImpl(em);
		Checkpoint c = checkpointRepository.addCheckpoint(checkpoint);
		if (c != null) {
			return c.getCheckpointId();
		}
		return null;
	}
	
	public Checkpoint getCheckpoint(Session s, Long checkpointId) {
		EntityManager em = s.getEntityManager();
		CheckpointRepository checkpointRepository = new CheckpointRepositoryImpl(em);
		return checkpointRepository.getCheckpointById(checkpointId);
	}
	
	public boolean modifyCheckpoint(Session s, Long checkpointId, Checkpoint checkpoint) {
		EntityManager em = s.getEntityManager();
		CheckpointRepository checkpointRepository = new CheckpointRepositoryImpl(em);
		return checkpointRepository.addCheckpoint(checkpoint) != null;
	}
	
	
	public Inspection addInspection(Session s, Inspection inspection) {
		EntityManager em = s.getEntityManager();
		InspectionRepository inspectionRepository = new InspectionRepositoryImpl(em);
		return inspectionRepository.addInspection(inspection);
	}
	
	public Inspection getInspection(Session s, Long id) {
		EntityManager em = s.getEntityManager();
		InspectionRepository inspectionRepository = new InspectionRepositoryImpl(em);
		return inspectionRepository.getInspectionById(id);
	}
	
	public Inspection getActiveInspection(Session s, Long tripId) {
		EntityManager em = s.getEntityManager();
		TypedQuery<Inspection> query = em.createQuery("SELECT i FROM Inspection i WHERE i.trip.id "
												+ "= :id AND i.proof.signature is NULL", Inspection.class);
		query.setParameter("id", tripId);
		try {
			return (Inspection) query.getSingleResult(); 
		} catch (Exception e) {
			//System.out.println(e.getMessage());
			return null;
		}
		
	}
	
	public ArrayList<Long> getAvailableTrips(Session s) {
		EntityManager em = s.getEntityManager();
		TypedQuery<Long> query = em.createQuery("SELECT id FROM Trip t WHERE t.status = :status " + 
				 									"AND t.id NOT IN " + 
				 								"(SELECT trip.id FROM Inspection i WHERE i.proof.signature is NULL)", Long.class);
		query.setParameter("status", Trip.StatusEnum.ON_ROUTE);
		try {
			return (ArrayList<Long>) query.getResultList();
		} catch (Exception e) {
			//System.out.println(e.getMessage());
			return null;
		}
	 }
	
	public Session createSession() {
		return new Session();
	}
	
	public class Session {
		private EntityManager em;
		
		private Session() {
			this.em = entityManagerFactory.createEntityManager();
		}
		
		private EntityManager getEntityManager() {
			return this.em;
		}
		
		public void close() {
			this.em.close();
		}
	}
	
}
