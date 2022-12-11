package pt.ulisboa.tecnico.captor.cl.repository;

import jakarta.persistence.EntityManager;
import pt.ulisboa.tecnico.captor.captorsharedlibrary.trip.LocationChain;

public class LocationChainRepositoryImpl implements LocationChainRepository {
	
	private EntityManager em;
	
	public LocationChainRepositoryImpl(EntityManager em) {
		this.em = em;
	}

	@Override
	public LocationChain addLocationChain(LocationChain l) {
		em.getTransaction().begin();
		if (l.getId() == null) {
			em.persist(l);
		} else {
			l = em.merge(l);
		}
		try {
			em.getTransaction().commit();
		} catch (Exception ex) {
			return l;
		}
		return l;
	}

	@Override
	public LocationChain getLocationChainById(Long id) {
		return em.find(LocationChain.class, id);
	}

	@Override
	public void deleteLocationChain(LocationChain l) {
		em.getTransaction().begin();
		if (em.contains(l)) {
			em.remove(l);
		} else {
			em.merge(l);
		}
		em.getTransaction().commit();
	}

}
