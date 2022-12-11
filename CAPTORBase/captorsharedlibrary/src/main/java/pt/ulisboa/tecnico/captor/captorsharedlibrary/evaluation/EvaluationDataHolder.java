package pt.ulisboa.tecnico.captor.captorsharedlibrary.evaluation;

import java.util.HashMap;

public class EvaluationDataHolder {
    private static final String TAG = "EvaluationDataHolder";

    private static EvaluationDataHolder evaluationDataHolderInstance = null;

    public Timer selectionTransport = new Timer();
    public Timer selectionInspect = new Timer();
    public Timer findInspectDevice = new Timer();
    public Timer receiveProofRequest = new Timer();
    public Timer receiveProof = new Timer();
    public Timer approval = new Timer();
    public Timer sendRequest = new Timer();
    public Timer sendProofToTransport = new Timer();
    public Timer timeToCheckpoint = new Timer();
    public Timer sendInspectorProofToCL = new Timer();
    public Timer sendProofToCL = new Timer();
    public Timer sendPointToCL = new Timer();
    
    private HashMap<Long, Timer> inspectionsTimers;

    protected EvaluationDataHolder() {}

    public static EvaluationDataHolder getInstance() {
        if (evaluationDataHolderInstance == null) {
            evaluationDataHolderInstance = new EvaluationDataHolder();
        }
        return evaluationDataHolderInstance;
    }
    
    public void insertInspectionTimer(Long id, Timer timer) {
    	if (inspectionsTimers == null) {
    		inspectionsTimers = new HashMap<Long, Timer>();
    	}
    	inspectionsTimers.put(id, timer);
    }
    
    public Timer popInspectionTimer(Long id) {
    	if (inspectionsTimers == null) {
    		return null;
    	}
    	return inspectionsTimers.remove(id);
    }
}
