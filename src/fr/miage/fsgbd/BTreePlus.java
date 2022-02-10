package fr.miage.fsgbd;

import javax.swing.tree.DefaultMutableTreeNode;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.stream.Stream;


/**
 * @author Galli Gregory, Mopolo Moke Gabriel
 * @param <Type>
 */
public class BTreePlus<Type> implements java.io.Serializable {
    private Noeud<Type> racine;
    private Noeud<Type> precedent = null;
    private boolean refresh = false;

    public BTreePlus(int u, Executable e) {
        racine = new Noeud<Type>(u, e, null);
    }

    public void afficheArbre() {
        racine.afficheNoeud(true, 0);
    }

    /**
     * Méthode récursive permettant de récupérer tous les noeuds
     *
     * @return DefaultMutableTreeNode
     */
    public DefaultMutableTreeNode bArbreToJTree() {
        if(precedent!= null)
            refresh = true;
        return bArbreToJTree(racine);
    }

    private DefaultMutableTreeNode bArbreToJTree(Noeud<Type> root) {
        StringBuilder txt = new StringBuilder();
        if(root.fils.size()==0) {
            if (precedent != null && !refresh)
                precedent.next = root;
            precedent = root;
        }
        for (Type key : root.keys)
            txt.append(key.toString()).append(" ");

        DefaultMutableTreeNode racine2 = new DefaultMutableTreeNode(txt.toString(), true);
        for (Noeud<Type> fil : root.fils)
            racine2.add(bArbreToJTree(fil));

        return racine2;
    }


    public boolean addValeur(Type valeur) {
        refresh = false;
        this.precedent =null;
        System.out.println("Ajout de la valeur : " + valeur.toString());
        if (racine.contient(valeur) == null) {
            Noeud<Type> newRacine = racine.addValeur(valeur);
            if (racine != newRacine)
                racine = newRacine;
            return true;
        }
        return false;
    }

    public boolean addValeur(Type valeur,int ligne) {
        refresh = false;
        this.precedent =null;
        System.out.println("Ajout de la valeur : " + valeur.toString());
        if (racine.contient(valeur) == null) {
            Noeud<Type> newRacine = racine.addValeur(valeur,ligne);
            if (racine != newRacine)
                racine = newRacine;
            return true;
        }
        return false;
    }


    public void removeValeur(Type valeur) {
        refresh = false;
        this.precedent =null;
        System.out.println("Retrait de la valeur : " + valeur.toString());
        if (racine.contient(valeur) != null) {
            Noeud<Type> newRacine = racine.removeValeur(valeur, false);
            if (racine != newRacine)
                racine = newRacine;
        }
    }

    public void searchLine() throws IOException {
        Long timeSeq = (long)0, timeInd= (long)0, seqMin= (long)9999, seqMax= (long)0, indMin= (long)9999, indMax = (long)0;
        ArrayList<Integer> values = new ArrayList<>();
        String id;
        try(BufferedReader br = new BufferedReader(new FileReader("data.txt"))) {
            int ligne = 0;
            for(String line; (line = br.readLine()) != null; ) {
                ligne++;
                if (ligne%100 ==0) {
                    id = line.substring(0, line.indexOf(","));
                    int val = Integer.parseInt(id);
                    values.add(val);
                }
            }
        } catch (IOException ioException) {
            System.out.println("Veuillez charger les données du fichier");
        }
        System.out.println("Recherche des lignes sequentiellement: ");

        //sequences
        for( Integer value : values ) {
            Long start = System.nanoTime();
            try(BufferedReader br = new BufferedReader(new FileReader("data.txt"))) {
                for(String line; (line = br.readLine()) != null; ) {
                    id = line.substring( 0, line.indexOf(","));
                    if(Integer.parseInt(id) == (int)value)
                    {
                        Long end = System.nanoTime();
                        Long total = (end-start)/1000;
                        System.out.println(line +" "+total+" microseconds");
                        timeSeq = timeSeq+total;
                        if (total > seqMax)
                            seqMax = total;
                        else if (total < seqMin)
                            seqMin = total;
                        break;
                    }
                }
                // line is not visible here.
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
        System.out.println("Temps total sequentiel : "+timeSeq+ " microseconds");
        String line;

        //indexes
        System.out.println("Recherche des lignes de maniere indexee: ");
        for( Integer value : values ) {
            Long start = System.nanoTime();
            try (Stream<String> lines = Files.lines(Paths.get("data.txt"))) {
                int test = Noeud.pointer.get(value);
                line = lines.skip(test).findFirst().get();
            }
            Long end = System.nanoTime();
            Long total = (end-start)/1000;
            System.out.println(line +" "+total+" microseconds");
            timeInd = timeInd+total;
            if (total > indMax) {
                indMax = total;
            }
            else if (total < indMin) {
                indMin = total;
            }
        }

        System.out.println("\nTemps total sequentiel = "+ timeSeq +" microseconds, en moyenne une recherche prend "+ timeSeq/100 +" microseconds la plus courte est de "+ seqMin +" microseconds et la plus longue de "+ seqMax +" microseconds");
        System.out.println("Temps total index = "+ timeInd +" microseconds, en moyenne une recherche prend "+ timeInd/100 +" microseconds la plus courte est de "+ indMin +" microseconds et la plus longue de "+ indMax +" microseconds");
    }
}
