import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;


public class master_shavadoop {
	
private static BufferedReader br;
private static ArrayList<String> split_input;


//Fonction d'affichage comme la classe LecteurFlux
//On utilise cette fonction lors de la lecture du fichier de machine, elle nous permet de renvoyer le message de la console
public static void Afficheur(BufferedWriter writer,String current,Process processing) throws IOException{
	InputStream is = processing.getInputStream();
	InputStreamReader isr = new InputStreamReader(is);
    BufferedReader br = new BufferedReader(isr);
    

	String line;

	    	while ((line = br.readLine()) != null) {
	    		
	    			System.out.println(line);
            		System.out.println(current);
            		writer.write(current);   
    			    writer.write('\n');
    
	    	}
	    	
}

//Fonction Normalizar, également utiliser dans le slave pour normaliser les mots 
public static String normalizar(String s) {
    String str;
    String str_2;
    	str = s
            	.replaceAll("[-+.^:,;'%!]"," ") //Supprésion de caractère spéciaux
            	.replaceAll(" +", " ") //Remplacement d'espace inutilse
            	.replaceAll("[)( %]"," ") //Supprésion de caractère spéciaux
            	.toLowerCase(); //to miniscule 
    			
        str_2 = Normalizer
        	.normalize(str, Normalizer.Form.NFD)
        	.replaceAll("[^\\p{ASCII}]", "");
        	
    
    return str_2;
}



//On utilise la fonction Compute pour nous renvoyer le nombre de machine qui ont réussi à se connecter
public static ArrayList<String> Compute(String  path_i) throws IOException{

	//on prend en input le fichier où sont inscrite toutes les machines bien connéctées
	
	br = new BufferedReader(new FileReader(path_i + "connect_machine.txt"));
	
	//on renseigne l'ensemble de ces machines dans une arraylist
	
	ArrayList<String> Liste = new ArrayList<String>();
	String sCurrentLine;
	while ((sCurrentLine = br.readLine()) != null) {	
		Liste.add(sCurrentLine);
	}
	return Liste ;
}

//La fonction read_write est la fonction qui nous permet de nous connecter à toutes les machines
//elle prend en input le fichier où toutes les machines succeptibles de se connecter sont renseignés 
public static void read_write(String lect, String ecriture,String path_i) throws InterruptedException{
		
		try (BufferedReader br = new BufferedReader(new FileReader(path_i + lect));
			 BufferedWriter writer = new BufferedWriter(new FileWriter(path_i+ecriture));

			)
			{
	
				String sCurrentLine;

				
				while ((sCurrentLine = br.readLine()) != null) {

				try {
						List<String> command = new ArrayList<String>();
					    command.add("ssh"); //tentative de connexion à la machine sCurrentLine
					    command.add(sCurrentLine);
					    command.add("echo ok");
					   
					    //On lance notre processbuilder qui permet d'exécuter la commande que l'on a définit au dessus
					    ProcessBuilder builder = new ProcessBuilder(command);
					    final Process process = builder.start();
					    
					    //appel de notre fonction afficheur pour connaitre la réponse du terminal à la connexion SSH
					    Afficheur(writer,sCurrentLine,process);
   
					    
					} catch (IOException e) {
						System.out.println("Failure connection to : " + sCurrentLine ); //en cas d'échec
			      } 
				
				}
		
				
			br.close();
			writer.close();
			
			} catch (IOException e) {
				e.printStackTrace();
			}
		
		
	}

//Fonction similaire à la fonction du Slave
//permet de renvoyer l'ensemble des mots d'un texte dans une arraylist
public static ArrayList<String> text_to_list(String Path, String Filename) throws FileNotFoundException, IOException{
	ArrayList<String> ma_list_text = new ArrayList<String>();
	try (BufferedReader br = new BufferedReader(new FileReader(Path + Filename));
			)
			{
	
				String sCurrentLine;
				
				while ((sCurrentLine = br.readLine()) != null) {
					String[] Words = sCurrentLine.split(" ");
			    	
			    	for (int i = 0; i < Words.length; i++) { 
			    		if( ! Words[i].equals("") ){ 
			    			ma_list_text.add(normalizar(Words[i]));
			    		}
			    		
			    	}
					
				}
			}
	//on renvoie la liste de tout les mots normalisé, et sans mot nul
	return ma_list_text ;
}

//La fonction breakfile est la fonction qui split le fichier au début de notre processus 
//Au départ cette fonction split le fichier par ligne mais pas optimisation la fonction a été modifiée
//La fonction split le fichier par mot ici
//et elle écrit autant de mot dans chaque fichier afin de créer autant de fichier Sx que de machine disponible
public static List<String> breakFile(String fname, int pieces,String pathj) throws FileNotFoundException, IOException {

	//On crée une la l'arraylist des mots présent dans notre input
    ArrayList<String> liste_words= text_to_list(pathj, fname);	
	
    //On vérifie que l'on n'est pas conservé des mots "composé"
    //exemple : "j avais"
    //dans ce cas on split à nouveau par " ", on remove l'élement de la list puis on ajoute les nouveaux élements
    //exemple : on ajoute "j" et "avais"
	for(int w = 0;w<liste_words.size();w++){
		String[] Words = liste_words.get(w).split(" ");
		if(Words.length>1){
			liste_words.remove(w);
			for(int i=0;i<Words.length;i++){
				if( ! Words[i].equals("") ){ 
					liste_words.add(normalizar(Words[i]));
	    		}			
			}
		}
	}
    
	
	
    ArrayList<String> res = new ArrayList<String>();
    
    //On récupère le nombre de machine et le nombre de mot total
    
    Integer x = Integer.valueOf(pieces);
    Integer y = Integer.valueOf(liste_words.size());
    int size ;
    int size_mod;
    
    //si le nombre de machine est plus important que le nombre de mot, notre code n'est plus très optimal
    //il semble plus optimal dans ce cas de split par ligne
    if(x>y){
    	x = y ;  //ici dans ce cas on donne un mot à chaque machine pour le split
    	size = 1;
    	size_mod = 0;
    }else{
    	//si le nombre de machine est inférieur au nombre de mot
    	size = y / x ; //on calcule le nombre de mot attribué à chaque mot par une division
        size_mod = y % x; //on capture le reste de la division
    }
    
    
    
    int mot = 0 ;
    int iter_words = 0;
    int iter_s = 0;
   
    //pour chaque machine on lui attribue le nombre de mot size défini au dessus 
    for(int i=0;i<x;i++){
    	
    	String name = "S" + i;
    	PrintWriter writer;
        try {
        	writer = new PrintWriter(pathj + name, "UTF-8");
        		for(int j = mot;j<mot+size;j++){
        			
        		if(!liste_words.get(j).equals("")){
     
                		writer.println(normalizar(liste_words.get(j)));
                		
                		
                		}
        	iter_words = j ;
        	
        		}
        		writer.close();
        		mot = iter_words+1;
        		iter_s = i+1 ;
        } catch (FileNotFoundException | UnsupportedEncodingException e) {
                e.printStackTrace();
        }
        
        res.add(name); 
    }
    
    //une fois le nombre de mot attribué à chaque machine on doit attribuer les mots restants de la division
    //Si le restant est un multiple de 2, on attribue 2 mot à chaque machine jusqu'a qu'il ne reste plus de mot 
    if((size_mod % 2) == 0){
    
    	int	nb_part = size_mod/2;
    	
    	for(int i=0;i<nb_part;i++){
    		String name = "S" + iter_s;
        	PrintWriter writer;
            writer = new PrintWriter(pathj + name, "UTF-8");
            
            		for(int j = mot;j<mot+2;j++){
            			
            			if(!liste_words.get(j).equals("")){
         
                    		writer.println(normalizar(liste_words.get(j)));
                    		
                    		
                    		}
            	
            			iter_words = j ;
            		}
            writer.close();
            mot = iter_words+1;
        	iter_s= iter_s +1 ; 
        	res.add(name);
    	}
    	//Si le restant est un multiple de 3, on attribue 3 mot à chaque machine jusqu'a qu'il ne reste plus de mot 
    }else if ((size_mod % 3)==0){
    	
    	int	nb_part = size_mod/3;
    	
    	for(int i=0;i<nb_part;i++){
    		String name = "S" + iter_s;
        	PrintWriter writer;
            writer = new PrintWriter(pathj + name, "UTF-8");
            
            		for(int j = mot;j<mot+3;j++){
            			
            			if(!liste_words.get(j).equals("")){
         
                    		writer.println(normalizar(liste_words.get(j)));
                    		       	
                    		}
            	
            			iter_words = j ;
            		}
            writer.close();
            mot = iter_words+1;
            iter_s= iter_s +1 ; 
        	res.add(name);
    	}
    	
    	
    }else{
    	//si le nombre de mot restant n'est ni un multiple de 2 et de 3, on attribut les mots restant à une machine
    	String name = "S" + iter_s;
    	PrintWriter writer;
    	writer = new PrintWriter(pathj + name, "UTF-8");
        for(int j = mot;j<liste_words.size();j++){

        	if(!liste_words.get(j).equals("")){

        		writer.println(normalizar(liste_words.get(j)));
        		       		
        		}
        }
        res.add(name);
        
    }
    
    
    return res;
}



//la fonction mergefile est utilisé pour transformer l'ensemble des RM en un fichier output
public static void mergefile(ArrayList<String> RM, String out_name,String Path) throws IOException{
	
	BufferedWriter writer = new BufferedWriter(new FileWriter(Path+out_name));
	for(String name : RM){
		//pour chaque RM on écrit son contenu dans notre output
		try ( BufferedReader br = new BufferedReader(new FileReader(Path + name));
				)
				{
					String sCurrentLine;
					while ((sCurrentLine = br.readLine()) != null) {
						writer.write(sCurrentLine);
						writer.newLine();
				}
					
				br.close();
				
				} catch (IOException e) {
					e.printStackTrace();
				}
		
		
	
	}
	writer.close();
	
}

//fonction que l'on utilise pour trier les mots par leurs nombre d'occurence
public static void sorted_file(String path, String Filename) throws FileNotFoundException, IOException{

//Dans une hashmap on renseigne le mot et son nombre d'occurence à partir du fichier d'input
Map<String,Integer> my_sort = new HashMap<String,Integer>() ;

try (BufferedReader br = new BufferedReader(new FileReader(path + Filename));
		)
		{
			String sCurrentLine;

			
			while ((sCurrentLine = br.readLine()) != null) {
				
				String[] Words = sCurrentLine.split(" ");
				my_sort.put(Words[0],Integer.parseInt(Words[1]));
				
			}
		br.close();
	}

BufferedWriter writer = new BufferedWriter(new FileWriter(path+"output_sort.txt"));
//on compare les valeurs de chacun des mots 
List<Map.Entry<String, Integer>> list = new ArrayList<Map.Entry<String, Integer>>(my_sort.entrySet());

Collections.sort(list, new Comparator<Map.Entry<String, Integer>>() {
    public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {
    
        return (o2.getValue()).compareTo(o1.getValue());
    }
    
});




for (Map.Entry<String, Integer> entry : list) {
	
		writer.write(entry.getKey()+ " " + entry.getValue());
	    writer.write("\n");

}

writer.close();
	
}


	public static void main(String[] args) throws Exception {
		
		long startTime = System.currentTimeMillis();
		
		
		//chemin d'enregistrement de tout les fichiers
		String path_project = args[0];
		
		/* ------------------------------------------------------------------------- */
		/* ---------------- Read Ip Adress and save on write file  ---------------- */
		/* ----------------------------------------------------------------------- */
		
		//on lanche la fonction read_write qui tente de se connecter à chacune des machines et 
		//écrit dans un ficher write_file les machines qui ont réussit à se connecter
		read_write("ip_adress.txt","connect_machine.txt",path_project);
		
		//On renseigne dans l'arrayliste ListeMachine toute les machines connecté à partir de la fonction Compute
		ArrayList<String> ListeMachine = Compute(path_project) ; 
		
		long first_step = System.currentTimeMillis();
		long total_first_step = first_step - startTime;
		
		
		/* ------------------------------------------------ */
		/* ---------------- Splitting Step ---------------- */
		/* ------------------------------------------------ */
		
		long star_time_step = System.currentTimeMillis();
		
		//On split notre fichier input en mot et on attribut un nombre de mot à peu près équivalent à chaque machine
		//la fonction breakfile renvoie un arraylist avec l'ensemble des noms des fichiers Sx créée 
		split_input = (ArrayList<String>) breakFile(args[1],ListeMachine.size(),path_project);
		
		long split_step = System.currentTimeMillis();
		long total_split_step = split_step - star_time_step;
		
		
		/* ------------------------------------------------*/
		/* ---------------- Mapping Step ---------------- */
		/* --------------------------------------------- */
		
		star_time_step = System.currentTimeMillis();
		
		HashMap<String,String> dict_UMX = new HashMap<String,String>(); 
		HashMap<String, ArrayList<String>> dict_W = new HashMap<String,ArrayList<String>>();
		
		String j_machine = new String();
		int i =0;
		int j = 0;
		
		ArrayList<LaunchSlaveShavadoop> slaves = new ArrayList<LaunchSlaveShavadoop>();
		ArrayList<String > uniqueword = new ArrayList<String>();
		String um_part = new String();
		
		
		//Pour chaque "morceau" soit pour chaque fichier Sx on lance le traitement SxUMx afin de créer nos fichier UMx
		//chacun de ces traitements sont lancés sur une machine
		//si il y'a plus de traitement que de machine on recommence à lancer sur les premières machine
		for(String morceau:split_input ){
			
			if (j>=ListeMachine.size()){j=0;}
			
			j_machine= ListeMachine.get(j);
					

			    String command = "cd "+path_project + " " +";java -jar salve_shavadoop.jar modeSXUMX "+ path_project+ " " +morceau ;
			    
			    LaunchSlaveShavadoop slave = new LaunchSlaveShavadoop(j_machine,command,morceau,40);
			    slave.start();
			    
			    slaves.add(slave);
			    
			    
			    System.out.println("Début process " + j_machine + " avec la commande " + morceau );
			    
				//On crée le dictionnaire UMX qui renseigne sur quel machine à crée quel fichier UMx
	            dict_UMX.put(morceau.replace("S","UM"),j_machine);
	            
			j++;
		}
		for (LaunchSlaveShavadoop thread : slaves)  //boucle qui lance chacun des thread sur chacun des machines
		{
		if (thread!=null){
		thread.join();
		uniqueword = thread.getOutput(); //renvoi les mots traités par le thread 
		um_part = thread.getmorceau(); //renvoi le morceau, soit le Sx, traité par ce thread
		
		
			for(i=0;i<uniqueword.size();i++){
		    	
		    	ArrayList<String> list = new ArrayList<String>();
		    	
		    	if (dict_W.containsKey(uniqueword.get(i))){
		    	
		    		 list.addAll(dict_W.get(uniqueword.get(i)));
		    		 list.add(um_part.replace("S","UM"));
		    	}
		    	else{
		    		
		    		list.add(um_part.replace("S","UM"));
	
		    	}
		    	
		    dict_W.put(uniqueword.get(i),list);
		    }
		    	
		}
		else {System.out.println("Not really a thread");}}
		System.out.println("Tout est fini");
	
		//renvoi le dictionnaire UMx avec la key UMx et la valeurs le numéro de machine
		System.out.println(dict_UMX.toString());
		//renvoi en key un mot unique et en valeurs les UMx qui contiennent ce mot 
		System.out.println(dict_W.toString()); 
		
		long map_step = System.currentTimeMillis();
		long total_map_step = map_step - star_time_step;
		
		
		
		/* ------------------------------------------------------------ */
		/* ---------------- Shuffling + Reducing Step ---------------- */
		/* ---------------------------------------------------------- */
		
		star_time_step = System.currentTimeMillis();
		
		ArrayList<LaunchSlaveShavadoop> slaves_rm = new ArrayList<LaunchSlaveShavadoop>();
		HashMap<String, String> rm_machine = new HashMap<String,String>();
		ArrayList<String> rm_name = new ArrayList<String>();
		
		j = 0;
		int id = 0;
		
		//pour chaque key du dictionnaire Dict_W, donc soit pour chaque mot on crée les SM et RM associé
		//on lance le traitement du slave UMxSMx sur chaque machine
		for (String keys : dict_W.keySet()) {
				
				String name_out = "SM" + String.valueOf(id) ;
				
				ArrayList<String> value_keys = new ArrayList<String>();
				value_keys  = dict_W.get(keys);
				
				String listString_value = new String();
				//on récupère dans un string la liste des UMx concerné par le mot, soit la key
				for (String s : value_keys)
				{
				    listString_value += s + " ";
				}
				
				//une fois la liste de UMx recupéré je lance sur une machine le traitement
				//si le nombre de traitement est plus grand que le nombre de machine, on repart de la première machine
				if (j>=ListeMachine.size()){j=0;}
				
				j_machine= ListeMachine.get(j);
				
				String command = "cd "+path_project + " " + " ;java -jar salve_shavadoop.jar modeUMXSMX " + path_project + " "
									+ keys + " " + name_out + " "+ listString_value ;
				
				LaunchSlaveShavadoop slave_rm = new LaunchSlaveShavadoop(j_machine,command,
												name_out.replace("SM","RM"),40);
			    slave_rm.start();
			    
			    slaves_rm.add(slave_rm);
			    
			
			    System.out.println("Début process " + j_machine + " sur la key : " + keys);
			    
			    //on renseigne le dictionnaire rm_machine qui indique par quel machine a été traité un RM
			    rm_machine.put(name_out.replace("SM","RM"),j_machine);
			    
				id++ ;
				j++;
		}
		for (LaunchSlaveShavadoop thread : slaves_rm) 
		{
		if (thread!=null){
		thread.join();
		rm_name.add(thread.getmorceau()); 
		}
		else {System.out.println("Not really a thread");}}
		System.out.println("Tout est fini");
		
		System.out.println(rm_machine.toString());
		System.out.println(rm_name.toString());
		
		long shuffle_reduce_step = System.currentTimeMillis();
		long total_shuffle_reduce_step = shuffle_reduce_step - star_time_step;
		
		
		
		/* ------------------------------------------------------------ */
		/* ---------------- Assembling Step -------------------------- */
		/* ---------------------------------------------------------- */
		
		//on assemble l'ensemble de nos fichier RM 
		mergefile(rm_name,"output",path_project);
		
		long ass_time  = System.currentTimeMillis();
		long assembling_time = ass_time - shuffle_reduce_step ;
		
		
		/* ------------------------------------------------------------ */
		/* ---------------- Sorting Step -------------------------- */
		/* ---------------------------------------------------------- */
		
		//on appel notre fonction sorting qui à partir de l'ouput obtenu dans l'assembling step
		//nous crée un fichier ordonnée par occurence
		sorted_file(path_project,"output");
		
		long endTime = System.currentTimeMillis();
		long sortingtime = endTime - ass_time;
		long totalTime = endTime - startTime;
		/* ------------------------------------------------------------ */
		/* ---------------- Print time ------------------------------- */
		/* ---------------------------------------------------------- */
		
		System.out.println("Temps d'éxécution du démarage: " + (total_first_step));
		System.out.println("Temps d'éxécution du splitting: " + (total_split_step));
		System.out.println("Temps d'éxécution du mapping: " + (total_map_step));
		System.out.println("Temps d'éxécution du shuffle+reducing: " + (total_shuffle_reduce_step));
		System.out.println("Temps d'éxécution de l'assembling " + (assembling_time));
		System.out.println("Temps d'éxécution du sorting " + (sortingtime));
		System.out.println("Temps d'éxécution total: " + (totalTime));
		
		
		
	}
}


