package Controleur;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.swing.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import Model.Liaison;
import Model.Noeud;
import Vue.FenetrePrincipal;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

/**
 * Permet de sauvegarde et de charger des fichiers XML
 */
public class SaveAndLoad {

    public static void ModelToXml(ArrayList<Noeud> noeuds, String pathname) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();

            Document xml = builder.newDocument();
            Element mindMap = xml.createElement("MindMap");
            xml.appendChild(mindMap);

            Element noeudsXml = xml.createElement("Noeuds");
            mindMap.appendChild(noeudsXml);

            Element liaisonsXml = xml.createElement("Liaisons");
            mindMap.appendChild(liaisonsXml);
            //############# DEBUT DES AJOUT D'ELEMENTS################################

            for (Noeud n : noeuds) {

                Element titre = xml.createElement("titre");
                titre.appendChild(xml.createTextNode(n.getTitre()));

                Element description = xml.createElement("Description");
                description.appendChild(xml.createTextNode(n.getDescription()));

                Element x = xml.createElement("X");
                x.appendChild(xml.createTextNode("" + n.getX()));

                Element y = xml.createElement("Y");
                y.appendChild(xml.createTextNode("" + n.getY()));

                Element noeud = xml.createElement("Noeud");


                Attr id = xml.createAttribute("id");
                id.setValue("" + n.getId());
                noeud.setAttributeNode(id);

                noeud.appendChild(titre);
                noeud.appendChild(description);
                noeud.appendChild(x);
                noeud.appendChild(y);

                noeudsXml.appendChild(noeud);

                for (Liaison l : n.getLiaisonFils()) {

                    Element noeudPere = xml.createElement("Pere");
                    noeudPere.appendChild(xml.createTextNode("" + l.getNoeudPere().getId()));

                    Element noeudFils = xml.createElement("Fils");
                    noeudFils.appendChild(xml.createTextNode("" + l.getNoeudFils().getId()));

                    Element liaison = xml.createElement("Liaison");
                    noeudsXml.appendChild(noeud);

                    liaison.appendChild(noeudPere);
                    liaison.appendChild(noeudFils);

                    liaisonsXml.appendChild(liaison);
                }
            }
            //############# FIN DES AJOUT D'ELEMENTS################################
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(xml);
            pathname = pathname.replace(".xml", "");
            StreamResult resultat = new StreamResult(new File(pathname + ".xml"));
            transformer.transform(source, resultat);
            System.out.println("Fichier sauvegardé avec succès!");

        } catch (ParserConfigurationException | TransformerException pce) {
            pce.printStackTrace();
        }
    }

    public static ArrayList<Noeud> XmlToModel(String pathname) {

        HashMap<Integer, Noeud> noeudsMap = new HashMap<>();
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

        try {
            pathname = pathname.replace(".xml", "");
            DocumentBuilder builder = factory.newDocumentBuilder();
            File fileXML = new File(pathname+".xml");

            Document xml = builder.parse(fileXML);

            Element MindMap = xml.getDocumentElement();

            Node noeudsXml = MindMap.getFirstChild();
            NodeList listNoeuds = noeudsXml.getChildNodes();

            for (int i = 0; i < listNoeuds.getLength(); i++) {
                Node noeudXml = listNoeuds.item(i);

                Node titre = noeudXml.getFirstChild();
                Node description = titre.getNextSibling();
                Node x = description.getNextSibling();
                Node y = x.getNextSibling();
                NamedNodeMap attr = noeudXml.getAttributes();
                Noeud noeud = new Noeud(
                        Integer.parseInt(attr.getNamedItem("id").getTextContent()),
                        titre.getTextContent(),
                        description.getTextContent(),
                        Double.parseDouble(x.getTextContent()),
                        Double.parseDouble(y.getTextContent())
                );

                noeudsMap.put(noeud.getId(), noeud);
            }

            Node liaisonsXml = MindMap.getLastChild();
            NodeList listLiaisons = liaisonsXml.getChildNodes();

            for (int i = 0; i < listLiaisons.getLength(); i++) {
                Node liaisonXml = listLiaisons.item(i);

                Node noeudIdPere = liaisonXml.getFirstChild();
                Node noeudIdFils = noeudIdPere.getNextSibling();

                Liaison liaison = new Liaison(
                        noeudsMap.get(Integer.parseInt(noeudIdPere.getTextContent())),
                        noeudsMap.get(Integer.parseInt(noeudIdFils.getTextContent()))
                );
                noeudsMap.get(Integer.parseInt(noeudIdPere.getTextContent())).addLiaisonFils(liaison);
            }


        } catch (ParserConfigurationException | SAXException | IOException e) {
            System.out.println("Vous n'avez pas choisi un fichier valid !");

        }

        return new ArrayList<>(noeudsMap.values());
    }

    public static void  ModelToView(ArrayList<Noeud> noeuds, FenetrePrincipal fenetre){
        HashMap<Integer, JPanel> noeudsView = new HashMap<>();
        ArrayList<Liaison> liaisons = new ArrayList<>();

        for (Noeud noeud: noeuds) {

            fenetre.ajouterNoeudWithModel(
                    noeud.getId(),
                    noeud.getTitre(),
                    noeud.getDescription(),
                    noeud.getX(),
                    noeud.getY()
            );


            liaisons.addAll(noeud.getLiaisonFils());
        }
        for (Map.Entry m : fenetre.getNoeuds().entrySet()) {
            JPanel panel = (JPanel) m.getValue();
            noeudsView.put(Integer.parseInt(panel.getName()), panel);
        }


        for (Liaison l: liaisons) {
            JPanel pere = noeudsView.get(l.getNoeudPere().getId());
            JPanel fils = noeudsView.get(l.getNoeudFils().getId());
            fenetre.getSurface().getLiaisons().add(pere);
            fenetre.getSurface().getLiaisons().add(fils);
        }

    }

    public static ArrayList<Noeud> ViewToModel(FenetrePrincipal fenetre){
        HashMap<Integer, Noeud> noeudsMap = new HashMap<>();

        for (Map.Entry m : fenetre.getNoeuds().entrySet()) {
            JPanel panel = (JPanel) m.getValue();
            JTextField titreField = (JTextField) panel.getComponent(0);
            JTextArea descriptionField = (JTextArea) panel.getComponent(1);

            int id = Integer.parseInt(panel.getName());
            String titre = titreField.getText();
            String description = descriptionField.getText();

            double x = panel.getX();
            double y = panel.getY();

            Noeud noeud = new Noeud(id,titre,description,x,y);
            noeudsMap.put(noeud.getId(), noeud);
        }
        ArrayList<Component> listLiaisons = fenetre.getSurface().getLiaisons();
        HashMap<String,Liaison> listLiaisonsSansDoublons = new HashMap<>();

        for (int i = 0; i < listLiaisons.size()-1; i+=2) {

            JPanel panelPere = (JPanel) listLiaisons.get(i);
            JPanel panelFils = (JPanel) listLiaisons.get(i+1);

            Liaison liaison = new Liaison(
                    noeudsMap.get(Integer.parseInt(panelPere.getName())),
                    noeudsMap.get(Integer.parseInt(panelFils.getName()))
            );
            listLiaisonsSansDoublons.put(panelPere.getName()+"-"+panelFils.getName(),liaison);
        }

        for (Map.Entry l : listLiaisonsSansDoublons.entrySet()) {
            Liaison liaison = (Liaison) l.getValue();
            noeudsMap.get(liaison.getNoeudPere().getId()).addLiaisonFils(liaison);
        }


        return new ArrayList<>(noeudsMap.values());
    }
}