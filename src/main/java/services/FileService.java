package services;

import model.graph.Graph;
import model.agents.Agent;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class FileService {
    public static final String SAVE_DIR = "simulations/";

    public static void ensureSaveDirectoryExists() {
        File directory = new File(SAVE_DIR);
        if (!directory.exists()) {
            directory.mkdir(); // Cree le dossier s'il n'existe pas
        }
    }

    public static void saveSimulation(String filename, Graph graph, List<Agent> agents) throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filename))) {
            oos.writeObject(graph);
            oos.writeObject(agents);
        }
    }

    public static void quickSave(Graph graph, List<Agent> agents) throws IOException {
        ensureSaveDirectoryExists();
        saveSimulation(SAVE_DIR + "autosave.sim", graph, agents);
    }

    @SuppressWarnings("unchecked")
    public static SimulationData loadSimulation(String filename) throws IOException, ClassNotFoundException {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filename))) {
            Graph graph = (Graph) ois.readObject();
            List<Agent> agents = (List<Agent>) ois.readObject();
            return new SimulationData(graph, agents);
        }
    }

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