package pkg;

import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.LinkedList;

import org.apache.jena.base.Sys;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.ontology.DatatypeProperty;
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
import org.apache.jena.vocabulary.XSD;

import models.Proposition;

public class Mapeador {
	private OntModel model;
	private String NS;
	
	public Mapeador() {
		super();
//		this.setModel(ModelFactory.createOntologyModel(OntModelSpec.OWL_LITE_MEM_RULES_INF)); //problemas aqui, PESQUISAR
		
		this.setModel(ModelFactory.createOntologyModel());
		this.setNS("http://example.com/test#"); // PESQUISAR SOBRE URIs
		
//		OntClass classFrom = this.model.createClass(this.getNS() + "Carro");
//		Individual i = classFrom.createIndividual(this.getNS() + "Fusca Preto");
////		System.out.println("m�todo getLocalName(): ----->" + i.getLocalName());
////		System.out.println("m�todo getNameSpace(): ----->" + i.getNameSpace());
//		System.out.println("m�todo getURI(): ----->" + i.getURI());
//		System.out.println("full name: ----->" + i.getURI().split("#")[1]);
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
					this.criaIndividuosIguais(p);
					break;
					
				case "different from": //rela��o: indiv�duos diferentes
					this.criaIndividuosDiferentes(p);
					break;
					
				case "is an attribute of": //rela��o: dataTypeObject-classe
					this.criaAtributoDeClasse(p);
					break;
					
				case "that is": //rela��o: dataTypeObject-type (Integer, String, ...) 
					this.criaTipoDeAtributo(p);
					break;	
		
				default: //caso sem esteri�tipo
					this.criaRelacaoSemEsteriotipo(p);
					break;
			}
		}
		this.imprimeOWL();
	}
	
	
	public void imprimeOWL () {	
		FileWriter out = null;
		RDFWriter w = this.model.getWriter("RDF/XML-ABBREV");
		
		try {
			out = new FileWriter("mymodel.owl", false);
			w.write(this.model, out, "RDF/XML-ABBREV" );
		} catch (IOException e) {
			System.out.print(e);
		} finally {
			if (out != null) {
			    try {
			    	out.close();
			    } catch (IOException ignore) {
			    	System.out.print(ignore);
			    }
		    }
		}
		
//		RDFWriter w = this.model.getWriter("RDF/XML-ABBREV");
//		w.write(this.model, System.out, "RDF/XML-ABBREV" );
		
		
//		w.setProperty("allowBadURIs","true");
		
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
		OntClass classTo = this.model.createClass(this.getNS() + to);
		OntClass classFrom = this.model.createClass(this.getNS() + from);
		
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
		OntClass classTo = this.model.createClass(this.getNS() + to);
		OntClass classFrom = this.model.createClass(this.getNS() + from);
		
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
		OntClass classTo = this.model.createClass(this.getNS() + to);
		OntClass classFrom = this.model.createClass(this.getNS() + from);
		
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
		OntClass classTo = this.model.createClass(this.getNS() + to);
		OntClass classFrom = this.model.createClass(this.getNS() + from);
		
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
		
		OntClass classFrom = this.model.createClass(this.getNS() + to);
		classFrom.createIndividual(this.getNS() + from);		
	}
	
	
	/**
	 * M�todo que diz que duas inst�ncias diferentes � a mesma (From � uma inst�ncia igual a To)
	 * Exemplo: Dede e Davidson Cury s�o o mesmo indiv�duo
	 * 
	 * @author Guilherme N. Pinotte
	 * 
	 */
	private void criaIndividuosIguais (Proposition p) {
		String to = p.getTo().getLabel();
		String from = p.getFrom().getLabel();
		Individual iFrom = null;
		Individual iTo = null;
				
		for (OntClass o : this.model.listClasses().toList()) {		
//			System.out.println("Classe: ---> "+o.getURI().split("#")[1]);			
			for (OntResource  ind : o.listInstances().toList()) {				
//				System.out.println("               Individuo: ---> "+ind.getURI().split("#")[1]);
				//pega a uri completa e da um split quando encontra #, pega a segunda String e compara ignorando case
				if (ind.getURI().split("#")[1].equalsIgnoreCase(from)) {
					iFrom = (Individual) ind;
//					System.out.println("               1 ----> " + from + " = " + iFrom.getURI().split("#")[1]);
				}
			}
			for (OntResource  ind : o.listInstances().toList()) {				
//				System.out.println("               Individuo: ---> "+ind.getURI().split("#")[1]);
				if (ind.getURI().split("#")[1].equalsIgnoreCase(to)) {
					iTo = (Individual) ind;
//					System.out.println("               2 ----> " + to + " = " + iTo.getURI().split("#")[1]);
				}
			}
		}
		if (iFrom != null){
			if (iTo == null) {
				OntClass classFrom = iFrom.getOntClass();
//				System.out.println("               Classe aqui: ---> " + classFrom.getLocalName());
				iTo = classFrom.createIndividual(this.getNS() + to);
			}
			iFrom.addSameAs(iTo);
		} else {
			if (iTo == null){
				//criar uma classe default??
				//ver o que fazer aqui
			} else {
				OntClass classTo = iTo.getOntClass();
				iFrom = classTo.createIndividual(this.getNS() + from);
			}
			iTo.addSameAs(iFrom);
		}
	}
	
	
	/**
	 * M�todo que diz que duas inst�ncias s�o diferentes (From � uma inst�ncia diferente a To)
	 * Exemplo: Guilherme Pinotte e Davidson Cury s�o indiv�duos diferentes
	 * 
	 * @author Guilherme N. Pinotte
	 * 
	 */
	private void criaIndividuosDiferentes (Proposition p) {
		String to = p.getTo().getLabel();
		String from = p.getFrom().getLabel();
		Individual iFrom = null;
		Individual iTo = null;
				
		for (OntClass o : this.model.listClasses().toList()) {			
			for (OntResource  ind : o.listInstances().toList()) {
				if (ind.getURI().split("#")[1].equalsIgnoreCase(from)) {
					iFrom = (Individual) ind;
				}
			}
			for (OntResource  ind : o.listInstances().toList()) {
				if (ind.getURI().split("#")[1].equalsIgnoreCase(to)) {
					iTo = (Individual) ind;
				}
			}
		}
		if (iFrom != null){
			if (iTo == null) {
				OntClass classFrom = iFrom.getOntClass();
				iTo = classFrom.createIndividual(this.getNS() + to);
			}
			iFrom.addDifferentFrom(iTo);
		} else {
			if (iTo == null){
				//criar uma classe default??
				//ver o que fazer aqui
			} else {
				OntClass classTo = iTo.getOntClass();
				iFrom = classTo.createIndividual(this.getNS() + from);
			}
			iTo.addDifferentFrom(iFrom);
		}
	}
	
	
	/**
	 * M�todo que cria um DataType Property (atributo) de uma Classe
	 * 
	 * @author Guilherme N. Pinotte
	 * 
	 */
	private void criaAtributoDeClasse (Proposition p) {
		String to = p.getTo().getLabel();
		String from = p.getFrom().getLabel();
		OntClass classTo = this.model.createClass(this.getNS() + to);
		DatatypeProperty attr = this.model.createDatatypeProperty(this.getNS() + from);
		
		attr.addDomain(classTo);
	}
	
	
	/**
	 * M�todo que cria um Tipo (String, int, ...) para um DataType Property de uma Classe
	 * 
	 * @author Guilherme N. Pinotte
	 * 
	 */
	private void criaTipoDeAtributo (Proposition p) {
		String sAttr = p.getTo().getLabel();
		String from = p.getFrom().getLabel();
		DatatypeProperty attr = this.model.createDatatypeProperty(this.getNS() + from);
		Resource r = null;
		
		//olhar quais outros tipos acrescentar nessa lista
		switch (sAttr) {
		case "String":
			r = XSD.xstring;
			break;
		case "Integer":
			r = XSD.xint;
			break;
		case "Date":
			r = XSD.date;
			break;
					
		default:
			break;
		}	
		attr.addRange(r);
	}
	
	
	/**
	 * M�todo que cria duas classes e uma object property
	 * 
	 * @author Guilherme N. Pinotte
	 * 
	 */
	private void criaRelacaoSemEsteriotipo (Proposition p) {
		String to = p.getTo().getLabel();
		String from = p.getFrom().getLabel();
		OntClass classTo = this.model.createClass(this.getNS() + to);
		OntClass classFrom = this.model.createClass(this.getNS() + from);
		ObjectProperty relation = this.model.createObjectProperty(p.getRel().getLabel());
		
		relation.addDomain(classFrom);
		relation.addRange(classTo);
		relation.addLabel(p.getRel().getLabel(), "en" );
	}
	
	
}
