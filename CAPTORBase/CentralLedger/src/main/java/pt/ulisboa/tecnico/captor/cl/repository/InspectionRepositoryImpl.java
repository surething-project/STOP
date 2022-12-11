package pt.ulisboa.tecnico.captor.cl.repository;

import jakarta.persistence.EntityManager;
import pt.ulisboa.tecnico.captor.captorsharedlibrary.inspection.Inspection;

public class InspectionRepositoryImpl implements InspectionRepository {
	
	private EntityManager em;
	
	public InspectionRepositoryImpl(EntityManager em) {
		this.em = em;
	}

	@Override
	public Inspection addInspection(Inspection i) {
		em.getTransaction().begin();
		if (i.getInspectionId() == null) {
			em.persist(i);
		} else {
			i = em.merge(i);
		}
		try {
			em.getTransaction().commit();
		} catch (Exception ex) {
			return i;
		}
		return i;
	}

	@Override
	public Inspection getInspectionById(Long id) {
		return em.find(Inspection.class, id);
	}

	@Override
	public void deleteInspection(Inspection i) {
		em.getTransaction().begin();
		if (em.contains(i)) {
			em.remove(i);
		} else {
			em.merge(i);
		}
		em.getTransaction().commit();
	}
}
