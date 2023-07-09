package com.light;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Scanner;


public class Main {
    private static final Scanner inStream = new Scanner(System.in);
    private static ArrayList<Edge> allEdges = new ArrayList<>();
    private static int D;
    private static int startEdgeId;

    public static void main(String[] args) {
        //Initialization parameters
        Scanner scanner = new Scanner(System.in);
        String line;
        line = inStream.nextLine();
        String[] s = line.split(" ");
        int N = Integer.parseInt(s[0]);
        int M = Integer.parseInt(s[1]);
        int T = Integer.parseInt(s[2]);
        int P = Integer.parseInt(s[3]);
        D = Integer.parseInt(s[4]);
        startEdgeId = M;
        HashMap<Integer, Node> nodes = new HashMap<>();
        Map<Integer, List<int[]>> graph = new HashMap<>();
        for (int i = 0; i < M; i++) {
            line = inStream.nextLine();
            String[] temp = line.split(" ");
            int nodeAIndex = Integer.parseInt(temp[0]);
            int nodeBIndex = Integer.parseInt(temp[1]);
            int distance = Integer.parseInt(temp[2]);

            Edge edge = new Edge(P);

            edge.setId(i);
            edge.setWeight(distance);
            edge.setFrom(nodeAIndex);
            edge.setTo(nodeBIndex);
            Node nodeA = nodes.getOrDefault(nodeAIndex, new Node(nodeAIndex));
            Node nodeB = nodes.getOrDefault(nodeBIndex, new Node(nodeBIndex));

            List<Edge> edgesA = nodeA.getEdges();
            edgesA.add(edge);
            List<Edge> edgesB = nodeB.getEdges();
            edgesB.add(edge);
            allEdges.add(edge);
            nodes.put(nodeAIndex, nodeA);
            nodes.put(nodeBIndex, nodeB);
            addEdge(graph, nodeAIndex, nodeBIndex, distance, i);
        }
        //My Task
        ArrayList<Task> tasks = new ArrayList<>();
        for (int i = 0; i < T; i++) {
            line = inStream.nextLine();
            String[] temp = line.split(" ");
            int start = Integer.parseInt(temp[0]);

            int end = Integer.parseInt(temp[1]);

            Task task = new Task(i, start, end);
            tasks.add(task);
        }


        scanner.close();

        int addEdgeNum = 0;
        ArrayList<ArrayList<Road>> res = new ArrayList<>();
        ArrayList<int[]> addEdge = new ArrayList<>();

        for (int i = 0; i < tasks.size(); i++) {

            Task task = tasks.get(i);
            Integer start = task.getStart();
            Integer end = task.getEnd();


            //    bfs+dijkstra

            ArrayList<Road> road = new ArrayList<>();


            ArrayList<Road> startList = new ArrayList<>();
            Road startTemp = new Road();
            startTemp.setNodeId(start);
            startTemp.setRest(D);
            startList.add(startTemp);
            ArrayList<ArrayList<Road>> all = new ArrayList<>();
            HashMap<Integer, Integer> map = new HashMap<>();


                startList.clear();
                Road startTemp2 = new Road();
                startTemp2.setNodeId(start);
                startTemp2.setRest(D);
                startList.add(startTemp2);
                // my parameters
                int dijTime=12;

                if (N + M > 2000) {
                    dijTime = 7;
                } else if (N + M > 4000) {
                    dijTime = 6 ;
                } else if (N + M > 8000) {
                    dijTime = 5;
                }
                Map<Integer, List<List<Integer>>> listMap = dijkstraThreeRes(graph, start, end,dijTime,P);
                List<List<Integer>> ansRoad = listMap.get(end);


                road = findMinCostByDijkstra(ansRoad, nodes, P, start, end, i);


                for (int j = 0; j < road.size(); j++) {
                    Road current = road.get(j);
                    if (current.isAddEdge() && current.getTid() == i) {
                        //new edge
                        current.setEdgeId(startEdgeId++);
                        addEdgeNum++;
                        Integer endNodeIndex = current.getNodeId();
                        Road startRoadNode = road.get(j - 1);
                        Integer startNodeIndex = startRoadNode.getNodeId();
                        int[] temp = {startNodeIndex, endNodeIndex};
                        addEdge.add(temp);

                        Edge edge = new Edge(P);
                        edge.setId(current.getEdgeId());
                        edge.setFrom(startNodeIndex);
                        edge.setTo(endNodeIndex);
                        Node startNode = nodes.get(startNodeIndex);
                        List<Edge> edgesStart = startNode.getEdges();

                        for (int k = 0; k < edgesStart.size(); k++) {
                            Edge tempEdge = edgesStart.get(k);
                            if (tempEdge.getFrom().equals(startNodeIndex) && tempEdge.getTo().equals(endNodeIndex)) {
                                if (edge.getWeight() != null) {
                                    edge.setWeight(Math.min(edge.getWeight(), tempEdge.getWeight()));

                                } else {
                                    edge.setWeight(tempEdge.getWeight());
                                }
                            }
                            if (tempEdge.getTo().equals(startNodeIndex) && tempEdge.getFrom().equals(endNodeIndex)) {
                                if (edge.getWeight() != null) {

                                    edge.setWeight(Math.min(edge.getWeight(), tempEdge.getWeight()));
                                } else {
                                    edge.setWeight(tempEdge.getWeight());

                                }
                            }
                        }
                        edge.setWeight(D);
                        startNode.getEdges().add(edge);
                        Node endNode = nodes.get(endNodeIndex);
                        endNode.getEdges().add(edge);
                        //add all edge
                        allEdges.add(edge);




                    }

                }
                Road lastRoad = road.get(road.size() - 1);

                ArrayList<Integer> canUserPortList = lastRoad.getCanUserPortList();
                //0 port
                Integer portIndex = canUserPortList.get(0);
                lastRoad.setPort(portIndex);

                for (int k = 0; k < road.size(); k++) {
                    Road tempRoad = road.get(k);
                    Integer edgeId = tempRoad.getEdgeId();
                    if (edgeId != null) {
                        Edge tempEdge = allEdges.get(edgeId);
                        tempEdge.setPortBusy(portIndex);
                    }

                }


            //Amplifier every road
            getAmplifier(road);

            res.add(road);

        }

        //ans
        System.out.println(addEdgeNum);
        for (int i = 0; i < addEdgeNum; i++) {
            int[] ints = addEdge.get(i);
            System.out.println(ints[0] + " " + ints[1]);
        }

        List<String> ans = getAnsStr(res);
        for (int i = 0; i < ans.size(); i++) {
            System.out.println(ans.get(i));
        }

    }

    private static ArrayList<Road> findMinCostByDijkstra(List<List<Integer>> ansRoad,
            HashMap<Integer, Node> nodes, int P, int start, int end, int tid) {

        ArrayList<Road> res = new ArrayList<>();
        int minEdge = 5000;
        int maxPort = 0;
        for (int i = 0; i < ansRoad.size(); i++) {


            List<Integer> current = ansRoad.get(i);
            int addNum = 0;
            int numPorts=1;
            ArrayList<Road> tempRoad = new ArrayList<>();
            Road startNode = new Road();
            startNode.setRest(D);
            startNode.setNodeId(start);
            ArrayList<Integer> canUsePortList = new ArrayList<>();
            startNode.setCanUserPortList(canUsePortList);
            tempRoad.add(startNode);
            for (int j = 1; j < current.size(); j++) {
                Integer currentNodeIndex = current.get(j);


                Road road = new Road();
                Integer preNodeIndex = current.get(j - 1);


                Node currentNode = nodes.get(currentNodeIndex);

                Node preNode = nodes.get(preNodeIndex);
                road.setNodeId(currentNodeIndex);

                List<Integer> edges = getCommonEdges(currentNode, preNode);

                canUsePortList = tempRoad.get(tempRoad.size() - 1).getCanUserPortList();

                ArrayList<Integer> edgeList = new ArrayList<>();
                Deque<Integer> queue = new ArrayDeque<Integer>();
                for (int k = 0; k < edges.size(); k++) {
                    Integer edgeId = edges.get(k);
                    Edge edge = allEdges.get(edgeId);
                    ArrayList<Integer> freeList = edge.isFree();
                    if (freeList.size() == 0) {

                        queue.addLast(edge.getId());
                    } else {

                        List<Integer> preList = new ArrayList<>(canUsePortList);
                        if (preList.size()==0){

                            queue.addFirst(edge.getId());
                            continue;
                        }
                        preList.retainAll(freeList);
                        if (preList.size() == 0) {
                            //add edge
                            queue.addLast(edge.getId());

                        } else {
                            queue.addFirst(edge.getId());
                        }

                    }


                }
                //ensure which edge
                Integer edgeId = queue.pollFirst();
                Edge edge = allEdges.get(edgeId);
                ArrayList<Integer> freeList = edge.isFree();
                if (canUsePortList.size()==0){
                    if (freeList.size()==0){
                        road.setAddEdge(true);
                        road.setTid(tid);
                        addNum++;
                        for (int k = 0; k < P; k++) {
                            canUsePortList.add(k);
                        }
                        road.setCanUserPortList(canUsePortList);

                    }else {
                        road.setCanUserPortList(freeList);
                    }

                }else {
                    if (freeList.size()==0){
                        road.setAddEdge(true);
                        road.setTid(tid);
                        addNum++;
                        road.setCanUserPortList(canUsePortList);
                    }else {
                        ArrayList<Integer> preList = new ArrayList<>(canUsePortList);
                        canUsePortList.retainAll(freeList);
                        if (canUsePortList.size()==0){
                            road.setAddEdge(true);
                            road.setTid(tid);
                            addNum++;
                            road.setCanUserPortList(preList);
                        }else {
                            road.setCanUserPortList(canUsePortList);
                        }
                    }
                }



                road.setRest(edge.getWeight());
                road.setEdgeId(edgeId);

                road.setRest(edge.getWeight());
                if (road.getCanUserPortList()!=null){
                    numPorts=road.getCanUserPortList().size();
                }

                tempRoad.add(road);

            }
            if (addNum < minEdge) {
                res = tempRoad;
                minEdge = addNum;
            }else if (addNum==minEdge){
                if (numPorts>maxPort){
                    res = tempRoad;
                    maxPort=numPorts;
                }
            }

        }
        return res;

    }

    private static List<Integer> getCommonEdges(Node currentNode, Node nextNodeEdges) {


        ArrayList<Integer> current = new ArrayList<>();
        ArrayList<Integer> next = new ArrayList<>();


        List<Edge> edges = currentNode.getEdges();
        for (int i = 0; i < edges.size(); i++) {
            current.add(edges.get(i).getId());
        }

        edges = nextNodeEdges.getEdges();
        for (int i = 0; i < edges.size(); i++) {
            next.add(edges.get(i).getId());
        }
        current.retainAll(next);
        return current;

    }



    private static void addEdge(Map<Integer, List<int[]>> graph, int start, int end, int weight, int edgeId) {
        List<int[]> edges = graph.getOrDefault(start, new ArrayList<>());
        edges.add(new int[] {end, weight, edgeId});
        graph.put(start, edges);


        List<int[]> edgesend = graph.getOrDefault(end, new ArrayList<>());
        edgesend.add(new int[] {start, weight, edgeId});
        graph.put(end, edgesend);
    }


    private static Map<Integer, List<List<Integer>>> dijkstraThreeRes(Map<Integer, List<int[]>> graph, int start,
            int end, int dijTime,int P) {
        Map<Integer, List<List<Integer>>> distance = new HashMap<>();
        Map<Integer, Integer> timeMap = new HashMap<>();

        PriorityQueue<DijkstraNode> heap = new PriorityQueue<>(new Comparator<DijkstraNode>() {
            @Override
            public int compare(DijkstraNode o1, DijkstraNode o2) {
                if (o1.dist < o2.dist) {
                    return -1;
                } else if (o1.dist > o2.dist) {
                    return 1;
                }
                return 0;
            }
        });
        DijkstraNode startNode = new DijkstraNode();
        startNode.setAddEdge(0);
        startNode.setNodeId(start);
        startNode.setCanUsePorts(null);
        ArrayList<Integer> startPreList = new ArrayList<>();
        startPreList.add(start);
        startNode.setPreList(startPreList);
        HashSet<Integer> startIsVisit = new HashSet<>();
        startIsVisit.add(start);
        startNode.setIsVisit(startIsVisit);

        heap.offer(startNode);

        while (!heap.isEmpty()) {
            DijkstraNode poll = heap.poll();
            Integer node = poll.nodeId, dist = poll.dist;
            if (timeMap.getOrDefault(node, 0) > dijTime) {
                continue;
            }


            if (distance.containsKey(node)) {
                List<List<Integer>> lists = distance.get(node);
                ArrayList<Integer> preList = poll.getPreList();
                boolean hasSame = false;
                for (int i = 0; i < lists.size(); i++) {
                    List<Integer> temp = lists.get(i);
                    boolean flag = true;

                    if (temp.size() == preList.size()) {
                        for (int j = 0; j < temp.size(); j++) {
                            if (!temp.get(j).equals(preList.get(j))) {
                                flag = false;
                                break;
                            }
                        }
                    } else {
                        continue;
                    }
                    if (flag) {
                        hasSame = true;
                        break;
                    }
                }
                if (hasSame) {
                    continue;
                }

            }


            timeMap.put(node, timeMap.getOrDefault(node, 0) + 1);
            List<List<Integer>> defaultList = distance.getOrDefault(node, new ArrayList<>());
            defaultList.add(poll.getPreList());
            distance.put(node, defaultList);

            if (node.equals(end) && timeMap.get(end) > dijTime) {
                return distance;
            }





            if (graph.containsKey(node)) {
                for (int[] edges : graph.get(node)) {

                    int edgeId = edges[2];
                    DijkstraNode nextNode = new DijkstraNode();
                    Edge edge = allEdges.get(edgeId);
                    Integer nextStart;
                    if (edge.getFrom().equals(node)) {
                        nextStart = edge.getTo();
                    } else {
                        nextStart = edge.getFrom();
                    }
                    HashSet<Integer> isVisit = poll.getIsVisit();
                    if (isVisit.contains(nextStart)) {
                        continue;
                    }

                    nextNode.setNodeId(nextStart);
                    nextNode.setDist(dist + edge.getWeight());

                    ArrayList<Integer> currentPreList = new ArrayList<>();
                    ArrayList<Integer> preList = poll.getPreList();
                    for (int i = 0; i < preList.size(); i++) {
                        currentPreList.add(preList.get(i));
                    }

                    HashSet<Integer> currentSet = new HashSet<>();

                    for (Integer preNode : isVisit) {
                        currentSet.add(preNode);
                    }
                    currentSet.add(nextStart);

                    currentPreList.add(nextStart);

                    nextNode.setPreList(currentPreList);
                    nextNode.setIsVisit(currentSet);



                    heap.offer(nextNode);
                }
            }
        }
        return distance;
    }


    private static List<String> getAnsStr(ArrayList<ArrayList<Road>> res) {
        ArrayList<String> list = new ArrayList<>();

        for (int i = 0; i < res.size(); i++) {
            StringBuilder stringBuilder = new StringBuilder();
            ArrayList<Road> roads = res.get(i);

            if (roads.size() == 0) {

                continue;
            }
            // Integer port = roads.get(roads.size() - 1).getCanUserPortList().get(0);
            Integer port = roads.get(roads.size() - 1).getPort();
            stringBuilder.append(port);
            stringBuilder.append(" ");

            int tempEdge = roads.size() - 1;

            stringBuilder.append(tempEdge);
            stringBuilder.append(" ");
            int amplNum = 0;
            ArrayList<Integer> amplList = new ArrayList<>();
            ArrayList<Integer> edgeList = new ArrayList<>();
            for (int j = 0; j < roads.size(); j++) {
                Road road = roads.get(j);
                edgeList.add(road.getEdgeId());
                if (road.isAmplifier()) {
                    amplNum++;
                    amplList.add(road.getNodeId());
                }
            }

            stringBuilder.append(amplNum);
            stringBuilder.append(" ");
            for (int j = 0; j < edgeList.size(); j++) {
                if (edgeList.get(j) != null) {
                    stringBuilder.append(edgeList.get(j));
                    stringBuilder.append(" ");
                }

            }
            for (int j = 0; j < amplList.size(); j++) {
                stringBuilder.append(amplList.get(j));
                stringBuilder.append(" ");
            }
            String trim = stringBuilder.toString().trim();
            list.add(trim);

        }
        return list;
    }

    private static void getAmplifier(ArrayList<Road> road) {

        for (int i = 1; i < road.size(); i++) {

            Road preRoad = road.get(i - 1);
            Integer pre = preRoad.getRest();
            Road current = road.get(i);
            Integer edgeId = current.getEdgeId();
            if (edgeId == null) {
                continue;
            }

            Edge edge = allEdges.get(edgeId);
            if (pre - edge.getWeight() < 0) {

                preRoad.setAmplifier(true);
                preRoad.setRest(D);
                current.setRest(D - edge.getWeight());
            } else {
                current.setRest(pre - edge.getWeight());
            }

        }

    }


}

class DijkstraNode {

    Integer nodeId;

    ArrayList<Integer> canUsePorts;

    int addEdge;

    int dist;

    ArrayList<Integer> preList;
    HashSet<Integer> isVisit;
    StringBuilder path;

    public DijkstraNode() {
    }

    public HashSet<Integer> getIsVisit() {
        return isVisit;
    }

    public void setIsVisit(HashSet<Integer> isVisit) {
        this.isVisit = isVisit;
    }

    public StringBuilder getPath() {
        return path;
    }

    public void setPath(StringBuilder path) {
        this.path = path;
    }

    public ArrayList<Integer> getPreList() {
        return preList;
    }

    public void setPreList(ArrayList<Integer> preList) {
        this.preList = preList;
    }

    public int getDist() {
        return dist;
    }

    public void setDist(int dist) {
        this.dist = dist;
    }

    public Integer getNodeId() {
        return nodeId;
    }

    public void setNodeId(Integer nodeId) {
        this.nodeId = nodeId;
    }

    public ArrayList<Integer> getCanUsePorts() {
        return canUsePorts;
    }

    public void setCanUsePorts(ArrayList<Integer> canUsePorts) {
        this.canUsePorts = canUsePorts;
    }

    public int getAddEdge() {
        return addEdge;
    }

    public void setAddEdge(int addEdge) {
        this.addEdge = addEdge;
    }
}

class Road {

    int roadIndex;
    Integer nodeId;
    Integer edgeId;
    boolean addEdge;
    boolean amplifier;
    Integer tid;
    //>=0 is can use
    Integer rest;
    Integer port;


    ArrayList<Integer> canUserPortList;


    public Integer getRoadIndex() {
        return roadIndex;
    }

    public void setRoadIndex(Integer roadIndex) {
        this.roadIndex = roadIndex;
    }

    public Road() {
        canUserPortList = new ArrayList<>();
    }

    public Road(Integer nodeId, Integer edgeId) {
        this.nodeId = nodeId;
        this.edgeId = edgeId;
        canUserPortList = new ArrayList<>();
    }

    public ArrayList<Integer> getCanUserPortList() {
        return canUserPortList;
    }

    public void setCanUserPortList(ArrayList<Integer> canUserPortList) {
        this.canUserPortList = canUserPortList;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public Integer getRest() {
        return rest;
    }

    public void setRest(int rest) {
        this.rest = rest;
    }

    public Integer getTid() {
        return tid;
    }

    public void setTid(Integer tid) {
        this.tid = tid;
    }

    public boolean isAddEdge() {
        return addEdge;
    }

    public void setAddEdge(boolean addEdge) {
        this.addEdge = addEdge;
    }

    public boolean isAmplifier() {
        return amplifier;
    }

    public void setAmplifier(boolean amplifier) {
        this.amplifier = amplifier;
    }

    public Integer getNodeId() {
        return nodeId;
    }

    public void setNodeId(Integer nodeId) {
        this.nodeId = nodeId;
    }

    public Integer getEdgeId() {
        return edgeId;
    }

    public void setEdgeId(Integer edgeId) {
        this.edgeId = edgeId;
    }

    @Override
    public String toString() {
        return "->" + nodeId;
    }


}


class Node {
    Integer id;
    List<Edge> edges;

    public Node(Integer id) {
        this.id = id;
        edges = new ArrayList<>();
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public List<Edge> getEdges() {
        return edges;
    }

    public void setEdges(List<Edge> edges) {
        this.edges = edges;
    }
}

class Edge {

    Integer id;
    List<Port> ports;
    Integer weight;
    Integer from;
    Integer to;

    public Edge(int p) {
        ports = new ArrayList<>();
        for (int i = 0; i < p; i++) {
            Port port = new Port(i);
            ports.add(port);
        }
    }

    public Integer getFrom() {
        return from;
    }

    public void setFrom(Integer from) {
        this.from = from;
    }

    public Integer getTo() {
        return to;
    }

    public void setTo(Integer to) {
        this.to = to;
    }

    public Integer getWeight() {
        return weight;
    }

    public void setWeight(Integer weight) {
        this.weight = weight;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public List<Port> getPorts() {
        return ports;
    }

    public void setPorts(List<Port> ports) {
        this.ports = ports;
    }

    public boolean portsFree() {

        for (int i = 0; i < ports.size(); i++) {
            if (ports.get(i).getIsUsed() == -1) {
                return true;
            }
        }
        return false;

    }

    public int setPortBusy() {
        for (int i = 0; i < ports.size(); i++) {
            if (ports.get(i).getIsUsed() == -1) {
                // return true;
                ports.get(i).setIsUsed(1);
                return i;
            }
        }
        return ports.size() - 1;

    }

    public boolean portsFree(Integer prePort) {
        if (prePort == null || prePort >= ports.size()) {
            return false;
        }

        Port port = ports.get(prePort);
        if (port.getIsUsed() == -1) {
            return true;
        } else {
            return false;
        }
    }

    public void setPortBusy(Integer prePort) {

        Port port = ports.get(prePort);
        port.setIsUsed(1);
        port.setTemporaryOccupation(port.getTemporaryOccupation() + 1);

    }



    public ArrayList<Integer> isFree() {
        ArrayList<Integer> freeList = new ArrayList<>();
        for (int i = 0; i < ports.size(); i++) {
            if (ports.get(i).getIsUsed() == -1) {
                freeList.add(i);
            }
        }
        return freeList;

    }
}

class Port {
    Integer id;
    // -1 1
    int isUsed;

    int temporaryOccupation;

    public int getIsUsed() {
        return isUsed;
    }

    public void setIsUsed(int isUsed) {
        this.isUsed = isUsed;
    }

    public Port(Integer id) {
        this.id = id;
        isUsed = -1;
    }

    public Integer getTemporaryOccupation() {
        return temporaryOccupation;
    }

    public void setTemporaryOccupation(Integer temporaryOccupation) {
        this.temporaryOccupation = temporaryOccupation;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }
}

class Task {

    Integer id;

    Integer start;
    Integer end;

    public Task() {
    }

    public Task(Integer id, Integer start, Integer end) {
        this.id = id;
        this.start = start;
        this.end = end;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getStart() {
        return start;
    }

    public void setStart(Integer start) {
        this.start = start;
    }

    public Integer getEnd() {
        return end;
    }

    public void setEnd(Integer end) {
        this.end = end;
    }
}

