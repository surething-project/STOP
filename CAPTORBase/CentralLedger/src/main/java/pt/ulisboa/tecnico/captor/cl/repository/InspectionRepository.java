package pt.ulisboa.tecnico.captor.cl.repository;

import pt.ulisboa.tecnico.captor.captorsharedlibrary.inspection.Inspection;

public interface InspectionRepository {
	
	Inspection addInspection(Inspection i);
	
	Inspection getInspectionById(Long id);
	
	void deleteInspection(Inspection i);

}
