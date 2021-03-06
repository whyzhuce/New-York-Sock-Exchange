/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package exchange.g4.edmonds;

import java.util.ArrayList;

/**
 * @author raf
 */
public class GreenBlossom extends Blossom {

    ArrayList<Blossom> blossoms; // zoznam bublin vnutri bubliny, v tej prvej v poradi je vzdy stopka
    ArrayList<Edge> edgesBetweenBlossoms; // na i-tej pozicii je hrana medzi i a (i+1) % n

    public GreenBlossom(ArrayList<Blossom> blossoms, ArrayList<Edge> edgesBetweenBlossoms) {
        this.thickness = 0;
        this.blossoms = blossoms;

        this.edgesBetweenBlossoms = edgesBetweenBlossoms;

        if (blossoms.size() % 2 != 1) {
            System.err.println("NIEKTO CPE DO BLOSSOMU PARNU KRUZNICU MARHA");
        }

        if (blossoms.size() != edgesBetweenBlossoms.size()) {
            System.err.println(blossoms + " " + edgesBetweenBlossoms + " " + "NIEKTO SA SNAZI VYTVORIT BLOSSOM S PRIMALO HRANAMI!!!!!");
        }

        // updatujeme vnutornym vrcholom referenciu na blossom
        for (int i = 0; i < blossoms.size(); i++) {
            Blossom b = blossoms.get(i);
            if (b instanceof GreenBlossom) {
                ArrayList<Vertex> innerVertices = ((GreenBlossom) b).getInnerVertices();
                for (int j = 0; j < innerVertices.size(); j++) {
                    innerVertices.get(j).addToBlossom(this);
                }
            } else {
                ((BlueBlossom) b).vertex.addToBlossom(this);
            }
        }
    }

    @Override
    public String toString() {
        String ret = super.toString() + "[";
        ArrayList<Vertex> arr = this.getInnerVertices();
        for (Vertex v : arr) {
            ret += (v.id + 1) + " ";
        }
        ret += "]";

        ret += "{";
        for (Edge e : this.edgesBetweenBlossoms) {
            ret += e + ", ";
        }
        ret += "}";
        return ret;
    }

    // sluzi na updatovanie hrubky kvetu
    @Override
    public void zmena(double r) {
        if (this.levelParity == -1 && r > 0) {
            System.err.println("NESEDI PARITA -1");
        }

        if (this.levelParity == 1 && r < 0) {
            System.err.println("NESEDI PARITA 1");
        }
        thickness += r;
    }

    // vrati zoznam vrcholov vo vnutri bubliny
    public ArrayList<Vertex> getInnerVertices() {
        ArrayList<Vertex> ret = new ArrayList<Vertex>();
        getInnerVertices(ret);
        return ret;
    }

    private void getInnerVertices(ArrayList<Vertex> vertexList) {
        for (int i = 0; i < blossoms.size(); i++) {
            if (blossoms.get(i) instanceof BlueBlossom) {
                vertexList.add(((BlueBlossom) blossoms.get(i)).vertex);
            } else if (blossoms.get(i) instanceof GreenBlossom) {
                ((GreenBlossom) blossoms.get(i)).getInnerVertices(vertexList);
            } else {
                System.err.println("getInnerVertices: ani zeleny ani modry blossom, to je cudne");
            }
        }
    }

    @Override
    public Pair<Integer, ArrayList<Edge>> getMatchingPrice() {
        int ret = 0;
        ArrayList<Edge> edge_list = new ArrayList<Edge>();
        for (int i = 0; i < edgesBetweenBlossoms.size(); i++) {
            if (i % 2 == 1) {
//                System.out.println(edgesBetweenBlossoms.get(i));
                ret += edgesBetweenBlossoms.get(i).price;
                edge_list.add(edgesBetweenBlossoms.get(i));
            }
        }
        for (int i = 0; i < blossoms.size(); i++) {
            Pair<Integer, ArrayList<Edge>> R = blossoms.get(i).getMatchingPrice();
            ret += R.getLeft();
            edge_list.addAll(R.getRight());
        }
        return new Pair<Integer, ArrayList<Edge>>(ret, edge_list);
    }


    @Override
    public int getStopka() {
        return this.blossoms.get(0).getStopka();
    }

    // rekurzivne updatuje stopku bubliny tak, aby pasovala na danu hranu
    @Override
    public void setStopkaByEdge(Edge e) {
        setStopkaByEdge(e, 1);
    }

    // level je potrebny, aby sme sa pozerali na bublinu na spravnej urovni
    @Override
    public void setStopkaByEdge(Edge e, int level) {
        int newStopkaIndex = 0;
        for (newStopkaIndex = 0; true; newStopkaIndex++) { // musime tu stopku najst, inak nieco neni vporiadku
            if ((e.u.getNthOutermostBlossom(level) == this.blossoms.get(newStopkaIndex)) || (e.v.getNthOutermostBlossom(level) == this.blossoms.get(newStopkaIndex))) {
                break;
            }
        }
        // cyklicky posunieme nas arrayList blossomov a hran o newStopkaIndex policok
        // vytvorime si pomocne pole, vykoname cyklicky posun a skopirujeme namiesto povodnych
        ArrayList<Blossom> newBlossomsOrder = new ArrayList<Blossom>();
        ArrayList<Edge> newEdgesBetweenBlossomsOrder = new ArrayList<Edge>();

        for (int i = newStopkaIndex; i < blossoms.size() + newStopkaIndex; i++) {
            newBlossomsOrder.add(blossoms.get(i % blossoms.size()));
        }


        for (int i = 0; i < edgesBetweenBlossoms.size(); i++) {
            newEdgesBetweenBlossomsOrder.add(edgesBetweenBlossoms.get((i + newStopkaIndex) % edgesBetweenBlossoms.size()));
        }

        this.blossoms = newBlossomsOrder;
        this.edgesBetweenBlossoms = newEdgesBetweenBlossomsOrder;


        // rekurzivne sa vnorime, aby sme updatovali stopku
        this.blossoms.get(0).setStopkaByEdge(e, level + 1);
        for (int i = 0; i < blossoms.size(); i++) {
            if (i % 2 == 1) {
                this.blossoms.get(i).setStopkaByEdge(edgesBetweenBlossoms.get(i), level + 1);
                this.blossoms.get(i + 1).setStopkaByEdge(edgesBetweenBlossoms.get(i), level + 1);
            }
        }
    }

    public void pop() {
        ArrayList<Vertex> innerVertices = new ArrayList<Vertex>();
        this.getInnerVertices(innerVertices);

        for (int i = 0; i < innerVertices.size(); i++) {
            innerVertices.get(i).popOutermostBlossom();
        }
    }

}
