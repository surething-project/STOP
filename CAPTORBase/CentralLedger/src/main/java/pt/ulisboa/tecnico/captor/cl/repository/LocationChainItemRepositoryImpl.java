package pt.ulisboa.tecnico.captor.cl.repository;

import jakarta.persistence.EntityManager;
import pt.ulisboa.tecnico.captor.captorsharedlibrary.trip.LocationChainItem;

public class LocationChainItemRepositoryImpl implements LocationChainItemRepository {
	
	private EntityManager em;
	
	public LocationChainItemRepositoryImpl(EntityManager em) {
		this.em = em;
	}

	@Override
	public LocationChainItem addLocationChainItem(LocationChainItem l) {
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
	public LocationChainItem getLocationChainItemById(Long id) {
		return em.find(LocationChainItem.class, id);
	}

	@Override
	public void deleteLocationChainItem(LocationChainItem l) {
		em.getTransaction().begin();
		if (em.contains(l)) {
			em.remove(l);
		} else {
			em.merge(l);
		}
		em.getTransaction().commit();
	}

}
