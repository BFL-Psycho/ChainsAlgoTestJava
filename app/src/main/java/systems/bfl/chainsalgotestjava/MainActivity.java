package systems.bfl.chainsalgotestjava;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.jgrapht.Graph;
import org.jgrapht.alg.shortestpath.BFSShortestPath;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ThreadLocalRandom;

public class MainActivity extends AppCompatActivity {

    //UI Elements
    private Button loadDictionary, generateGraph, findWordContains, runDijkstras, runBFS, runPermutation, runBackLevenstein, runCompulsionFinding;
    private EditText graphWordLength, wordFindWord, startWord, endWord, chainLength;

    // Console
    private TextView consoleOutput, newStartWordOutput;

    // Dictionary
    private BufferedReader bufferedReader = null;
    private Set<String> dictionarySet;
    private ArrayList<String> dictionaryArray;

    // Graph
    private Graph<String, DefaultEdge> currentGraph;
    private DijkstraShortestPath dijkstraShortestPath;
    private BFSShortestPath bfsShortestPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initiateUIelements();
        setButtonActions();

    }


    private void initiateUIelements() {
        loadDictionary = (Button) findViewById(R.id.btn_loadDictionary);
        generateGraph = (Button) findViewById(R.id.btn_generateGraph);
        findWordContains = (Button) findViewById(R.id.btn_testWordContains);
        runDijkstras = (Button) findViewById(R.id.btn_testDijkstra);
        runBFS = (Button) findViewById(R.id.btn_testBFS);
        runPermutation = (Button) findViewById(R.id.btn_testPermutation);
        runBackLevenstein = (Button) findViewById(R.id.btn_runBackLevenstein);
        runCompulsionFinding = (Button) findViewById(R.id.btn_runCompulsion);


        graphWordLength = (EditText) findViewById(R.id.edittxt_Letters);
        wordFindWord = (EditText) findViewById(R.id.edittxt_Word);
        startWord = (EditText) findViewById(R.id.edittxt_startWord);
        endWord = (EditText) findViewById(R.id.edittxt_endWord);
        chainLength = (EditText) findViewById(R.id.editChainLength);

        consoleOutput = (TextView) findViewById(R.id.consoleOutput);
        newStartWordOutput = (TextView) findViewById(R.id.text_newStartWord);
    }

    private void setButtonActions() {
        loadDictionary.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadDictionary();
            }
        });

        generateGraph.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                consoleOutput.setText("Wait...");
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        generateGraph(Integer.valueOf(graphWordLength.getText().toString()));
                    }
                }, 500);
            }
        });

        findWordContains.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                findWordContains(wordFindWord.getText().toString());
            }
        });

        runDijkstras.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                consoleOutput.setText("Wait...");
                runDijkstras(startWord.getText().toString(), endWord.getText().toString());
            }
        });

        runBFS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                consoleOutput.setText("Wait...");
                runBFS(startWord.getText().toString(), endWord.getText().toString());
            }
        });

        runPermutation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                runPermutation(startWord.getText().toString(), endWord.getText().toString());
            }
        });

        runBackLevenstein.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

               String newWord = backLevenstein(endWord.getText().toString(), Integer.valueOf(chainLength.getText().toString()));
               newStartWordOutput.setText(newWord);
            }
        });

        runCompulsionFinding.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<String> result = randomLadders(endWord.getText().toString(), Integer.valueOf(chainLength.getText().toString()));
            }
        });

    }

    private void loadDictionary() {
        try {
            bufferedReader = new BufferedReader(new InputStreamReader(getAssets().open("words_dirty.txt")));
            dictionarySet = new HashSet<>();
            String line = bufferedReader.readLine();
            consoleOutput.setText("Loading dictionary...");
            long startTime = System.currentTimeMillis();
            while (line != null) {
                dictionarySet.add(line);
                line = bufferedReader.readLine();
            }
            bufferedReader.close();
            long endTime = System.currentTimeMillis();
            consoleOutput.setText("Dictionary loaded in: "+(endTime-startTime)+" ms.");

        } catch (Exception e) {
            consoleOutput.setText(e.getMessage());
        }

        dictionaryArray = new ArrayList<>(dictionarySet);
    }

    private void generateGraph(int graphLength) {
        if (dictionarySet==null) {consoleOutput.setText("Load dictionary first!"); return;}
        try {
            currentGraph = new SimpleGraph<>(DefaultEdge.class);

        if (graphLength==0) {
            consoleOutput.setText("Set word length for a graph!");
            return;
        }

        Set<String> shortDictionary = new HashSet<>();
        consoleOutput.setText("Loading words with length: "+graphLength);

        for (String word : dictionarySet) {
            if (word.length()==graphLength) {
                shortDictionary.add(word);
            }
        }
        consoleOutput.setText("Loaded "+shortDictionary.size()+" words with length: "+graphLength);

        long startTime = System.currentTimeMillis();
        // starting to generate a graph
        for (String word : shortDictionary) {
            currentGraph.addVertex(word);
        }
        consoleOutput.setText("Added vertices: "+currentGraph.vertexSet().size());
        for (String vertex : currentGraph.vertexSet()) {
            Set<String> edgeCandidates = getNextNodes(vertex, currentGraph.vertexSet());
            for (String candidate : edgeCandidates) {
                if (!currentGraph.containsEdge(vertex, candidate)) {
                    currentGraph.addEdge(vertex, candidate);
                }
            }
        }
        long endTime = System.currentTimeMillis();
        consoleOutput.setText("Graph generated within: "+(endTime-startTime)+" ms. Contains vertices: "+currentGraph.vertexSet().size());

        } catch (Exception e) {
            consoleOutput.setText(e.getMessage());
        }
    }

    private boolean isOneCharOff(String str1, String str2) {
        if (str1.length() != str2.length())
            return false;
        int same = 0;
        for (int i = 0; i < str1.length(); ++i) {
            if (str1.charAt(i) == str2.charAt(i))
                same++;
        }
        return same == str1.length() - 1;
    }

    private Set<String> getNextNodes(String node, Set<String> dictionary) {
        Set<String> nextNodes = new HashSet<>();
        for (String word : dictionary) {
            if (isOneCharOff(node, word)) {
                nextNodes.add(word);
            }
        }
        return nextNodes;
    }

    private void findWordContains(String word) {
        try {

            if (word != null) {
                long startTime = System.currentTimeMillis();
                boolean exists = dictionarySet.contains(word);
                long endTime = System.currentTimeMillis();
                if (exists) {
                    consoleOutput.setText("Word " + word + " exists! Found within: " + (endTime - startTime) + " ms.");
                } else {
                    consoleOutput.setText("Word " + word + " does not exists! Found within: " + (endTime - startTime) + " ms.");
                }
            }
        } catch (Exception e) {
            consoleOutput.setText(e.getMessage());
        }
    }

    private void runDijkstras(String startWord, String endWord) {
        try {
            if (currentGraph != null) {
                dijkstraShortestPath = new DijkstraShortestPath(currentGraph);
                long startTime = System.currentTimeMillis();
                String path = dijkstraShortestPath.getPath(startWord, endWord).getVertexList().toString();
                long endTime = System.currentTimeMillis();
                consoleOutput.setText(path + ". Dijkstras Found within: " + (endTime - startTime) + " ms.");
            }
        } catch (Exception e) {
            consoleOutput.setText(e.getMessage());
        }
    }

    private void runBFS(String startWord, String endWord) {
        try {
            if (currentGraph != null) {
                bfsShortestPath = new BFSShortestPath(currentGraph);
                long startTime = System.currentTimeMillis();
                String path = bfsShortestPath.getPath(startWord, endWord).getVertexList().toString();
                long endTime = System.currentTimeMillis();
                consoleOutput.setText(path + ". BFS Found within: " + (endTime - startTime) + " ms.");
            }
        } catch (Exception e) {
            consoleOutput.setText(e.getMessage());
        }
    }

    private void runPermutation(String startWord, String endWord) {
        try {

            long startTime = System.currentTimeMillis();
            List<List<String>> paths = findLadders(startWord, endWord);
            long endTime = System.currentTimeMillis();
            consoleOutput.setText(Arrays.toString(paths.toArray()) + ". Permutation Found within: " + (endTime - startTime) + " ms.");
        } catch (Exception e) {
            consoleOutput.setText(e.getMessage());
        }
    }

    private List<List<String>> findLadders(String beginWord, String endWord) {
        List<List<String>> result = new ArrayList<List<String>>();

        HashSet<String> unvisited = new HashSet<>();
        unvisited.addAll(dictionarySet);

        LinkedList<Node> queue = new LinkedList<>();
        Node node = new Node(beginWord,0,null);
        queue.offer(node);

        int minLen = Integer.MAX_VALUE;
        while(!queue.isEmpty()){
            Node top = queue.poll();

            //top if have shorter result already
            if(result.size()>0 && top.depth>minLen){
                return result;
            }

            for(int i=0; i<top.word.length(); i++){
                char c = top.word.charAt(i);
                char[] arr = top.word.toCharArray();
                for(char j='z'; j>='a'; j--){
                    if(j==c){
                        continue;
                    }
                    arr[i]=j;
                    String t = new String(arr);

                    if(t.equals(endWord)){
                        //add to result
                        List<String> aResult = new ArrayList<>();
                        aResult.add(endWord);
                        Node p = top;
                        while(p!=null){
                            aResult.add(p.word);
                            p = p.prev;
                        }

                        Collections.reverse(aResult);
                        result.add(aResult);

                        //stop if get shorter result
                        if(top.depth<=minLen){
                            minLen=top.depth;
                        }else{
                            return result;
                        }
                    }

                    if(unvisited.contains(t)){
                        Node n=new Node(t,top.depth+1,top);
                        queue.offer(n);
                        unvisited.remove(t);
                    }
                }
            }
        }
        return result;
    }

   /* private ArrayList<String> compulsionFindLadders(String beginWord, String endWord, int chainLength) {

        long startTime = System.currentTimeMillis();
        ArrayList<String> result = new ArrayList<>();
        boolean isFounded = false;

        while (!isFounded) {
            List<List<String>> ladders = findLadders(beginWord, endWord);

            for (int i = 0; i < ladders.size(); i++) {
                if (ladders.get(i).size() == chainLength) {
                    result = new ArrayList<>(ladders.get(i));
                    isFounded = true;
                    break;

                }
            }

        }

        long endTime = System.currentTimeMillis();

        consoleOutput.setText("Compulsion finding took: " + (endTime - startTime) + " ms");

        return result;
    }*/

   private ArrayList<String> randomLadders(String endWord, int chainLength) {

       long startTime = System.currentTimeMillis();

        ArrayList<String> result = new ArrayList<>();
        boolean isFound = false;

        while (!isFound) {

            //Random rn = new Random(); rn.nextInt();
            int index = (int)(Math.random() * (dictionarySet.size() + 1));
            System.out.println("Index is " + index);
            String randomWord = dictionaryArray.get(index);
            System.out.println("Word is " + randomWord);

            if (randomWord.length() == endWord.length()) {

                List<List<String>> ladder = findLadders(randomWord, endWord);

                //Looping makes 0 sense
                for (int i = 0; i < ladder.size(); i++) {
                    if (ladder.get(i).size() == chainLength) {
                        result = new ArrayList<>(ladder.get(i));
                        isFound = true;
                        break;
                    }
                }
            }

        }

        long endTime = System.currentTimeMillis();

        consoleOutput.setText("Random ladder finding took: " + (endTime - startTime) + " ms");

        return result;

   }

    //return whole path
    public String backLevenstein(String endWord, int chainLenght) {

        long startTime = System.currentTimeMillis();

        String startWord = "";

        HashSet<String> unvisited = new HashSet<>();
        unvisited.addAll(dictionarySet);

        int counter = 0;

        String currentWord = endWord;

        while (counter <= chainLenght) {

            int index = ThreadLocalRandom.current().nextInt(0, endWord.length());

            char c = currentWord.charAt(index);
            char[] array = currentWord.toCharArray();

            for (char j ='z'; j >= 'a'; j--) {
                if (j==c) {
                    continue;
                }

                array[index] = j;
                String newWord = new String(array);

                if (unvisited.contains(newWord)) {
                    counter ++;
                    unvisited.remove(newWord);
                    currentWord = newWord;
                    break;
                }
            }
        }

        startWord = currentWord;
        long endTime = System.currentTimeMillis();
        consoleOutput.setText("Finding start word lasted " + (endTime-startTime) +"ms");

        return startWord;
    }

}

class Node{
    public String word;
    public int depth;
    public Node prev;

    public Node(String word, int depth, Node prev){
        this.word=word;
        this.depth=depth;
        this.prev=prev;
    }
}

