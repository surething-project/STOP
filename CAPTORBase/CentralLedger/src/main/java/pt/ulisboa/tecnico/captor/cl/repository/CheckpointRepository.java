package pt.ulisboa.tecnico.captor.cl.repository;

import pt.ulisboa.tecnico.captor.captorsharedlibrary.inspection.Checkpoint;

public interface CheckpointRepository {
	
	Checkpoint addCheckpoint(Checkpoint c);
	
	Checkpoint getCheckpointById(Long id);
	
	void deleteCheckpoint(Checkpoint c);

}
