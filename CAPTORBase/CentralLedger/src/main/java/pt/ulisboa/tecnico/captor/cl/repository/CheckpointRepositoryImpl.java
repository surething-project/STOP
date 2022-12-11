package pt.ulisboa.tecnico.captor.cl.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import pt.ulisboa.tecnico.captor.captorsharedlibrary.inspection.Checkpoint;

@Transactional
public class CheckpointRepositoryImpl implements CheckpointRepository {

	@PersistenceContext
	private EntityManager em;
	
	public CheckpointRepositoryImpl(EntityManager em) {
		this.em = em;
	}

	@Override
	public Checkpoint addCheckpoint(Checkpoint c) {
		em.getTransaction().begin();
		if (c.getCheckpointId() == null) {
			em.persist(c);
		} else {
			c = em.merge(c);
		}
		em.getTransaction().commit();
		return c;
	}

	@Override
	public Checkpoint getCheckpointById(Long id) {
		return em.find(Checkpoint.class, id);
	}

	@Override
	public void deleteCheckpoint(Checkpoint c) {
		em.getTransaction().begin();
		if (em.contains(c)) {
			em.remove(c);
		} else {
			em.merge(c);
		}
		em.getTransaction().commit();
	}
}
