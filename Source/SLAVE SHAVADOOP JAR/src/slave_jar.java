import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;



public class slave_jar {

	private static String line;
	
	//Cette fonction permet de récupérer dans une arraylist l'ensemble des mots composants le fichier txt en input
	//On va notammment utiliser cette fonction pour récupérer les mots interdits dans une arraylist
	public static ArrayList<String> text_to_list(String Path, String Filename) throws FileNotFoundException, IOException{
		ArrayList<String> ma_list_text = new ArrayList<String>();
		try (BufferedReader br = new BufferedReader(new FileReader(Path + Filename));
				)
				{
		
					String sCurrentLine;
				
					while ((sCurrentLine = br.readLine()) != null) {
						ma_list_text.add(normalizar(sCurrentLine)); //on ajoute les mots normalisés à notre arraylist
					}
				}
		
		return ma_list_text ;
	}
	//Fonction pour normaliser les mots
	public static String normalizar(String s) {
	    String str;
	    //pour chaque mot en input on vérifie un ensemble de paramètre
	        str = Normalizer
	        	.normalize(s, Normalizer.Form.NFD)
	        	.replaceAll("[^\\p{ASCII}]", "") //Supprésion caractère non ascii
	        	.replaceAll("[-+.^:,';!]"," ") //Supprésion de caractère spéciaux
	        	.replaceAll(" +", " ") //Supprésion des espaces
	        	.toLowerCase(); //Passage en miniscule 
	    
	    return str;
	}


	
	public static void main(String[] args) throws InterruptedException, FileNotFoundException, IOException {


		String path_project = args[1];
		ArrayList<String> res = new ArrayList<String>();
		String word_idt = new String();
		String name_output = new String();
		
		//on import notre liste de mot interdit 
		ArrayList<String> list_pronom = new ArrayList<String>() ;
		list_pronom = text_to_list(path_project ,"pronom_list.txt");
		
		/* ---------------------------------------------------*/
		/* ---------------- Mapping Command ---------------- */
		/* ------------------------------------------------ */
		
		if (args[0].equals("modeSXUMX") ){

			try (	
				    InputStream fis = new FileInputStream(path_project + args[2]);
				    InputStreamReader isr = new InputStreamReader(fis, Charset.forName("UTF-8"));
				    BufferedReader br = new BufferedReader(isr);
						
				) {
				    while ((line = br.readLine()) != null) {
				    	String[] Words = line.split(" "); //après lecture du fichier SX on le split par mot
				    	
				    	for (int i = 0; i < Words.length; i++) { 
				    		//pour chaque mot contenu dans le fichier SX on vérifie qu'il n'appartient pas à la liste de mot interdit
				    		//et que le mot ne contient pas de chiffre puis on l'ajoute à une arraylist
				    		//le mot est à nouveau normalisé 
				    		if(!list_pronom.contains(Words[i]) && !Words[i].matches(".*\\d.*")){
				    			res.add(normalizar(Words[i]));
				    		}
				    		
				    	}
				    }
				    br.close();
				}
				
				//on écrit l'ensemble des mots lus dans le fichier SX dans un ficher UM
				//pour chaque mot on écrit le chiffre 1 à coté
				try (BufferedWriter writer = new BufferedWriter(new FileWriter(path_project+args[2].replace("S", "UM")));){
					
					for (int i = 0; i < res.size(); i++){
						writer.write(res.get(i) + " " + "1");
						writer.newLine();
					}
					
				writer.close();
				}
				
				//on print chaque mot lu dans le fichier SX de manière unique
				//la méthode HashSet permet de ne pas conserver les doublons dans notre arraylist res
				Set<String> uniqueWord = new HashSet<String>(res);
				for(String wordu : uniqueWord){
					System.out.println(wordu);
				}
			
		/* --------------------------------------------------------------*/
		/* ---------------- Shuffling+Reducing Command ---------------- */
		/* ----------------------------------------------------------- */
			
		}else if(args[0].equals("modeUMXSMX")){
			
			 word_idt = args[2];
			 String true_value_word = new String();
			 name_output = args[3];
			 BufferedWriter writer = new BufferedWriter(new FileWriter(path_project+name_output));
			 ArrayList<Integer> RMx_Count = new ArrayList<Integer>();
			
			 //on boucle sur l'ensemble des arguments à partir du 3ème car les deux premiers arguments sont le nom de sortie
			 //et le mot à chercher 
			 //on boucle sur toute les UM qui détienent le mot à chercher afin d'effectuer la somme de toutes les occurences
			 for(int i=4;i<args.length;i++){
				 
				 try (
						    InputStream fis = new FileInputStream(path_project + args[i]);
						    InputStreamReader isr = new InputStreamReader(fis, Charset.forName("UTF-8"));
						    BufferedReader br = new BufferedReader(isr);		 	
								
						) {
						    while ((line = br.readLine()) != null) {
						    	String[] Words_i = line.split(" ");
						    	
						    	//a chaque lecture des fichiers UM on observe si les mots contenus sont égaux au mot cherché
						    	if(Words_i[0].toLowerCase().equals(word_idt.toLowerCase())){
						    		
						    		//Si l'égalité est vérifié on ajoute 1 à la liste des occurences du mot (RMx_Count)
						    		RMx_Count.add(1);
						    		true_value_word = Words_i[0];
						    		
						    		//on écrit notre fichier output SMx qui renseigne l'ensemble des occurences du mot dans un fichier
						    		//au lieu d'une multitude de fichier UMx
						    		
						    		for(int t = 0; t < Words_i.length; t++) {
						    			writer.write(Words_i[t]+ " ");
										
						    		}
						    	writer.newLine();
						    	}
						    	

						    }
						    br.close();
					
						}
				 
				
				 
			 }
			 
			 writer.close();
			 //Une fois la boucle terminé sur l'ensemble des UMx on a crée notre fichier SMx
			 
			 //On peut crée le fichier Rmx en réalisant la somme des occurences de l'arrayList RMx_Count
			 int s;
			 int sum=0;
			 for(s = 0; s < RMx_Count.size(); s++){
				 sum += RMx_Count.get(s);
			 }
			
			 //On crée un fichier RMx
			 BufferedWriter writer_rm = new BufferedWriter(new FileWriter(path_project+name_output.replace("SM","RM")));
			 //On écrit sur le fichier le mot et son nombre d'occurence totale
			 //puis on renvoit en sortie le mot et sa somme
			 writer_rm.write(true_value_word + " ");
			 writer_rm.write(String.valueOf(sum));
			 writer_rm.close();
			 System.out.println(true_value_word + " " + String.valueOf(sum));
			 
			
		}
		
			
		
	}

}
