package services;

import model.graph.Graph;
import model.agents.Agent;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class FileService {
    public static final String SAVE_DIR = "simulations/";

    /**
     * Verifies the existence of the target save directory and creates it if missing.
     */
    public static void ensureSaveDirectoryExists() {
        File directory = new File(SAVE_DIR);
        if (!directory.exists()) {
            directory.mkdir();
        }
    }

    /**
     * Serializes and writes the current graph architecture and active agents state to a binary file.
     * @param filename The target file path specification.
     * @param graph The structural graph framework model instance.
     * @param agents The list of active simulation tracking agents.
     * @throws IOException If a file streaming or writing error occurs.
     */
    public static void saveSimulation(String filename, Graph graph, List<Agent> agents) throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filename))) {
            oos.writeObject(graph);
            oos.writeObject(agents);
        }
    }

    /**
     * Triggers an automatic fast-save pipeline using predefined workspace directory paths.
     * @param graph The structural graph framework model instance.
     * @param agents The list of active simulation tracking agents.
     * @throws IOException If a file streaming or writing error occurs.
     */
    public static void quickSave(Graph graph, List<Agent> agents) throws IOException {
        ensureSaveDirectoryExists();
        saveSimulation(SAVE_DIR + "autosave.sim", graph, agents);
    }

    /**
     * Reads and reconstructs a previously serialized simulation dataset from a file.
     * @param filename The source file path containing simulation object data records.
     * @return A consolidated data record wrapper containing the graph layout and agents list.
     * @throws IOException If a streaming reading operation fails.
     * @throws ClassNotFoundException If class bytecode translation definitions are missing.
     */
    @SuppressWarnings("unchecked")
    public static SimulationData loadSimulation(String filename) throws IOException, ClassNotFoundException {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filename))) {
            Graph graph = (Graph) ois.readObject();
            List<Agent> agents = (List<Agent>) ois.readObject();
            return new SimulationData(graph, agents);
        }
    }

    /**
     * Scans the system storage directories to extract a filtered manifest of tracking save states.
     * @return A list containing the filenames of matching serialized simulation profiles.
     */
    public static List<String> getSavedFiles() {
        ensureSaveDirectoryExists();
        File folder = new File(SAVE_DIR);
        File[] files = folder.listFiles((dir, name) -> name.endsWith(".sim"));

        List<String> fileNames = new ArrayList<>();
        if (files != null) {
            for (File f : files) {
                fileNames.add(f.getName());
            }
        }
        return fileNames;
    }

    public record SimulationData(Graph graph, List<Agent> agents) {
    }
}