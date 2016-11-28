 # Projet SHAVADOOP  

 
#### Introduction 
Pour rappel, notre objectif est de construire un programme qui imite le fonctionnement de l’algorithme du framwork Map-Reduce. Notre souhait est d’implémenter un algorithme qui calcule un « Word Count » sur plusieurs machines, et de manière parallélisée.  Pour cela nous aurons à disposition l’ensemble des machines disponibles sur le site de Télécom Paris-Tech, sur lesquelles nous pouvons nous connecter sans restriction à l’aide de la commande SSH. 

Notre implémentation personnalisée de l’algorithme Map-Reduce se fera selon le modèle ci-dessous :  


->![Algo MAP-ReDUCE](https://s3.amazonaws.com/files.dezyre.com/images/Tutorials/MapReduce_Example.jpg)<-

Cette note est accompagnée de plusieurs fichiers, d’une part le fichier zip qui contient le code source de notre programme, et d’autre part, les deux fichiers jar servant à l’utilisation du programme. Egalement vous trouverez une note plus complète au format PDF.  


#### Comment lancer le projet 


Pour lancer notre programme, nous aurons besoin des deux fichiers jar joints (master_shavadoop.jar & salve_shavadoop.jar). Le premier cité représente le code associé au Master de notre algorithme et le second au programme que nous exécuterons sur chaque machine. Lors du lancement de notre « Word Count » nous aurons seulement besoin du premier cité. Une fois les deux jar récupérés, et sauvegardés dans le même chemin, nous pouvons tenter d’exécuter le programme. Pour cela, il suffit de se rendre dans le terminal, de se placer dans le dossier où se situe nos jar et d’exécuter la commande suivante : 

> java –jar master_shavadoop.jar PATH INPUT.txt 

 

Le « PATH » doit être un chemin tel que « /cal/homes/brehelin/Desktop/Sx_Folder/ », et le fichier INPUT.txt doit être le nom du fichier texte sur lequel on souhaite effectuer notre comptage tel que « forestier_mayotte.txt ».  

Le « PATH » représente le répertoire de référence tout au long du processus de traitement, il est donc nécessaire que celui-ci contienne tous les éléments dont a besoin notre programme. Il est impératif que notre dossier contienne: 

    1. Le fichier source texte sur lequel nous voulons lancer notre programme 

    2. Un fichier texte qui référence l’adresse des machines sur lesquelles nous souhaitons paralléliser notre programme sous le nom « ip_adress.txt » (joint avec cette note).  

    3. Le fichier texte « pronom_list.txt »  qui référence tous les pronoms et autres mots à bannir de notre « Word Count » 

Après lancement du projet, on retrouvera tous nos fichiers de sortie dans ce dossier de référence.  