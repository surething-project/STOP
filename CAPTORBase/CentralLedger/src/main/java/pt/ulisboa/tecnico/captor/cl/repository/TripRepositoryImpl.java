package pt.ulisboa.tecnico.captor.cl.repository;

import jakarta.persistence.EntityManager;
import pt.ulisboa.tecnico.captor.captorsharedlibrary.trip.Trip;

public class TripRepositoryImpl implements TripRepository {
	
	private EntityManager em;
	
	public TripRepositoryImpl(EntityManager em) {
		this.em = em;
	}

	@Override
	public Trip addTrip(Trip t) {
		em.getTransaction().begin();
		if (t.getId() == null) {
			em.persist(t);
		} else {
			t = em.merge(t);
		}
		try {
			em.getTransaction().commit();
		} catch (Exception ex) {
			return t;
		}
		return t;
	}

	@Override
	public Trip getTripById(Long id) {
		return em.find(Trip.class, id);
	}

	@Override
	public void deleteTrip(Trip t) {
		em.getTransaction().begin();
		if (em.contains(t)) {
			em.remove(t);
		} else {
			em.merge(t);
		}
		em.getTransaction().commit();

	}

}
