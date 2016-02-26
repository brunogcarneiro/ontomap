package pkg;

import java.util.LinkedList;

import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.ObjectProperty;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.ontology.OntResource;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFWriter;
import org.apache.jena.rdf.model.Resource;

import models.Proposition;

public class Mapeador {
	private OntModel model;
	private String NS;
	
	public Mapeador() {
		super();
//		this.setModel(ModelFactory.createOntologyModel(OntModelSpec.OWL_LITE_MEM_RULES_INF)); //problemas aqui, PESQUISAR
		
		this.setModel(ModelFactory.createOntologyModel());
		this.setNS("http:example.com/test#"); // PESQUISAR SOBRE URIs
		
		OntClass classFrom = this.model.createClass(this.getNS() + "Carro");
		classFrom.createIndividual(this.getNS() + "fusca");
		
		RDFWriter w = this.model.getWriter("TURTLE");
		w.setProperty("allowBadURIs", "true");
		w.setProperty("relativeURIs","");
		w.write(this.model, System.out, "TURTLE");
		
	}

	public OntModel getModel() {
		return this.model;
	}

	public void setModel(OntModel model) {
		this.model = model;
	}

	public String getNS() {
		return this.NS;
	}

	public void setNS(String nS) {
		this.NS = nS;
	}
	
	/**
	 * M�todo que faz a l�gica de mapeamento criando as listas de Classes, Individuals e DataType Objects
	 * 
	 * @author Guilherme N. Pinotte
	 * 
	 */
	public void fazMapeamento (LinkedList<Proposition> propositions){
		for (Proposition p : propositions) {
			switch (p.getRel().getLabel()) {
				case "are": //rela��o: classe-subclasse
					this.criaClasseSubClasse(p);
					break;
					
				case "equivalent to": //rela��o: classes equivalentes
					this.criaClassesEquivalentes(p);
					break;
					
				case "cannot be": //rela��o: classes disjuntas
					this.criaClassesDisjuntas(p);
					break;
					
				case "exact opposite of": //rela��o: complemento de classes
					this.criaComplementoDeClasses(p);
					break;
					
				case "is composed of": //rela��o: rela��o de todo-parte
//					this.criaRela��oTodoParte(p);
					break;
					
				case "is a": //rela��o: indiv�duo-classe
					this.criaInstaciaDeClasse(p);
					break;
					
				case "same as": //rela��o: indiv�duos iguais
//					this.criaIndividuosIguais(p);
					break;
					
				case "different from": //rela��o: indiv�duos diferentes
//					this.criaIndividuosDiferentes(p);
					break;
					
				case "is attibute of": //rela��o: dataTypeObject-classe
//					this.criaAtributoDeClasse(p);
					break;
					
				case "that is": //rela��o: dataTypeObject-type (Integer, String, ...) 
//					this.criaTipoDeAtributo(p);
					break;	
		
				default: //caso sem esteri�tipo
					this.criaRelacaoSemEsteriotipo(p);
					break;
			}
		}
		this.imprimeOWL();
	}
	
	public void imprimeOWL () {
		
//		out = new FileWriter( "mymodel.xml" );
//		m.write( out, "RDF/XML-ABBREV" );
		
		RDFWriter w = this.model.getWriter("RDF/XML");
//		w.setProperty("allowBadURIs","true");
		w.write(this.model, System.out, "RDF/XML");
	}
	
	/**
	 * M�todo que cria rela��o de heran�a entre as classes (From vira subclasse de To)
	 * 
	 * @author Guilherme N. Pinotte
	 * 
	 */
	private void criaClasseSubClasse (Proposition p) {
		String to = p.getTo().getLabel();
		String from = p.getFrom().getLabel();
		OntClass classTo = this.model.createClass(to);
		OntClass classFrom = this.model.createClass(from);
		
		//fazendo a classe From virar superclasse da To (HERAN�A)
		classFrom.addSuperClass(classTo);
	}
	
	/**
	 * M�todo que cria duas classes equivalentes (From equivale a To)
	 * 
	 * @author Guilherme N. Pinotte
	 * 
	 */
	private void criaClassesEquivalentes (Proposition p) {
		String to = p.getTo().getLabel();
		String from = p.getFrom().getLabel();
		OntClass classTo = this.model.createClass(to);
		OntClass classFrom = this.model.createClass(from);
		
		classFrom.addEquivalentClass(classTo);
	}
	
	/**
	 * M�todo que cria duas classes disjuntas (From n�o possui indiv�duos em comum com To)
	 * 
	 * @author Guilherme N. Pinotte
	 * 
	 */
	private void criaClassesDisjuntas (Proposition p) {
		String to = p.getTo().getLabel();
		String from = p.getFrom().getLabel();
		OntClass classTo = this.model.createClass(to);
		OntClass classFrom = this.model.createClass(from);
		
		classFrom.addDisjointWith(classTo);
	}
	
	/**
	 * M�todo que cria duas classes complementares (From complementa To)
	 * Exemplo: Fumantes e N�o Fumantes s�o complementares
	 * 
	 * @author Guilherme N. Pinotte
	 * 
	 */
	private void criaComplementoDeClasses (Proposition p) {
		String to = p.getTo().getLabel();
		String from = p.getFrom().getLabel();
		OntClass classTo = this.model.createClass(to);
		OntClass classFrom = this.model.createClass(from);
		
		classFrom.convertToComplementClass(classTo);
	}

	/**
	 * M�todo que cria inst�ncia de uma classe (From � indiv�duo de To)
	 * Exemplo: Fumantes e N�o Fumantes s�o complementares
	 * 
	 * @author Guilherme N. Pinotte
	 * 
	 */
	private void criaInstaciaDeClasse (Proposition p) {
		String to = p.getTo().getLabel();
		String from = p.getFrom().getLabel();
		
//		System.out.println("################# " + from + " #################");
		
//		OntClass classTo = this.model.createClass(to);
		OntClass classFrom = this.model.createClass(from);
		classFrom.createIndividual(this.getNS() + to);
	}

	/**
	 * M�todo que diz que duas inst�ncias diferentes � a mesma (From � uma inst�ncia igual a To)
	 * Exemplo: Dede e Davidson Cury s�o o mesmo indiv�duo
	 * 
	 * @author Guilherme N. Pinotte
	 * 
	 */
//	private void criaIndividuosIguais (Proposition p) {
//		String to = p.getTo().getLabel();
//		String from = p.getFrom().getLabel();
//		Individual iFrom = null;
//		Individual iTo = null;
//		
//		//ANALISAR MELHOR ESSE M�TODO, POIS PARA CRIAR UMA INSTANCIA PRIMEIRO � NECESS�RIO CRIAR UMA CLASSE
//		//TALVEZ DE PARA CRIAR UMA INSTANCIA DESSA FORMA: this.model.createIndividual(from, iFrom);
//		
//		// 
//		
//		
//		for (OntClass o : this.model.listClasses().toList()) {
//			for (OntResource  ind : o.listInstances().toList()) {
////				ind = (Individual) ind;
//				if (ind.getLocalName().equalsIgnoreCase(from)) {
//					iFrom = (Individual) ind;
//				}
//			}
//			for (OntResource  ind : o.listInstances().toList()) {
////				ind = (Individual) ind;
//				if (ind.getLocalName().equalsIgnoreCase(to)) {
//					iTo = (Individual) ind;
//				}
//			}
//		}
//		if (iFrom != null && iTo != null)
//			iFrom.addSameAs(iTo);
//		
//	}
	
	/**
	 * M�todo que cria duas classes e uma object property
	 * 
	 * @author Guilherme N. Pinotte
	 * 
	 */
	private void criaRelacaoSemEsteriotipo (Proposition p) {
		String to = p.getTo().getLabel();
		String from = p.getFrom().getLabel();
		OntClass classTo = this.model.createClass(to);
		OntClass classFrom = this.model.createClass(from);
		ObjectProperty relation = this.model.createObjectProperty(p.getRel().getLabel());
		
		relation.addDomain(classFrom);
		relation.addRange(classTo);
		relation.addLabel(p.getRel().getLabel(), "en" );
	}
	
}
