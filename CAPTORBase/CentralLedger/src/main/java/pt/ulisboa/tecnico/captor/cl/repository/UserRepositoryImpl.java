package pt.ulisboa.tecnico.captor.cl.repository;

import jakarta.persistence.EntityManager;
import pt.ulisboa.tecnico.captor.captorsharedlibrary.auth.User;

public class UserRepositoryImpl implements UserRepository {
	
	private EntityManager em;
	
	public UserRepositoryImpl(EntityManager em) {
		this.em = em;
	}

	@Override
	public User getUserById(Long id) {
		return em.find(User.class, id);
	}

	@Override
	public User saveUser(User u) {
		em.getTransaction().begin();
		if (u.getId() == null) {
			em.persist(u);
		} else {
			u = em.merge(u);
		}
		em.getTransaction().commit();
		return u;
	}

	@Override
	public void deleteUser(User u) {
		em.getTransaction().begin();
		if (em.contains(u)) {
			em.remove(u);
		} else {
			em.merge(u);
		}
		em.getTransaction().commit();
	}

}
