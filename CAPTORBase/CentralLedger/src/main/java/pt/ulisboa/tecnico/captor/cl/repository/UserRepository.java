package pt.ulisboa.tecnico.captor.cl.repository;

import pt.ulisboa.tecnico.captor.captorsharedlibrary.auth.User;

public interface UserRepository {
	
	User getUserById(Long id);
	
	User saveUser(User u);
	
	void deleteUser(User u);

}
